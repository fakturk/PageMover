package com.unist.netlab.fakturk.pagemover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import static android.hardware.SensorManager.GRAVITY_EARTH;

public class MainActivity extends AppCompatActivity
{
    WebView netlab;
    Button buttonStart;
    ViewGroup.MarginLayoutParams marginParams;

    float[] acc, gyr, oldAcc, oldGyr, gravity,sideY,sideX, oldGravity, rotatedGyr;
    float[][] rotation, resultOfDynamic;
    boolean start;

    Gravity g;
    Orientation orientation;
    DynamicAcceleration dynamic;
    int counter;
    float omega_x,omega_y, omega_z;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStart = (Button) findViewById(R.id.buttonStart);

        netlab = (WebView) findViewById(R.id.webView);
        netlab.setWebViewClient(new MyWebViewClient());
        netlab.getSettings().setJavaScriptEnabled(true);
        netlab.loadUrl("http://netlab.unist.ac.kr/people/changhee-joo/");


        marginParams = new ViewGroup.MarginLayoutParams(netlab.getLayoutParams());

        acc = new float[3];
        gyr = new float[3];
        oldAcc = null;
        oldGyr = null;
        gravity = new float[3];
        sideX = new float[3];
        sideY = new float[3];
        rotation = null;
        rotatedGyr = new float[3];
        resultOfDynamic = new float[5][3];

        g = new Gravity();
        orientation = new Orientation();
        dynamic = new DynamicAcceleration();
        counter=0;
        omega_x =0;
        omega_y =0;
        omega_z =0;


        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                marginParams.setMargins(250, 250, 250, 250);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                netlab.setLayoutParams(layoutParams);

                netlab.setRotation(omega_z*10);
                netlab.setRotationX(omega_x*10);
                netlab.setRotationY(-1*omega_y*10);

                acc = (intent.getFloatArrayExtra("ACC_DATA"));
                gyr = intent.getFloatArrayExtra("GYR_DATA");

                if (acc != null && oldAcc == null)
                {
                    oldAcc = new float[3];
                    System.arraycopy(acc, 0, oldAcc, 0, acc.length);

                }
                if (gyr != null && oldGyr == null)
                {
                    oldGyr = new float[3];
                    System.arraycopy(gyr, 0, oldGyr, 0, gyr.length);

                }
                if (acc == null && oldAcc != null)
                {
                    acc = new float[3];
                    System.arraycopy(oldAcc, 0, acc, 0, oldAcc.length);

                }
                if (gyr == null && oldGyr != null)
                {
                    gyr = new float[]{0, 0, 0};

                }


                if (acc != null && gyr != null && start != true)
                {
                    start = true;
                    float accNorm = (float) Math.sqrt(Math.pow(acc[0], 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2));
                    for (int j = 0; j < 3; j++)
                    {
                        gravity[j] = acc[j] * (GRAVITY_EARTH / accNorm);
                    }
                    rotation = orientation.rotationFromGravity(gravity);


                }
                if (start)
                {

                    float thresholdAcc = 0; //0.15
                    float thresholdGyr = 0.15f; //0.2
                    float accNorm = (float) Math.sqrt(Math.pow(acc[0], 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2));
                    //if phone stable gravity = acc
                    if ((Math.abs(gyr[0]) + Math.abs(gyr[1]) + Math.abs(gyr[2])) < thresholdGyr)
                    {

                        System.out.print("stable, ");
                        if (counter > 10) //if phone is stable over a second
                        {
                            for (int j = 0; j < 3; j++)
                            {
                                gravity[j] = acc[j] * (GRAVITY_EARTH / accNorm);

                            }

                            counter = 0;
                        } else
                        {
                            counter++;
                        }


                    } else // not stable
                    {
                        System.out.print("not stable, ");
                        counter = 0;

                        rotation = orientation.rotationFromGravity(gravity);
                        omega_x += rotatedGyr[0] * dynamic.getDeltaT();
                        omega_y += rotatedGyr[1] * dynamic.getDeltaT();
                        omega_z += rotatedGyr[2] * dynamic.getDeltaT();

                        rotatedGyr = orientation.rotatedGyr(gyr, rotation);
                        rotation = orientation.updateRotationMatrix(rotation, rotatedGyr, dynamic.getDeltaT());

                        gravity = g.gravityAfterRotation(rotation);

                        rotation = orientation.updateRotationAfterOmegaZ(rotation, omega_z);
                        sideX = g.sideXAfterRotation(rotation);
                        sideY = g.sideYAfterRotation(rotation);


                    }


                    //store acc values
                    System.arraycopy(acc, 0, oldAcc, 0, acc.length);




                }
            }
        }, new IntentFilter(SensorService.ACTION_SENSOR_BROADCAST));

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonStart.getText().equals("Start")) {
                    buttonStart.setText("Stop");
                    startService(new Intent(MainActivity.this, SensorService.class));

                } else {
                    buttonStart.setText("Start");
                    stopService(new Intent(MainActivity.this, SensorService.class));


                }
            }
        });
    }

    public class MyWebViewClient extends WebViewClient
    {

        public MyWebViewClient() {
            super();
            //start anything you need to
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Do something to the urls, views, etc.
        }
    }
}
