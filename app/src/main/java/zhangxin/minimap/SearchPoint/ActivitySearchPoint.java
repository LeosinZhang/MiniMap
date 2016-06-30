package zhangxin.minimap.SearchPoint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.platform.comapi.location.CoordinateType;

import java.util.ArrayList;
import java.util.List;

import zhangxin.minimap.R;
import zhangxin.minimap.SearchPoint.base.ViewHolder;
import zhangxin.minimap.SearchPoint.base.WBaseAdapter;
import zhangxin.minimap.SearchPoint.utils.CameraUtils;
import zhangxin.minimap.SearchPoint.utils.FileUtils;

public class ActivitySearchPoint extends Activity implements OnItemClickListener, OnClickListener{

	/**
	 * 构造广播监听类，监听 SDK key 验证以及网络异常广播
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				Toast.makeText(getApplicationContext(), "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置", Toast.LENGTH_SHORT).show();
			} else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
				Toast.makeText(getApplicationContext(), "key 验证成功! 功能可以正常使用", Toast.LENGTH_SHORT).show();
			}
			else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				Toast.makeText(getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private SDKReceiver mReceiver;
	
	
	private MapView mMapView;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private BaiduMap mBaiduMap;
	/**POI信息列表  */
	private ArrayList<PoiInfo> mInfoList;
	/**当前物理坐标*/
	private Point mCenterPoint;
	/**当前地理坐标*/
	private LatLng mLoactionLatLng;
	/**当前选中地理坐标*/
	private LatLng mCurrentSelected;
	/**是否第一次定位  */
	public boolean isFirstLoc = true;
	/**地理编码*/
	private GeoCoder mGeoCoder;
	private PlaceListAdapter mAdapter;
	private LocationClient mLocationClient;
	private ImageView mImageViewPointer;
	private String mMapSavePath;
	// 初始化全局 bitmap 信息，不用时及时 recycle
	private BitmapDescriptor mSelectIco = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);
	private BitmapDescriptor mPointer = BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_chatbox);
	private BitmapDescriptor mCurrentMarker;
	private String mAddress;
	private String mName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchpoint);
		
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		initView();
		initSettingMapView();
	}

	private void initView() {
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText("位置");
		txt_title.setOnClickListener(this);
		TextView txt_right = (TextView) findViewById(R.id.txt_right);
		txt_right.setVisibility(View.VISIBLE);
		txt_right.setText("发送");
		txt_right.setOnClickListener(this);
		
		mMapView = (MapView) findViewById(R.id.mapview);
		mListView = (ListView) findViewById(R.id.lv_content);
		mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
	}

	private void initSettingMapView() {
		// 初始化地图  
		mMapView.showZoomControls(false);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyBDLocationListner());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps  
        option.setCoorType(CoordinateType.BD09LL); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
		
        //初始化缩放比例
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);
		mBaiduMap.setOnMapTouchListener(new MyMapTouchListener());
		
		// 初始化POI信息列表  
        mInfoList = new ArrayList<PoiInfo>();
  
        // 初始化当前MapView中心屏幕坐标(物理坐标)
        mMapView.post(new Runnable() {
			
			@Override
			public void run() {
				mCenterPoint = mBaiduMap.getMapStatus().targetScreen;  
		        int x = mCenterPoint.x;
		        int y = mCenterPoint.y;
		        
		        mImageViewPointer = new ImageView(getApplicationContext());
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_location_chatbox);
				mImageViewPointer.setImageBitmap(bitmap);
				mImageViewPointer.setX(x - bitmap.getWidth() / 2);
				mImageViewPointer.setY(y - bitmap.getHeight());
		        ViewGroup parent = (ViewGroup) mMapView.getParent();
		        parent.addView(mImageViewPointer,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT));
			}
		});
        //初始化当前地理坐标  
        mLoactionLatLng = mBaiduMap.getMapStatus().target;
        
        // 地理编码  
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new MyGetGeoCoderResultListener());
        
        //// 周边位置列表  
        mAdapter = new PlaceListAdapter(getApplicationContext(), mInfoList, R.layout.listitem_place);
        mListView.setAdapter(mAdapter); 
        mListView.setOnItemClickListener(this);
	}
	
	private class PlaceListAdapter extends WBaseAdapter<PoiInfo> {

		private int notifyTip;
		public PlaceListAdapter(Context context, List<PoiInfo> data, int layoutId) {
			super(context, data, layoutId);
			notifyTip = -1;
		}

		public void setNotifyTip(int position) {
			notifyTip = position;
			notifyDataSetChanged();
		}

		@Override
		public void convert(ViewHolder holder, PoiInfo t, int position) {
			holder.setText(R.id.place_name, t.name);
			holder.setText(R.id.place_adress, t.address);
			// 根据重新加载的时候第position条item是否是当前所选择的，选择加载不同的图片
			ImageView imageView = holder.getView(R.id.place_select);
			if (notifyTip == position) {
				imageView.setImageResource(R.mipmap.ic_contact_list_selected);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}
	
	// 定位监听器  
    private class MyBDLocationListner implements BDLocationListener {
  
        @Override  
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置  
            if (location == null || mMapView == null){
            	return;
            }
            MyLocationData data = new MyLocationData.Builder()//
                    // .direction(mCurrentX)//  
                    .accuracy(0)//location.getRadius()
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())//  
                    .longitude(location.getLongitude())//  
                    .build();  
            mBaiduMap.setMyLocationData(data);
  
            double mLantitude = location.getLatitude();  
            double mLongtitude = location.getLongitude();  
            mLoactionLatLng = new LatLng(mLantitude, mLongtitude);
  
            // 是否第一次定位  
            if (isFirstLoc) {  
                isFirstLoc = false;  
                // 实现动画跳转 
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLoactionLatLng);
                mBaiduMap.animateMapStatus(u);  
                mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(mLoactionLatLng));
            }
        }  
    }
    // 地图触摸事件监听器   
    private class MyMapTouchListener implements OnMapTouchListener {
		
		@Override
		public void onTouch(MotionEvent event) {
			// TODO BaiduMap onTouch
			if (event.getAction() == MotionEvent.ACTION_UP) {  
	            if (mCenterPoint == null) {  
	                return;  
	            }  
	            // 获取当前MapView中心屏幕坐标对应的地理坐标
	            LatLng currentLatLng = mBaiduMap.getProjection().fromScreenLocation(mCenterPoint);
	            //发起反地理编码检索
	            mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(currentLatLng));
	            mProgressBar.setVisibility(View.VISIBLE);  
	        }  
		}
	};
	// 地理编码监听器  
	private class MyGetGeoCoderResultListener implements OnGetGeoCoderResultListener {
		
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			//TODO 地理编码
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
	            // 没有找到检索结果  
	        }  
	        // 获取反向地理编码结果  
	        else {
	            // 当前位置信息  
	        	PoiInfo mCurentInfo = new PoiInfo();
	            mCurentInfo.address = result.getAddress();  
	            mCurentInfo.location = result.getLocation();  
	            mCurentInfo.name = "[位置]";
	            mInfoList.clear();  
	            mInfoList.add(mCurentInfo);  

	            // 将周边信息加入表  
	            if (result.getPoiList() != null) {  
	                mInfoList.addAll(result.getPoiList());  
	            }  
	            
	            // 通知适配数据已改变  
	            mAdapter.setNotifyTip(0);
	            mListView.setSelection(0);
	            mBaiduMap.clear();
	            mProgressBar.setVisibility(View.GONE);
	            runShakeAnimation(mImageViewPointer);
	            
	            //初始选中信息
	            mCurrentSelected = result.getLocation();
	            mAddress = result.getAddress();
	        }  
		}
		
		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			//TODO 地理编码
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
	            // 没有检索到结果  
	        }
			// 获取地理编码结果  
		}
	};
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Item点击事件

		// 通知是适配器第position个item被选择了
		mAdapter.setNotifyTip(position);

		mBaiduMap.clear();
		PoiInfo info = mAdapter.getItem(position);
		mCurrentSelected = info.location;
		mName = info.name;
		mAddress = info.address;

		// 动画跳转
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mCurrentSelected);
		mBaiduMap.animateMapStatus(u);
		runShakeAnimation(mImageViewPointer);
		
		// 添加覆盖物
		addOverlay(mCurrentSelected, mSelectIco, 0.5f, 0.5f);
		

//		Uri mUri = Uri.parse("geo:"+la.latitude+","+la.longitude);
//		Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
//		startActivity(mIntent);
	}
	
	/** 添加覆盖物 */
	private void addOverlay(LatLng la, BitmapDescriptor descriptor, float anchorX, float anchorY){
		MarkerOptions ooA = new MarkerOptions().position(la).icon(descriptor);
		if(anchorX>0 && anchorY>0){
			ooA.anchor(anchorX, anchorY);
		}
		mBaiduMap.addOverlay(ooA);
	}

	/** 截图当前地图 */
	private void screenshotsMap(){
		chageIcon(R.mipmap.icon_null); //隐藏当前位置
		mBaiduMap.clear(); //清空绘制
		addOverlay(mCurrentSelected, mPointer, 0, 0); //绘制指针
		mMapView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBaiduMap.snapshot(new SnapshotReadyCallback() {

					public void onSnapshotReady(Bitmap snapshot) {
						mMapSavePath = FileUtils.createTmpFile().getAbsolutePath();
						CameraUtils.saveBitmap2Path(mMapSavePath, snapshot);
						
						LatLng latLng = mCurrentSelected; //经纬度信息
						double latitude = latLng.latitude;
						double longitude = latLng.longitude;
						
						String path = mMapSavePath; //图片路径
						String name = mName; //标题
						String address = mAddress; //地址详情

						// 打开坐标位置
						Intent intent = new Intent(getApplicationContext(), ActivityShowPosition.class);
						intent.putExtra("latitude", latitude);
						intent.putExtra("longitude", longitude);
						intent.putExtra("name", name);
						intent.putExtra("address", address);
						startActivity(intent);
						//finish();
					}
				});
			}
		}, 200);
	}
	
	public void runShakeAnimation(View view) {
		TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 10);
		anim.setDuration(500);
		anim.setInterpolator(new CycleInterpolator(1));
		view.startAnimation(anim);
	}
	
	private void chageIcon(int resId){
		if (mBaiduMap != null) {
			mCurrentMarker = null;
			if (resId > 0) {
				mCurrentMarker = BitmapDescriptorFactory.fromResource(resId);
			}
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					LocationMode.NORMAL, true, mCurrentMarker));
		}
	}

	@Override
	protected void onDestroy() {
		mLocationClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		mGeoCoder.destroy();
		
		mCurrentMarker = null;
		mSelectIco = null;
		mPointer = null;
		
		// 取消监听 SDK 广播
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_title:
			finish();
			break;
		case R.id.txt_right:
			screenshotsMap();
			break;
		}
	}
}
