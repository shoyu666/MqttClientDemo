package io.netty.handler.codec.mqtt;

/**
 * message builder
 */
public class MessageBuilder {
    public static MqttMessageBuilders.ConnectBuilder createConnectionMessage() {
        MqttMessageBuilders.ConnectBuilder builder = new MqttMessageBuilders.ConnectBuilder();
        return builder;
    }

    public static MqttMessageBuilders.PublishBuilder createPublishMessage() {
        MqttMessageBuilders.PublishBuilder builder = new MqttMessageBuilders.PublishBuilder();
        return builder;
    }
}
