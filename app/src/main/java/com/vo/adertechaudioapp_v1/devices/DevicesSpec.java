package com.vo.adertechaudioapp_v1.devices;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DevicesSpec {

    private static final String TAG = DevicesSpec.class.getSimpleName();

    private static final String FILE_NAME  = "dev_spec.json";


    Context context;
    List<DeviceSpecModel> deviceSpecModelList;

    public DevicesSpec(Context context) {
        this.context = context;
        this.deviceSpecModelList = new ArrayList<>();

        // если файла нет, то создаём его, если есть то читаем
        File file = new File(this.context.getFilesDir(), FILE_NAME );

        if (!file.exists()) {
            // файл с базовой структурой

            addSpecification();
            Gson gson = new Gson();

            DevicesSpec.DataItems dataItems = new DevicesSpec.DataItems();
            dataItems.setDevicesSpec(deviceSpecModelList);
            String jsonString = gson.toJson(dataItems);



            try (FileOutputStream fileOutputStream = context.openFileOutput(FILE_NAME , Context.MODE_PRIVATE)) {
                fileOutputStream.write(jsonString.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Failed to create JSON object", e);
            }
        } else {
            try (FileInputStream fileInputStream = context.openFileInput(FILE_NAME);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream)) {

                Gson gson = new Gson();
                DevicesSpec.DataItems dataItems = gson.fromJson(streamReader, DevicesSpec.DataItems.class);
                deviceSpecModelList = dataItems.getDevicesSpec();
//                for (DeviceSpecModel deviceSpecModel : deviceSpecModelList) {
//                    Log.d(TAG, "model: " + deviceSpecModel.model + ", inputs analog: " + deviceSpecModel.inputs.analog);
//                }

            }
            catch (IOException e){
                Log.e(TAG, "Failed to read JSON object", e);
            }
        }


    }

    private void addSpecification() {
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-44",
                new DeviceSpecItem( 4, 1, 0, 1),
                new DeviceSpecItem(4, 1, 0, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-44D",
                new DeviceSpecItem(4, 1, 4, 1),
                new DeviceSpecItem(4, 1, 4,1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-88",
                new DeviceSpecItem(8, 1, 0, 1),
                new DeviceSpecItem(8, 1, 0, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-88D",
                new DeviceSpecItem(8, 1, 8, 1),
                new DeviceSpecItem(8, 1, 8, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-1212",
                new DeviceSpecItem(12, 1, 0, 1),
                new DeviceSpecItem(12, 1, 0, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-1212D",
                new DeviceSpecItem(12, 1, 8, 1),
                new DeviceSpecItem(12, 1, 8, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-128",
                new DeviceSpecItem(12, 1, 0, 1),
                new DeviceSpecItem(8, 1, 0, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-128D",
                new DeviceSpecItem(12, 1, 8, 1),
                new DeviceSpecItem(8, 1, 8, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-1616",
                new DeviceSpecItem(16, 1, 0, 1),
                new DeviceSpecItem(16, 1, 0, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-1616D",
                new DeviceSpecItem(16, 1, 16, 1),
                new DeviceSpecItem(16, 1, 16, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-168D",
                new DeviceSpecItem(16, 1, 16, 1),
                new DeviceSpecItem(8, 1, 16, 1)
        ));
        deviceSpecModelList.add(new DeviceSpecModel(
                "APROC-6464D",
                new DeviceSpecItem(16, 1, 64, 1),
                new DeviceSpecItem(16, 1, 64, 1)
        ));
    }

    public List<DeviceSpecModel> getDeviceSpecModelList() {
        return deviceSpecModelList;
    }

    private static class DataItems {
        private List<DeviceSpecModel> devicesSpec;

        List<DeviceSpecModel> getDevicesSpec() {
            return devicesSpec;
        }
        void setDevicesSpec(List<DeviceSpecModel> devicesSpec) {
            this.devicesSpec = devicesSpec;
        }
    }
}
