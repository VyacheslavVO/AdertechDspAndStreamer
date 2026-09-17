package com.vo.adertechaudioapp_v1.config;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.vo.adertechaudioapp_v1.devices.DevicesSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppConfig {

    private static final String TAG = DevicesSpec.class.getSimpleName();
    private static final String FILE_NAME  = "app_config.json";
    Context context;
    private AppConfigModel appConfigModel;

    public AppConfig(Context context) {
        this.context = context;

        // если файла нет, то создаём его то читаем
        File file = new File(this.context.getFilesDir(), FILE_NAME);

        if (!file.exists()) {
            addConfiguration();
            Gson gson = new Gson();

            AppConfig.DataItems dataItems = new AppConfig.DataItems();
            dataItems.setAppConfig(getAppConfigModel());
            String jsonString = gson.toJson(dataItems);

            try (FileOutputStream fileOutputStream = context.openFileOutput(FILE_NAME , Context.MODE_PRIVATE)) {
                fileOutputStream.write(jsonString.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Failed to create JSON object", e);
            }
        } else {
            try (FileInputStream fileInputStream = context.openFileInput(FILE_NAME);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream)) {

                appConfigModel = new AppConfigModel();
                Gson gson = new Gson();
                AppConfig.DataItems dataItems = gson.fromJson(streamReader, AppConfig.DataItems.class);
                this.appConfigModel = dataItems.getAppConfig();
            } catch (IOException e){
                Log.e(TAG, "Failed to read JSON object", e);
            }
        }
    }

    public AppConfigModel getAppConfigModel() {
        return appConfigModel;
    }

    private void addConfiguration() {

        List<AppConfigItem> appConfigItemList = new ArrayList<>();
        appConfigItemList.add(new AppConfigItem("Analog In 1",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG,0,true));
        appConfigItemList.add(new AppConfigItem("Analog In 2",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG,1,true));
        appConfigItemList.add(new AppConfigItem("Analog In 3",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG,2,true));
        appConfigItemList.add(new AppConfigItem("Analog In 4",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.ANALOG,3,true));
        appConfigItemList.add(new AppConfigItem("AutoMixer",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.MIXER,4,true));
        appConfigItemList.add(new AppConfigItem("USB In",
                AppConfigItem.Type.INPUT, AppConfigItem.InterfaceType.USB,5,true));

        appConfigItemList.add(new AppConfigItem("Analog Out 1",
                AppConfigItem.Type.OUTPUT, AppConfigItem.InterfaceType.ANALOG,1,true));

        appConfigModel = new AppConfigModel("192.168.10.22", 50000, "APROC-44",
                "192.168.10.23",
                appConfigItemList);
    }

    public void saveConfiguration(AppConfigModel appConfigModel) {

        /// перезаписываем данные без чтения из файла
        this.appConfigModel = new AppConfigModel(appConfigModel.getDspIpAddress(), appConfigModel.getDspIpPort(), appConfigModel.getDeviceModel(),
                appConfigModel.getStreamerIpAddress(),
                new ArrayList<>(appConfigModel.getAppConfigItemList())
        );

        Gson gson = new Gson();

        AppConfig.DataItems dataItems = new AppConfig.DataItems();
        dataItems.setAppConfig(appConfigModel);
        String jsonString = gson.toJson(dataItems);

        try (FileOutputStream fileOutputStream = context.openFileOutput(FILE_NAME , Context.MODE_PRIVATE)) {
            fileOutputStream.write(jsonString.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Failed to create JSON object", e);
        }
    }

    private static class DataItems {
        private AppConfigModel appConfig;

        AppConfigModel getAppConfig() {
            return appConfig;
        }
        void setAppConfig(AppConfigModel appConfig) {
            this.appConfig = appConfig;
        }
    }
}
