package com.valak.medo;

import android.app.Application;
import android.content.Intent;

import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationClass extends Application {

    private static final String ONESIGNAL_APP_ID = "dac6c447-c7b0-4732-92af-dd9c6ab5b0b2";

    @Override
    public void onCreate() {
        super.onCreate();

        // Uncomment to enable verbose OneSignal logging to debug issues if needed.
        // OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        OneSignal.setNotificationOpenedHandler(
                new OneSignal.OSNotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenedResult result) {
                        JSONObject AdditionalData = result.getNotification().getAdditionalData();
                        try {
                            String key = AdditionalData.get("key").toString();
                            startAction(key);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    private void startAction(String actionKey) {

        if (actionKey.equals("updP"))
            {
                Intent intent = new Intent(ApplicationClass.this, UpdateProfile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        else{
            Intent intent = new Intent(ApplicationClass.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
}
