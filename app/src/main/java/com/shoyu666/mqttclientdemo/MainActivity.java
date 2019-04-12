package com.shoyu666.mqttclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shoyu666.client.MqttAndroidClientApi;
import com.shoyu666.client.token.IAndroidToken;
import com.shoyu666.client.token.IAndroidTokenListener;
import com.shoyu666.job.MqttJob;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.MqttOption;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final String Host = "iot.eclipse.org";
    public static final int Port = 1883;
    private static final ByteBufAllocator ALLOCATOR = new UnpooledByteBufAllocator(false);
    public MqttAndroidClientApi client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new MqttAndroidClientApi(this);
        client.setMqttOption(getMqttOption());
    }

    public MqttOption getMqttOption(){
        MqttOption option = new MqttOption();
        option.clientId = "MainActivity";
        option.port = Port;
        option.host = Host;
        option.id = Host;
        return option;
    }

    public void publish(View view) {
        ByteBuf byteBuf = ALLOCATOR.buffer();
        IAndroidToken token = client.publish("xxxx", byteBuf, new IAndroidTokenListener() {
            @Override
            public void operationComplete(IAndroidToken token) throws Exception {
                if (token.isSuccess()) {
                    MqttLogger.d(TAG, "send success ");
                } else {
                    Throwable throwable = token.getCase();
                    MqttLogger.e(TAG, "send failt ", throwable);
                }
            }
        });
    }

    public void publishSync(View view) {
        ByteBuf byteBuf = ALLOCATOR.buffer();
        IAndroidToken token = client.publish("xxxx", byteBuf);
        token.waitForCompletion(5000);
        if (token.isSuccess()) {
            MqttLogger.d(TAG, "send success ");
        } else {
            Throwable throwable = token.getCase();
            MqttLogger.e(TAG, "send failt ", throwable);
        }
    }

    public void addJob(){
        MqttJob cycleJob = MqttJob.create("com.icongtai.zebra.dilink.DlinkDataConsumer", getMqttOption());
        client.addJob(cycleJob);
    }
}
