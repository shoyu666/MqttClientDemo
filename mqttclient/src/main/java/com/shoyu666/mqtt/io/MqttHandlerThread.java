package com.shoyu666.mqtt.io;

import android.os.HandlerThread;

public class MqttHandlerThread extends HandlerThread {
    public MqttHandlerThread(String name) {
        super(name);
    }

    public MqttHandlerThread(String name, int priority) {
        super(name, priority);
    }
}
