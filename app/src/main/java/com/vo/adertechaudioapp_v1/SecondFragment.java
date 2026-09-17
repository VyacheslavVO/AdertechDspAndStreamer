package com.vo.adertechaudioapp_v1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.vo.adertechaudioapp_v1.config.AppConfigItem;
import com.vo.adertechaudioapp_v1.config.AppConfigModel;
import com.vo.adertechaudioapp_v1.databinding.FragmentSecondBinding;
import com.vo.adertechaudioapp_v1.devices.DeviceSpecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecondFragment extends Fragment {

    public interface ISettingsListeners {
        void onClickSaveSettingsListener(AppConfigModel appConfigModel);
    }

    static ISettingsListeners settingsListeners;

    private static final String TAG = SecondFragment.class.getSimpleName();
    final String KEY_CONFIG_MODEL = "key_app_config_model";
    private FragmentSecondBinding binding;
    private static String[] devicesName;
    private ArrayAdapter<String> adapterZone;

    private AppConfigModel appConfigModel;              // конфигурация из файла

    private AppConfigModel appConfigModelToSave;        // конфигурация для записи
    private static List<DeviceSpecModel> deviceSpecModelList;

    private boolean isEnableSaved = false;

///    public static void setAppConfigModel(AppConfigModel appModel) {
///        appConfigModel = appModel;
///    }

    public static void setDeviceSpecModelList(List<DeviceSpecModel> deviceSpecModelList) {
        SecondFragment.deviceSpecModelList = deviceSpecModelList;
        SecondFragment.devicesName = SecondFragment.getDevicesName();
    }

    public static void addListeners(ISettingsListeners listeners) {
        settingsListeners = listeners;
    }

    private static String[] getDevicesName() {
        List<String> list = new ArrayList<>();
        for(DeviceSpecModel deviceSpecModel : SecondFragment.deviceSpecModelList) {
            list.add(deviceSpecModel.getModel());
        }
        return list.toArray(new String[0]);
    }

    @NonNull
    private String[] getZonesName(String deviceName) {
        List<String> listZone = new ArrayList<>();
        for (DeviceSpecModel deviceSpecModel : SecondFragment.deviceSpecModelList) {
            if (deviceSpecModel.getModel().equals(deviceName)) {
                for (int i = 0; i < deviceSpecModel.getOutputs().getAnalog(); i++) {
                    if (deviceSpecModel.getOutputs().getAnalog() == 1) {
                        listZone.add("Analog Out");
                    } else {
                        listZone.add("Analog Out " + (i + 1));
                    }
                }
                for (int i = 0; i < deviceSpecModel.getOutputs().getDante(); i++) {
                    if (deviceSpecModel.getOutputs().getDante() == 1) {
                        listZone.add("Dante Out");
                    } else {
                        listZone.add("Dante Out " + (i + 1));
                    }
                }
                for (int i = 0; i < deviceSpecModel.getOutputs().getUsb(); i++) {
                    if (deviceSpecModel.getOutputs().getUsb() == 1) {
                        listZone.add("USB Out");
                    } else {
                        listZone.add("USB Out " + (i + 1));
                    }
                }
            }
        }
        return listZone.toArray(new String[0]);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        /// добавляем данные по ссылке из навигации
        if (getArguments() != null) {
            appConfigModel = getArguments().getParcelable(KEY_CONFIG_MODEL);
            assert appConfigModel != null;
//            Log.d(TAG, "Device model OK: " + appConfigModel);
        }

        ///  клонируем данные для записи
        appConfigModelToSave = new AppConfigModel(
                appConfigModel.getDspIpAddress(), appConfigModel.getDspIpPort(), appConfigModel.getDeviceModel(),
                appConfigModel.getStreamerIpAddress(),
                appConfigModel.getAppConfigItemList()
        );


        adapterZone = new ArrayAdapter<>(
                requireContext(),
                R.layout.list_item,
                getZonesName(appConfigModel.getDeviceModel())
        );
        binding.AutoCompleteTextViewZoneNumber.setAdapter(adapterZone);

        /// загрузить список моделей устройств в выбор моделей
        /// данные беруться из файла и не будут меняться на протяжении жизни программы
        ArrayAdapter<String> adapterModel = new ArrayAdapter<>(
                requireContext(),
                R.layout.list_item,
                SecondFragment.devicesName
        );
        binding.AutoCompleteTextViewDeviceModel.setAdapter(adapterModel);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /// установить текущий DSP IP Address из конфигурации
        binding.TextInputEditTextIpAddress.setText(appConfigModel.getDspIpAddress());
        /// установить текущий DSP IP Port из конфигурации
        binding.TextInputEditTextIpPort.setText(String.valueOf(appConfigModel.getDspIpPort()));
        /// установить текущий Audio Streamer IP Address из кофигурации
        binding.TextInputEditTextIpAddressAudioStreamer.setText(appConfigModel.getStreamerIpAddress());
        /// установить значение из выбранной модели
        for (AppConfigItem appConfigItem : appConfigModel.getAppConfigItemList()) {
            if (appConfigItem.getType() == AppConfigItem.Type.OUTPUT) {
                for (String s : SecondFragment.devicesName) {
                    if (appConfigModel.getDeviceModel().equals(s)) {
                        binding.AutoCompleteTextViewDeviceModel.setText(s, false);
                    }
                    /// установить згачение выхода из конфигурации
                    binding.AutoCompleteTextViewZoneNumber.setText(appConfigItem.getName(), false);
                }
            }
        }

        // деактивируем кнопку до получения необходимых данных
        binding.buttonSaveSettings.setEnabled(isEnableSaved);

        binding.TextInputEditTextIpAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ipAddress = s.toString().trim();
                isEnableSaved = isValidIPAddress(ipAddress);
                if (isEnableSaved) {
                    // IP is valid, clear any errors
                    binding.TextInputEditTextIpAddress.setError(null);
                    appConfigModelToSave.setDspIpAddress(ipAddress);
                } else {
                    // IP is invalid, set an error
                    binding.TextInputEditTextIpAddress.setError("Invalid DSP IP Address");
                }
                binding.buttonSaveSettings.setEnabled(isEnableSaved);
            }
        });

        binding.TextInputEditTextIpPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ipPort = s.toString().trim();
                isEnableSaved = isValidIPPort(ipPort);
                if (isEnableSaved) {
                    // Port is valid, clear any errors
                    binding.TextInputEditTextIpPort.setError(null);
                    appConfigModelToSave.setDspIpPort(Integer.parseInt(ipPort));
                } else {
                    // Port is invalid, set an error
                    binding.TextInputEditTextIpPort.setError("Invalid IP Port");
                }
                binding.buttonSaveSettings.setEnabled(isEnableSaved);
            }
        });

        binding.TextInputEditTextIpAddressAudioStreamer.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ipAddress = s.toString().trim();
                isEnableSaved = isValidIPAddress(ipAddress);
                if (isEnableSaved) {
                    // IP is valid, clear any errors
                    binding.TextInputEditTextIpAddressAudioStreamer.setError(null);
                    appConfigModelToSave.setStreamerIpAddress(ipAddress);
                } else {
                    // IP is invalid, set an error
                    binding.TextInputEditTextIpAddressAudioStreamer.setError("Invalid Streamer IP Address");
                }
                binding.buttonSaveSettings.setEnabled(isEnableSaved);
            }
        });

        binding.AutoCompleteTextViewDeviceModel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                adapterZone = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.list_item,
                        getZonesName(selectedItem)
                );
                binding.AutoCompleteTextViewZoneNumber.setAdapter(adapterZone);

                /// установить значения входов в appConfigModelToSave для выбранного устройства
                appConfigModelToSave.inputsClear();
                for (DeviceSpecModel deviceSpecModel : SecondFragment.deviceSpecModelList) {
                    if (deviceSpecModel.getModel().equals(selectedItem)) {
//                        Log.d(TAG, "Selected model name: " + deviceSpecModel.getModel());
                        appConfigModelToSave.setDeviceModel(deviceSpecModel.getModel());
                        ///  в протоколе управления отчёт входов и выходов начинается с нуля
                        int idxChannel = 0;
                        for (int i = 0; i < deviceSpecModel.getInputs().getAnalog(); i++) {

                            if (deviceSpecModel.getInputs().getAnalog() == 1) {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("Analog In",
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG, idxChannel, true));
                            } else {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("Analog In " + (i + 1),
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG, idxChannel, true));
                            }
                            idxChannel++;
                        }
                        for (int i = 0; i < deviceSpecModel.getInputs().getDante(); i++) {

                            if (deviceSpecModel.getInputs().getDante() == 1) {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("Dante In",
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.DANTE, idxChannel, true));
                            } else {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("Dante In " + (i + 1),
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.DANTE, idxChannel, true));
                            }
                            idxChannel++;
                        }
                        for (int i = 0; i < deviceSpecModel.getInputs().getMixer(); i++) {

                            if (deviceSpecModel.getInputs().getMixer() == 1) {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("AutoMixer",
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.MIXER, idxChannel, true));
                            } else {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("AutoMixer " + (i + 1),
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.MIXER, idxChannel, true));
                            }
                            idxChannel++;
                        }
                        for (int i = 0; i < deviceSpecModel.getInputs().getUsb(); i++) {

                            if (deviceSpecModel.getInputs().getUsb() == 1) {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("USB In",
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.USB, idxChannel, true));
                            } else {
                                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("USB In " + (i + 1),
                                        AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.USB, idxChannel, true));
                            }
                            idxChannel++;
                        }
                    }
                }

                /// установить значение первого выхода в appConfigModelToSave из выбранной модели
                appConfigModelToSave.outputsClear();
                binding.AutoCompleteTextViewZoneNumber.setText(getZonesName(selectedItem)[0], false);
                appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem("Analog Out 1",
                        AppConfigItem.Type.OUTPUT, AppConfigItem.InterfaceType.ANALOG, 0, true));

                isEnableSaved = true;

                binding.buttonSaveSettings.setEnabled(true);
            }
        });

        binding.AutoCompleteTextViewZoneNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);

                final String regexNameOut = "(Analog|Dante|USB) Out[ (\\d+)]*";
                final Pattern patternNameOut = Pattern.compile(regexNameOut, Pattern.MULTILINE);
                final Matcher matcherNameOut = patternNameOut.matcher(selectedItem);

                appConfigModelToSave.outputsClear();

                ///  в выходах AutoMixer отсутсвует
                while (matcherNameOut.find()) {
                    switch (Objects.requireNonNull(matcherNameOut.group(1))) {
                        case "Analog":
                            appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem(selectedItem,
                                    AppConfigItem.Type.OUTPUT, AppConfigItem.InterfaceType.ANALOG, position, true));
                            break;
                        case "Dante":
                            appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem(selectedItem,
                                    AppConfigItem.Type.OUTPUT, AppConfigItem.InterfaceType.DANTE, position, true));
                            break;
                        case "USB":
                            appConfigModelToSave.getAppConfigItemList().add(new AppConfigItem(selectedItem,
                                    AppConfigItem.Type.OUTPUT, AppConfigItem.InterfaceType.USB, position, true));
                            break;
                    }
                }

//                for (AppConfigItem item : appConfigModelToSave.getAppConfigItemList()) {
//                    Log.d(TAG, item.toString());
//                }

                isEnableSaved = true;

                binding.buttonSaveSettings.setEnabled(true);
            }
        });

        binding.buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isEnableSaved = false;

                binding.buttonSaveSettings.setEnabled(false);

                settingsListeners.onClickSaveSettingsListener(appConfigModelToSave);
            }
        });

        binding.buttonCancelSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                appConfigModelToSave = new AppConfigModel(
                        appConfigModel.getDspIpAddress(), appConfigModel.getDspIpPort(), appConfigModel.getDeviceModel(),
                        appConfigModel.getStreamerIpAddress(),
                        appConfigModel.getAppConfigItemList()
                );

                /// установить текущий DSP IP Address из конфигурации
                binding.TextInputEditTextIpAddress.setText(appConfigModelToSave.getDspIpAddress());
                /// установить текущий DSP IP Port из конфигурации
                binding.TextInputEditTextIpPort.setText(String.valueOf(appConfigModelToSave.getDspIpPort()));
                /// установить текущий Media IP Address из конфигурации
                binding.TextInputEditTextIpAddressAudioStreamer.setText(appConfigModelToSave.getStreamerIpAddress());
                /// установить значение из выбранной модели
                for (AppConfigItem appConfigItem : appConfigModelToSave.getAppConfigItemList()) {
                    if (appConfigItem.getType() == AppConfigItem.Type.OUTPUT) {
//                        Log.d(TAG, "name output: " + appConfigItem.getFriendlyName());
                        for (String s : SecondFragment.devicesName) {
                            if (appConfigModelToSave.getDeviceModel().equals(s)) {
                                binding.AutoCompleteTextViewDeviceModel.setText(s, false);

                                ///  переконфигурировать адаптер
                                adapterZone = new ArrayAdapter<>(
                                        requireContext(),
                                        R.layout.list_item,
                                        getZonesName(appConfigModelToSave.getDeviceModel())
                                );
                                binding.AutoCompleteTextViewZoneNumber.setAdapter(adapterZone);

                                /// установить значение выхода из конфигурации
                                binding.AutoCompleteTextViewZoneNumber.setText(appConfigItem.getName(), false);
                            }
                        }
                    }
                }

                isEnableSaved = false;

                binding.buttonSaveSettings.setEnabled(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // A method to validate the IP address using a regular expression
    private static boolean isValidIPAddress(final String ip) {
        // Regex for IPv4 address validation
        final String IPV4_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(IPV4_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private static boolean isValidIPPort(final String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}