package name.caiyao.fakegps;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class MockGpsService extends Service {

    public static final String ACTION_START = "name.caiyao.fakegps.START_FAKE";
    public static final String ACTION_STOP = "name.caiyao.fakegps.STOP_FAKE";

    public static MockGpsService instance = null;

    UpdateGPSThread currentThread = null;

    public MockGpsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getStringExtra("action").equalsIgnoreCase(ACTION_START)) {
            if (currentThread != null) {
                currentThread.Running = false;
            }

            currentThread = new UpdateGPSThread();
            currentThread.mLocation = intent.getStringExtra("location");
            currentThread.start();
        }
        if (intent.getStringExtra("action").equalsIgnoreCase(ACTION_STOP)) {
            if (currentThread != null) {
                currentThread.Running = false;
                currentThread.interrupt();
                currentThread = null;
                stopSelf();
            }
        }

        return START_STICKY;
    }


    public void createProgressNotification() {
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        startForeground(1337, notification);

    }

    public void removeProgressNotification() {
        stopForeground(true);
    }

    class UpdateGPSThread extends Thread {

        String mLocation;
        public boolean Running;

        @Override
        public void run() {
            Log.i("MockGPSService", "Starting UpdateGPSThread");
            createProgressNotification();
            Running = true;
            String[] loArr = mLocation.split(":");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = new Location("gps");
            location.setTime(System.currentTimeMillis());
            location.setLatitude(Double.parseDouble(loArr[0]));
            location.setLongitude(Double.parseDouble(loArr[1]));
            location.setAltitude(2.0f);
            location.setAccuracy(3.0f);
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            try {
                locationManager.addTestProvider("gps",

                        "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""),

                        "supportsAltitude".equals(""), "supportsSpeed".equals(""),

                        "supportsBearing".equals(""), android.location.Criteria.POWER_LOW,

                        android.location.Criteria.ACCURACY_FINE);
                locationManager.setTestProviderLocation("gps", location);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            while (Running) {
                locationManager.setTestProviderLocation("gps", location);
                try {
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            }
            locationManager.setTestProviderEnabled("gps", false);
            locationManager.removeTestProvider("gps");
            removeProgressNotification();
            if (currentThread == this)
                currentThread = null;
            Log.i("MockGPSService", "Ending UpdateGPSThread");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (instance == this)
            instance = null;
    }
}
