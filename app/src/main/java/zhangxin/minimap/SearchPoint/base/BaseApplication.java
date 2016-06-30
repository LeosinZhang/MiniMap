package zhangxin.minimap.SearchPoint.base;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public class BaseApplication extends Application {

	private static BaseApplication mBaseApplication;
	@Override
	public void onCreate() {
		super.onCreate();
		mBaseApplication = this;
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
	}

	public static BaseApplication getApplication() {
		return mBaseApplication;
	}
}