package com.unist.netlab.fakturk.pagemover;

/**
 * Created by fakturk on 9/8/16.
 * this class seperate dynamic and static acceleration
 */

class DynamicAcceleration
{
    private float[] oldAcc,oldGyr, dynAcc;
    private long timeInMillis;
    float epsilon = 0.31f;

    public float getDeltaT()
    {

        return deltaT;
    }

    public void setDeltaT(float deltaT)
    {

        this.deltaT = deltaT;
    }

    float deltaT;
    Gravity g ;
    Orientation orientation ;

    DynamicAcceleration()
    {
        timeInMillis = 10;
        this.deltaT = (float) (timeInMillis*1.0/1000);
        oldAcc = new float[]{0.0f, 0.0f, 0.0f};
        oldGyr = new float[]{0.0f, 0.0f, 0.0f};
        dynAcc = new float[]{0.0f, 0.0f, 0.0f};

         g = new Gravity();
         orientation = new Orientation();

    }
    public DynamicAcceleration(long time)
    {
        timeInMillis = time;
        this.deltaT = (float) (timeInMillis*1.0/1000);
        oldAcc = new float[]{0.0f, 0.0f, 0.0f};
        oldGyr = new float[]{0.0f, 0.0f, 0.0f};
        dynAcc = new float[]{0.0f, 0.0f, 0.0f};

        g = new Gravity();
        orientation = new Orientation();

    }




    //find acceleration difference d_t
    private float[] accDiff(float[] oldAcc, float[] newAcc)
    {
        float[] diffAcc = new float[]{0.0f, 0.0f, 0.0f};

        for (int i = 0; i < 3; i++)
        {
            diffAcc[i] = (newAcc[i]-oldAcc[i]);
        }
        return diffAcc;
    }









    // calculates and return dynamic acceleration, velocity and distance
    float[][] calculate(float[] acc, float[] oldAcc, float[] gyr , float[] oldGra, float[][] oldRotationMatrix)
    {
//
      float[][] results = new float[5][3];
        float[] accDiff = accDiff(acc,oldAcc);
        float[] graDiff = g.getGravityDifference(oldRotationMatrix,gyr,deltaT);
        float[][] R = orientation.updateRotationMatrix(oldRotationMatrix,gyr,deltaT);
        float[] gravity = g.gravityAfterRotation(R);

        for (int i = 0; i < 3; i++)
        {
            results[0][i]=accDiff[i]-graDiff[i];
            results[1][i]=gravity[i];
            results[2][i]=R[0][i];
            results[3][i]=R[1][i];
            results[4][i]=R[2][i];
        }
        return results;
    }

}
