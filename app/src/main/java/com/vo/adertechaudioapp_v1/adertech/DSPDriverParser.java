package com.vo.adertechaudioapp_v1.adertech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSPDriverParser {


    private static final String TAG = DSPDriverParser.class.getSimpleName();

    String messageType;             // set|get
    String moduleName;              // input|output|mixer|scene|sysctl|rescene|refactory
    String itemName;                // (input)mute,gain,sens,phant,type,freq,name,phase,step,link,level
                                    // (output)mute,gain,name,step,link,level
                                    // (mixer)switch,gain
                                    // (scene)toggle,save,name
                                    // (sysctl)mute
                                    // (rescene)
                                    // (refactory)
    int startChannel;
    int endChannel;
    ArrayList<String> arrParameterValue;

    public DSPDriverParser(String message) {
        String[] arrMessage = message.split("#");
        parseModule(arrMessage[0]);
        itemName = arrMessage[1].trim();
        parseChannels(arrMessage[2]);
        if (getMessageType().equals("set"))
            parseValue(Arrays.copyOfRange(arrMessage, 3, arrMessage.length));
    }

    private void parseModule(String module) {
        module = module.trim();

        final String regexModule = "(set|get):(input|output|mixer|scene|sysctl|rescene|refactory)";
        final Pattern patternModule = Pattern.compile(regexModule, Pattern.MULTILINE);
        final Matcher matcherModule = patternModule.matcher(module);

        while (matcherModule.find()) {
            messageType = matcherModule.group(1);
            moduleName = matcherModule.group(2);
        }
        // Log.d(TAG, "message type: " + messageType + ", module name: " + moduleName);
    }
    private void parseChannels(String channels) {
        channels = channels.trim();

        final String regexChannels = "(\\d+)-(\\d+)";
        final Pattern patternChannels = Pattern.compile(regexChannels, Pattern.MULTILINE);
        final Matcher matcherChannels = patternChannels.matcher(channels);

        if (matcherChannels.matches()) {
            /// установка группы каналов
            while (matcherChannels.find()) {
                startChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(1)));
                endChannel = Integer.parseInt(Objects.requireNonNull(matcherChannels.group(2)));
            }
        } else {
            /// иначе установка одного канала
            startChannel = Integer.parseInt(Objects.requireNonNull(channels));
            endChannel = startChannel;
        }
    }

    private void parseValue (String[] arrValue) {

        final String regexValue = "(\\d+)-(\\d+)";
        final Pattern patternValue = Pattern.compile(regexValue, Pattern.MULTILINE);
        final Matcher matcherValue = patternValue.matcher(arrValue[0]);

        if (matcherValue.matches()) {
            /// проверить, может это диаппазон выходов
            while (matcherValue.find()) {
                startChannel = Integer.parseInt(Objects.requireNonNull(matcherValue.group(1)));
                endChannel = Integer.parseInt(Objects.requireNonNull(matcherValue.group(2)));
            }
        } else {
            /// иначе это массив значений
            arrParameterValue = new ArrayList<>();
            arrParameterValue.addAll(Arrays.asList(arrValue));
        }
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getStartChannel() {
        return startChannel;
    }

    public void setStartChannel(int startChannel) {
        this.startChannel = startChannel;
    }

    public int getEndChannel() {
        return endChannel;
    }

    public void setEndChannel(int endChannel) {
        this.endChannel = endChannel;
    }

    public ArrayList<String> getArrParameterValue() {
        return arrParameterValue;
    }

    public void setArrParameterValue(ArrayList<String> arrParameterValue) {
        this.arrParameterValue = arrParameterValue;
    }
}
