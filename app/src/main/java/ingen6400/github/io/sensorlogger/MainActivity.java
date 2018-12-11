package ingen6400.github.io.sensorlogger;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView accelX_text;
    private TextView accelY_text;
    private TextView accelZ_text;
    private TextView gyroX_text;
    private TextView gyroY_text;
    private TextView gyroZ_text;
    private TextView accel_info_text;
    private TextView gyro_info_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        accelX_text = findViewById(R.id.accelX);
        accelY_text = findViewById(R.id.accelY);
        accelZ_text = findViewById(R.id.accelZ);
        gyroX_text = findViewById(R.id.gyroX);
        gyroY_text = findViewById(R.id.gyroY);
        gyroZ_text = findViewById(R.id.gyroZ);

        accel_info_text = findViewById(R.id.accel_info);
        gyro_info_text = findViewById(R.id.gyro_info);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float sensorX, sensorY, sensorZ;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            accelX_text.setText("X:"+sensorX);
            accelY_text.setText("Y:"+sensorY);
            accelZ_text.setText("Z:"+sensorZ);

            showInfo(event, accel_info_text);
        }else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            gyroX_text.setText("X:"+sensorX);
            gyroY_text.setText("Y:"+sensorY);
            gyroZ_text.setText("Z:"+sensorZ);

           showInfo(event, gyro_info_text);
        }
    }

    private void showInfo(SensorEvent event, TextView textView){
        // センサー名
        StringBuffer info = new StringBuffer("Name: ");
        info.append(event.sensor.getName());
        info.append("\n");

        // ベンダー名
        info.append("Vendor: ");
        info.append(event.sensor.getVendor());
        info.append("\n");

        // 型番
        info.append("Type: ");
        info.append(event.sensor.getType());
        info.append("\n");

        // 最小遅れ
        int data = event.sensor.getMinDelay();
        info.append("Mindelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // 最大遅れ
        data = event.sensor.getMaxDelay();
        info.append("Maxdelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // レポートモード
        data = event.sensor.getReportingMode();
        String stinfo = "unknown";
        if(data == 0){
            stinfo = "REPORTING_MODE_CONTINUOUS";
        }else if(data == 1){
            stinfo = "REPORTING_MODE_ON_CHANGE";
        }else if(data == 2){
            stinfo = "REPORTING_MODE_ONE_SHOT";
        }
        info.append("ReportingMode: ");
        info.append(stinfo);
        info.append("\n");

        // 最大レンジ
        info.append("MaxRange: ");
        float fData = event.sensor.getMaximumRange();
        info.append(String.valueOf(fData));
        info.append("\n");

        // 分解能
        info.append("Resolution: ");
        fData = event.sensor.getResolution();
        info.append(String.valueOf(fData));
        info.append(" m/s^2\n");

        // 消費電流
        info.append("Power: ");
        fData = event.sensor.getPower();
        info.append(String.valueOf(fData));
        info.append(" mA\n");

        textView.setText(info);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
