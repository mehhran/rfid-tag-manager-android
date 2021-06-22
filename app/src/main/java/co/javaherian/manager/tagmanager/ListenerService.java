package co.javaherian.manager.tagmanager;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenerService extends Service {

    private static final String TAG = "ListenerService";

    SocketThread socketThread;
    static ServerSocket serverSocket;
    public static final int SERVER_PORT = 11850;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        socketThread = new SocketThread();
        socketThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        /*
         Your service should implement this to
         clean up any resources such as threads, registered listeners, or receivers.
        */
        socketThread.cancel();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    static class SocketThread extends Thread {

        boolean stopThread = false;
        private BufferedReader input;
        Context ctx;
        int antenna = 255;

        public void run(){
            while(!stopThread)
            {
                Socket socket;
                try {
                    serverSocket = new ServerSocket(SERVER_PORT);
                    Log.d(TAG, "ServerSocket instantiated");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    socket = serverSocket.accept();
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (input.readLine().equals("esmeshab")) {
                        try {
                            // schedule a read job
                            ComponentName componentName = new ComponentName(ctx.getApplicationContext(), ReaderJobService.class);
                            PersistableBundle bundle = new PersistableBundle();
                            bundle.putInt("antenna", antenna);
                            JobInfo info = new JobInfo.Builder(123, componentName)
                                    .setPersisted(true)
                                    .setOverrideDeadline(0) /* just for this: java.lang.IllegalArgumentException: You're trying to build a job with no constraints, this is not allowed. */
                                    .setExtras(bundle)
                                    .build();
                            JobScheduler scheduler = (JobScheduler) ctx.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
                            int resultCode = scheduler.schedule(info);
                            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                                Log.d(TAG, "Job scheduled");
                            } else {
                                Log.d(TAG, "Job scheduling failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            Log.d(TAG, "after a 'while'");
        }

        public void cancel(){
            stopThread = true;
        }
    }
}
