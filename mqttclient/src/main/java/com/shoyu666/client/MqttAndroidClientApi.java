package com.shoyu666.client;

import android.content.Context;

import com.shoyu666.client.token.IAndroidToken;
import com.shoyu666.client.token.IAndroidTokenListener;
import com.shoyu666.job.MqttJob;
import com.shoyu666.job.JobManager;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.service.MqttService;

import io.netty.buffer.ByteBuf;

public class MqttAndroidClientApi extends MqttAndroidClient {
    public MqttAndroidClientApi(Context context) {
        super(context);
        MqttService.startService(context);
    }
    public static final String TAG=MqttAndroidClientApi.class.getSimpleName();
    public IAndroidToken publish(String topic, ByteBuf playload) {
        MqttLogger.d(TAG,"publish "+topic);
        return publish(topic, playload, null);
    }

    public IAndroidToken publish(String topic, ByteBuf playload,IAndroidTokenListener listener) {
        MqttLogger.d(TAG,"publish with listener "+topic);
        return tryPublish(topic, playload, listener);
    }

    public void addJob(MqttJob cycleJob) {
        JobManager.addJob(cycleJob, context);
        refreshJob();
    }
}
