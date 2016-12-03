package com.lanying.baidumapdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;


/**
 * DrawerLayout + 选择不同的功能
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, BaiduMap.OnMarkerClickListener {
    private ListView mListView;
    MapView mMapView ;
    BaiduMap mBaiduMap;
    BitmapDescriptor mCurrentMarker;//自定义图标，null表示默认图标
    PoiSearch mPoiSearch ;
    private Toolbar mToolbar;

    /*
     * 定位：第一步，初始化LocationClient类
     */
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        init();

        // 缩放等级
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.00f);// 范围是500m
        mBaiduMap.setMapStatus(msu);



        //============定位================
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();


    }

    private void init() {
        mListView = (ListView) findViewById(R.id.lv);
        mListView.setOnItemClickListener(this);

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        LinearLayout ll = (LinearLayout) mToolbar.getChildAt(0);
        final EditText et = (EditText) ll.findViewById(R.id.et_keyword);
        Button btn = (Button) ll.findViewById(R.id.btn_search);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = et.getText().toString().trim();
                searchInCity("上海",keyword);
            }
        });

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMarkerClickListener(this);
    }


    // 第二步，配置定位SDK参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 4、开始定位
        mLocationClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();

        mPoiSearch.destroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 点击DrawerLayout中ListView的项，显示不同的效果
        switch (position){
            case 0:
                //普通地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);

                break;
            case 2:
                //开启/关闭 交通图
                mBaiduMap.setTrafficEnabled(!mBaiduMap.isTrafficEnabled());
                break;
            case 3:
                // 我在哪儿
                mLocationClient.start();
                break;
            case 4:
                //普通
                mBaiduMap
                        .setMyLocationConfigeration(new MyLocationConfiguration(
                                MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker));
                break;
            case 5:
                //罗盘
                mBaiduMap
                        .setMyLocationConfigeration(new MyLocationConfiguration(
                                MyLocationConfiguration.LocationMode.COMPASS, true, mCurrentMarker));
                break;
            case 6:
                //跟随
                mBaiduMap
                        .setMyLocationConfigeration(new MyLocationConfiguration(
                                MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker));
                break;
            case 7:
                //城市内搜索
                boolean result = searchInCity("上海","电影");


                Log.d("lanying", "城市内检索结果： "+result);

                break;
        }
    }

    /**
     * 根据城市和关键字搜索
     * @param city
     * @param keyword
     * @return
     */
    private boolean searchInCity(String city,String keyword) {
        mBaiduMap.clear();// 先清空旧数据
        return mPoiSearch.searchInCity((new PoiCitySearchOption())
                            .city(city)
                            .keyword(keyword)
                            .pageNum(10));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng ll = marker.getPosition();
        final Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.button_title);
        button.setText(marker.getTitle());//想显示该处的信息…… 通过经纬度 --> 语义信息/全景信息
        //button.setPadding(15,10,15,10);

        // 将Button添加到覆盖物上
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, new InfoWindow.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick() {
                button.setVisibility(View.GONE);// 没有效果
                mBaiduMap.hideInfoWindow();
            }
        });
        mBaiduMap.showInfoWindow(mInfoWindow);
        Toast.makeText(MainActivity.this, ll.latitude + " -- " + ll.longitude, Toast.LENGTH_SHORT).show();


        // 全景
//        Intent intent = PanoramaDemoActivityMain.newIntant(MainActivity.this,ll);
//        startActivity(intent);

        return true;
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());



            //设置定位数据, 只有先允许定位图层后设置数据才会生效
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);


            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(14.0f);
            //以动画方式更新地图状态，跳转到我的位置
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()),3000);

            mLocationClient.stop();//更新完位置，停止定位
        }
    }

    // POI监听
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
        public void onGetPoiResult(PoiResult result){

            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            List<PoiInfo> allPoi = result.getAllPoi();
            if(allPoi != null) {
                for (PoiInfo info :
                        allPoi) {
                    Log.d("lanying", "info: " + info.name + "\n" + info.address);

                    // 添加覆盖物
                    LatLng llA = info.location;
                    BitmapDescriptor bdGround = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_gcoding);
                    MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdGround)
                            .zIndex(9).draggable(true).title(info.name);//给覆盖物添加title
                    mBaiduMap.addOverlay(ooA);
                }
            }

            
            //获取POI检索结果
        }
        public void onGetPoiDetailResult(PoiDetailResult result){
            //获取Place详情页检索结果
            Log.d("lanying", "PoiDetailResult: "+result.toString());
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };



}
