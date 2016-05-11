package name.caiyao.fakegps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class MockGpsService extends Service {

    public static final String ACTION_START = "name.caiyao.fakegps.START_FAKE";
    public static final String ACTION_STOP = "name.caiyao.fakegps.STOP_FAKE";

    UpdateGPSThread currentThread = null;
    private SQLiteDatabase mSQLiteDatabase;

    public MockGpsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSQLiteDatabase = new DbHelper(this).getWritableDatabase();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG", "startCommand");
        if (intent.getStringExtra("action").equalsIgnoreCase(ACTION_START)) {
            currentThread = new UpdateGPSThread();
            currentThread.mLocation = intent.getStringExtra("location");
            currentThread.start();
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", "hook");
            contentValues.put("value", "1");
            mSQLiteDatabase.insertWithOnConflict(DbHelper.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        if (intent.getStringExtra("action").equalsIgnoreCase(ACTION_STOP)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", "hook");
            contentValues.put("value", "0");
            mSQLiteDatabase.insertWithOnConflict(DbHelper.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            if (currentThread != null) {
                currentThread.Running = false;
                currentThread.interrupt();
                currentThread = null;
                stopSelf();
            }
        }

        return START_STICKY;
    }


    public void createProgressNotification(Location location) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.gps);//最为重要的一个参数，如果不设置，通知不会出现在状态栏中。
        builder.setTicker("开始模拟位置:" + location.getLatitude() + "," + location.getLongitude());
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.gps));//设置状态栏下拉后显示的图标
        builder.setContentTitle("模拟位置:");
        builder.setContentText(location.getLatitude() + "," + location.getLongitude());
        builder.setWhen(System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0));
        startForeground(1337, builder.build());

    }

    public void removeProgressNotification() {
        stopForeground(true);
    }

    class UpdateGPSThread extends Thread {

        String mLocation;
        public boolean Running;

        @Override
        public void run() {
            Running = true;
            String[] loArr = mLocation.split(":");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = new Location("gps");
            location.setTime(System.currentTimeMillis());
            location.setLatitude(Double.parseDouble(loArr[0]));
            location.setLongitude(Double.parseDouble(loArr[1]));
            location.setAltitude(2.0f);
            location.setAccuracy(3.0f);
            createProgressNotification(location);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            try {
                locationManager.addTestProvider("gps",
                        "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),
                        "supportsAltitude".equals(""), "supportsSpeed".equals(""),
                        "supportsBearing".equals(""), Criteria.POWER_LOW,
                        Criteria.ACCURACY_FINE);
                locationManager.setTestProviderLocation("gps", location);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            while (Running) {
                try {
                    locationManager.setTestProviderLocation("gps", location);
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
            locationManager.setTestProviderEnabled("gps", false);
            locationManager.removeTestProvider("gps");
            removeProgressNotification();
            if (currentThread == this)
                currentThread = null;
        }

    }
}
