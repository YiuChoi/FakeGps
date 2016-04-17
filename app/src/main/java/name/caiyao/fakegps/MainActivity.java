package name.caiyao.fakegps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, LocationListener {

    private MapView mv;
    private AMap aMap;
    private LocationManager locationManager;
    private String mMockProviderName = LocationManager.GPS_PROVIDER;

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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        try {
            locationManager.addTestProvider(mMockProviderName,

                    "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                    "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                    "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                    android.location.Criteria.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(mMockProviderName, true);
            locationManager.requestLocationUpdates(mMockProviderName, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "请打开模拟位置权限！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mv.onResume();
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
        Log.i("TAG", latLng.latitude + ":" + latLng.longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
        aMap.addMarker(markerOptions);
        Location location = new Location(mMockProviderName);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        location.setAltitude(2.0f);
        location.setAccuracy(3.0f);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        try {
            locationManager.addTestProvider(mMockProviderName,

                    "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                    "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                    "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                    android.location.Criteria.ACCURACY_FINE);
            locationManager.setTestProviderLocation(mMockProviderName, location);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "请打开模拟位置权限！", Toast.LENGTH_SHORT).show();
        }
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
