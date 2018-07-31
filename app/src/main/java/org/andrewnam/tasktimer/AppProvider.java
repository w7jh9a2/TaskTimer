package org.andrewnam.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Provider for the TaskTimer app. This is the only class that know about {@link AppDatabase}
 */

public class AppProvider extends ContentProvider{
    private static final String TAG = "AppProvider";

    private AppDatabase mOpenHelper;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    static final String CONTENT_AUTHORITY = "org.andrewnam.tasktimer.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final int TASKS = 100;
    public static final int TASKS_ID = 101;

    public static final int TIMINGS = 200;
    public static final int TIMINGS_ID = 201;

    /*
        private static final int TASK_TIMINGS = 300;
        private static final int TASK_TIMINGS_ID = 301;
     */

    public static final int TASK_DURATIONS = 400;
    public static final int TASK_DURATIONS_ID = 401;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // e.g. content://org.andrewnaam.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
        // e.g. content://org.andrewnam.tasktimer.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID);

//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS);
//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME + "/#", TIMINGS_ID);
//
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS);
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME + "/#", TASK_DURATIONS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG, "query: called with URI " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query: match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(match) {
            case TASKS:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;
            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
                break;

//            case TIMINGS:
//                queryBuilder.setTables(TimingsContract.TABLE_NAME);
//                break;
//            case TIMINGS_ID:
//                queryBuilder.setTables(TimingsContract.TABLE_NAME);
//                long timingId = TimingsContract.getTimingID(uri);
//                queryBuilder.appendWhere(TimingsContract.Columns._ID + " = " + timingId);
//                break;
//
//            case TASK_DURATIONS:
//                queryBuilder.setTables(DurationsContract.TABLE_NAME);
//                break;
//            case TASK_DURATIONS_ID:
//                queryBuilder.setTables(DurationsContract.TABLE_NAME);
//                long durationId = DurationsContract.getDurationID(uri);
//                queryBuilder.appendWhere(DurationsContract.Columns._ID + " = " + durationId);
//                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d(TAG, "query: rows in returned cursor " + cursor.getCount()); // TODO remove this line

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return TasksContract.CONTENT_TYPE;

            case TASKS_ID:
                return TasksContract.CONTENT_ITEM_TYPE;

//            case TIMINGS:
//                return TimingsContract.Timings.CONTENT_TYPE;
//
//            case TIMINGS_ID:
//                return TimingsContract.Timings.CONTENT_ITEM_TYPE;
//
//            case TASK_DURATIONS:
//                return DurationsContract.TaskDurations.CONTENT_TYPE;
//
//            case TASK_DURATIONS_ID:
//                return DurationsContract.TaskDurations.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "Entering insert, called with uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is " + match);

        final SQLiteDatabase db;

        Uri returnUri;
        long recordId;

        switch(match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME, null, values);
                if(recordId >= 0) {
                    returnUri = TasksContract.buildTaskUri(recordId);
                } else {
                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
                }
                break;

//            case TIMINGS:
//                db = mOpenHelper.getWritableDatabase();
//                recordId = db.insert(TimingsContract.Timings.buildTimingUri((recordId));
//                if(recordId >= 0) {
//                    returnUri = TimingsContract.Timings.buildTimingUri(recordId);
//                } else {
//                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
//                }
//                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        if(recordId >= 0) {
            // something was inserted
            Log.d(TAG, "insert: Setting notifyChanged with " + uri);
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.d(TAG, "insert: nothing inserted");
        }
        Log.d(TAG, "Exiting insert, returning " + returnUri);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        Log.d(TAG, "update called with uri " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is " + match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch(match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME,  selection, selectionArgs);
                break;

            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;

                if((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;

//            case TIMINGS:
//                db = mOpenHelper.getWritableDatabase();
//                count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs);
//                break;
//
//            case TIMINGS_ID:
//                db = mOpenHelper.getWritableDatabase();
//                long timingsId = TimingsContract.getTimingsId(uri);
//                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
//
//                if((selection != null) && (selection.length() > 0)) {
//                    selectionCriteria += " AND (" + selection + ")";
//                }
//                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
//                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        if(count > 0) {
            // something was deleted
            Log.d(TAG, "delete: Setting notifyChange with " + uri);
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.d(TAG, "delete: nothing deleted");
        }

        Log.d(TAG, "Exiting update, returning " + count);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update called with uri " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "match is " + match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch(match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TASKS_ID:
                db = mOpenHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;

                if((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;

//            case TIMINGS:
//                db = mOpenHelper.getWritableDatabase();
//                count = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs);
//                break;
//
//            case TIMINGS_ID:
//                db = mOpenHelper.getWritableDatabase();
//                long timingsId = TimingsContract.getTimingsId(uri);
//                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
//
//                if((selection != null) && (selection.length() > 0)) {
//                    selectionCriteria += " AND (" + selection + ")";
//                }
//                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
//                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        if(count > 0) {
            // something was updated
            Log.d(TAG, "update: Setting notifyChange with " + uri);
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.d(TAG, "update: nothing deleted");
        }

        Log.d(TAG, "Exiting update, returning " + count);
        return count;
    }
}
