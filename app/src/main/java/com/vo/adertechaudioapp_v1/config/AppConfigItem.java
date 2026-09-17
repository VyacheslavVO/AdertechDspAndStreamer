package com.vo.adertechaudioapp_v1.config;

import androidx.annotation.NonNull;

import java.util.Objects;

public class AppConfigItem {
    public static final String TAG = AppConfigItem.class.getSimpleName();
    public enum Type { INPUT, OUTPUT };
    public enum InterfaceType { ANALOG, DANTE, USB, MIXER };
    private String name;
    private String friendlyName;
    private Type type;
    private InterfaceType interfaceType;
    private int channel;
    private boolean visible;
    public AppConfigItem(String name, Type type, InterfaceType interfaceType, int channel, boolean visible) {
        this.name = name;
        this.friendlyName = name;
        this.type = type;
        this.interfaceType = interfaceType;
        this.channel = channel;
        this.visible = visible;
    }

    public AppConfigItem(String name, String friendlyName, Type type, InterfaceType interfaceType, int channel, boolean visible) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.type = type;
        this.interfaceType = interfaceType;
        this.channel = channel;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() { return friendlyName; }

    public void setFriendlyName(String friendlyName) { this.friendlyName = friendlyName; }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @NonNull
    @Override
    public String toString() {
        return "Name: " + getName() +
                ", PortType: " + getType() +
                ", InterfaceType: " + getInterfaceType() +
                ", Channel: " + getChannel() +
                ", Visible: " + isVisible() + "\n";
    }

    /// будем сравнивать только изменяемые поля — это friendlyName и visible
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                                                         // проверка на идентичность (одна и та-же ссылка)
        if (o == null || getClass() != o.getClass()) return false;                          // проверка типа
        AppConfigItem that = (AppConfigItem) o;
        return visible == that.visible && Objects.equals(friendlyName, that.friendlyName);  // сравнение полей
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, visible);
    }
}
