package com.shoyu666.job;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import com.shoyu666.db.MqttSqliteOpenHelper;
import com.shoyu666.log.MqttLogger;
import com.shoyu666.mqtt.MqttOption;

import java.sql.SQLException;
import java.util.List;

/**
 * 周期任务
 *
 * @author xining
 * @date 2019/3/4
 */
@DatabaseTable(tableName = "mqtt_job")
public class MqttJob {
    public static final String TAG = "CycleJob";
    @DatabaseField(id = true)
    public String consumerClazz;

    /**
     * 优先级
     */
    @DatabaseField
    public int priority = 1;

    /**
     * job的mqtt通道信息
     */
    @DatabaseField(foreign = true)
    public MqttOption mqttOption;
    /**
     * 任务延迟时间
     */
    @DatabaseField
    public long delaySeconds = 5;

    public MqttJob() {

    }

    public static void clean(Context context) {
        try {
            Dao<MqttJob, String> dao = MqttSqliteOpenHelper.getHelper(context).getDao(MqttJob.class);
            TableUtils.clearTable(dao.getConnectionSource(), MqttJob.class);
        } catch (Exception e) {
            MqttLogger.e("CycleJob", "clean", e);
        }
    }

    public static List<MqttJob> queryForAll(Context context) {
        List<MqttJob> result = null;
        try {
            Dao<MqttJob, String> dao = MqttSqliteOpenHelper.getHelper(context).getDao(MqttJob.class);
            QueryBuilder queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy("priority", true);
            result = queryBuilder.query();
        } catch (SQLException e) {
            MqttLogger.e(TAG, "queryCycleJobGroupOrderByPriority", e);
        }
        return result;
    }

    public void refresh(Context context) throws SQLException {
        mqttOption.refresh(context);
    }

    public static MqttJob create(Class<? extends IMqttConsumer> sourceClazz, MqttOption options) {
        MqttJob job = new MqttJob();
        job.consumerClazz = sourceClazz.getName();
        job.mqttOption = options;
        return job;
    }

    public static MqttJob create(String sourceClazz, MqttOption options) {
        MqttJob job = new MqttJob();
        job.consumerClazz = sourceClazz;
        job.mqttOption = options;
        return job;
    }

    public IMqttConsumer iJobSource;

    public IMqttConsumer getIMqttConsumer() {
        if (iJobSource == null) {
            iJobSource = createIMqttConsumer();
        }
        return iJobSource;
    }

    private IMqttConsumer createIMqttConsumer() {
        IMqttConsumer source = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Class clazz = classLoader.loadClass(consumerClazz);
            // Class clazz = Class.forName(consumerClazz);
            Object object = clazz.newInstance();
            source = (IMqttConsumer) object;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return source;
    }

    public void createOrUpdate(Context context) throws SQLException {
        this.mqttOption.createOrUpdate(context);
        Dao<MqttJob, String> dao = MqttSqliteOpenHelper.getHelper(context).getDao(MqttJob.class);
        dao.createOrUpdate(this);
    }
}
