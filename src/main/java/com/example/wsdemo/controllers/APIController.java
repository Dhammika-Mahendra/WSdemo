package com.example.wsdemo.controllers;

import com.example.wsdemo.model.Cred;
import com.example.wsdemo.util.Store;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class APIController {

    @PostMapping("/dev/login")
    public String devLogin(@RequestBody Cred credentials) {

        if (Store.isValidDevice(credentials.getId())) {
            String token = UUID.randomUUID().toString();
            //attaching id to token
            token = token+credentials.getId();
            Store.saveToken(credentials.getId(), token);
            System.out.println("User logged in: " + credentials.getId() + ", token: " + token);
            return token;
        } else {
            return "";
        }
    }

    @PostMapping("/usr/login")
    public String usrLogin(@RequestBody String userName) {
            if (Store.isUserExists(userName)) {
                String token = UUID.randomUUID().toString() + userName;
                Store.saveUserToken(userName, token);
                System.out.println("User logged in: " + userName + ", token: " + token);
                return token;
            }
        return "";
    }
}