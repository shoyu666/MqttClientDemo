package com.shoyu666.mqtt.token;

public interface IMqttTokenListener {
    void operationComplete(IMqttToken token) throws Exception;
}
