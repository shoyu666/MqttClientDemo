package com.shoyu666.client.token;

import com.shoyu666.mqtt.token.IMqttToken;

public interface IAndroidToken {
    boolean isSuccess();
    boolean isComplete();
    Throwable getCase();
    void setListener(IAndroidTokenListener listener);
    void waitForCompletion(long timeOut);
    void ackIMqttToken(IMqttToken token);
    void ackThrowable(Throwable throwable);
}
