package co.javaherian.manager.tagmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class ListenerService extends Service {

    private static final String TAG = "ListenerService";

    SocketThread socketThread;

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

        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    static class SocketThread extends Thread {

        public void run(){
            while(true)
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //REST OF CODE HERE//
                Log.d(TAG, "5 seconds passed");
            }
        }
    }
}
