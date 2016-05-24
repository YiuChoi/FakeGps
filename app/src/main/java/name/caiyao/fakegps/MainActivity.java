package name.caiyao.fakegps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, LocationListener {

    private MapView mv;
    private AMap aMap;
    private LatLng latLng;
    private SharedPreferences sharedPreferences;
    private long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("locationHistory", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mv = (MapView) findViewById(R.id.mv);
        assert mv != null;
        mv.onCreate(savedInstanceState);
        aMap = mv.getMap();
        double lat = Double.parseDouble(sharedPreferences.getString("lat", "0.0"));
        double lon = Double.parseDouble(sharedPreferences.getString("lon", "0.0"));
        if (lat != 0.0 && lon != 0.0) {
            latLng = new LatLng(lat, lon);
            aMap.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(true);
            markerOptions.title("经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
            aMap.addMarker(markerOptions);
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat, lon)));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
        }
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.setOnMapClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            String mockProviderName = LocationManager.GPS_PROVIDER;
            locationManager.addTestProvider(mockProviderName,

                    "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                    "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                    "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                    android.location.Criteria.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(mockProviderName, true);
            locationManager.requestLocationUpdates(mockProviderName, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
            showDialog();
        }
        mv.onResume();
    }

    private void showDialog() {
        new AlertDialog.Builder(this).setTitle("需要打开模拟位置").setMessage("是否跳转到开发者选项打开模拟位置").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                if (latLng == null) {
                    Toast.makeText(this, "请点击地图选择一个地点！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                sharedPreferences.edit().putString("lat", String.valueOf(latLng.latitude)).putString("lon", String.valueOf(latLng.longitude)).apply();
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                try {
                    String mockProviderName = LocationManager.GPS_PROVIDER;
                    locationManager.addTestProvider(mockProviderName,

                            "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                            "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                            "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                            android.location.Criteria.ACCURACY_FINE);
                    locationManager.setTestProviderEnabled(mockProviderName, true);
                    locationManager.requestLocationUpdates(mockProviderName, 0, 0, this);
                    double[] gpsLocation = GpsUtils.gcj02towgs84(latLng.longitude, latLng.latitude);
                    startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_START).putExtra("location", gpsLocation[1] + ":" + gpsLocation[0]));
                    Toast.makeText(this, "重启需定位的应用以生效！", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog();
                }
                break;
            case R.id.stop:
                Toast.makeText(this, "重启需定位的应用以生效！", Toast.LENGTH_LONG).show();
                startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_STOP));
                break;
            case R.id.search:
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_search, null, false);
                final EditText et_key = (EditText) view.findViewById(R.id.key);
                new AlertDialog.Builder(this).setView(view)
                        .setTitle("搜索位置")
                        .setPositiveButton("搜索", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                search(et_key.getText().toString());
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.donate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.alipay.com/apoy1zw1o2xpc7915d")));
                break;
            case R.id.about:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://caiyao.name/releases")));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mv.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {//
                // 如果两次按键时间间隔大于2000毫秒，则不退出
                Toast.makeText(this, "再按一次退出程序,停止模拟位置！", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();// 更新mExitTime
            } else {
                System.exit(0);// 否则退出程序
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void search(final String key) {
        PoiSearch.Query query = new PoiSearch.Query(key, null, null);
        query.setPageSize(10);
        query.setPageNum(0);
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    final ArrayList<PoiItem> poiItems = poiResult.getPois();
                    if (poiItems.size() != 0) {
                        String[] keyList = new String[poiItems.size()];
                        for (int j = 0; j < poiItems.size(); j++) {
                            keyList[j] = poiItems.get(j).getTitle();
                        }
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("选择位置")
                                .setSingleChoiceItems(keyList, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(poiItems.get(which).getLatLonPoint().getLatitude(), poiItems.get(which).getLatLonPoint().getLongitude())));
                                        aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        Toast.makeText(MainActivity.this, "没有搜索结果", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearch.searchPOIAsyn();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mv.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mv.onDestroy();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
        aMap.addMarker(markerOptions);
        this.latLng = latLng;
    }

    @Override
    public void onBackPressed() {
        startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_STOP));
        super.onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Log.i("gps", String.format("location: x=%s y=%s", lat, lng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
