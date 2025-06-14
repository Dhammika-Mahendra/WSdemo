package com.example.wsdemo.util;

import org.springframework.stereotype.Component;

import java.util.List;

public class Store {

    private static final List<DeviceRecord> deviceRecords = List.of(
            new DeviceRecord("123", "Dead", "1"),
            new DeviceRecord("456", "Dead", "1"),
            new DeviceRecord("789", "Dead", "0")
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

    //get session id by device id
    public static String getSessionIdByDeviceId(String id) {
        return deviceRecords.stream()
                .filter(record -> record.id.equals(id))
                .map(record -> record.sessionId)
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

    //update device active by the given id
    public static void saveDeviceActive(String id, String active) {
        deviceRecords.stream()
            .filter(record -> record.id.equals(id))
            .findFirst()
            .ifPresent(record -> record.active = active);
    }

    //=========================================================
    //                      Validation
    //=========================================================

    public static boolean isValidDevice(String id) {
        //does this id presents with in the device records?
        return deviceRecords.stream().anyMatch(record -> record.id.equals(id));
    }

    //does this id and token pair presents with in the device records?
    public static boolean isMatchingToken(String id, String token) {
        return deviceRecords.stream()
                .anyMatch(record -> record.id.equals(id) && record.token != null && record.token.equals(token));
    }

    //whether the token exist
    public static boolean isValidToken(String token) {
        return deviceRecords.stream()
                .anyMatch(record -> record.token != null && record.token.equals(token));
    }

    //extract id (last 3 digits of the token)
    public static String extractIdFromToken(String token) {
        if (token == null || token.length() < 3) return null;
        return token.substring(token.length() - 3);
    }
}