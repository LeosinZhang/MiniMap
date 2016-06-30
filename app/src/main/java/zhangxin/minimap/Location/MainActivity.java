package zhangxin.minimap.Location;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import zhangxin.minimap.R;
import zhangxin.minimap.SQLite.DataBaseManager;
import zhangxin.minimap.SearchPoint.ActivitySearchPoint;
import zhangxin.minimap.ZomView.ZoomControlsView;

/**
 * Created by Administrator on 2015/12/28.
 */
public class MainActivity extends FragmentActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
    private DataBaseManager mgr;
    private ActivityNavigation Navigation = new ActivityNavigation();
    private Handler handler;
    private ActivityGeoCoder GeoCoder = new ActivityGeoCoder();
    public static List<Activity> activityList = new LinkedList<Activity>();
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MapView mMapView;//百度地图控件
    public BaiduMap mBaiduMap;//地图对象控制器
    public ZoomControlsView zcvZomm;//缩放控件

    // UI相关
    boolean isFirstLoc = true;// 是否首次定位
    boolean isRequest = false;//是否手动定位
    Polyline mTexturePolyline;
    BitmapDescriptor mRedTexture;
    BitmapDescriptor mBlueTexture;
    BitmapDescriptor mGreenTexture;

    //线路轨迹UI相关
    private double localLongitude,localLatitude,startLongitude,startLatitude,stopLongitude,stopLatitude;
    private boolean isFirst = true;
    //导航相关模块
    private double StartLongitude,StartLatitude,StopLongitude,StopLatitude;
    private String StartPlace,StopPlace;
    private String Local_City;


    //搜索相关
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    //搜索关键字输入窗口
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int load_Index = 0;
    private String Local_Location,Local_Describe;
    private float direction = 0;
    ConnectivityManager con;
    boolean internet,wifi,traffic_light = false,mapMode = false;
    //定位用户ID
    private String userID = "000001";
    private Double temp[][] = new Double[10000][2];
    private double locate[][] = new double[10000][2];
    public static LatLng localLocate;

    RelativeLayout ShowPlace;
    LinearLayout SwitchButton;
    ImageButton OnclickLocate;
    TextView showText;
    int[] location = new int[2];
    int[] location1 = new int[2];
    int BtnLocLeft,TextLocButton,BtnLocButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //先去除应用程序标题栏  注意：一定要在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main );
        con = (ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
        wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        //SQL实例化
        mgr = new DataBaseManager(this);
        //画线的画笔颜色
        mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");
        mBlueTexture = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");
        mGreenTexture = BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png");
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        //获取地图对象控制器
        mBaiduMap = mMapView.getMap();
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//普通：NORMAL   罗盘：COMPASS  跟随：FOLLOWING
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));//定位默认图标
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //设置比例
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        option.setNeedDeviceDirect(true);//可选，设置是否需要设备方向结果
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocClient.setLocOption(option);
        mLocClient.start();
        mBaiduMap.setOnMapLongClickListener(longClickListener);
        mBaiduMap.setOnMapClickListener(listener);
        mBaiduMap.setOnMapLoadedCallback(loadedCallback);
        //初始化自定义按钮
        initMap();
        //初始化百度引擎
        Navigation.Navigation(MainActivity.this);
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.ActivityMain_SearchKey);
        sugAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        //长按的相关控件
        ShowPlace = (RelativeLayout)findViewById(R.id.ShowPlace);
        SwitchButton = (LinearLayout)findViewById(R.id.SwitchButton);
        OnclickLocate = (ImageButton)findViewById(R.id.ActivityMain_locate);
        showText = (TextView)findViewById(R.id.AddressText);
        //获取控件大小
        getLoc();

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                 if(wifi|internet) {
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city(Local_City));
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "网络未连接", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void  getLoc(){
        OnclickLocate.getLocationInWindow(location);
        BtnLocLeft = location[0]+OnclickLocate.getMeasuredWidth();
        BtnLocButton = location[1]-OnclickLocate.getMeasuredHeight()/2;
        ShowPlace.getLocationInWindow(location1);
        TextLocButton = location1[1]-ShowPlace.getMeasuredHeight()/2-OnclickLocate.getMeasuredHeight()/2;
    }


    /**
     * 初始化地图
     */
    private void initMap(){

        // 隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }

        //地图上比例尺
         //mMapView.showScaleControl(false);
        //地图上比例尺
         mMapView.showZoomControls(false);

        //获取缩放控件
        zcvZomm=(ZoomControlsView) findViewById(R.id.ZoomControlView);
        zcvZomm.setMapView(mMapView);//设置百度地图控件

    }

    BaiduMap.OnMapLoadedCallback loadedCallback = new BaiduMap.OnMapLoadedCallback(){
        @Override
        public void onMapLoaded() {
            getLoc();
            mMapView.setScaleControlPosition(new Point(BtnLocLeft, BtnLocButton));
        }
    };
/*####################################################################################################################################*/
    BaiduMap.OnMapLongClickListener longClickListener = new BaiduMap.OnMapLongClickListener() {
        /**
         * 地图长按事件监听回调函数
         * @param point 长按的地理坐标
         */
        public void onMapLongClick(LatLng point){
            ShowText();
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

            //等待计算的位置
            handler = new Handler(){
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what){
                        case 0:
                            String str =  msg.obj.toString();
                            showText.setText(str);
                            StopPlace = str;
                            break;
                        default:
                            break;
                    }
                }
            };
            GeoCoder.getGeoHandler(handler);
            GeoCoder.SearchReGeoProcess(point.latitude,point.longitude);

            StopLatitude = point.latitude;
            StopLongitude = point.longitude;
        }
    };

    /*####################################################################################################################################*/
    BaiduMap.OnMapClickListener listener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         * @param point 点击的地理坐标
         */
        public void onMapClick(LatLng point){
            mMapView.getMap().clear();
            ShowPlace.setVisibility(View.GONE);
            SwitchButton.setVisibility(View.VISIBLE);
            ShowPlace.setId(R.id.ShowPlace);
            SwitchButton.setId(R.id.SwitchButton);
            mMapView.setScaleControlPosition(new Point(BtnLocLeft, BtnLocButton));
        }
        /**
         * 地图内 Poi 单击事件回调函数
         * @param poi 点击的 poi 信息
         */
        public boolean onMapPoiClick(MapPoi poi){
            LatLng point = poi.getPosition();
            String Address = poi.getName();
            ShowText();
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
            showText.setText(Address);
            StopLatitude = point.latitude;
            StopLongitude = point.longitude;
            StopPlace = Address;
            return true;
        }
    };

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }

            new Thread() { // 开启线程执行防止阻塞
                @Override
                public void run() {
                    Local_Location = location.getAddrStr();
                    Local_City = location.getCity();
                    direction = location.getDirection();
                    Local_Describe = location.getLocationDescribe();
                    localLongitude = location.getLongitude();//经度
                    localLatitude = location.getLatitude();//纬度
                    //记录当前位置，写入数据库
                    localLocate = new LatLng(localLatitude,localLongitude);
                    StartLatitude = localLatitude;
                    StartLongitude = localLongitude;
                    StartPlace = Local_Describe;


                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    final String sys_time = formatter.format(curDate);
                    mgr.add(userID, localLatitude, localLongitude, sys_time);

                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(location.getRadius())
                                    // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(direction).latitude(location.getLatitude())
                            .longitude(location.getLongitude()).build();
                    mBaiduMap.setMyLocationData(locData);
                    if (isFirstLoc || isRequest) {
                        isFirstLoc = false;
                        isRequest = false;
                        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                        mBaiduMap.animateMapStatus(u);
                    }
                }
            }.start();
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        if(mLocClient != null) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        //关闭poi搜索
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 影响搜索按钮点击事件
     *
     * @param v
     */
    public void OnClick(View v) {
     //   EditText editCity = (EditText) findViewById(R.id.city);
        EditText editSearchKey = (EditText) findViewById(R.id.ActivityMain_SearchKey);
        switch (v.getId()) {
            case R.id.ActivityMain_SearchButton:
            {
                if (wifi | internet) {
                    mPoiSearch.searchInCity((new PoiCitySearchOption())
                            .city(Local_City)  //Local_City
                            .keyword(editSearchKey.getText().toString())
                            .pageNum(load_Index));
                } else {
                    Toast.makeText(getApplicationContext(),
                            "网络未连接", Toast.LENGTH_LONG)
                            .show();
                }
            }
            break;

            case R.id.ActivityMain_locate:
            {
                //手动定位
                //需要再Manifest里加入service段代码
                if(mLocClient != null && mLocClient.isStarted()) {
                    isRequest = true;
                    mLocClient.requestLocation();

                }
                ShowText();
                if(Local_Location!= null && Local_Describe!= null)
                    showText.setText("我的位置在："+ Local_Location+Local_Describe);
                else if (Local_Location== null && Local_Describe!= null)
                        showText.setText("我的位置在："+ Local_Describe);
                else if (Local_Location!= null && Local_Describe== null)
                    showText.setText("我的位置在："+ Local_Location);
                else
                    showText.setText("定位失败，请检查网络！");
            }
            break;

            case R.id.ActivityMain_Clear:
            {
                // 清除所有图层
                mMapView.getMap().clear();
            }
            break;

            case R.id.ActivityMain_RoadLight:
            {
                traffic_light = !traffic_light;
                ImageButton lightButton = (ImageButton)findViewById(R.id.ActivityMain_RoadLight);
                if(traffic_light){
                    lightButton.setBackgroundResource(R.mipmap.road_on);
                    mBaiduMap.setTrafficEnabled(true);
                }else{
                    lightButton.setBackgroundResource(R.mipmap.road_off);
                    mBaiduMap.setTrafficEnabled(false);
                }
            }
            break;

            case R.id.ActivityMain_MapMode:
            {
                mapMode = !mapMode;
                ImageButton ModeButton = (ImageButton)findViewById(R.id.ActivityMain_MapMode);
                if(mapMode){
                    ModeButton.setBackgroundResource(R.mipmap.standard_map);
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }else {
                    ModeButton.setBackgroundResource(R.mipmap.satellite_map);
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
            }
            break;

            case R.id.ActivityMain_offline:
            {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityLoadMap.class);
                startActivity(intent);
            }
            break;

            case R.id.a2:
            {
                new Thread() { // 开启线程执行防止阻塞
                    @Override
                    public void run() {
                        isFirst = true;
                        temp = mgr.query(userID);
                        int k = 0;
                        for (int i = 0; i < temp.length; i++) {
                            if (temp[i][0] != null) {
                                locate[i][0] = temp[i][0];
                                locate[i][1] = temp[i][1];
                                k++;
                            }
                        }
                        for (int i = 0; i < k; i++) {
                            drawPath(locate[i][0], locate[i][1]);
                        }
                    }
                }.start();
            }
            break;

            case R.id.a3:
            {
                mgr.delete(userID);
            }
            break;

            case R.id.a4:
            {
                Intent intent = new Intent(getApplicationContext(), ActivitySearchPoint.class);
                startActivity(intent);
            }
            break;

            case R.id.navigation:
            {
               // Navigation.setLoc(StartLatitude,StartLongitude,StopLatitude,StopLongitude,StartPlace,StopPlace);
                if (BaiduNaviManager.isNaviInited()) {
                    routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
                }
            }
            break;

            case R.id.go_to_place:
            {
                Intent intent = new Intent(MainActivity.this, ActivityRoutePlan.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("StartLatitude",StartLatitude);
                bundle.putDouble("StartLongitude",StartLongitude);
                bundle.putDouble("StopLatitude",StopLatitude);
                bundle.putDouble("StopLongitude",StopLongitude);
                bundle.putString("StartPlace", StartPlace);
                bundle.putString("StopPlace", StopPlace);
                bundle.putString("City",Local_City);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            break;

        }
    }

    private void RoutePlan(){


    }

    public void goToNextPage(View v) {
        load_Index++;
        OnClick(null);
    }

    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            ShowText();
            showText.setText("抱歉，未找到结果");
        } else {
            ShowText();
            showText.setText( result.getName() + ": " + result.getAddress());
            LatLng point = result.getLocation();
            StopLatitude = point.latitude;
            StopLongitude = point.longitude;
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(result.getLocation()).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        sugAdapter.clear();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null)
                sugAdapter.add(info.key);
        }
        sugAdapter.notifyDataSetChanged();
    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            // if (poi.hasCaterDetails) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            // }
            return true;
        }
    }

    //###################↓↓↓↓↓↓↓↓↓↓   路径规划  路径规划 路径规划  ↓↓↓↓↓↓↓↓↓↓#####################
    private void drawPath(Double latitude,Double longitude){
        if(isFirst){
        isFirst = false;
        startLatitude = latitude;
        startLongitude = longitude;
         }else {
            stopLatitude = latitude;
            stopLongitude = longitude;
            // 添加多纹理分段的折线绘制
            LatLng p111 = new LatLng(startLatitude,startLongitude);
            LatLng p211 = new LatLng(stopLatitude, stopLongitude);
            List<LatLng> points11 = new ArrayList<LatLng>();
            points11.add(p111);
            points11.add(p211);
            List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
            textureList.add(mBlueTexture);
            List<Integer> textureIndexs = new ArrayList<Integer>();
            textureIndexs.add(0);
            textureIndexs.add(1);
            textureIndexs.add(2);
            OverlayOptions ooPolyline11 = new PolylineOptions(). width(7).points(points11).dottedLine(true).customTextureList(textureList).textureIndex(textureIndexs);
            mTexturePolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline11);
            startLatitude = stopLatitude;
            startLongitude = stopLongitude;
        }
    }
    //#####################↑↑↑↑↑↑↑↑↑↑  描绘轨迹  描绘轨迹 描绘轨迹 ↑↑↑↑↑↑↑↑↑↑ ##################

    private void ShowText(){
        mMapView.getMap().clear();
        ShowPlace.setVisibility(View.VISIBLE);
        SwitchButton.setVisibility(View.GONE);
        ShowPlace.setId(R.id.SwitchButton);
        SwitchButton.setId(R.id.ShowPlace);
        mMapView.setScaleControlPosition(new Point(BtnLocLeft, TextLocButton));
    }


    //###################↓↓↓↓↓↓↓↓↓↓   描绘轨迹  描绘轨迹  描绘轨迹  ↓↓↓↓↓↓↓↓↓↓#####################
    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;

        sNode = new BNRoutePlanNode(StartLongitude,StartLatitude , StartPlace, null, coType);
        eNode = new BNRoutePlanNode(StopLongitude,StopLatitude , StopPlace, null, coType);

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }


    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */
            for (Activity ac : activityList) {
                if (ac.getClass().getName().endsWith("ActivityNaviGuide")) {
                    return;
                }
            }
            Intent intent = new Intent(MainActivity.this, ActivityNaviGuide.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(VOID_MODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

        @Override
        public void stopTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "stopTTS");
        }

        @Override
        public void resumeTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "resumeTTS");
        }

        @Override
        public void releaseTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "releaseTTSPlayer");
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

            return 1;
        }

        @Override
        public void phoneHangUp() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneHangUp");
        }

        @Override
        public void phoneCalling() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneCalling");
        }

        @Override
        public void pauseTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "pauseTTS");
        }

        @Override
        public void initTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "initTTSPlayer");
        }

        @Override
        public int getTTSState() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "getTTSState");
            return 1;
        }
    };

    //#####################↑↑↑↑↑↑↑↑↑↑  路径规划  路径规划 路径规划 ↑↑↑↑↑↑↑↑↑↑ ##################



}
