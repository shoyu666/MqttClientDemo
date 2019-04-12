package com.shoyu666.mqtt;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.shoyu666.bradcast.MessageDispatcher;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.channel.MqttChannel;
import com.shoyu666.mqtt.handler.MessageHandler;
import com.shoyu666.mqtt.io.MqttHandlerThread;
import com.shoyu666.mqtt.token.MqttMessageToken;
import com.shoyu666.mqtt.token.TokenAckUtil;

import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MessageBuilder;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class MqttClient {
    public static final String TAG = "MqttClient";
    public MqttHandlerThread mqttHandlerThread = new MqttHandlerThread("MqttHandlerThread");
    public Handler mqttHandler;
    public volatile int packageId;

    public Context context;

    public int getStatus() {
        return status;
    }

    public volatile int status=MqttStatusUnConnect;
    public static final int MqttStatusUnConnect = 1;
    public static final int MqttStatusConnected = 2;

    public volatile MqttOption mMqttOption;

    public Map<Integer, MqttMessageToken> tokens = new ConcurrentHashMap<>();
    public MqttMessageToken mMqttMessageToken;

    public MessageHandler messageHandler = new MessageHandler() {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            if (msg instanceof MqttMessage) {
                onMqttMessageRead((MqttMessage) msg);
            }
        }
    };

    private void onMqttMessageRead(MqttMessage msg) {
        if (msg instanceof MqttConnAckMessage) {
            TokenAckUtil.ackMqttConnAckMessage(mMqttMessageToken, (MqttConnAckMessage) msg);
        }
        if (msg instanceof MqttPubAckMessage) {
            MqttPubAckMessage ackMessage = (MqttPubAckMessage) msg;
            int messageId = ackMessage.variableHeader().messageId();
            MqttMessageToken messageToken = tokens.remove(messageId);
            filterCleanTimeOutTokens("pub ack",messageToken);
            TokenAckUtil.ackMqttPubAckMessage(messageToken, (MqttPubAckMessage) msg);
        }
        if(msg instanceof MqttPublishMessage){
            MqttPublishMessage publishMessage = (MqttPublishMessage)msg;
            String topic = publishMessage.variableHeader().topicName();
            MessageDispatcher.sendArriveMessage(context,topic,publishMessage);
        }
    }

    public MqttChannel channel = new MqttChannel() {
        @Override
        protected void onChannelClosed() {
            setStatus(MqttStatusUnConnect," onChannelClosed",true);
            cleanAllTokens("onChannelClosed");
        }

        @Override
        protected ChannelHandler getMessageHandler() {
            return messageHandler;
        }
    };

    /**
     * clean timeout token
     * @param reason
     * @param ackToken
     */
    private void filterCleanTimeOutTokens(String reason,MqttMessageToken ackToken) {
        if(ackToken==null){
            return;
        }
        Iterator<Map.Entry<Integer,MqttMessageToken>> iterator =  tokens.entrySet().iterator();
        while (iterator.hasNext()){
            MqttMessageToken token =  iterator.next().getValue();
            if(token.isOverDue()){
                MqttLogger.e(TAG,"clean a token "+token.packetId);
                iterator.remove();
                token.ack(new TimeoutException(reason));
            }
        }
    }

    /**
     * clean all token
     * @param reason
     */
    private void cleanAllTokens(String reason) {
       Iterator<Map.Entry<Integer,MqttMessageToken>> iterator =  tokens.entrySet().iterator();
       while (iterator.hasNext()){
           MqttMessageToken token =  iterator.next().getValue();
           iterator.remove();
           token.ack(new SocketException(reason));
       }
        tokens.clear();
    }

    public MqttClient(Context context) {
        this.context = context;
        mqttHandlerThread.start();
        mqttHandler = new Handler(mqttHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                MqttMessageToken mqttToken = tokens.get(msg.obj);
                MqttLogger.d(TAG, " handleMessage " + mqttToken.packetId);
                try {
                    doSendPack(mqttToken);
                } catch (Throwable throwable) {
                    TokenAckUtil.ackThrowable(mqttToken, throwable);
                }
            }
        };
    }

    private void doSendPack(final MqttMessageToken mqttToken) throws Throwable {
        channel.checkConnectionBlock(mMqttOption.host, mMqttOption.port);
        checkMqttConnectBlock(5000);
        ChannelFuture future = channel.sendPacket(mqttToken.message,"  mqtt publish");
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    MqttLogger.d(TAG, " sendPacket operationComplete  waiting  ack");
                }else{
                    MqttLogger.e(TAG, " sendPacket fail ");
                    TokenAckUtil.ackThrowable(mqttToken, new Exception(future.cause()));
                }
            }
        });
    }

    private void checkMqttConnectBlock(long timeout) throws Throwable {
        if (status == MqttStatusUnConnect) {
            MqttLogger.d(TAG, " mqtt connecting.....");
            MqttMessageBuilders.ConnectBuilder message = MessageBuilder.createConnectionMessage();
            message.clientId(mMqttOption.clientId);
            message.username(mMqttOption.username);
            message.password(mMqttOption.password);
            message.cleanSession(true);
            message.protocolVersion(MqttVersion.MQTT_3_1_1);
            MqttMessage connetionMessage = message.build();
            mMqttMessageToken = new MqttMessageToken();
            mMqttMessageToken.message = connetionMessage;
            channel.sendPacket(connetionMessage," >> mqtt connect ").sync();
            mMqttMessageToken.waitForCompletion(timeout);
            if (mMqttMessageToken.isComplete() && mMqttMessageToken.isSuccess()) {
                MessageDispatcher.connectComplete(context,false,mMqttOption.host);
                setStatus(MqttStatusConnected,">> connection success",false);
            } else {
                MqttLogger.e(TAG, " connection error ");
                if(mMqttMessageToken.getCase()!=null){
                    throw mMqttMessageToken.getCase();
                }else {
                    throw new Exception(" connection error");
                }
            }
            mMqttMessageToken = null;
        }
    }

    private void setStatus(int status,String reason,boolean error) {
        if(error){
            MqttLogger.e(TAG, " mqttstatus change  "+status+"  for "+reason);
        }else{
            MqttLogger.d(TAG, " mqttstatus change  "+status+"  for "+reason);
        }
        this.status = status;
    }


    protected void storeToken(Integer key, MqttMessageToken mqttToken) {
        tokens.put(key, mqttToken);
    }

    public MqttPublishMessage getPublishMessage(MqttQoS atLeastOnce, String topick, ByteBuf payload) {
        MqttMessageBuilders.PublishBuilder message = MessageBuilder.createPublishMessage();
        message.qos(atLeastOnce);
        message.topicName(topick);
        message.payload(payload);
        message.messageId(nextPackageId());
        return message.build();
    }

    public synchronized int nextPackageId() {
        packageId++;
        if (packageId > 65535) {
            packageId = 0;
        }
        return packageId;
    }
}
