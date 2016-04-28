package com.example.fatuze.fanran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.widget.Button;
import android.util.Log;
import android.view.LayoutInflater;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
//import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;

public class MainActivity extends Activity implements OnClickListener {
    private static final String LOG_TAG = "MainActivity";
    // Site arrays
    private static Site SITES_ARRAY[] = {
            new Site(39.9970982784,116.2859123708, 0, R.string.site_tongniu),             //铜牛
            new Site(40.0048724752,116.2801408455, 1, R.string.site_paiyundian_qfxr),     //排云殿骑凤仙人
            new Site(40.0027696952,116.2864436155, 2, R.string.site_wenchangge),          //文昌阁
    };


	private MapView mapView=null;
	private BaiduMap myBaiduMap=null;
	//修改默认View相关
	private View defaultBaiduMapScaleButton,defaultBaiduMapLogo,defaultBaiduMapScaleUnit;
	//定位相关
	private LocationClient myLocationClient = null;
	private BDLocationListener myListener = null;
	private double latitude,longtitude;//经纬度
	private BitmapDescriptor myBitmapLocation;//定位的自定义图标
	private boolean isFirstIn=true;//设置一个标记，查看是否是第一次

	private float current = 0;//放大或缩小的比例系数
	private ImageView expandMap;//放大地图控件
	private ImageView narrowMap;//缩小地图
	private ImageView myLocation;
	private String locationTextString;//定义的位置的信息
	private TextView locationText;//显示定位信息的TextView控件
	private ImageButton startReportButton;
	private Button mButtonLocalResult;
	private Site mSelectedSite;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		mapView=(MapView) findViewById(R.id.map_view_test);
		initMapView();
		changeDefaultBaiduMapView();
		initMapLocation();
		initMapMarkers();
	}

	private void changeDefaultBaiduMapView() {
		changeInitializeScaleView();//改变默认百度地图初始加载的地图比例
		//设置隐藏缩放和扩大的百度地图的默认的比例按钮
		for (int i = 0; i < mapView.getChildCount(); i++) {//遍历百度地图中的所有子View,找到这个扩大和缩放的按钮控件View，然后设置隐藏View即可
			View child = mapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				defaultBaiduMapScaleButton=child;//该defaultBaiduMapScaleButton子View是指百度地图默认产生的放大和缩小的按钮，得到这个View
				break;
			}
		}
		defaultBaiduMapScaleButton.setVisibility(View.GONE);//然后将该View的Visiblity设为不存在和不可见，即隐藏
		defaultBaiduMapLogo = mapView.getChildAt(1);//该View是指百度地图中默认的百度地图的Logo,得到这个View
		defaultBaiduMapLogo.setPadding(300, -10, 100, 100);//设置该默认Logo View的位置，因为这个该View的位置会影响下面的刻度尺单位View显示的位置
		mapView.removeViewAt(1);//最后移除默认百度地图的logo View
		defaultBaiduMapScaleUnit=mapView.getChildAt(2);//得到百度地图的默认单位刻度的View
		defaultBaiduMapScaleUnit.setPadding(100, 0, 115, 200);//最后设置调整百度地图的默认单位刻度View的位置
	}
	private void changeInitializeScaleView() {
		myBaiduMap=mapView.getMap();//改变百度地图的放大比例,让首次加载地图就开始扩大到500米的距离
		MapStatusUpdate factory=MapStatusUpdateFactory.zoomTo(15.0f);
		myBaiduMap.animateMapStatus(factory);		
	}

	/**
	 * @author Mikyou
	 * 初始化定位功能
	 * */
	private void initMapLocation() {
		myLocationClient=new LocationClient(this);//创建一个定位客户端对象
		myListener=new MyLocationListener();//创建一个定位事件监听对象
		myLocationClient.registerLocationListener(myListener);//并给该定位客户端对象注册监听事件
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
		int span=1000;
		option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
		option.setOpenGps(true);//可选，默认false,设置是否使用gps
		option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		//option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
		option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		myLocationClient.setLocOption(option);
	}

	/**
	 * @author zhongqihong
	 * 初始化地图的View
	 * */
	private void initMapView() {
		registerAllIds();
		registerAllEvents();
	}
	private void registerAllIds() {
		expandMap=(ImageView) findViewById(R.id.add_scale);
		narrowMap=(ImageView) findViewById(R.id.low_scale);
		locationText=(TextView) findViewById(R.id.mylocation_text);
		myLocation=(ImageView) findViewById(R.id.my_location);
        startReportButton=(ImageButton) findViewById(R.id.start_go);
		mButtonLocalResult = (Button) findViewById(R.id.button_local_result);
	}
	private void registerAllEvents() {
		expandMap.setOnClickListener(this);
		narrowMap.setOnClickListener(this);
		myLocation.setOnClickListener(this);
        startReportButton.setOnClickListener(this);
		updateButtonResult();
	}

	private void initMapMarkers() {
        //准备 marker 的图片
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.mark);
        for(Site site : SITES_ARRAY) {
            LatLng point = new LatLng(site.GetLatitude(), site.GetLongitude());
            //准备 marker option 添加 marker 使用
            OverlayOptions option = new MarkerOptions().icon(bitmap).position(point);
            //获取添加的 marker 这样便于后续的操作
            Marker marker = (Marker) myBaiduMap.addOverlay(option);
            site.SetMarker(marker);
        }
        myBaiduMap.setOnMarkerClickListener(markerClickListener);
	}

    private void updateButtonResult() {
		SharedPreferences resultPreferences = getSharedPreferences("report_status", Activity.MODE_PRIVATE);
		int save_count = resultPreferences.getInt("saved_count", 0);
		Log.d(LOG_TAG, "updateButtonResult: save_count = " + save_count);
		if(save_count > 0) {
			mButtonLocalResult.setVisibility(View.VISIBLE);
			mButtonLocalResult.setOnClickListener(this);
		} else {
			mButtonLocalResult.setVisibility(View.GONE);
			mButtonLocalResult.setOnClickListener(null);
		}
	}


   // baiduMap.setOnMarkerClickListener(new OnMarkerClickListener()
    private final BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
       @Override
       public boolean onMarkerClick(Marker arg0) {
           boolean found = false;
           Site matchSite = null;
           // 先隐藏Marker信息
           myBaiduMap.hideInfoWindow();
           for (Site site : SITES_ARRAY) {
               if(site.GetMarker().equals(arg0)) {
                   found = true;
                   matchSite = site;
                   break;
               }
           }
           if(matchSite == null) {
               Log.w(LOG_TAG, "No match marker found!");
               return true;
           }

		   mSelectedSite = matchSite;

            /*--------弹出窗覆盖物---------*/
           //(1)创建InfoWindow展示的view
/*
           Button button = new Button(getApplicationContext());
           button.setText(getResources().getString(matchSite.GetDescription()));
*/
           //(2)定义用于显示该InfoWindow的坐标
           LatLng pt = new LatLng(matchSite.GetLatitude(), matchSite.GetLongitude());
           LatLng myPosition = new LatLng(latitude, longtitude);
           int distance = (int)DistanceUtil.getDistance(pt, myPosition);
		   LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		   View infoView = inflater.inflate(R.layout.marker_pop_info, null);
		   TextView siteView = (TextView)infoView.findViewById(R.id.SiteNameTextView);
           siteView.setText(getResources().getString(matchSite.GetDescription()));
           TextView distanceView = (TextView)infoView.findViewById(R.id.SiteDistanceTextView);
           String sDistanceFormat = getResources().getString(R.string.text_site_distance_meter);
           String distanceString = String.format(sDistanceFormat, distance);
           distanceView.setText(distanceString);
		   Button button_report = (Button)infoView.findViewById(R.id.start_report_button);
		   button_report.setOnClickListener(MainActivity.this);
		   //button_report.

           //(3)创建InfoWindow,传入VIew，地理坐标，y轴偏移量
           InfoWindow mInfoWindow = new InfoWindow(infoView, pt, -47);
           //(4)显示InfoWindow
           myBaiduMap.showInfoWindow(mInfoWindow);
           return true;
       }
    };



    //点击事件相关
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_scale://放大地图比例
				expandMapScale();
				break;
			case R.id.low_scale://缩小地图比例
				narrowMapScale();
				break;
			case R.id.my_location://定位功能，需要用到LocationClient进行定位
				//BDLocationListener
				getMyLatestLocation(latitude, longtitude);
				break;
            case R.id.start_go: //上报功能
                goToReportActivity(mSelectedSite);
				break;
			case R.id.start_report_button: //上报功能
				Log.d(LOG_TAG, "start_report_button is clicked!");
				goToReportActivity(mSelectedSite);
				break;
			case R.id.button_local_result: //本地暂存巡查结果
				Log.d(LOG_TAG, "button_local_result is clicked!");
				break;
			default:
				break;
		}
	}

	/**
	 * @author zhongqihong
	 * 放大地图的比例
	 * */
	private void narrowMapScale() {
		current-=0.5f;
		MapStatusUpdate msu=MapStatusUpdateFactory.zoomTo(15.0f+current);
		myBaiduMap.animateMapStatus(msu);
	}
	/**
	 *@author zhongqihong
	 *缩小地图的比例
	 * */
	private void expandMapScale() {
		current+=0.5f;
		MapStatusUpdate msu2=MapStatusUpdateFactory.zoomTo(15.0f+current);
		myBaiduMap.animateMapStatus(msu2);
	}
    /**
     *@author fanran
     *跳转上报界面
     * */
    private void goToReportActivity(Site site) {
        Intent intent = new Intent();
		String siteName = getResources().getString(site.GetDescription());
		Log.d(LOG_TAG, "goToReportActivity: siteName = " + siteName);
		Bundle b = new Bundle();
		b.putString("SiteName", siteName);
		intent.putExtras(b);
		intent.setClass(MainActivity.this, ReportActivity.class);
        startActivity(intent);
    }
	/**
	 * @author zhongqihong
	 * 获取位置信息的客户端对象的监听器类MyLocationListener
	 * */
	class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			//得到一个MyLocationData对象，需要将BDLocation对象转换成MyLocationData对象
			MyLocationData data=new MyLocationData.Builder()
					.accuracy(location.getRadius())//精度半径
					.latitude(location.getLatitude())//经度
					.longitude(location.getLongitude())//纬度
					.build();
			myBaiduMap.setMyLocationData(data);
			//配置自定义的定位图标,需要在紧接着setMyLocationData后面设置
			//调用自定义定位图标
			//changeLocationIcon();
			latitude=location.getLatitude();//得到当前的经度
			longtitude=location.getLongitude();//得到当前的纬度
			//toast("经度："+latitude+"     纬度:"+longtitude);
			if (isFirstIn) {//表示用户第一次打开，就定位到用户当前位置，即此时只要将地图的中心点设置为用户此时的位置即可
				getMyLatestLocation(latitude,longtitude);//获得最新定位的位置,并且地图的中心点设置为我的位置
				isFirstIn=false;//表示第一次才会去定位到中心点
				locationTextString=""+location.getAddrStr();//这里得到地址必须需要在设置LocationOption的时候需要设置isNeedAddress为true;
				toast(locationTextString);
				locationText.setText(locationTextString);
			}
		}

	}

	/**
	 * @author zhongqihong
	 * 获得最新定位的位置,并且地图的中心点设置为我的位置
	 * */
	private void getMyLatestLocation(double lat,double lng) {
		LatLng latLng=new LatLng(lat, lng);//创建一个经纬度对象，需要传入当前的经度和纬度两个整型值参数
		MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);//创建一个地图最新更新的状态对象，需要传入一个最新经纬度对象
		myBaiduMap.animateMapStatus(msu);//表示使用动画的效果传入，通过传入一个地图更新状态对象，然后利用百度地图对象来展现和还原那个地图更新状态，即此时的地图显示就为你现在的位置
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 创建退出对话框
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// 设置对话框标题
			//isExit.setTitle("系统提示");
			// 设置对话框消息
			isExit.setMessage(getText(R.string.ask_if_exit));
			// 添加选择按钮并注册监听
            isExit.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.text_confirm), exitDialogClicklistener);
            isExit.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.text_cancel), exitDialogClicklistener);
			// 显示对话框
			isExit.show();
            return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
    /**监听对话框里面的button点击事件*/
    DialogInterface.OnClickListener exitDialogClicklistener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    @Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onStart() {
		myBaiduMap.setMyLocationEnabled(true);//开启允许定位
		if (!myLocationClient.isStarted()) {
			myLocationClient.start();//开启定位
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		myBaiduMap.setMyLocationEnabled(false);
		myLocationClient.stop();//将定位与Activity生命周期进行绑定,关闭定位
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//在Activity执行onResume是执行MapView(地图)生命周期管理
		mapView.onResume();
		// 开启定位图层
		myBaiduMap.setMyLocationEnabled(true);
	}
	@Override
	protected void onDestroy() {
		mapView.onDestroy();
		super.onDestroy();
	}

	public void toast(String str){
		Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
	}

}
