package com.InstitutIcare.KomedBeacon9Tf;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.InstitutIcare.KomedBeacon9Tf.estimote.BeaconID;
import com.InstitutIcare.KomedBeacon9Tf.estimote.EstimoteCloudBeaconDetails;
import com.InstitutIcare.KomedBeacon9Tf.estimote.EstimoteCloudBeaconDetailsFactory;
import com.InstitutIcare.KomedBeacon9Tf.estimote.ProximityContentManager;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.cloud.model.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final Map<Color, Integer> BACKGROUND_COLORS = new HashMap<>();

    static {
        BACKGROUND_COLORS.put(Color.ICY_MARSHMALLOW, android.graphics.Color.rgb(109, 170, 199));
        BACKGROUND_COLORS.put(Color.BLUEBERRY_PIE, android.graphics.Color.rgb(98, 84, 158));
        BACKGROUND_COLORS.put(Color.MINT_COCKTAIL, android.graphics.Color.rgb(155, 186, 160));
    }

    private static final int BACKGROUND_COLOR_NEUTRAL = android.graphics.Color.rgb(160, 169, 172);

    private ProximityContentManager proximityContentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proximityContentManager = new ProximityContentManager(this,
                Arrays.asList(
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 61800, 4),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 61800, 1),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 61800, 2)),
                new EstimoteCloudBeaconDetailsFactory());
        proximityContentManager.setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {
                String text;
                Integer backgroundColor;
                if (content != null) {
                    EstimoteCloudBeaconDetails beaconDetails = (EstimoteCloudBeaconDetails) content;
                    text = "You entered room " + beaconDetails.getBeaconName() + ".";
                    sendPostRequest(beaconDetails.getBeaconName());
                    backgroundColor = BACKGROUND_COLORS.get(beaconDetails.getBeaconColor());
                } else {
                    text = "No beacons in range.";
                    backgroundColor = null;
                }
                ((TextView) findViewById(R.id.textView)).setText(text);
                findViewById(R.id.relativeLayout).setBackgroundColor(
                        backgroundColor != null ? backgroundColor : BACKGROUND_COLOR_NEUTRAL);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            Log.d(TAG, "Starting ProximityContentManager content updates");
            proximityContentManager.startContentUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping ProximityContentManager content updates");
        proximityContentManager.stopContentUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        proximityContentManager.destroy();
    }

    private void sendPostRequest(String beaconName) {
        Log.d(TAG, "sendPostRequest: "+beaconName);
        new PostTask().execute(new String[] {beaconName});
    }

    private class PostTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... data) {
            byte[] result = null;
            Log.d(TAG, "BACKGROUND sendPostRequest " + data[0]);
            String str = "";

            HttpClient httpclient = new DefaultHttpClient();
            //HttpPut httppost = new HttpPut("https://arkathon.komed-health.com:8001/api/v1/location/");
            HttpPost httppost = new HttpPost("http://104.199.44.19/api/v1/location/");


            try {
                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair("id", data[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                Log.d(TAG, httppost.toString());
                //execute http post
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();
                str = statusLine.getStatusCode()+"";
            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return str;
        }

        protected void onPostExecute(String page)
        {
            Log.d(TAG, "POST sendPostRequest: "+page);
            Toast toast = Toast.makeText(getApplicationContext(), "HTTP Code "+page+".", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
