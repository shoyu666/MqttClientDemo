package com.shoyu666.mqtt.channel;

import com.shoyu666.log.MqttLogger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public abstract class MqttChannel {
    public static final String TAG="MqttChannel";
    private Bootstrap bs;
    public ChannelFuture channelFuture;
    public GenericFutureListener closedListenr = new GenericFutureListener() {

        @Override
        public void operationComplete(Future future) throws Exception {
            onChannelClosed();
        }
    };

    protected abstract void onChannelClosed();

    public void doConnect(String host, int port) {
        try {
            if (bs == null) {
                MqttLogger.d(TAG, "create   EventLoopGroup");
                EventLoopGroup group = new NioEventLoopGroup();
                bs = new Bootstrap();
                bs.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new ReadTimeoutHandler(600));
                                p.addLast(MqttEncoder.INSTANCE);
                                p.addLast(new MqttDecoder());
                                p.addLast(getMessageHandler());
                            }
                        });
            }
            realConnect(host, port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract ChannelHandler getMessageHandler();

    private void realConnect(String host, int port) throws InterruptedException {
        MqttLogger.d(TAG, "realConnect "+host+":"+port);
        channelFuture = bs.connect(host, port).sync();
        channelFuture.channel().closeFuture().addListener(closedListenr);
    }

    public ChannelFuture sendPacket(MqttMessage message,String log) {
        MqttLogger.d(TAG, ">> sendPacket "+log);
        return channelFuture.channel().writeAndFlush(message);
    }

    public void checkConnectionBlock(String host, int port) {
        if (channelFuture == null || !channelFuture.channel().isActive()) {
            doConnect(host, port);
        }
    }
}
