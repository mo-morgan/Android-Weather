package com.example.morgan.android_weather;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    /**
     *  Handler for handling messages after parsing
     */
    private Handler mHandler;

    /**
     *  Integer for representing when the task is complete
     */
    private static final int TASK_COMPLETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TASK_COMPLETE:
                        addWeather((String) msg.obj);
                        break;
                }
            }

            /**
             *  adds the temperature as a text view to the activity
             *
             * @param temp  the temperature parsed
             */
            private void addWeather(String temp) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout);
                TextView textView = new TextView(MainActivity.this);
                textView.setText(getString(R.string.Celcius, temp));
                linearLayout.addView(textView);
            }
        };

        Button weatherButton = (Button) findViewById(R.id.weather_button);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadWeather();
            }
        });
    }

    /**
     *  Separate thread for parsing weather data from Google
     */
    private class LoadWeather implements Runnable {
        private ProgressDialog progressDialog;

        LoadWeather() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressNumberFormat(null);
            progressDialog.setProgressPercentFormat(null);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            Thread thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            parseData();
        }

        private void parseData() {
            try {
                Document doc = Jsoup.connect("https://www.google.com/search?q=houston+weather+usa&rlz=1C1CHBF_enCA804CA804&oq=houst&aqs=chrome.1.69i59l3j69i57j69i60l2.2554j0j7&sourceid=chrome&ie=UTF-8").get();
                Elements weather = doc.select("#wob_tm");
                String temp = weather.html();
                Message completeMessage = mHandler.obtainMessage(TASK_COMPLETE, temp);
                completeMessage.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    }
}
