package com.shoyu666.exception;

public class MqttException extends Exception{
    public MqttException(int code) {
        this.code = code;
    }
    public int code;
    public static final int CONNECTION_UNKNOW=0;
    public static final int CONNECTION_ACCEPTED=1;
    public static final int CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION=2;
    public static final int CONNECTION_REFUSED_IDENTIFIER_REJECTED=3;
    public static final int CONNECTION_REFUSED_SERVER_UNAVAILABLE=4;
    public static final int CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD=5;
    public static final int CONNECTION_REFUSED_NOT_AUTHORIZED=6;
    public static final int SERVICE_TIME_OUT=100;
}
