package com.corneliudascalu.glass.phone.service;

/**
 * @author Corneliu Dascalu <corneliu.dascalu@gmail.com>
 */
public interface BackService {

    void forceGcmRegistration();

    void registerToServerInBackground();
}
