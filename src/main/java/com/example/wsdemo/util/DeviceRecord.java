package com.example.wsdemo.util;

public class DeviceRecord {
    public String id;
    public String token;
    public String sessionId;
    public String status;

    public String active;

    public DeviceRecord(String id, String status, String active) {
        this.id = id;
        this.status = status;
        this.active = active;
    }
}
