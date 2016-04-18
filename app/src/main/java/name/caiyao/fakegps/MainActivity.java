package name.caiyao.fakegps;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, LocationListener {

    private MapView mv;
    private AMap aMap;
    private LatLng latLng;
    private boolean hasOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mv = (MapView) findViewById(R.id.mv);
        assert mv != null;
        mv.onCreate(savedInstanceState);
        aMap = mv.getMap();
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
            hasOpen = true;
        } catch (Exception e) {
            e.printStackTrace();
            hasOpen = false;
            Toast.makeText(this, "请打开模拟位置权限！", Toast.LENGTH_SHORT).show();
        }
        mv.onResume();
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
                startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_START).putExtra("location", latLng.latitude + ":" + latLng.longitude));
                break;
            case R.id.stop:
                startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_STOP));
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
        Log.i("TAG", "经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
        markerOptions.title("经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
        if (hasOpen) {
            aMap.addMarker(markerOptions);
            this.latLng = latLng;
        } else {
            Toast.makeText(this, "请打开模拟位置权限！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        startService(new Intent(MainActivity.this, MockGpsService.class).putExtra("action", MockGpsService.ACTION_START).putExtra("location", latLng.latitude + ":" + latLng.longitude));
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
