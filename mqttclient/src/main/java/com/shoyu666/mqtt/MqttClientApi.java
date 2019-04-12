package com.shoyu666.mqtt;

import android.content.Context;

import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.token.IMqttToken;
import com.shoyu666.mqtt.token.IMqttTokenListener;
import com.shoyu666.mqtt.token.MqttMessageToken;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttClientApi extends MqttClient{
    public MqttClientApi(Context context){
        super(context);
    }
    public IMqttToken publish(MqttOption option, String topic, ByteBuf playload, IMqttTokenListener mqttTokenListener) {
        mMqttOption = option;
        MqttPublishMessage mqttPublishMessage = getPublishMessage(MqttQoS.AT_LEAST_ONCE, topic, playload);
        MqttMessageToken mqttToken = new MqttMessageToken();
        mqttToken.setListener(mqttTokenListener);
        mqttToken.packetId = mqttPublishMessage.variableHeader().messageId();
        mqttToken.message = mqttPublishMessage;
        storeToken(mqttToken.packetId, mqttToken);
        MqttLogger.d(TAG, "sendMessage packetId："+mqttToken.packetId);
        mqttHandler.sendMessage(mqttHandler.obtainMessage(1, mqttToken.packetId));
        return mqttToken;
    }
}
