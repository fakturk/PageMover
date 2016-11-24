package com.unist.netlab.fakturk.pagemover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Switch;

import static android.hardware.SensorManager.GRAVITY_EARTH;

public class MainActivity extends AppCompatActivity
{

    WebView netlab;
    Button buttonStart, buttonReset;
    Switch switchGyr, switchAcc, switchReset, switchSmooth;
    ViewGroup.MarginLayoutParams marginParams;



    float[] acc, gyr, oldAcc, oldGyr, gravity, sideY, sideX, oldGravity, rotatedGyr, rotational_vel, rotational_vel_earth;
    float[][] rotation, resultOfDynamic;
    boolean start, onlyGyr, accEnable, resetEnable, smoothEnable;

    Gravity g;
    Orientation orientation;
    DynamicAcceleration dynamic;
    int counter;
    float omega_x, omega_y, omega_z;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonReset = (Button) findViewById(R.id.buttonReset);
        switchGyr = (Switch) findViewById(R.id.switch_gyr);
        switchAcc = (Switch) findViewById(R.id.switchAcc);
        switchReset = (Switch) findViewById(R.id.switchReset);
        switchSmooth = (Switch) findViewById(R.id.switchSmoothReset);

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
        rotational_vel = new float[]{0,0,0};
        rotational_vel_earth = new float[]{0,0,0};

        g = new Gravity();
        orientation = new Orientation();
        dynamic = new DynamicAcceleration();
        counter = 0;
        omega_x = 0;
        omega_y = 0;
        omega_z = 0;

        start = false;
        onlyGyr = false;
        accEnable=false;
        resetEnable=false;
        smoothEnable=false;

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

//                marginParams.setMargins(250, 250, 250, 250);

//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//                netlab.setLayoutParams(layoutParams);


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

//                        System.out.print("stable, ");
                        if (counter > 10) //if phone is stable over a second
                        {
                            if (resetEnable)
                            {
                                if (smoothEnable)
                                {
                                    smoothReset(2);
                                }
                                else
                                {
                                    reset();
                                }
                            }


//                            reset();
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
//                        System.out.print("not stable, ");
                        counter = 0;

                        rotation = orientation.rotationFromGravity(gravity);
                        rotational_vel[0] += gyr[0] * dynamic.getDeltaT();
                        rotational_vel[1] += gyr[1] * dynamic.getDeltaT();
                        rotational_vel[2] += gyr[2] * dynamic.getDeltaT();

                        rotatedGyr = orientation.rotatedGyr(gyr, rotation);

                        omega_x += rotatedGyr[0] * dynamic.getDeltaT();
                        omega_y += rotatedGyr[1] * dynamic.getDeltaT();
                        omega_z += rotatedGyr[2] * dynamic.getDeltaT();
                        rotation = orientation.updateRotationMatrix(rotation, rotatedGyr, dynamic.getDeltaT());

                        gravity = g.gravityAfterRotation(rotation);

                        rotation = orientation.updateRotationAfterOmegaZ(rotation, omega_z);
                        float[] reRotatedGyr = orientation.reRotatedGyr(rotatedGyr,rotation);

                        rotational_vel_earth[0]+=reRotatedGyr[0]* dynamic.getDeltaT();
                        rotational_vel_earth[1]+=reRotatedGyr[1]* dynamic.getDeltaT();
                        rotational_vel_earth[2]+=reRotatedGyr[2]* dynamic.getDeltaT();

                        sideX = g.sideXAfterRotation(rotation);
                        sideY = g.sideYAfterRotation(rotation);


                    }


                    //store acc values
                    System.arraycopy(acc, 0, oldAcc, 0, acc.length);

                    if (onlyGyr)
                    {
                        netlab.setRotation(rotational_vel[2] * 10);
                        netlab.setRotationX(rotational_vel[0] * 10);
                        netlab.setRotationY(-1 * rotational_vel[1] * 10);
                    } else
                    {
                        netlab.setRotation(rotational_vel_earth[2] * 10);
                        netlab.setRotationX(rotational_vel_earth[0] * 10);
                        netlab.setRotationY(-1 * rotational_vel_earth[1] * 10);
                    }


                }
            }
        }, new IntentFilter(SensorService.ACTION_SENSOR_BROADCAST));

        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (buttonStart.getText().equals("Start"))
                {
                    buttonStart.setText("Stop");
                    startService(new Intent(MainActivity.this, SensorService.class));

                } else
                {
                    buttonStart.setText("Start");
                    stopService(new Intent(MainActivity.this, SensorService.class));


                }
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                acc= new float[]{0, 0, 0};
//                gyr = new float[]{0, 0, 0};

               reset();
            }
        });


        switchGyr.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (switchGyr.isChecked())
                {
                    onlyGyr = true;
                    switchReset.setEnabled(false);
                    switchSmooth.setEnabled(false);
                } else
                {
                    onlyGyr = false;
                    switchReset.setEnabled(true);
                    switchSmooth.setEnabled(true);
                }
            }
        });

        switchAcc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (switchAcc.isChecked())
                {
                    accEnable = true;
                } else
                {
                    accEnable = false;
                }
            }
        });

        switchReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (switchReset.isChecked())
                {
                    resetEnable = true;
                    switchSmooth.setEnabled(true);
                } else
                {
                    resetEnable = false;
                    switchSmooth.setEnabled(false);
                    switchSmooth.setChecked(false);
                    smoothEnable=false;
                }
            }
        });

        switchSmooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (switchSmooth.isChecked())
                {
                    smoothEnable = true;
                } else
                {
                    smoothEnable = false;
                }
            }
        });




    }

    private void smoothReset(float factor)
    {
//            float factor=10f;

            for (int j = 0; j < 3; j++)
            {
//                acc[j]/=factor;
//                gyr[j]/=factor;

                rotational_vel_earth[j]/=factor;
            }
            omega_x/=factor;
            omega_y /=factor;
            omega_z /=factor;

        System.out.println("rotation before: "+netlab.getRotation());
            netlab.setRotation(netlab.getRotation()/factor);
        System.out.println("rotation after: "+netlab.getRotation());
            netlab.setRotationX(netlab.getRotationX()/factor);
            netlab.setRotationY(netlab.getRotationY()/factor);

//        acc= new float[]{0, 0, 0};
//        gyr = new float[]{0, 0, 0};
//        omega_x =0;
//        omega_y =0;
//        omega_z =0;
//        rotational_vel_earth[0] = 0;
//        rotational_vel_earth[1] = 0;
//        rotational_vel_earth[2] = 0;
//
//        netlab.setRotation(0);
//        netlab.setRotationX(0);
//        netlab.setRotationY(0);
    }

    private void reset()
    {



        netlab.setRotation(0);
        netlab.setRotationX(0);
        netlab.setRotationY(0);
        omega_x =0;
        omega_y =0;
        omega_z =0;
        rotational_vel_earth[0] = 0;
        rotational_vel_earth[1] = 0;
        rotational_vel_earth[2] = 0;
        rotational_vel=new float[]{0, 0, 0};
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    netlab.setScrollY(netlab.getScrollY()-100);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    netlab.setScrollY(netlab.getScrollY()+100);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    public class MyWebViewClient extends WebViewClient
    {

        public MyWebViewClient()
        {

            super();
            //start anything you need to
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            //Do something to the urls, views, etc.
        }
    }

    @Override
    protected void onPause() {

        super.onPause();


    }


    @Override
    protected void onResume() {

        super.onResume();
//        startService(new Intent(this, SensorService.class));
    }


    @Override
    protected void onDestroy() {


        super.onDestroy();
    }


    @Override
    public void onStart() {

        super.onStart();


    }

    @Override
    public void onStop() {

        super.onStop();


    }
}
