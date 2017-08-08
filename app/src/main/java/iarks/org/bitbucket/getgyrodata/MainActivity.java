package iarks.org.bitbucket.getgyrodata;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.text.DecimalFormat;


public class MainActivity extends Activity
{
    TextView textX, textY, textZ;
    EditText port,ip;
    Button button;
    SensorManager sensorManager;
    Sensor sensorGyro;
    UDPClient udp;

    private final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    public static final float EPSILON = 0.000000001f;


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        textX = (TextView) findViewById(R.id.textx);
        textY = (TextView) findViewById(R.id.texty);
        textZ = (TextView) findViewById(R.id.textz);
        port = (EditText)findViewById(R.id.port);
        ip = (EditText)findViewById(R.id.ip);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
//                udp = new UDPClient(49443,"192.168.1.39",activity);
//                udp = new UDPClient(Integer.parseInt(port.getText().toString()),ip.getText().toString(), activity);
                Log.d("port",port.getText().toString());
                Log.d("ip",ip.getText().toString());
                udp = new UDPClient(49443,ip.getText().toString());
                sensorManager.registerListener(gyroListener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });
    }

    public void onResume()
    {
        super.onResume();
//        sensorManager.registerListener(gyroListener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onStop()
    {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }

    public SensorEventListener gyroListener = new SensorEventListener()
    {
        public void onAccuracyChanged(Sensor sensor, int acc)
        {
        }

        public void onSensorChanged(SensorEvent event)
        {
            float directionX=0.0f;
            float directionY=0.0f;
            String deltas;

            float[] deltaOrientation = new float[9];
            deltaOrientation[0]=1;
            deltaOrientation[4]=1;
            deltaOrientation[8]=1;
            DecimalFormat df = new DecimalFormat("0.00");

            double x,y,z;

            if (timestamp != 0)
            {
                final float dT = (event.timestamp - timestamp) * NS2S;

                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];
                directionX = axisZ;
                directionY = axisX;
                textZ.setText("Z : " + df.format(directionX) + " rad/s");


                // Calculate the angular speed of the sample
                float omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            // rotationCurrent = rotationCurrent * deltaRotationMatrix;


            SensorManager.getOrientation(deltaRotationMatrix,deltaOrientation);

            z=(deltaOrientation[0]);
            x=(deltaOrientation[1]);
            y=(deltaOrientation[2]);

            z=(Math.toDegrees(z));
            x=(Math.toDegrees(x));
            y=(Math.toDegrees(y));
            deltas = "{\"X\":" + "\""+df.format(z)+"\"," + "\"Y\":\"" + df.format(x) +"\"}" + "\0";

            textY.setText("Y : " + x + " rad/s");

            textX.setText(deltas);

            udp.setData(deltas);
            Thread th = new Thread(udp);
            th.start();
        }
    };
}