package com.shoyu666.mqtt;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import com.shoyu666.db.MqttSqliteOpenHelper;
import com.shoyu666.log.MqttLogger;

import java.sql.SQLException;

@DatabaseTable(tableName = "mqtt_connect_option")
public class MqttOption {
    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String clientId;
    @DatabaseField
    public String host;
    @DatabaseField
    public int port;
    @DatabaseField
    public String username;
    @DatabaseField
    public String password;

    public static MqttOption get(String host, int port, String clientId) {
        MqttOption connectOptions = new MqttOption();
        connectOptions.host = host;
        connectOptions.clientId = clientId;
        connectOptions.port = port;
        connectOptions.id = host;
        return connectOptions;
    }

    public void createOrUpdate(Context context) throws SQLException {
        Dao<MqttOption, String> daoZebraStoreMqttConnectOptions = MqttSqliteOpenHelper.getHelper(context).getDao(MqttOption.class);
        daoZebraStoreMqttConnectOptions.createOrUpdate(this);
    }

    public void refresh(Context context) throws SQLException {
        Dao<MqttOption, String> daoZebraStoreMqttConnectOptions = MqttSqliteOpenHelper.getHelper(context).getDao(MqttOption.class);
        daoZebraStoreMqttConnectOptions.refresh(this);
    }

    public void update(Context context) throws SQLException {
        Dao<MqttOption, String> daoZebraStoreMqttConnectOptions = MqttSqliteOpenHelper.getHelper(context).getDao(MqttOption.class);
        daoZebraStoreMqttConnectOptions.update(this);
    }

    public static void clean(Context context) {
        try {
            Dao<MqttOption, String> dao = MqttSqliteOpenHelper.getHelper(context).getDao(MqttOption.class);
            TableUtils.clearTable(dao.getConnectionSource(), MqttOption.class);
        } catch (Exception e) {
            MqttLogger.e("MqttOption", "clean", e);
        }
    }
}
