package zhangxin.minimap.SQLite;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import zhangxin.minimap.Location.MainActivity;


/**
 * Created by Administrator on 2015/10/21.
 */
public class DataBaseManager extends Activity {
    private DataBaseHelper helper;
    private SQLiteDatabase db;
    private boolean rename_flag,recover_flag = false,time_flag = false,range_flag = true;//recover_flag默认不覆盖坐标，time_flag时间间隔默认大于一秒，range_flag距离默认合法;
    private Double location_info[][] = new Double[10000][2];
    private String endTime;
    private double LastLatitude,LastLongitude;
    private static final  double EARTH_RADIUS = 6378137;//赤道半径(单位m)

    public DataBaseManager(Context context) {
        helper = new DataBaseHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public void add(String userID,Double userLatitude,Double userLongitude,String data){
        if(!Is_recover(userLatitude,userLongitude) && isRangeLegal(userLongitude,userLatitude,data) ) {
            ContentValues cv = new ContentValues();//实例化一个ContentValues用来装载待插入的数据
            Cursor c = db.query("location", null, null, null, null, null, null);//查询并获得游标
            int id = c.getCount();
            cv.put("id", id);//id
            cv.put("user", userID);//添加用户名
            cv.put("latitude", userLatitude); //添加用户位置经度
            cv.put("longitude", userLongitude); //添加是用户位置纬度
            cv.put("time", data); //添加录入时间
            db.insert("location", null, cv);//执行插入操作
        }
    }

    //用于查询所有的位置记录
    public Double[][] query(String m_userID) {
        Cursor c = db.query("location", null, null, null, null, null, null);//查询并获得游标
        String longitude="",latitude="",user = "",id="";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            for(int i=0;i<c.getCount();i++) {
                if (m_userID.equals(c.getString(c.getColumnIndex(user) + 2))) {
                    location_info[i][0] = c.getDouble(c.getColumnIndex(latitude) + 3);
                    location_info[i][1] = c.getDouble(c.getColumnIndex(longitude) + 4);
                }
                c.moveToNext();
            }
        }
        return location_info;
    }


    public void delete(String name){
        String whereClause = "user=?";//删除的条件
        String[] whereArgs = {name};//删除的条件参数
        db.delete("location",whereClause,whereArgs);//执行删除
        closeDB();
        Log.d("delete success", "#############");
    }

    public void deleteLine(){
        String sql = "delete from location where id in (select id from location order by id limit 0,3000)";//删除操作的SQL语句
       // String sql = "delete from location order by id limit 20";//删除操作的SQL语句
        db.execSQL(sql);//执行删除操作
    }

    //用于查询是否与当前最新地理位置重叠，（重叠则不将当前位置写入数据库）
    public boolean Is_recover(double Latitude,double Longitude) {
        db = helper.getWritableDatabase();
        Cursor c = db.query("location", null, null, null, null, null, null);//查询并获得游标
        if(c.getCount() > 0 ){
            c.moveToLast();
            String longitude="",latitude="",time="";
            LastLatitude = c.getDouble(c.getColumnIndex(latitude) + 3);
            LastLongitude = c.getDouble(c.getColumnIndex(longitude) + 4);
            endTime = c.getString(c.getColumnIndex(time) + 5);
            if(LastLatitude == Latitude && LastLongitude == Longitude)
                recover_flag = true;
            else
                recover_flag = false;
        }

        if(c.getCount() >9500){
            deleteLine();
        }
        closeDB();
        return recover_flag;
    }

    private boolean lessOneSeconds(String curData){
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        db = helper.getWritableDatabase();
        Cursor c = db.query("location", null, null, null, null, null, null);//查询并获得游标
        if(c.getCount() > 0 ) {
            try {
                Date curDate = myFormatter.parse(curData);
                Date endDate = myFormatter.parse(endTime);
                long diff = (curDate.getTime() - endDate.getTime())/1000;
                if (diff <= 1)
                    time_flag = true;
                else
                    time_flag = false;
            } catch (Exception e) {
                return time_flag;
            }
        }
        return time_flag;
    }


    /**
     * 转化为弧度(rad)
     * */
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    /**
     * 基于余弦定理求两经纬度距离
     * @param Longitude 第一点的精度
     * @param Latitude 第一点的纬度
     * @return 返回的距离，单位m
     * */
    public boolean isRangeLegal(double Longitude, double Latitude,String data) {

        if( lessOneSeconds(data) ){
            //计算时间内距离是否合法
            LatLng p1 = new LatLng(Latitude,Longitude);
            LatLng p2 = new LatLng(LastLatitude,LastLongitude);
            if(SpatialRelationUtil.isCircleContainsPoint(MainActivity.localLocate, 10000000, p1)) {
                double dis = DistanceUtil.getDistance(p1, p2);
                if (dis <= 50)
                    range_flag = true;
                else
                    range_flag = false;
            }
            else {
                range_flag = false;
            }
        }
        return range_flag;
    }

    //用于查询是否重命名
    public boolean Is_rename(String username) {
        db = helper.getWritableDatabase();
        Cursor c = db.query("location", null, null, null, null, null, null);//查询并获得游标
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String m_username = c.getString(c.getColumnIndex(username)+1);
            if (username.equals(m_username) == true) {
                rename_flag = true;
            } else
                rename_flag = false;
            c.moveToNext();
        }
        return rename_flag;
    }


    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }



}
