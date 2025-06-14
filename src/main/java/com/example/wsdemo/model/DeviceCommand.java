package com.example.wsdemo.model;

public class DeviceCommand {
    public String type;
    public String id;
    public String command;

    public DeviceCommand() {
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCommand() {
        return command;
    }
    public void setCommand(String command) {
        this.command = command;
    }
}
