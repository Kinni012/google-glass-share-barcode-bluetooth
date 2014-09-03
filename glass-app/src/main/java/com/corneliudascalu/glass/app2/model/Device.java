package com.corneliudascalu.glass.app2.model;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author Corneliu Dascalu <corneliu.dascalu@gmail.com>
 */
public class Device {

    private String name;

    private String token;

    public Device() {
        name = RandomStringUtils.random(7);
        token = RandomStringUtils.random(20);
    }

    public Device(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}