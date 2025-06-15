package com.example.wsdemo.util;

public class User {
    public String name;
    public Boolean loggedIn;
    public String token;

    public User(String name, Boolean loggedIn) {
        this.name = name;
        this.loggedIn = loggedIn;
    }
}
