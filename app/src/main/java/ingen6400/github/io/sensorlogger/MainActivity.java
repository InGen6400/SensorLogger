package ingen6400.github.io.sensorlogger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;
    private VideoRecorder v_recorder;
    private final int REQUEST_PERMISSION = 1000;

    private TextView accel_delta_text;
    private TextView accelX_text;
    private TextView accelY_text;
    private TextView accelZ_text;

    private TextView gyro_delta_text;
    private TextView gyroX_text;
    private TextView gyroY_text;
    private TextView gyroZ_text;
    private TextView accel_info_text;
    private TextView gyro_info_text;
    private long accel_start_milli;
    private long accel_prev_milli;
    private long gyro_start_milli;
    private long gyro_prev_milli;

    private boolean isRecording = false;
    private String file_path;
    private String file_name = "sensor_%s.csv";
    private String saved_file;
    private File csv_file;
    private DateFormat csv_date_format;
    private BufferedWriter out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        csv_date_format = new SimpleDateFormat("yyyy/MM/dd,HH,mm,ss.SSSS", Locale.JAPAN);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        accel_delta_text = findViewById(R.id.accel_delta);
        accelX_text = findViewById(R.id.accelX);
        accelY_text = findViewById(R.id.accelY);
        accelZ_text = findViewById(R.id.accelZ);

        gyro_delta_text = findViewById(R.id.gyro_delta);
        gyroX_text = findViewById(R.id.gyroX);
        gyroY_text = findViewById(R.id.gyroY);
        gyroZ_text = findViewById(R.id.gyroZ);

        accel_info_text = findViewById(R.id.accel_info);
        gyro_info_text = findViewById(R.id.gyro_info);

        file_path = Environment.getExternalStorageDirectory().getPath() + File.separator +
                "Documents"+File.separator + "SensorLogger" + File.separator;

        v_recorder = new VideoRecorder((SurfaceView) findViewById(R.id.surfaceView), file_path, this);
        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
        }


        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.JAPAN);
        final Date date = new Date(System.currentTimeMillis());

        saved_file = String.format(file_name, df.format(date));
        csv_file = new File(file_path + saved_file);
        File path = new File(file_path);
        if(!path.exists()){
            if(!path.mkdirs()){
                Log.e("SensorLogger", "ファイルが作成できませんでした. exit");
                finishAndRemoveTask();
            }
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        accel_start_milli = System.currentTimeMillis();
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        gyro_start_milli = System.currentTimeMillis();
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float sensorX, sensorY, sensorZ;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            accel_delta_text.setText("delta: "+ String.valueOf(System.currentTimeMillis() - accel_prev_milli));
            accelX_text.setText("X:"+sensorX);
            accelY_text.setText("Y:"+sensorY);
            accelZ_text.setText("Z:"+sensorZ);

            if(isRecording) {
                try {
                    out.write(("Accel," + csv_date_format.format(new Date()) + ","
                            + String.valueOf(sensorX) + ","
                            + String.valueOf(sensorY) + ","
                            + String.valueOf(sensorZ) + "\n"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            showInfo(event, accel_info_text);
            accel_prev_milli = System.currentTimeMillis();
        }else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            gyro_delta_text.setText("delta: "+ String.valueOf(System.currentTimeMillis() - gyro_prev_milli));
            gyroX_text.setText("X:"+sensorX);
            gyroY_text.setText("Y:"+sensorY);
            gyroZ_text.setText("Z:"+sensorZ);

            if(isRecording) {
                try {
                    out.write(("Gyro," + csv_date_format.format(new Date()) + ","
                            + String.valueOf(sensorX) + ","
                            + String.valueOf(sensorY) + ","
                            + String.valueOf(sensorZ) + "\n"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           showInfo(event, gyro_info_text);
           gyro_prev_milli = System.currentTimeMillis();
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

    public String readFile()
    {
        String str = null;
        if(isExternalStorageReadable()) {
            try (FileInputStream fileInputStream =
                         new FileInputStream(file_path + saved_file);
                 InputStreamReader inputStreamReader =
                         new InputStreamReader(fileInputStream, "UTF8");
                 BufferedReader reader =
                         new BufferedReader(inputStreamReader);) {

                String lineBuffer;

                while ((lineBuffer = reader.readLine()) != null) {
                    str = lineBuffer;
                }

            } catch (Exception e) {
                str = "error: FileInputStream";
                e.printStackTrace();
            }
        }
        return str;
    }

    @Override
    public void onClick(View v) {
        if(isRecording){
            isRecording = false;
            try {
                out.close();
                v_recorder.Stop();
                MediaScannerConnection.scanFile(MainActivity.this, new String[]{file_path+saved_file}, null, null);
                Log.d("TEST", file_path + saved_file +": \n"+readFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            if(checkPermission()) {
                if (isExternalStorageWritable()) {
                    try {
                        FileOutputStream fileOutputStream =
                                new FileOutputStream(csv_file, true);
                        OutputStreamWriter outputStreamWriter =
                                new OutputStreamWriter(fileOutputStream, "UTF-8");
                        out = new BufferedWriter(outputStreamWriter);
                        out.write("sensor,date,hour,min,sec,x,y,z\n");
                        v_recorder.Start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isRecording = true;
                } else {
                    Log.e("SENSOR Recorder", "Storage is not available");
                }
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    // permissionの確認
    public boolean checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            return true;
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
            return false;
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast =
                    Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // それでも拒否された時の対応
                Toast toast =
                        Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
