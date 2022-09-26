package com.aliyun.sls.android.producer.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.aliyun.sls.android.producer.utils.Utils;

/**
 * Used for obtain application context only.
 * @author gordon
 * @date 2022/9/18
 */
public class SLSContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Utils.setContext(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
