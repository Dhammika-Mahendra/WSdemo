package com.example.wsdemo.controllers;

import com.example.wsdemo.model.DeviceInfo;
import com.example.wsdemo.util.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class APIController {

    @PostMapping("/login")
    public String login(@RequestBody DeviceInfo deviceInfo) {

        if (Store.isValidDevice(deviceInfo.getId())) {
            String token = UUID.randomUUID().toString();
            Store.saveToken(deviceInfo.getId(), token);
            System.out.println("User logged in: " + deviceInfo.getId() + ", token: " + token);
            return token;
        } else {
            return "";
        }
    }
}