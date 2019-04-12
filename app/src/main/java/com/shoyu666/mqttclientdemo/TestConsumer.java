package com.shoyu666.mqttclientdemo;

import com.shoyu666.exception.MqttException;
import com.shoyu666.job.IMqttConsumer;
import com.shoyu666.job.MqttJob;
import com.shoyu666.mqtt.MqttClientApi;
import com.shoyu666.mqtt.token.IMqttToken;

public class TestConsumer implements IMqttConsumer {
    @Override
    public boolean comsume(MqttJob cycleJob, MqttClientApi conntion) throws Exception {
        IMqttToken  token = conntion.publish(cycleJob.mqttOption,"",null,null);
        token.waitForCompletion(5000);
        if(!token.isSuccess()){
            if(token.getCase() instanceof MqttException){
                MqttException exception = (MqttException)token.getCase();
                if(exception.code ==MqttException.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD){
                    //登录
                }
            }
        }
        return false;
    }

    @Override
    public void onThrowable(Throwable e) {

    }

    @Override
    public boolean needUpload() {
        return false;
    }
}
