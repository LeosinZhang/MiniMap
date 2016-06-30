package zhangxin.minimap.Trajectory;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import zhangxin.minimap.Location.MainActivity;

/**
 * Created by Administrator on 2016/1/11.
 */
public class DrawPath {
    private MainActivity mainActivity = new MainActivity();
    private boolean isFirst = true;
    private double startLongitude,startLatitude,stopLongitude,stopLatitude;
    BitmapDescriptor mRedTexture;
    BitmapDescriptor mBlueTexture;
    BitmapDescriptor mGreenTexture;
    Polyline mTexturePolyline;

    public void drawPath(double latitude, double longitude){
        if(isFirst){
            isFirst = false;
            mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");
            mBlueTexture = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");
            mGreenTexture = BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png");
        }
        startLatitude = latitude;
        startLongitude = longitude;
        // 添加多纹理分段的折线绘制
        LatLng p111 = new LatLng(startLatitude,startLongitude);
        LatLng p211 = new LatLng(stopLatitude+0.05,stopLongitude+0.05);
      //  LatLng p211 = new LatLng(stopLatitude, stopLongitude);
        List<LatLng> points11 = new ArrayList<LatLng>();
        points11.add(p111);
        points11.add(p211);
        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        textureList.add(mBlueTexture);
        List<Integer> textureIndexs = new ArrayList<Integer>();
        textureIndexs.add(0);
        textureIndexs.add(1);
        textureIndexs.add(2);
        OverlayOptions ooPolyline11 = new PolylineOptions().width(7)  //6  22
                .points(points11).dottedLine(true).customTextureList(textureList).textureIndex(textureIndexs);
        mTexturePolyline = (Polyline) mainActivity.mBaiduMap.addOverlay(ooPolyline11);

        startLatitude = stopLatitude;
        startLongitude = stopLongitude;
    }
}
