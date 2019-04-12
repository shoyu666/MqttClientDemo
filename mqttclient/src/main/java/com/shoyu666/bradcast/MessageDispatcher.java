package com.shoyu666.bradcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.mqtt.MqttPublishMessage;


/**
 * @author xining
 * @date 2019/3/5
 */
public class MessageDispatcher {
    public static final String MqttCallbackACTION = "com.zebra.module.mqtt.service.conntion";
    public static final String MqttCallback = "MqttCallback";
    public static final int MqttCallback_ConnectionComplete = 1;
    public static final String Reconnect = "Reconnect";
    public static final String ConnectBroker = "ConnectBroker";
    public static final String MSG = "message";
    public static final Map<String,String> mTopWithAction = new HashMap<>();
    public static void registMqttCallback(Context context, BroadcastReceiver receiver) {
        IntentFilter intent = new IntentFilter(MqttCallbackACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intent);
    }

    public static void unRegist(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public static void registMqttCallback(Context context, IntentFilter intentFilter, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
    }

    public static void addTopicAndMqtt(String topic, String action){
        mTopWithAction.put(topic, action);
    }

    public static void connectComplete(Context context, boolean reconnect, String serverURI) {
        Intent intent = new Intent(MqttCallbackACTION);
        intent.putExtra(MqttCallback, MqttCallback_ConnectionComplete);
        intent.putExtra(Reconnect, reconnect);
        intent.putExtra(ConnectBroker, serverURI);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendArriveMessage(Context context, String topic, MqttPublishMessage mqttMessage){
        String action = mTopWithAction.get(topic);
        if (TextUtils.isEmpty(action)) {
            action = MqttCallbackACTION;
        }
        Intent intent = new Intent(action);
        intent.putExtra(MSG, mqttMessage.payload().array());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
