package com.shoyu666.job;

import android.content.Context;

import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.MqttClientApi;
import com.shoyu666.mqtt.MqttOption;
import com.shoyu666.service.MqttService;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xining
 * @date 2019/3/4
 */
public class JobManager {
    public static final String TAG = JobManager.class.getSimpleName();
    public MqttService mMqttService;
    public List<MqttJob> jobGroupsInMem = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService mExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> futureTask;
    public Runnable jobExecute = new Runnable() {
        @Override
        public void run() {
            MqttLogger.d(TAG, "job run......");
            try {
                if (jobGroupsInMem.size() > 0 && mMqttService.connectivityManager.isConnected()) {
                    loopJob(jobGroupsInMem);
                }
            } catch (Exception e) {
                MqttLogger.e(TAG, "job run...... Exception end ", e);
            }
            MqttLogger.d(TAG, "job run...... end");
        }

        private void loopJob(List<MqttJob> jobs) throws SQLException {
            Iterator<MqttJob> cycleJobs = jobs.iterator();
            boolean breakGroup = false;
            while (cycleJobs.hasNext()) {
                MqttJob cycleJob = cycleJobs.next();
                cycleJob.refresh(mMqttService.getApplicationContext());
                IMqttConsumer mqttConsumer = cycleJob.getIMqttConsumer();
                if (mqttConsumer == null) {
                    MqttLogger.e(TAG, "mqttConsumer not find "+cycleJob.consumerClazz);
                    continue;
                }
                if (!mqttConsumer.needUpload()) {
                    MqttLogger.d(TAG, "no need consume continue "+cycleJob.consumerClazz);
                    continue;
                }
                try {
                    MqttClientApi mqttClientApi = mMqttService.getMqttClient(cycleJob.mqttOption);
                    if (mqttClientApi != null) {
                        breakGroup = mqttConsumer.comsume(cycleJob, mqttClientApi);
                    }
                } catch (Exception e) {
                    MqttLogger.e(TAG, "comsume error "+cycleJob.consumerClazz, e);
                    breakGroup = true;
                }
                if (breakGroup) {
                    break;
                }
            }
        }
    };

    public JobManager(MqttService mMqttService) {
        this.mMqttService = mMqttService;
    }

    public static void clean(Context app) {
        MqttOption.clean(app);
        MqttJob.clean(app);
    }

    public static void addJob(MqttJob cycleJob, Context context) {
        try {
            cycleJob.createOrUpdate(context);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refresheCycleJob(Context context, String reason) {
        MqttLogger.d(TAG, "refresheCycleJob " + reason);
        restoreJobGroups(context);
        changeExecutorRate();
    }

    /**
     * 所有任务当中最大频率为基准
     *
     * @return
     */
    private long getMiniRate() {
        long delaySeconds = Integer.MAX_VALUE;
        if (jobGroupsInMem.size() > 0) {
            Iterator<MqttJob> it = jobGroupsInMem.iterator();
            while (it.hasNext()) {
                MqttJob entry = it.next();
                delaySeconds = Math.min(delaySeconds, entry.delaySeconds);
            }
        }
        return delaySeconds;
    }

    /**
     * 修改任务周期频率
     */
    private void changeExecutorRate() {
        long delay = getMiniRate();
        if (futureTask != null) {
            MqttLogger.d(TAG, "cancel  all job");
            futureTask.cancel(true);
        }
        if (delay == Integer.MAX_VALUE) {
            MqttLogger.d(TAG, "no job find");
            return;
        }
        MqttLogger.d(TAG, "change job rate  to  " + delay);
        futureTask = mExecutorService.scheduleWithFixedDelay(jobExecute, 1, delay, TimeUnit.SECONDS);
    }

    /**
     * 加载离线任务
     *
     * @param context
     */
    private void restoreJobGroups(Context context) {
        List<MqttJob> jobGroups = MqttJob.queryForAll(context);
        jobGroupsInMem.clear();
        if (jobGroups != null && jobGroups.size() > 0) {
            jobGroupsInMem.addAll(jobGroups);
        }
    }

    /**
     * 更新配置 一般用于密码修改
     *
     * @param options
     */
    public void updateConnectionOption(MqttOption options, Context context) {
        if (options != null) {
            try {
                MqttLogger.d(TAG, "updateConnectionOption");
                options.update(context);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
