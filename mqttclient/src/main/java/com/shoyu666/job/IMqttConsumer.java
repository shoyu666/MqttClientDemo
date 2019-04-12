package com.shoyu666.job;

import com.shoyu666.mqtt.MqttClientApi;

import java.io.Serializable;

/**
 * @author xining
 * @date 2019/3/4
 */
public interface IMqttConsumer extends Serializable {
    /**
     *
     * @param cycleJob
     * @param conntion
     * @return breakGroup 是否终止所在的group
     */
    boolean comsume(MqttJob cycleJob, MqttClientApi conntion) throws Exception;
    /**
     * 是否有数据需要上传
     * @return
     */
    boolean needUpload();

    void onThrowable(Throwable e);
}
