package com.vo.adertechaudioapp_v1.adertech;

import androidx.annotation.NonNull;

import com.vo.adertechaudioapp_v1.config.AppConfigItem;

public class DspChannel {

    private String name;
    private int number;
    private double volume;
    private boolean mute;
    private AppConfigItem.Type type;
    private AppConfigItem.InterfaceType interfaceType;

    public DspChannel(String name, int number, double volume, boolean mute, AppConfigItem.Type type, AppConfigItem.InterfaceType interfaceType) {
        this.name = name;
        this.number = number;
        this.volume = volume;
        this.mute = mute;
        this.type = type;
        this.interfaceType = interfaceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public AppConfigItem.Type getType() {
        return type;
    }

    public void setType(AppConfigItem.Type type) {
        this.type = type;
    }

    public AppConfigItem.InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(AppConfigItem.InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    @NonNull
    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", number=" + number +
                ", volume=" + volume +
                ", mute=" + mute +
                ", type=" + type +
                ", interfaceType=" + interfaceType +
                '}';
    }
}
