package com.example.wsdemo.controllers;

import com.example.wsdemo.model.Cred;
import com.example.wsdemo.util.Store;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class APIController {

    @PostMapping("/login")
    public String login(@RequestBody Cred credentials) {

        if (Store.isValidDevice(credentials.getId())) {
            String token = UUID.randomUUID().toString();
            Store.saveToken(credentials.getId(), token);
            System.out.println("User logged in: " + credentials.getId() + ", token: " + token);
            return token;
        } else {
            return "";
        }
    }
}