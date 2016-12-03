//package com.lanying.baidumapdemo;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//
//import com.baidu.lbsapi.BMapManager;
//import com.baidu.lbsapi.panoramaview.PanoramaView;
//import com.baidu.mapapi.model.LatLng;
//
///**
// * Created by lanying on 2016/12/3.
// */
//public class PanoramaDemoActivityMain extends Activity{
//    private PanoramaView mPanoView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.panorama_layout);
//
//
//        mPanoView = (PanoramaView) findViewById(R.id.panorama);
//
//        PanoDemoApplication app = (PanoDemoApplication) this.getApplication();
//        if (app.mBMapManager == null) {
//            app.mBMapManager = new BMapManager(app);
//
//            app.mBMapManager.init(new PanoDemoApplication.MyGeneralListener());
//        }
//
//        mPanoView.setPanorama("0100220000130817164838355J5");
//
//        Intent intent = getIntent();
//        if(intent != null){
//            LatLng ll = (LatLng) intent.getParcelableExtra("latlng");
//            mPanoView.setPanorama(ll.longitude,ll.latitude);
//        }
//
//    }
//
//
//    public static Intent newIntant(Context context, LatLng ll) {
//
//       Intent intent = new Intent(context,PanoramaDemoActivityMain.class);
//        intent.putExtra("latlng",ll);
//        context.startActivity(intent);
//        return intent;
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mPanoView.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mPanoView.onResume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        mPanoView.destroy();
//        super.onDestroy();
//    }
//}
