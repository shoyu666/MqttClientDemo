package com.shoyu666.client.token;

import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.token.IMqttToken;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;

public class AndroidToken implements IAndroidToken {
    public static final String TAG="AndroidToken";
    public String topic;
    public ByteBuf playload;
    public CountDownLatch countDownLatch;
    public boolean isSend = false;
    public volatile boolean isComplete = false;
    public volatile boolean isSuccess = false;
    public volatile Throwable throwable;
    public IAndroidTokenListener listener;

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
    public void setListener(IAndroidTokenListener listener) {
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
            countDownLatch  = new CountDownLatch(1);
            countDownLatch.await(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void ack() {
        MqttLogger.d(TAG,"<< << ack ");
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

    @Override
    public void ackIMqttToken(IMqttToken token) {
        isSuccess = token.isSuccess();
        this.throwable = token.getCase();
        ack();
    }

    @Override
    public void ackThrowable(Throwable throwable) {
        isSuccess = throwable == null;
        this.throwable = throwable;
        ack();
    }
}
