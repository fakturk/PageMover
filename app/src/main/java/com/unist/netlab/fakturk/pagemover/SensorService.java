package com.unist.netlab.fakturk.pagemover;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by fakturk on 16. 10. 21.
 */

public class SensorService extends Service implements SensorEventListener
{

    private String  text_acc="", text_gyr="", text_mag="";

    SensorManager SM;
    float[] ACC_DATA=new float[3];
    float[] GYR_DATA=new float[3];
    float[] MAG_DATA=new float[3];

    public static final String ACTION_SENSOR_BROADCAST = SensorService.class.getName() + "SensorBroadcast";
    long mSensorTimeStamp;

    // I will write whole sensor readings on this srings and then before I close the files I write this strings to the files
    String acc_string="", gyr_string="";

    @Override
    public void onSensorChanged(SensorEvent se)
    {
//        Log.d(LOG_TAG, "onSensorChanged");
//

        sendBroadcastMessage(se);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


//        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        SM.registerListener(this, SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10);
        SM.registerListener(this, SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 10);
        SM.registerListener(this, SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 10);

        Intent bIntent = new Intent(SensorService.this, MainActivity.class);
        PendingIntent pbIntent = PendingIntent.getActivity(SensorService.this, 0 , bIntent, 0);
        NotificationCompat.Builder bBuilder =
                new NotificationCompat.Builder(this)
                        //.setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Title")
                        .setContentText("Subtitle")
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .setContentIntent(pbIntent);
        Notification barNotif = bBuilder.build();
        this.startForeground(1, barNotif);



        return SensorService.START_STICKY;
    }

    @Override
    public void onCreate() {
        //super.onCreate();
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy(){

        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
//        Log.d( LOG_TAG, "onDestroy" );
        SM.unregisterListener(this);
//
    }




    @Override
    public IBinder onBind(Intent intent)
    {
        return null;


    }

    private void sendBroadcastMessage(SensorEvent se) {
        if (se != null) {
            Intent intent = new Intent(ACTION_SENSOR_BROADCAST);

            mSensorTimeStamp = se.timestamp;
            long timeInMillis = (new Date()).getTime()
                    + (mSensorTimeStamp - System.nanoTime()) / 1000000L;
            intent.putExtra("TIME",mSensorTimeStamp);

            switch(se.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER :
                    text_acc = "";
                    text_acc += "X = " + se.values[0] + "\n";
                    text_acc += "Y = " + se.values[1] + "\n";
                    text_acc += "Z = " + se.values[2] + "\n";
                    ACC_DATA[0] = se.values[0];
                    ACC_DATA[1] = se.values[1];
                    ACC_DATA[2] = se.values[2];

                    acc_string = Long.toString(timeInMillis)+" ";
                    acc_string +=(ACC_DATA[0]+" "+ACC_DATA[1]+" "+ACC_DATA[2]+"\n");

                    intent.putExtra("ACC", text_acc);
                    intent.putExtra("ACC_DATA", ACC_DATA);

                    break;

                case Sensor.TYPE_GYROSCOPE :
                    text_gyr = "";
                    text_gyr += "X = " + se.values[0] + "\n";
                    text_gyr += "Y = " + se.values[1] + "\n";
                    text_gyr += "Z = " + se.values[2] + "\n";
                    GYR_DATA[0] = se.values[0];
                    GYR_DATA[1] = se.values[1];
                    GYR_DATA[2] = se.values[2];


                    gyr_string = Long.toString(timeInMillis)+" ";
                    gyr_string += (GYR_DATA[0]+" "+GYR_DATA[1]+" "+GYR_DATA[2]+"\n");

                    intent.putExtra("GYR", text_gyr);

                    intent.putExtra("GYR_DATA", GYR_DATA);

                    break;

                case Sensor.TYPE_MAGNETIC_FIELD :
                    text_mag = "";
                    text_mag += "X = " + se.values[0] + "\n";
                    text_mag += "Y = " + se.values[1] + "\n";
                    text_mag += "Z = " + se.values[2] + "\n";
                    MAG_DATA[0] = se.values[0];
                    MAG_DATA[1] = se.values[1];
                    MAG_DATA[2] = se.values[2];
                    break;



            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }


}