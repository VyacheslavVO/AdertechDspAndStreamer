package com.vo.adertechaudioapp_v1.devices;

public class DeviceSpecModel {

    String model;
    DeviceSpecItem inputs;
    DeviceSpecItem outputs;

    public DeviceSpecModel(String model, DeviceSpecItem inputs, DeviceSpecItem outputs) {

        this.model = model;
        this.inputs = inputs;
        this.outputs = outputs;

    }

    public String getModel() {
        return model;
    }

    public DeviceSpecItem getInputs() {
        return inputs;
    }

    public DeviceSpecItem getOutputs() {
        return outputs;
    }
}
