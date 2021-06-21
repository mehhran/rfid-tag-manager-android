package co.javaherian.manager.tagmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    int baud = 57600;
    TextView connect_result_view;
    int result = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_result_view  = findViewById(R.id.connect_result);

        TextView mConnectButton = findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(v -> {
            try {
                new Thread(() -> {
                    result = Reader.rrlib.Connect("/dev/ttyMT0", baud);
                    if (result == 0) {
                        connect_result_view.post(() -> connect_result_view.setText(String.valueOf(result)));
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                getString(R.string.openport_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }).start();
            } catch (Exception e)
            {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.openport_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });


        TextView mScanButton = findViewById(R.id.startScan);
        mScanButton.setOnClickListener(v -> {

            EditText ant_no_view = findViewById(R.id.antenna_number);
            String temp = ant_no_view.getText().toString();
            int ant_no = 0;
            if (!"".equals(temp)){
                ant_no=Integer.parseInt(temp);
            }

            try {
                // schedule a read job
                ComponentName componentName = new ComponentName(MainActivity.this, ReaderJobService.class);
                PersistableBundle bundle = new PersistableBundle();
                bundle.putInt("antenna", ant_no);
                JobInfo info = new JobInfo.Builder(123, componentName)
                        .setPersisted(true)
                        .setOverrideDeadline(0) /* just for this: java.lang.IllegalArgumentException: You're trying to build a job with no constraints, this is not allowed. */
                        .setExtras(bundle)
                        .build();
                JobScheduler scheduler = (JobScheduler) MainActivity.this.getSystemService(JOB_SCHEDULER_SERVICE);
                int resultCode = scheduler.schedule(info);
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG, "Job scheduled");
                } else {
                    Log.d(TAG, "Job scheduling failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void startService(View v) {
        startService(new Intent(getBaseContext(), ListenerService.class));
    }

    public void stopService(View v) {
        stopService(new Intent(getBaseContext(), ListenerService.class));
    }
}