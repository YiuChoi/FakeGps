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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;

public class MockGpsService extends Service {

    static final String ACTION_START = "name.caiyao.fakegps.START_FAKE";
    static final String ACTION_STOP = "name.caiyao.fakegps.STOP_FAKE";
    private static final int CHANGE_LOCATION = 1;

    private UpdateGPSThread currentThread = null;
    private SQLiteDatabase mSQLiteDatabase;
    private ArrayList<String> mLocation;
    private int locationIndex = 0;
    private int duration;
    private Location currentLocation;

    private Handler mockHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_LOCATION:
                    Location tempLocation = new Location("gps");
                    locationIndex++;
                    if (locationIndex == mLocation.size()) {
                        locationIndex = 0;
                    }
                    String[] loArr = mLocation.get(locationIndex).split(":");
                    tempLocation.setTime(System.currentTimeMillis());
                    tempLocation.setLatitude(Double.parseDouble(loArr[0]));
                    tempLocation.setLongitude(Double.parseDouble(loArr[1]));
                    tempLocation.setAltitude(2.0f);
                    tempLocation.setAccuracy(3.0f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tempLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    }
                    currentLocation = tempLocation;
                    removeProgressNotification();
                    createProgressNotification(currentLocation);
                    mockHandler.sendEmptyMessageDelayed(CHANGE_LOCATION, duration);
                    break;
            }
            return true;
        }
    });

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
        if (intent.getStringExtra("action").equalsIgnoreCase(ACTION_START)) {
            mLocation = intent.getStringArrayListExtra("location");
            duration = intent.getIntExtra("duration", 30 * 1000);
            mockHandler.sendEmptyMessage(CHANGE_LOCATION);
            currentThread = new UpdateGPSThread();
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
            mockHandler.removeMessages(CHANGE_LOCATION);
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


    private void createProgressNotification(Location location) {
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

    private void removeProgressNotification() {
        stopForeground(true);
    }

    private class UpdateGPSThread extends Thread {

        boolean Running;

        @Override
        public void run() {
            Running = true;
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                locationManager.addTestProvider("gps",
                        "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),
                        "supportsAltitude".equals(""), "supportsSpeed".equals(""),
                        "supportsBearing".equals(""), Criteria.POWER_LOW,
                        Criteria.ACCURACY_FINE);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            while (Running) {
                try {
                    locationManager.setTestProviderLocation("gps", currentLocation);
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
