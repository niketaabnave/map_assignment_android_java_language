package com.sumasoft.findcoffeeshop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sumasoft.findcoffeeshop.activities.MapsActivity;

/**
 * Created by sumasoft on 25/09/17.
 */

public class MapUtils {

    //check for google play service is available on device
    public static boolean isGooglePlayServicesAvailable(Context mContext, Activity activity) {
        try {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(mContext);
            if (result != ConnectionResult.SUCCESS) {
                if (googleAPI.isUserResolvableError(result)) {
                    googleAPI.getErrorDialog(activity, result,
                            0).show();
                }
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    //check for gps is enabled or not. If not enabled then redirect to settings of device to enable location manually
    public static void isGpsEnabled(Context mContext, final Activity activity) {
        try {
            final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
