package name.caiyao.fakegps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener {

    private MapView mv;
    private TextView tv_count;
    private AMap aMap;
    private SharedPreferences sharedPreferences;
    private long mExitTime = 0;
    private int duration, count = 0;
    private ArrayList<String> locationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("locationHistory", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_count = (TextView) findViewById(R.id.count);
        mv = (MapView) findViewById(R.id.mv);
        assert mv != null;
        mv.onCreate(savedInstanceState);
        aMap = mv.getMap();
        String location = sharedPreferences.getString("location", "");
        duration = sharedPreferences.getInt("duration", 30);
        if (!TextUtils.isEmpty(location)) {
            String[] locationArr = location.split(",");
            for (String s : locationArr) {
                locationList.add(s);
                count++;
                String[] lArr = s.split(":");
                LatLng l = new LatLng(Double.parseDouble(lArr[0]), Double.parseDouble(lArr[1]));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(l);
                markerOptions.draggable(true);
                aMap.addMarker(markerOptions);
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(l));
                aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
            }
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
                if (locationList.size() == 0) {
                    Toast.makeText(this, "请点击地图选择至少一个地点！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                String location = "";
                for (String s : locationList) {
                    location += s + ",";
                }
                sharedPreferences.edit().putString("location", location).apply();
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                try {
                    String mockProviderName = LocationManager.GPS_PROVIDER;
                    locationManager.addTestProvider(mockProviderName,

                            "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                            "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                            "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                            android.location.Criteria.ACCURACY_FINE);
                    locationManager.setTestProviderEnabled(mockProviderName, true);
                    startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_START).putStringArrayListExtra("location", locationList).putExtra("duration", duration));
                    Toast.makeText(this, "位置模拟成功，重启需定位的应用以生效！", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog();
                }
                break;
            case R.id.stop:
                Toast.makeText(this, "位置模拟关闭，重启需定位的应用以生效！", Toast.LENGTH_LONG).show();
                startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_STOP));
                break;
            case R.id.search:
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_search, null, false);
                final EditText et_key = (EditText) view.findViewById(R.id.key);
                new AlertDialog.Builder(this).setView(view)
                        .setTitle("搜索位置")
                        .setMessage("只能搜索国内地点，如需定位国外请拖拽地图选取！")
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
            case R.id.clear:
                count = 0;
                aMap.clear();
                locationList.clear();
                break;
            case R.id.duration:
                View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_search, null, false);
                final EditText et_key1 = (EditText) view1.findViewById(R.id.key);
                et_key1.setText(duration+"");
                et_key1.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(this).setView(view1)
                        .setTitle("设置多点间隔(s)")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                duration = Integer.parseInt(et_key1.getText().toString());
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
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
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        aMap.addMarker(markerOptions);
        count++;
        tv_count.setText(count+"");
        double[] gpsLocation = GpsUtils.gcj02towgs84(latLng.longitude, latLng.latitude);
        locationList.add(gpsLocation[1] + ":" + gpsLocation[0]);
    }

    @Override
    public void onBackPressed() {
        startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_STOP));
        super.onBackPressed();
    }
}
