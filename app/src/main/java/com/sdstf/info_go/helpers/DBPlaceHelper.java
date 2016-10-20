package com.sdstf.info_go.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kyliemmarshall on 16/10/16.
 */

public class DBPlaceHelper extends SQLiteOpenHelper  {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "infogo";
    private static final String TABLE_NAME= "placeDetails";

    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_PLACE_ARRAY = "placeArray";
    public DBPlaceHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLACE_DETAIL_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE, "
                + KEY_PLACE_ARRAY + " TEXT "+ ")";
        db.execSQL(CREATE_PLACE_DETAIL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean insertPlace(double latitude, double longitude, String placeArray){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_LATITUDE, latitude);
        contentValues.put(KEY_LONGITUDE , longitude);
        contentValues.put(KEY_PLACE_ARRAY, placeArray);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }


    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME + " where id="+KEY_ID+"", null );
        return res;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }
    public boolean updatePlace (Integer id, double latitude, double longitude, String placeArray)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_LATITUDE, latitude);
        contentValues.put(KEY_LONGITUDE, longitude);
        contentValues.put(KEY_PLACE_ARRAY, placeArray);
        db.update(TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deletePlace(Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllPlaces()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ TABLE_NAME + "", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(KEY_ID))+"\n"
                    + res.getString(res.getColumnIndex(KEY_LATITUDE))+"\n"
                    + res.getString(res.getColumnIndex(KEY_LONGITUDE))+"\n"
                    + res.getString(res.getColumnIndex(KEY_PLACE_ARRAY))+"\n");
            res.moveToNext();
        }
        return array_list;
    }
}
