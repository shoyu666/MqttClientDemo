package com.shoyu666.mqtt.token;

public interface IMqttToken {
    boolean isSuccess();
    Throwable getCase();
    void setListener(IMqttTokenListener listener);
    void waitForCompletion(long timeOut);
    boolean isComplete();
    void ack(Throwable throwable);
}
