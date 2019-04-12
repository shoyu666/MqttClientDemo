package com.shoyu666.mqtt.token;


import com.shoyu666.log.MqttLogger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.mqtt.MqttMessage;

public class MqttMessageToken implements IMqttToken {
    public static final String TAG="MqttMessageToken";
    public int packetId;
    public MqttMessage message;
    public volatile long startTime=System.currentTimeMillis();
    public volatile boolean isComplete = false;
    public volatile boolean isSuccess = false;
    public volatile Throwable throwable;
    public CountDownLatch countDownLatch;
    public volatile IMqttTokenListener listener;

    public boolean isOverDue(){
        long offset = System.currentTimeMillis() - startTime;
        return offset>10000;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public Throwable getCase() {
        return throwable;
    }

    @Override
    public void setListener(IMqttTokenListener listener) {
        this.listener = listener;
    }

    @Override
    public void waitForCompletion(long timeOut) {
        if (listener != null) {
            throw new RuntimeException("listener set ");
        }
        if(countDownLatch!=null){
            throw new RuntimeException("can not call waitForCompletion");
        }
        synchronized (this) {
            if (isComplete) {
                return;
            }
        }
        try {
            countDownLatch = new CountDownLatch(1);
            countDownLatch.await(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ack(Throwable throwable) {
        isSuccess = throwable == null;
        this.throwable = throwable;
        ack();
    }

    public void ack() {
        MqttLogger.d(TAG, "<< ack mqttPubAckMessage");
        isComplete = true;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        if (listener != null) {
            try {
                listener.operationComplete(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
