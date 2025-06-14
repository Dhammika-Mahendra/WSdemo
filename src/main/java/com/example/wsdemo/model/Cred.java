package com.example.wsdemo.model;

public class Cred {
    public String id;
    public String name;

    public Cred(String id, String name, String status) {
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
