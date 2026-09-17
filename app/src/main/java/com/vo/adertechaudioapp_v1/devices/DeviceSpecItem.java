package com.vo.adertechaudioapp_v1.devices;

public class DeviceSpecItem {
    int analog;
    int usb;
    int dante;
    int mixer;

    public DeviceSpecItem(int analog, int usb, int dante) {
        this.analog = analog;
        this.usb = usb;
        this.dante = dante;
        this.mixer = 0;
    }

    public DeviceSpecItem(int analog, int usb, int dante, int mixer) {
        this.analog = analog;
        this.usb = usb;
        this.dante = dante;
        this.mixer = mixer;
    }

    public int getAnalog() {
        return analog;
    }

    public int getUsb() {
        return usb;
    }

    public int getDante() {
        return dante;
    }

    public int getMixer() { return mixer; }
}
