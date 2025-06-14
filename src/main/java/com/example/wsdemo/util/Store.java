package com.example.wsdemo.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {

    private static final List<DeviceRecord> deviceRecords = List.of(
            new DeviceRecord("123", "Dead", "Active"),
            new DeviceRecord("456", "Dead", "Active"),
            new DeviceRecord("789", "Dead", "Active")
    );

    private Store() {
    }

    //===================================================
    //                      Data retrieval
    //===================================================

    public static List<DeviceRecord> getDeviceRecords() {
        return deviceRecords;
    }

    //get device id by session id
    public static String getDeviceIdBySessionId(String sessionId) {
        return deviceRecords.stream()
                .filter(record -> record.sessionId != null && record.sessionId.equals(sessionId))
                .map(record -> record.id)
                .findFirst()
                .orElse(null);
    }

    //===================================================
    //                      Data Save
    //===================================================

    //save token for device id
    public static void saveToken(String id, String token) {
        deviceRecords.stream()
            .filter(record -> record.id.equals(id))
            .findFirst()
            .ifPresent(record -> record.token = token);
    }

    public static String getToken(String id) {
        return deviceRecords.stream()
                .filter(record -> record.id.equals(id))
                .map(record -> record.token)
                .findFirst()
                .orElse(null);
    }

    public static void saveDeviceStatusById(String id, String status) {
        deviceRecords.stream()
            .filter(record -> record.id.equals(id))
            .findFirst()
            .ifPresent(record -> record.status = status);
    }

    //log in device (save new session id, token, status for the given device id)
    public static void loginDevice(String id,String sessionId, String token) {
        deviceRecords.stream()
                .filter(record -> record.id.equals(id))
                .findFirst()
                .ifPresent(record -> {
                    record.sessionId = sessionId;
                    record.token = token;
                    record.status = "Live";
                });
    }

    //log out device (make session id, token null for the given device id)
    public static void logoutDevice(String id) {
        deviceRecords.stream()
            .filter(record -> record.id.equals(id))
            .findFirst()
            .ifPresent(record -> {
                record.sessionId = null;
                record.token = null;
                record.status = "Dead";
            });
    }

    //=========================================================
    //                      Validation
    //=========================================================
    public static boolean isValidDevice(String id) {
        //does this id presents with in the device records?
        return deviceRecords.stream().anyMatch(record -> record.id.equals(id));
    }

    public static boolean isValidToken(String id, String token) {
        //does this id and token pair presents with in the device records?
        return deviceRecords.stream()
                .anyMatch(record -> record.id.equals(id) && record.token != null && record.token.equals(token));
    }
}