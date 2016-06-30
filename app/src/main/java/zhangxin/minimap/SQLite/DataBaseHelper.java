package zhangxin.minimap.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015/10/21.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub`
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
       // String sql = "create table location(id integer PRIMARY KEY AUTOINCREMENT,user varchar(100) not null , latitude varchar(100) not null, longitude varchar(100) not null);";
        String sql = "create table location(id varchar(100) not null,user varchar(100) not null , latitude varchar(100) not null, longitude varchar(100) not null, time varchar(100) not null);";
        db.execSQL(sql);
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
    }

}
