package com.yaycloud.sampleapp;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yaycloud.sdk.account.YayAccountManager;
import com.yaycloud.sdk.common.AppState;
import com.yaycloud.sdk.common.CallStatus;
import com.yaycloud.sdk.common.CallType;
import com.yaycloud.sdk.listeners.CallManagerListener;
import com.yaycloud.sdk.listeners.InitializationManagerListener;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SampleActivity.class.getSimpleName();

    // Shouldn't be empty for testing your calls. Please, fill it.
    private static final String WIDGET_ID = "";

    private TextView callStatusView;
    private TextView callTypeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        callStatusView = findViewById(R.id.callStatusView);
        callTypeView = findViewById(R.id.callTypeView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (TextUtils.isEmpty(WIDGET_ID)) {
            Toast.makeText(this, "Please, enter the widget_Id inside code for test your calls.", Toast.LENGTH_LONG).show();
            return;
        }

        // init YayCloudSDK
        YayAccountManager.initSDK(getApplicationContext(), AppState.FOREGROUND.getIntValue(),
                WIDGET_ID, new InitializationManagerListener() {
                    @Override
                    public void onSDKInitialized() {
                        Log.d(TAG, "YayCloud-SDK was initialized");
                    }

                    @Override
                    public void onSDKDestroyed() {
                        Log.d(TAG, "YayCloud-SDK was destroyed");
                    }

                    @Override
                    public void onLoadedAccountName(String name) {
                        TextView accountNameView = findViewById(R.id.accountView);
                        accountNameView.setText(name);
                    }
                });

        // Set CallManager listener for a possibility to inform about changes when call is active
        YayAccountManager.setCallManagerListener(new CallManagerListener() {
            @Override
            public void onCallStateChanged(int callStatus, int callId, int callType, String phoneNumber, String name, boolean isActiveCalls, boolean notifyEnded, String reasonMessage) {
                Log.d(TAG, "onCallStateChanged() callStatus: " + callStatus + " callId: " + callId + " callType: " + callType +
                        " phoneNumber: " + phoneNumber + " name: " + name +
                        " isActiveCalls: " + isActiveCalls + " notifyEnded: " + notifyEnded + " reasonMessage: " + reasonMessage);

                runOnUiThread(() -> {
                    String status = CallStatus.getStatusByIntValue(callStatus).name();
                    callStatusView.setText(status);

                    String callTypeStr = CallType.getTypeByIntValue(callType).name();
                    callTypeView.setText(callTypeStr);
                });
            }

            @Override
            public void onCallQuality(int qualityStatus) {
                String info = String.format("onCallQuality(): qualityStatus: %d", qualityStatus);
                Log.d(TAG, info);
            }
        });

        // request for runtime permissions on android 6.0+ to possibility make calls
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YayAccountManager.destroy();
    }

    @Override
    public void onClick(View view) {
        if (TextUtils.isEmpty(WIDGET_ID)) {
            Toast.makeText(this, "Please, fill the widget_Id constant in your code for testing calls.", Toast.LENGTH_LONG).show();
            return;
        }

        int id = view.getId();
        switch (id) {
            case R.id.callBtnView:
                YayAccountManager.callSync(WIDGET_ID);
                break;
            case R.id.hangupBtnView:
                YayAccountManager.hangupSync();
                break;
            case R.id.speakerBtnView:
                YayAccountManager.getYayAudioManager().setSpeakerDefaults();
                break;
            case R.id.receiverBtnView:
                YayAccountManager.getYayAudioManager().setCallDefaults();
                break;
            case R.id.btBtnView:
                YayAccountManager.getYayAudioManager().setBluetoothDefaults();
                break;
            case R.id.sendDTMFBtn:
                EditText dtmfTextView = findViewById(R.id.dtmfView);
                YayAccountManager.sendDigitsDTMF(dtmfTextView.getText().toString());
                break;
        }
    }
}
