package com.vo.adertechaudioapp_v1.adertech;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSPDriver {

    private static final String TAG = DSPDriver.class.getSimpleName();
    private static final double STEP = 0.5;
    private static final long UPDATE_LEVEL_STEP = 250;

    public interface DSPDriverListeners {
//        void onVolumeInputListener(int channel, double value);
        void onVolumeOutputListener(int channel, double value);
        void onLevelOutputListener(int channel, double value);
//        void onMuteInputListener(int channel, boolean value);
        void onMuteOutputListener(int channel, boolean value);
        void onMixerSwitchListener(int channelInput, int channelOutput, boolean value);
    }
    DSPDriverListeners dspDriverListeners;
    UDPSocket socket;

    List<DspChannel> dspChannelInputList;
    DspChannel dspChannelOutput;
    int inputSelected;

    private ScheduledExecutorService scheduledExecutorServiceGetLevel;

    public void addListeners(DSPDriver.DSPDriverListeners listeners) {
        this.dspDriverListeners = listeners;
    }

    private void startTaskGetLevel() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                getLevelOutput();
            }
        };

        if (scheduledExecutorServiceGetLevel == null) {
            scheduledExecutorServiceGetLevel = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorServiceGetLevel.scheduleWithFixedDelay(task, 0, UPDATE_LEVEL_STEP, TimeUnit.MILLISECONDS);
        }
    }

    private void stopTaskGetLevel() {
        if (scheduledExecutorServiceGetLevel != null) {
            scheduledExecutorServiceGetLevel.shutdownNow();
            scheduledExecutorServiceGetLevel = null;
        }
    }

    public DSPDriver(String ipAddress, int ipPort) {
        this.inputSelected = -1;
        this.socket = new UDPSocket(ipAddress, ipPort);
        this.dspChannelInputList = new ArrayList<>();
        socket.listeners = new UDPSocket.IUDPSocketListeners() {
            @Override
            public void onReceiveMessage(String string) {
//                Log.d(TAG, "Receive data: " + string);
                parseReceiveMessage(string);
            }
        };
    }

    public void parseReceiveMessage(String string) {

        string = string.trim();
        String[] arrMessage = string.split("#");
//        Log.d(TAG, "Receive data: " + Arrays.toString(arrMessage));
//        Log.d(TAG, "array length: " + arrMessage.length);

        int startChannel = -1;
        int endChannel = -1;

        final String regexChannels = "(\\d+)-(\\d+)";
        final Pattern patternChannels = Pattern.compile(regexChannels, Pattern.MULTILINE);
        final Matcher matcherChannels = patternChannels.matcher(arrMessage[2]);


        switch (arrMessage[0]) {

            case "set:mixer":
                /// установка группы каналов
                while (matcherChannels.find()) {
                    startChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(1)));
                    endChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(2)));
                }

                if (arrMessage[1].equals("switch")) {
                    /// установка диаппазонов каналов по входам (был обработан в регулярном выражении)
                    /// значит в следущем индексе обработки только один выход
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            ;
                        }
                    /// установка одиночного канала по входам (не был обработан в регулярном выражении)
                    /// в обработке следущего индекса может быть диаппазон выходов
                    } else {
                        final Matcher matcherChannelsOutputs = patternChannels.matcher(arrMessage[3]);

                        /// установка группы каналов
                        while (matcherChannelsOutputs.find()) {
                            startChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(1)));
                            endChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(2)));
                        }

                        /// это групповая установка выходов на один вход
                        if (startChannel >= 0) {
                            /// не эхо ответ
                            if (arrMessage.length > endChannel) {
                                ;
                            }
                        /// это одиночная установка одного входа на один выход
                        } else {
                            /// не эхо ответ
                            if (arrMessage.length > 4) {
                                dspDriverListeners.onMixerSwitchListener(
                                        Integer.parseInt(arrMessage[2]),
                                        Integer.parseInt(arrMessage[3]),
                                        arrMessage[4].equals("1")
                                );
                            }
                        }
                    }
                }
                break;
            case "set:input":
            case "get:input":

                /// установка группы каналов
                while (matcherChannels.find()) {
                    startChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(1)));
                    endChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(2)));
                }

                if (arrMessage[1].equals("gain")) {
                    /// установка диаппазонов каналов (был обработан в регулярном выражении)
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            /// отнимаем вход autoMixer и вход USB — они не регулируются
                            for (int i = startChannel; i < (endChannel - 1); i++) {
                                double value = Double.parseDouble(arrMessage[i + 3]);
                                dspChannelInputList.get(i).setVolume(Double.parseDouble(arrMessage[i + 3]));
                                // TODO: добавить слушатель
//                                dspDriverListeners.onVolumeInputListener(i, value);
                            }
                        }
                    }
                    /// установка одиночного канала (не был обработан в регулярном выражении)
                    else {
                        /// не эхо ответ
                        if (arrMessage.length > 3) {
                            startChannel = Integer.parseInt(arrMessage[2]);
                            double value = Double.parseDouble(arrMessage[3]);
                            dspChannelInputList.get(startChannel).setVolume(value);
                            // TODO: добавить слушатель
//                            dspDriverListeners.onVolumeInputListener(startChannel, value);
                        }
                    }
                } else if (arrMessage[1].equals("mute")) {
                    /// установка диаппазонов каналов (был обработан в регулярном выражении)
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            /// отнимаем вход autoMixer и вход USB — они не регулируются
                            for (int i = startChannel; i < (endChannel - 1); i++) {
                                boolean value = arrMessage[i + 3].equals("1");
                                dspChannelInputList.get(i).setMute(value);
                                // TODO: добавить слушатель
//                                dspDriverListeners.onMuteInputListener(i, value);
                            }
                        }
                    }
                    /// установка одиночного канала (не был обработан в регулярном выражении)
                    else {
                        /// не эхо ответ
                        if (arrMessage.length > 3) {
                            startChannel = Integer.parseInt(arrMessage[2]);
                            boolean value = arrMessage[3].equals("1");
                            dspChannelInputList.get(startChannel).setMute(value);
                            // TODO: добавить слушатель
//                            dspDriverListeners.onMuteInputListener(startChannel, value);
                        }
                    }
                }
                break;
            case "set:output":
            case "get:output":

                /// установка группы каналов
                while (matcherChannels.find()) {
                    startChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(1)));
                    endChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(2)));
                }

                if (arrMessage[1].equals("gain")) {
                    /// установка диаппазонов каналов (был обработан в регулярном выражении)
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            /// отнимаем вход autoMixer и вход USB — они не регулируются
                            for (int i = startChannel; i < (endChannel - 1); i++) {
                                if (i == dspChannelOutput.getNumber()) {
                                    try {
                                        dspChannelOutput.setVolume(Double.parseDouble(arrMessage[i + 3]));
                                        // TODO: добавить слушатель
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Parse volume error. NumberFormatException: " + e.getMessage());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /// установка одиночного канала (не был обработан в регулярном выражении)
                    else {
                        /// не эхо ответ
                        if (arrMessage.length > 3) {
                            startChannel = Integer.parseInt(arrMessage[2]);
                            if (startChannel == dspChannelOutput.getNumber()) {
                                try {
                                    double value = Double.parseDouble(arrMessage[3]);
                                    dspChannelOutput.setVolume(value);
                                    dspDriverListeners.onVolumeOutputListener(startChannel, value);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Parse volume error. NumberFormatException: " + e.getMessage());
                                }
                            }
                        }
                    }
                } else if (arrMessage[1].equals("mute")) {
                    /// установка диаппазонов каналов (был обработан в регулярном выражении)
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            /// отнимаем вход autoMixer и вход USB — они не регулируются
                            for (int i = startChannel; i < (endChannel - 1); i++) {
                                if (i == dspChannelOutput.getNumber()) {
                                    dspChannelOutput.setMute(arrMessage[i + 3].equals("1"));
                                    // TODO: добавить слушатель
                                }
                            }
                        }
                    }
                    /// установка одиночного канала (не был обработан в регулярном выражении)
                    else {
                        /// не эхо ответ
                        if (arrMessage.length > 3) {
                            startChannel = Integer.parseInt(arrMessage[2]);
                            if (startChannel == dspChannelOutput.getNumber()) {
                                boolean value = arrMessage[3].equals("1");
                                dspChannelOutput.setMute(arrMessage[3].equals("1"));
                                dspDriverListeners.onMuteOutputListener(startChannel, value);
                            }
                        }
                    }
                    /// запускаем процесс получения уровня, предполагается, что запрос состояния выхода
                    /// выполняется последним
                    if (arrMessage[0].equals("get:output"))
                        startTaskGetLevel();
                } else if (arrMessage[1].equals("level")) {
                    /// установка диаппазонов каналов (был обработан в регулярном выражении)
                    if (startChannel >= 0) {
                        /// не эхо ответ
                        if (arrMessage.length > endChannel) {
                            for (int i = startChannel; i < (endChannel - 1); i++) {
                                if (i == dspChannelOutput.getNumber()) {
                                    // TODO: добавить слушатель
                                }
                            }
                        }
                    }
                    /// установка одиночного канала (не был обработан в регулярном выражении)
                    else {
                        /// не эхо ответ
                        if (arrMessage.length > 3) {
                            startChannel = Integer.parseInt(arrMessage[2]);
                            if (startChannel == dspChannelOutput.getNumber()) {
                                try {
                                    double value = Double.parseDouble(arrMessage[3]);
                                    dspDriverListeners.onLevelOutputListener(startChannel, value);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Parse level error. NumberFormatException: " + e.getMessage());
                                }

                            }
                        }
                    }
                }
                break;
        }
    }

    public List<DspChannel> getChannelInputList() {
        return dspChannelInputList;
    }

    public DspChannel getChannelOutput() {
        return dspChannelOutput;
    }

    public UDPSocket getSocket() {
        return socket;
    }

    public void clearChannelInputs() {
        this.dspChannelInputList.clear();
    }
    public void addChannelInput(DspChannel dspChannel) {
        this.dspChannelInputList.add(dspChannel);
    }

    public void addChannelOutput(DspChannel dspChannel) {
        this.dspChannelOutput = dspChannel;
//        Log.d(TAG, this.channelOutput.toString());
    }

    public void setMuteOutput(boolean value) {
        String message = String.format(Locale.US,"set:output#mute#%d#%d", dspChannelOutput.getNumber(), value ? 1 : 0);
        socket.sendUDP(message);
    }

    public void getMuteOutput() {
        String message = String.format(Locale.US,"get:output#mute#%d", dspChannelOutput.getNumber());
        socket.sendUDP(message);
    }
    public void setVolumeOutputUp() {
        double currentVolume = dspChannelOutput.getVolume();
        if (currentVolume < 12.0) { currentVolume += STEP; }
        currentVolume = Math.min(currentVolume, 12.0);
        String message = String.format(Locale.US, "set:output#gain#%d#%.1f", dspChannelOutput.getNumber(), currentVolume);
        socket.sendUDP(message);
        dspChannelOutput.setVolume(currentVolume);
    }
    public void setVolumeOutputDown() {
        double currentVolume = dspChannelOutput.getVolume();
        if (currentVolume > -72.0) { currentVolume -= STEP; }
        currentVolume = Math.max(currentVolume, -72.0);
        String message = String.format(Locale.US, "set:output#gain#%d#%.1f", dspChannelOutput.getNumber(), currentVolume);
        socket.sendUDP(message);
        dspChannelOutput.setVolume(currentVolume);
    }
    public void getVolumeOutput() {
        String message = String.format(Locale.US,"get:output#gain#%d", dspChannelOutput.getNumber());
        socket.sendUDP(message);
    }

    public void getLevelOutput() {
        String message = String.format(Locale.US,"get:output#level#%d", dspChannelOutput.getNumber());
        socket.sendUDP(message);
    }

    public void setMuteInput(boolean value) {
        String message = String.format(Locale.US,"set:input#mute#%d#%d", inputSelected, value ? 1 : 0);
        socket.sendUDP(message);
    }

    public void setVolumeInputUp() {
        if (inputSelected < 0) return;
        double currentVolume = dspChannelInputList.get(inputSelected).getVolume();
        if (currentVolume < 12.0) { currentVolume += STEP; }
        currentVolume = Math.min(currentVolume, 12.0);
        String message = String.format(Locale.US, "set:input#gain#%d#%.1f", inputSelected, currentVolume);
        socket.sendUDP(message);
        dspChannelInputList.get(inputSelected).setVolume(currentVolume);
    }

    public void setVolumeInputDown() {
        if (inputSelected < 0) return;
        double currentVolume = dspChannelInputList.get(inputSelected).getVolume();
        if (currentVolume > -72.0) { currentVolume -= STEP; }
        currentVolume = Math.max(currentVolume, -72.0);
        String message = String.format(Locale.US, "set:input#gain#%d#%.1f", inputSelected, currentVolume);
        socket.sendUDP(message);
        dspChannelInputList.get(inputSelected).setVolume(currentVolume);
    }

    public void getAllVolumeInputs() {
        String message = String.format(Locale.US,"get:input#gain#0-%d", dspChannelInputList.size() - 1);
        socket.sendUDP(message);
    }

    public void getAllMuteInputs() {
        String message = String.format(Locale.US,"get:input#mute#0-%d", dspChannelInputList.size() - 1);
        socket.sendUDP(message);
    }

    public void resetRouting() {
        stopTaskGetLevel();
        this.inputSelected = -1;
        String message = String.format(Locale.US,"set:mixer#switch#0-%d#%d#0", dspChannelInputList.size() - 1, dspChannelOutput.getNumber());
        socket.sendUDP(message);
    }

    public void setRoute(int source) {
        String message = "";
        /// cначала сбрасываем предыдущий, затем устанавливаем новый канал
        if (this.inputSelected == -1) {
            message = String.format(Locale.US, "set:mixer#switch#%d#%d#1", source, dspChannelOutput.getNumber());
        } else if (this.inputSelected >= 0) {
            message = String.format(Locale.US, "set:mixer#switch#%d#%d#0", this.inputSelected, dspChannelOutput.getNumber());
        }
        socket.sendUDP(message);

        if (source == -1) {
            message = String.format(Locale.US, "set:mixer#switch#%d#%d#0", this.inputSelected, dspChannelOutput.getNumber());
        } else if (source >= 0) {
            message = String.format(Locale.US, "set:mixer#switch#%d#%d#1", source, dspChannelOutput.getNumber());
        }
        socket.sendUDP(message);

        this.inputSelected = source;
//        Log.d(TAG, "Channel: " + this.inputSelected + ", volume: " + channelInputList.get(inputSelected).getVolume());
    }

    public int getInputSelected() {
        return inputSelected;
    }
}
