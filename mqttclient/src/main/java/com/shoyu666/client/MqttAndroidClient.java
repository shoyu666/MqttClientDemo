package com.shoyu666.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.shoyu666.client.token.AndroidToken;
import com.shoyu666.client.token.IAndroidToken;
import com.shoyu666.client.token.IAndroidTokenListener;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.MqttClientApi;
import com.shoyu666.mqtt.MqttOption;
import com.shoyu666.mqtt.token.IMqttToken;
import com.shoyu666.mqtt.token.IMqttTokenListener;
import com.shoyu666.service.MqttService;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import io.netty.buffer.ByteBuf;

public class MqttAndroidClient {
    public static final String TAG = MqttAndroidClient.class.getSimpleName();
    public Queue<AndroidToken> tokens = new LinkedBlockingDeque<>();
    public MqttService mMqttService;

    public MqttAndroidClient(Context context) {
        this.context = context;
    }

    public Context context;

    public MqttOption mMqttOption;

    public void setMqttOption(MqttOption mMqttOption) {
        this.mMqttOption = mMqttOption;
    }

    /**
     * service connection
     */
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MqttLogger.d(TAG, ">> >> onServiceConnected ");
            mMqttService = ((MqttService.MqttServiceBinder) binder).getMqttService();
            republish();
            refreshJob();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MqttLogger.d(TAG, "onServiceDisconnected ");
            mMqttService = null;
        }
    };

    /**
     * republish after service connect
     */
    private void republish() {
        while (!tokens.isEmpty()) {
            AndroidToken token = tokens.poll();
            doPublish(token);
        }
    }

    protected IAndroidToken tryPublish(String topic, ByteBuf playload, IAndroidTokenListener listener) {
        AndroidToken token = new AndroidToken();
        token.topic = topic;
        token.playload = playload;
        token.setListener(listener);
        if (TextUtils.isEmpty(topic) || playload == null) {
            token.ackThrowable(new Exception(">> >> topic or playload not set"));
            return token;
        }
        if (mMqttService == null) {
            storeToken(token);
            MqttService.bindService(context, serviceConnection);
        } else {
            doPublish(token);
        }
        return token;
    }

    public void storeToken(AndroidToken token) {
        MqttLogger.d(TAG, ">> >> storeToken " + token);
        tokens.add(token);
    }

    private void doPublish(final AndroidToken androidToken) {
        MqttLogger.d(TAG, ">> >> doPublish" + androidToken);
        androidToken.isSend = true;
        if (mMqttOption == null) {
            androidToken.ackThrowable(new Exception("mMqttOption is null"));
            return;
        }
        IMqttToken mqttToken = mMqttService.getMqttClient(mMqttOption).publish(mMqttOption, androidToken.topic, androidToken.playload, new IMqttTokenListener() {

            @Override
            public void operationComplete(IMqttToken token) throws Exception {
                MqttLogger.d(TAG, ">> >> operationComplete");
                androidToken.ackIMqttToken(token);
            }
        });
    }

    protected void refreshJob() {
        if (mMqttService != null) {
            mMqttService.refresheCycleJob();
        }else{
            MqttService.bindService(context, serviceConnection);
        }
    }

    public MqttClientApi getMqttClientApi(MqttOption option) {
        if(mMqttService!=null){
           return mMqttService.getMqttClient(option);
        }
        return null;
    }
}
