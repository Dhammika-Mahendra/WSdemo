package com.example.wsdemo.util;

public class DeviceRecord {
    public String id;
    public String token;
    public String sessionId;
    public String status;

    public DeviceRecord(String id, String status) {
        this.id = id;
        this.status = status;
    }
}
