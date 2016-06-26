package howard.taylor.spotifysdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
/**
 * Created by Thoward on 6/19/2016.
 */
public class AlarmDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "AlarmDB.db";
    public static final String TABLE_NAME = "AlarmDB";
    private static final int DB_VERSION = 1;
    public static final String ALARM_ID = "alarm_id";
    public static final String ALARM_ACTIVE = "alarm_active";
    public static final String ALARM_DAY = "alarm_day";
    public static final String ALARM_TIME = "alarm_time";
    public static final String ALARM_PLAYLIST = "alarm_playlist";


    public AlarmDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + " ( "
                + ALARM_ID + " integer primary key, "
                + ALARM_ACTIVE + " integer, " + ALARM_DAY + " varchar, "
                + ALARM_TIME + " varchar, " + ALARM_PLAYLIST + " varchar);");
    }

    public boolean insertAlarm(int id, int active, String day, String time, String playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("alarm_id", id);
        contentValues.put("alarm_active", active);
        contentValues.put("alarm_day", day);
        contentValues.put("alarm_time", time);
        contentValues.put("alarm_playlist", playlist);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateAlarm(int id, int active, String day, String time, String playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("alarm_id", id);
        contentValues.put("alarm_active", active);
        contentValues.put("alarm_day", day);
        contentValues.put("alarm_time", time);
        contentValues.put("alarm_playlist", playlist);
        db.update("AlarmDB", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where id=" + id, null);
        return res;
    }

    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }

    public ArrayList<String> getAllAlarms() {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(ALARM_TIME)));
            res.moveToNext();
        }

        return array_list;
    }
    public ArrayList<Integer> getAllIDs() {
        ArrayList<Integer> array_list = new ArrayList<Integer>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getInt(res.getColumnIndex(ALARM_ID)));
            res.moveToNext();
        }

        return array_list;
    }

    public boolean removeAlarm(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_NAME, ALARM_ID + " = " + id, null) >0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
