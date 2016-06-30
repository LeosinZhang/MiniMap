package zhangxin.minimap.Location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import zhangxin.minimap.R;

public class ActivityNavigation extends Activity {
	public static List<Activity> activityList = new LinkedList<Activity>();
    //public static List<Context> activityList = new LinkedList<Context>();
	private static final String APP_FOLDER_NAME = "miniMap";
	private String mSDCardPath = null;
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

    private double StartLongitude,StartLatitude,StopLongitude,StopLatitude;
    private String StartPlace,StopPlace;
    private Activity ActivityIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
	}

    public void Navigation(Activity activity){
        activityList.add(activity);
        if (initDirs()) {
            initNavi(activity);
        }
        ActivityIn = activity;
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

    public void initListener() {
        if (BaiduNaviManager.isNaviInited()) {
            routeplanToNavi(CoordinateType.BD09LL);
        }
    }


	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	String authinfo = null;

    private void initNavi(final Activity activity) {
        BNOuterTTSPlayerCallback ttsCallback = null;
        BaiduNaviManager.getInstance().init(activity, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
                    Log.d("KEY","SUCCESS");
				} else {
					authinfo = "key校验失败, " + msg;
                    Log.d("KEY","FAILED");

				}
                activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
					//	Toast.makeText(activity, authinfo, Toast.LENGTH_LONG).show();
					}
				});
            }

            public void initSuccess() {
            //    Toast.makeText(activity, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
            }

            public void initStart() {
            //    Toast.makeText(activity, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
            //    Toast.makeText(activity, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        },  null);
    }

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private void routeplanToNavi(CoordinateType coType) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;

        sNode = new BNRoutePlanNode(StartLatitude, StartLongitude, StartPlace, null, coType);
        eNode = new BNRoutePlanNode(StopLatitude, StopLongitude, StopPlace, null, coType);


			if (sNode != null && eNode != null) {
				List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
				list.add(sNode);
				list.add(eNode);
				BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
			}
	}

	public class DemoRoutePlanListener implements RoutePlanListener {

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
            Intent intent = new Intent(ActivityIn, ActivityNaviGuide.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(ActivityNavigation.this, "算路失败", Toast.LENGTH_SHORT).show();
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

}
