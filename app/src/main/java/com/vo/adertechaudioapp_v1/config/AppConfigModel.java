package com.vo.adertechaudioapp_v1.config;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AppConfigModel implements Parcelable {

    private static final String TAG = AppConfigModel.class.getSimpleName();
    private String dspIpAddress;
    private int dspIpPort;
;
    private String deviceModel;
    private String streamerIpAddress;
    private List<AppConfigItem> appConfigItemList;

    public AppConfigModel() {
    }

    public AppConfigModel(String DspIpAddress, int dspIpPort, String deviceModel, String streamerIpAddress, List<AppConfigItem> appConfigItemList) {
        this.dspIpAddress = DspIpAddress;
        this.dspIpPort = dspIpPort;
        this.streamerIpAddress = streamerIpAddress;
        this.deviceModel = deviceModel;
        setAppConfigItemList(appConfigItemList);
    }


    protected AppConfigModel(Parcel in) {
        dspIpAddress = in.readString();
        dspIpPort = in.readInt();
        streamerIpAddress = in.readString();
        deviceModel = in.readString();
    }

    public static final Creator<AppConfigModel> CREATOR = new Creator<AppConfigModel>() {
        @Override
        public AppConfigModel createFromParcel(Parcel in) {
            return new AppConfigModel(in);
        }

        @Override
        public AppConfigModel[] newArray(int size) {
            return new AppConfigModel[size];
        }
    };

    public void inputsClear() {
        appConfigItemList.removeIf(nextConfigItem -> nextConfigItem.getType() == AppConfigItem.Type.INPUT);
    }

    public void outputsClear() {
        appConfigItemList.removeIf(nextConfigItem -> nextConfigItem.getType() == AppConfigItem.Type.OUTPUT);
    }

    public String getDspIpAddress() {
        return dspIpAddress;
    }

    public void setDspIpAddress(String dspIpAddress) {
        this.dspIpAddress = dspIpAddress;
    }

    public int getDspIpPort() {
        return dspIpPort;
    }

    public void setDspIpPort(int dspIpPort) {
        this.dspIpPort = dspIpPort;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getStreamerIpAddress() {
        return streamerIpAddress;
    }

    public void setStreamerIpAddress(String streamerIpAddress) {
        this.streamerIpAddress = streamerIpAddress;
    }

    public List<AppConfigItem> getAppConfigItemList() {
        return appConfigItemList;
    }

    public void setAppConfigItemList(List<AppConfigItem> appConfigItemList) {
        this.appConfigItemList = new ArrayList<>();
        this.appConfigItemList.addAll(appConfigItemList);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Device Model: " + getDeviceModel() + "\n" +
                "DSP IP Address: " + getDspIpAddress() + ",DSP IP Port: " + getDspIpPort() +
                ", Media IP Address: " + getStreamerIpAddress() + "\n");
        for (AppConfigItem appConfigItem: getAppConfigItemList()) {
            string.append("Name: ").append(appConfigItem.getName())
                    .append(", Friendly Name: ").append(appConfigItem.getFriendlyName())
                    .append(", Type: ").append(appConfigItem.getType())
                    .append(", Interface: ").append(appConfigItem.getInterfaceType())
                    .append(", Channel: ").append(appConfigItem.getChannel())
                    .append(", Visible: ").append(appConfigItem.isVisible()).append("\n");
        }
        return string.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(dspIpAddress);
        dest.writeString(streamerIpAddress);
        dest.writeString(deviceModel);
        dest.writeInt(dspIpPort);
        dest.writeList(appConfigItemList);
    }

}
