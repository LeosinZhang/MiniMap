package zhangxin.minimap.Location;

import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

/**
 * 此demo用来展示如何进行地理编码搜索（用地址检索坐标）、反地理编码搜索（用坐标检索地址）
 */
public class ActivityGeoCoder implements OnGetGeoCoderResultListener {
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    String strInfo;
    private Handler GeoHandler;

	/**
     * 发起搜索
     *
     * @param
     */
    public void SearchReGeoProcess(double latitude,double longtitude) {
            // 初始化搜索模块，注册事件监听
            mSearch = GeoCoder.newInstance();
            mSearch.setOnGetGeoCodeResultListener(this);
            LatLng ptCenter = new LatLng(latitude, longtitude);
            // 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
    }


    /**
     * 发起搜索
     *
     * @param
     */
    public void SearchGeoProcess(String city,String street) {
            // Geo搜索
            mSearch.geocode(new GeoCodeOption().city(city).address(street));
    }

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            strInfo = "抱歉，未能找到结果";
			return;
		}
		strInfo = String.format("纬度：%f 经度：%f",result.getLocation().latitude, result.getLocation().longitude);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            strInfo = "抱歉，未能找到结果";
			return;
		}
        Message msg = new Message();
        msg.what = 0;
        msg.obj = result.getAddress();
        GeoHandler.sendMessage(msg);
	}

    public Handler getGeoHandler(Handler handler){
        GeoHandler = handler;
        return GeoHandler;
    }


}
