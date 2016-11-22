package com.unist.netlab.fakturk.pagemover;

import static android.hardware.SensorManager.GRAVITY_EARTH;

/**
 * Created by fakturk on 16. 5. 2.
 */
public class Gravity
{
    float[] gravity ;

    public Gravity() {
        gravity = new float[]{0,0,GRAVITY_EARTH};
    }



    float[] gravityAfterRotation( float[][] rotationMatrix)
    {
        float[] newGravity = new float[3];
        float[] earth = getGravityEarth();
        for (int i = 0; i < 3; i++) {
            newGravity[i] = earth[2]* rotationMatrix[i][2]+ earth[2]* rotationMatrix[i][2]+earth[2]* rotationMatrix[i][2];
        }
        float graNorm = gravityNorm(newGravity);
        for (int j = 0; j < 3; j++)
        {
            newGravity[j] = newGravity[j]*(GRAVITY_EARTH/graNorm);
        }
        setGravity(newGravity);

        return newGravity;
    }
    float[] sideXAfterRotation( float[][] rotationMatrix)
    {
        float[] newSideX = new float[3];
        float[] earth = new float[]{1,0,0};

        for (int i = 0; i < 3; i++) {
            newSideX[i] = earth[0]* rotationMatrix[i][0]+ earth[0]* rotationMatrix[i][0]+earth[0]* rotationMatrix[i][0];
        }
        float sideNorm = (float) Math.sqrt(Math.pow(newSideX[0],2)+Math.pow(newSideX[1],2)+Math.pow(newSideX[2],2));
        for (int j = 0; j < 3; j++)
        {
            newSideX[j] = newSideX[j]*(sideNorm);
        }


        return newSideX;
    }
    float[] sideYAfterRotation( float[][] rotationMatrix)
    {
        float[] newSideY = new float[3];
        float[] earth = new float[]{0,-1,0};

        for (int i = 0; i < 3; i++) {
            newSideY[i] = earth[1]* rotationMatrix[i][1]+ earth[1]* rotationMatrix[i][1]+earth[1]* rotationMatrix[i][1];
        }
        float sideNorm = (float) Math.sqrt(Math.pow(newSideY[0],2)+Math.pow(newSideY[1],2)+Math.pow(newSideY[2],2));
        for (int j = 0; j < 3; j++)
        {
            newSideY[j] = newSideY[j]*(sideNorm);
        }


        return newSideY;
    }

    float gravityNorm(float[] gravity)
    {
        float graNorm = (float) Math.sqrt(Math.pow(gravity[0],2)+ Math.pow(gravity[1],2)+ Math.pow(gravity[2],2));
        return graNorm;
    }

    float[] getGravity()
    {
        return gravity;
    }

    void setGravity(float[] newGravity)
    {
       this.gravity = newGravity;
    }

    float[] getGravityEarth()
    {
        float[] gravityEarth = new float[]{0,0,GRAVITY_EARTH};
        return gravityEarth;
    }

    float[] getGravityDifference(float[][] R, float[] gyr, float deltaT)
    {
        float[] gravityEarth = getGravityEarth();
        float[] gD = new float[3];
        gD[0] = R[0][0]*(-1)*gyr[1]* gravityEarth[2]*deltaT+R[0][1]*gyr[0]* gravityEarth[2]*deltaT;
        gD[1] = R[1][0]*(-1)*gyr[1]* gravityEarth[2]*deltaT+R[1][1]*gyr[0]* gravityEarth[2]*deltaT;
        gD[2] = R[2][0]*(-1)*gyr[1]* gravityEarth[2]*deltaT+R[2][1]*gyr[0]* gravityEarth[2]*deltaT;
        return gD;
    }

    void printGravity(float[] gravity)
    {
        System.out.print("gravity: ");
        for (int i = 0; i < 3; i++) {
            System.out.print(gravity[i]+" ");
        }
        System.out.println();

    }


}
