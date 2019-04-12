package com.shoyu666.mqtt.token;

import com.shoyu666.exception.MqttException;
import com.shoyu666.log.MqttLogger;

import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;

public class TokenAckUtil {
    public static final String TAG = "TokenAckUtil";

    public static void ackThrowable(MqttMessageToken mqttToken, Throwable throwable) {
        if (mqttToken == null) {
            return;
        }
        MqttLogger.d(TAG, "ackThrowable  " + throwable);
        mqttToken.ack(throwable);
    }

    public static void ackMqttConnAckMessage(MqttMessageToken mqttToken, MqttConnAckMessage msg) {
        if (mqttToken == null) {
            return;
        }
        MqttConnectReturnCode returnCode = msg.variableHeader().connectReturnCode();
        switch (returnCode) {
            case CONNECTION_ACCEPTED:
                MqttLogger.d(TAG, "<<  ack CONNECTION_ACCEPTED "+MqttException.CONNECTION_ACCEPTED);
                mqttToken.ack(null);
                break;
            case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
                MqttLogger.e(TAG, "<< ack CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION));
                break;
            case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
                MqttLogger.e(TAG, "<< ack CONNECTION_REFUSED_IDENTIFIER_REJECTED");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_REFUSED_IDENTIFIER_REJECTED));
                break;
            case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
                MqttLogger.e(TAG, "<< ack CONNECTION_REFUSED_SERVER_UNAVAILABLE");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_REFUSED_SERVER_UNAVAILABLE));
                break;
            case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
                MqttLogger.e(TAG, "<< ack CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD));
                break;
            case CONNECTION_REFUSED_NOT_AUTHORIZED:
                MqttLogger.e(TAG, "<< ack CONNECTION_REFUSED_NOT_AUTHORIZED");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_REFUSED_NOT_AUTHORIZED));
                break;
            default:
                MqttLogger.e(TAG, "<< ack CONNECTION_UNKNOW");
                mqttToken.ack(new MqttException(MqttException.CONNECTION_UNKNOW));
        }
    }

    public static void ackMqttPubAckMessage(MqttMessageToken mqttToken, MqttPubAckMessage msg) {
        if (mqttToken == null) {
            MqttLogger.e(TAG, "<< can not ack ackMqttPubAckMessage  mqttToken null");
            return;
        }
        mqttToken.ack(null);
    }
}
