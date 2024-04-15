package ul.ie.cs4084.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {
    private static long lastShownTime = 0;
    private static final int SUPPRESS_DURATION = 10000;
    @Override
    public void onReceive(Context context, Intent intent) {
        long currentTime = System.currentTimeMillis();
        if (intent.getAction() != null && currentTime - lastShownTime > SUPPRESS_DURATION) {
            // Location provider status has changed
            String message = this.isLocationEnabled(context) ? "Location provider turned ON" : "Location provider turned OFF";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            lastShownTime = System.currentTimeMillis();
        }
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
