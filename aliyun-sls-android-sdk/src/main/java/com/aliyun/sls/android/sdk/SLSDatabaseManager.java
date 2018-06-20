package com.aliyun.sls.android.sdk;


import android.app.Service;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.DbUtils;


public class SLSDatabaseManager {
    private static volatile SLSDatabaseManager sInstance;
    private DaoSession daoSession;


    //private constructor.
    private SLSDatabaseManager(){

        //Prevent form the reflection api.
        if (sInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static SLSDatabaseManager getInstance() {
        //Double check locking pattern
        if (sInstance == null) { //Check for the first time

            synchronized (SLSDatabaseManager.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (sInstance == null) {
                    sInstance = new SLSDatabaseManager();
                }
            }
        }

        return sInstance;
    }

    public void setupDB(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME);
        //获取可写数据库

        SQLiteDatabase db = helper.getWritableDatabase();

        //限制数据库最大能存储30M的数据
        db.setMaximumSize(1024 * 1024 * 30);
        Log.i("MyApplication", "pageSize: " + db.getPageSize() + " MaximumSize: " + db.getMaximumSize());

        //获取数据库对象

        DaoMaster daoMaster =new DaoMaster(db);

        //获取Dao对象管理者

        daoSession = daoMaster.newSession();
    }

    public void insertRecordIntoDB(LogEntity entity) {
        try {
            daoSession.getLogEntityDao().insert(entity);
        } catch (SQLiteException e) {
            deleteTwoThousandRecords();
        }
    }

    public void deleteRecordFromDB(LogEntity entity) {
        daoSession.getLogEntityDao().delete(entity);
    }


    public List<LogEntity> queryRecordFromDB() {
        Query query = daoSession.getLogEntityDao().queryBuilder().where(LogEntityDao.Properties.Timestamp.le(new Long(new Date().getTime()))).orderAsc(LogEntityDao.Properties.Timestamp).limit(30).build();
        return query.list();
    }

    public void deleteTwoThousandRecords() {
        Query tableSelectQuery = daoSession.getLogEntityDao().queryBuilder().where(LogEntityDao.Properties.Timestamp.le(new Long(new Date().getTime()))).orderAsc(LogEntityDao.Properties.Timestamp).limit(2000).build();
        List<LogEntity> records = tableSelectQuery.list();
        List ids = new ArrayList();
        for(LogEntity log : records){
            ids.add(log.getId());
        }

        DeleteQuery tableDeleteQuery = daoSession.getLogEntityDao().queryBuilder().where(LogEntityDao.Properties.Id.in(ids)).buildDelete();
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        daoSession.clear();
        DbUtils.vacuum(daoSession.getDatabase());
    }
}
