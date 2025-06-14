package com.example.wsdemo.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {

    private static final List<DeviceRecord> deviceRecords = List.of(
            new DeviceRecord("123", "Active"),
            new DeviceRecord("456", "Active"),
            new DeviceRecord("789", "Active")
    );

    private Store() {
    }

    public static List<DeviceRecord> getDeviceRecords() {
        return deviceRecords;
    }

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

    //save device status by session id
    public static void saveDeviceStatusBySessionId(String sessionId, String status) {
        deviceRecords.stream()
            .filter(record -> record.sessionId != null && record.sessionId.equals(sessionId))
            .findFirst()
            .ifPresent(record -> record.status = status);
    }

    //save session id for a device id
    public static void saveSessionId(String id, String sessionId) {
        deviceRecords.stream()
            .filter(record -> record.id.equals(id))
            .findFirst()
            .ifPresent(record -> record.sessionId = sessionId);
    }

    //get device id by session id
    public static String getDeviceIdBySessionId(String sessionId) {
        return deviceRecords.stream()
                .filter(record -> record.sessionId != null && record.sessionId.equals(sessionId))
                .map(record -> record.id)
                .findFirst()
                .orElse(null);
    }

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