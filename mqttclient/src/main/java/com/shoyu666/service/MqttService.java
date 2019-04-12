package com.shoyu666.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.shoyu666.job.JobManager;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.MqttClientApi;
import com.shoyu666.mqtt.MqttOption;
import com.shoyu666.net.ConnectivityManager;

import java.util.HashMap;
import java.util.Map;

public class MqttService extends Service {
    public static final String TAG = MqttService.class.getSimpleName();
    public Map<String, MqttClientApi> clients = new HashMap<>();

    /**
     * 任务管理
     */
    public JobManager jobManager = new JobManager(this);
    /**
     * 网络管理
     */
    public ConnectivityManager connectivityManager = new ConnectivityManager(this);

    public static void startService(Context context) {
        Intent intent = new Intent(context, MqttService.class);
        context.startService(intent);
    }

    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, MqttService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jobManager.refresheCycleJob(this, "MqttService start");
        connectivityManager.start(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public MqttClientApi getMqttClient(MqttOption mMqttOption) {
        MqttClientApi client = clients.get(mMqttOption.id);
        if (client == null) {
            client = createClient(mMqttOption.id);
        }else{
            MqttLogger.d(TAG, "exist Client "+mMqttOption.id);
        }
        return client;
    }

    private MqttClientApi createClient(String id) {
        MqttLogger.d(TAG, "createClient "+id);
        MqttClientApi client = new MqttClientApi(this);
        clients.put(id, client);
        return client;
    }
    MqttServiceBinder binder = new MqttServiceBinder(){

        @Override
        public MqttService getMqttService() {
            return MqttService.this;
        }
    };

    public void refresheCycleJob() {
        jobManager.refresheCycleJob(this,"");
    }

    public abstract class MqttServiceBinder extends Binder{
       public abstract MqttService getMqttService();
    }
}
