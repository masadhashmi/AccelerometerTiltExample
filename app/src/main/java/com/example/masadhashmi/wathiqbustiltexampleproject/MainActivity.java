package com.example.masadhashmi.wathiqbustiltexampleproject;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private TextView TextView_Message;

    private TextView TextView_X;
    private TextView TextView_Y;
    private TextView TextView_Z;
    private TextView TextView_NetForce;

    ServerSocket ServerSocket;
    int ServerSocketPort=4422;
    boolean StartListning=true;
boolean StartSending=true;

    float _x=0;
    float _y=0;
    float _z=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView_Message=(TextView)findViewById(R.id.TextView_Message);
        TextView_X=(TextView)findViewById(R.id.TextView_X);
        TextView_Y=(TextView)findViewById(R.id.TextView_Y);
        TextView_Z=(TextView)findViewById(R.id.TextView_Z);
        TextView_NetForce=(TextView)findViewById(R.id.TextView_NetForce);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder strLog = new StringBuilder();
        int iIndex = 1;
        for (Sensor item : sensors) {
            strLog.append(iIndex + ".");
            strLog.append(" Sensor Type - " + item.getType() + "\r\n");
            strLog.append(" Sensor Name - " + item.getName() + "\r\n");
            strLog.append(" Sensor Version - " + item.getVersion() + "\r\n");
            strLog.append(" Sensor Vendor - " + item.getVendor() + "\r\n");
            strLog.append(" Maximum Range - " + item.getMaximumRange() + "\r\n");
            strLog.append(" Minimum Delay - " + item.getMinDelay() + "\r\n");
            strLog.append(" Power - " + item.getPower() + "\r\n");
            strLog.append(" Resolution - " + item.getResolution() + "\r\n");
            strLog.append("\r\n");
            iIndex++;
        }
        System.out.println(strLog.toString());


        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        initListeners();

        initiateTCPServer();
    }

    private void initiateTCPServer() {

        try {
        ServerSocket = new ServerSocket(ServerSocketPort);
      Thread d=  new Thread(){

            @Override
            public void run()
            {
                while (StartListning) {

                    try {
                        StartSending=true;
                        Socket clientsocket= ServerSocket.accept();

                       // Toast.makeText(MainActivity.this, "Socket Accepted, Sending data", Toast.LENGTH_SHORT).show();

                        OutputStream os=clientsocket.getOutputStream();
                        InputStream is=clientsocket.getInputStream();

                        while (StartSending) {
                            if(clientsocket.isConnected() && !clientsocket.isClosed()) {


                                    //PrintWriter output = new PrintWriter(clientsocket.getOutputStream(), true); //Autoflush


                                    String datas = _x + "," + _y + "," + _z + "\r\n";

                                    byte[] data = datas.getBytes();
                                    os.write(data);
                                    os.flush();

                                    int k = is.read();


                              //  output.println(data);
                            }
                            else
                            {
                                StartSending=false;
                                Toast.makeText(MainActivity.this, "Client Disconnected. Listning again...", Toast.LENGTH_SHORT).show();
                                try{
                                    os.close();
                                    is.close();

                                }
                                catch (Exception er)
                                {


                                }


                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        d.start();



        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void initListeners()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        if(magnetometer!=null) {
            mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    @Override
    public void onResume()
    {
        initListeners();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    float[] inclineGravity = new float[3];
    float[] mGravity;
    float[] gravity=new  float[]{0.0f,0.0f,0.0f};
    float[] linear_acceleration=new float[]{0.0f,0.0f,0.0f};

    float[] mGeomagnetic;
    float orientation[] = new float[3];
    float pitch;
    float roll;

    public double giveTiltAnngle(double x, double y, double z)
    {
        double Gpx=x;//Math.abs(x);
        double Gpy=y;//Math.abs(y);
        double Gpz=z;//Math.abs(z);

        //Math.cos()
        return Gpz/Math.sqrt(Gpx*Gpx+Gpy*Gpy+Gpz*Gpz);

        //return  -1;

    }

    public double calculateNetforce(double x, double y, double z)
    {
        double force= Math.sqrt(((x*2)+(y*2)+(z*2)));
        return  force;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //If type is accelerometer only assign values to global property mGravity
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            mGravity = event.values;


            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            //----
            float x=mGravity[0];
            float y=mGravity[1];
            float z=mGravity[2];
            _x=x;
            _y=y;
            _z=z;

            String data=mGravity[0]+","+mGravity[1]+","+mGravity[2];

            String tiltangle=String.format("%.2f",  giveTiltAnngle(x,y,z));

            TextView_X.setText("X:  "+mGravity[0]+" , acc:"+linear_acceleration[0]);//+", Angel : "+tiltangle);
            TextView_Y.setText("Y:  "+mGravity[1]+" , acc:"+linear_acceleration[1]);
            TextView_Z.setText("Z:  "+mGravity[2]+" , acc:"+linear_acceleration[2]);

            TextView_NetForce.setText("Force: "+calculateNetforce(x,y,z)+"");//(x,y,z)+"");

            String tilting="Tilting From \r\n";

            boolean isexecuted=false;
            if(x>1)
            {
                if(x>3 && x<5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.YELLOW);
                    tilting+="Warning ";
                }
                if(x>5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.RED);
                    tilting+="Emergency ";
                }
                tilting+="right\r\n";
            }
            if(x<-1)
            {
                if(x<-3 && x>-5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.YELLOW);
                    tilting+="Warning ";
                }
                if(x<-5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.RED);
                    tilting+="Emergency ";
                }
                tilting+="left\r\n";
            }

            if(y>1)
            {
                if(y>3 && y<5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.YELLOW);
                    tilting+="Warning ";
                }
                if(y>5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.RED);
                    tilting+="Emergency ";
                }
                tilting+="Front\r\n";

            }
            if(y<-1)
            {
                if(y<-3 && y>-5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.YELLOW);
                    tilting+="Warning ";
                }
                if(y<-5)
                {
                    isexecuted=true;
                    TextView_Message.setTextColor(Color.RED);
                    tilting+="Emergency ";
                }
                tilting+="Back\r\n";
            }

            if(z<0)
            {
                isexecuted=true;
                TextView_Message.setTextColor(Color.RED);
                tilting +="Car crashed, Accident Happend.";
            }

            if(z<0)
            {
                isexecuted=true;
                TextView_Message.setTextColor(Color.RED);
                tilting+="Car crashed, Accident Happened.";
            }


            if(!isexecuted)
            {
                TextView_Message.setTextColor(Color.BLACK);
            }

            TextView_Message.setText(tilting);

            /*if (isTiltUpwardAccelometer())
            {
                Log.d("test", "downwards");
                TextView_Message.setText("downwards");
            }
            else
            {
               // TextView_Message.setText(data);

            }*/
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mGeomagnetic = event.values;

            if (isTiltDownward())
            {
                Log.d("test", "downwards");
                TextView_Message.setText("downwards");
            }
            else if (isTiltUpward())
            {
                Log.d("test", "upwards");
                TextView_Message.setText("upwards");
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    public boolean isTiltUpwardAccelometer()
    {
        if (mGravity != null )//&& mGeomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success =true;// SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success)
            {
                float orientation[] = mGravity;//new float[3];
                //SensorManager.getOrientation(R, orientation);

                /*
                * If the roll is positive, you're in reverse landscape (landscape right), and if the roll is negative you're in landscape (landscape left)
                *
                * Similarly, you can use the pitch to differentiate between portrait and reverse portrait.
                * If the pitch is positive, you're in reverse portrait, and if the pitch is negative you're in portrait.
                *
                * orientation -> azimut, pitch and roll
                *
                *
                */

                pitch = orientation[1];
                roll = orientation[2];

                inclineGravity = mGravity.clone();

                double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

                // Normalize the accelerometer vector
                inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
                inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
                inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

                //Checks if device is flat on ground or not
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

                /*
                * Float obj1 = new Float("10.2");
                * Float obj2 = new Float("10.20");
                * int retval = obj1.compareTo(obj2);
                *
                * if(retval > 0) {
                * System.out.println("obj1 is greater than obj2");
                * }
                * else if(retval < 0) {
                * System.out.println("obj1 is less than obj2");
                * }
                * else {
                * System.out.println("obj1 is equal to obj2");
                * }
                */
                Float objPitch = new Float(pitch);
                Float objZero = new Float(0.0);
                Float objZeroPointTwo = new Float(0.2);
                Float objZeroPointTwoNegative = new Float(-0.2);

                int objPitchZeroResult = objPitch.compareTo(objZero);
                int objPitchZeroPointTwoResult = objZeroPointTwo.compareTo(objPitch);
                int objPitchZeroPointTwoNegativeResult = objPitch.compareTo(objZeroPointTwoNegative);

                if (roll < 0 && ((objPitchZeroResult > 0 && objPitchZeroPointTwoResult > 0) || (objPitchZeroResult < 0 && objPitchZeroPointTwoNegativeResult > 0)) && (inclination > 30 && inclination < 40))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isTiltUpward()
    {
        if (mGravity != null )//&& mGeomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                /*
                * If the roll is positive, you're in reverse landscape (landscape right), and if the roll is negative you're in landscape (landscape left)
                *
                * Similarly, you can use the pitch to differentiate between portrait and reverse portrait.
                * If the pitch is positive, you're in reverse portrait, and if the pitch is negative you're in portrait.
                *
                * orientation -> azimut, pitch and roll
                *
                *
                */

                pitch = orientation[1];
                roll = orientation[2];

                inclineGravity = mGravity.clone();

                double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

                // Normalize the accelerometer vector
                inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
                inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
                inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

                //Checks if device is flat on ground or not
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

                /*
                * Float obj1 = new Float("10.2");
                * Float obj2 = new Float("10.20");
                * int retval = obj1.compareTo(obj2);
                *
                * if(retval > 0) {
                * System.out.println("obj1 is greater than obj2");
                * }
                * else if(retval < 0) {
                * System.out.println("obj1 is less than obj2");
                * }
                * else {
                * System.out.println("obj1 is equal to obj2");
                * }
                */
                Float objPitch = new Float(pitch);
                Float objZero = new Float(0.0);
                Float objZeroPointTwo = new Float(0.2);
                Float objZeroPointTwoNegative = new Float(-0.2);

                int objPitchZeroResult = objPitch.compareTo(objZero);
                int objPitchZeroPointTwoResult = objZeroPointTwo.compareTo(objPitch);
                int objPitchZeroPointTwoNegativeResult = objPitch.compareTo(objZeroPointTwoNegative);

                if (roll < 0 && ((objPitchZeroResult > 0 && objPitchZeroPointTwoResult > 0) || (objPitchZeroResult < 0 && objPitchZeroPointTwoNegativeResult > 0)) && (inclination > 30 && inclination < 40))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isTiltDownward()
    {
        if (mGravity != null && mGeomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                pitch = orientation[1];
                roll = orientation[2];

                inclineGravity = mGravity.clone();

                double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

                // Normalize the accelerometer vector
                inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
                inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
                inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

                //Checks if device is flat on groud or not
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

                Float objPitch = new Float(pitch);
                Float objZero = new Float(0.0);
                Float objZeroPointTwo = new Float(0.2);
                Float objZeroPointTwoNegative = new Float(-0.2);

                int objPitchZeroResult = objPitch.compareTo(objZero);
                int objPitchZeroPointTwoResult = objZeroPointTwo.compareTo(objPitch);
                int objPitchZeroPointTwoNegativeResult = objPitch.compareTo(objZeroPointTwoNegative);

                if (roll < 0 && ((objPitchZeroResult > 0 && objPitchZeroPointTwoResult > 0) || (objPitchZeroResult < 0 && objPitchZeroPointTwoNegativeResult > 0)) && (inclination > 140 && inclination < 170))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }
}