package com.sumasoft.findcoffeeshop.activities;

/**
 * Created by sumasoft on 25/09/17.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sumasoft.findcoffeeshop.R;

import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;


public class SplashActivity extends ActivityManagePermission {

    private static final long SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //check for permission in android version greater than 6.0
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission(SplashActivity.this);
        }

    }

    //add delay of 3 sec. After 2 sec show next activity
    private void goToNextScreen() {
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
            Intent intent = new Intent(SplashActivity.this,MapsActivity.class);
            startActivity(intent);
            finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public  void checkLocationPermission(final Context mContext) {
        String permissionAsk[] = {PermissionUtils.Manifest_ACCESS_FINE_LOCATION};
        askCompactPermissions(permissionAsk, new PermissionResult() {

            public void permissionGranted() {
                goToNextScreen();
            }


            public void permissionDenied() {
                finish();
            }

            @Override
            public void permissionForeverDenied() {
                {
                    String permissionAsk[] = {PermissionUtils.Manifest_ACCESS_FINE_LOCATION};
                    boolean isGranted = isPermissionsGranted(mContext,permissionAsk);
                    if(!isGranted){
                        finish();
                    }else {
                        System.out.println("IS GRANTED -- " + isGranted);
                        goToNextScreen();
                    }
                }
            }
        });
    }
}
