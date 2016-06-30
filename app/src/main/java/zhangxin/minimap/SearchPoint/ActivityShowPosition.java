package zhangxin.minimap.SearchPoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import zhangxin.minimap.R;

public class ActivityShowPosition extends Activity implements OnClickListener{


	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private double longitude;
	private double latitude;
	private String name;
	private String address;

	private BitmapDescriptor mPointer = BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_chatbox);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showposition);
		
		initData();
		initView();
	}
	
	private void initData() {
		Intent intent = getIntent();
		if(intent != null){
			latitude = intent.getDoubleExtra("latitude", 0);
			longitude = intent.getDoubleExtra("longitude", 0);
			name = intent.getStringExtra("name");
			address = intent.getStringExtra("address");
		}
	}

	private void initView() {
		TextView txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText("位置预览");
		txt_title.setOnClickListener(this);
		
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.showZoomControls(false);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);

		// 动画跳转
		LatLng latlng = new LatLng(latitude, longitude);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latlng);
		mBaiduMap.animateMapStatus(u);
		// 添加覆盖物
		addOverlay(latlng, mPointer, 0, 0);
		
		TextView tv_name = (TextView) findViewById(R.id.tv_name);
		tv_name.setText(name);
		TextView tv_address = (TextView) findViewById(R.id.tv_address);
		tv_address.setText(address);
	}
	
	/** 添加覆盖物 */
	private void addOverlay(LatLng la, BitmapDescriptor descriptor, float anchorX, float anchorY){
		MarkerOptions ooA = new MarkerOptions().position(la).icon(descriptor);
		if(anchorX>0 && anchorY>0){
			ooA.anchor(anchorX, anchorY);
		}
		mBaiduMap.addOverlay(ooA);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_title:
			finish();
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

}
