package co.javaherian.manager.tagmanager;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresPermission;

import com.rfid.trans.ReadTag;
import com.rfid.trans.ReaderParameter;
import com.rfid.trans.TagCallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class ReaderJobService extends JobService {

    private static final String TAG = "SmsPostJobService";

    int baud = 57600;
    byte antenna = (byte) 255;
    int power = 30; // 0 ~ 30 dBm
    int fband = 2;  // US Band
    int minFre = 902;
    int maxFre = 928;
    int beepEn = 0;

    public static class InventoryTagMap {
        public String strEPC;
        public int antenna;
        public String strRSSI;
        public int nReadCount;
    }

    public static List<InventoryTagMap> lsTagList = new ArrayList<>();
    public Map<String, Integer> dtIndexMap = new LinkedHashMap<>();

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;

    }

    private void doBackgroundWork(JobParameters params) {

        new Thread(new Runnable(){

            @Override
            public void run() {

                try {
                    // connect
                    Reader.rrlib.Connect("/dev/ttyMT0", baud);

                    // preparing
                    MsgCallback callback = new MsgCallback();

                    antenna = (byte) params.getExtras().getInt("antenna");

                    Reader.rrlib.SetCallBack(callback);
                    Reader.rrlib.SetAntenna(antenna);
                    Reader.rrlib.SetRfPower(power);
                    Reader.rrlib.SetRegion(fband, minFre, maxFre);
                    Reader.rrlib.SetBeepNotification(beepEn);

                    // public static final int PRIORITY_SYNC_EXPEDITED = 10;

                    ReaderParameter myParam = Reader.rrlib.GetInventoryPatameter();
                    myParam.Antenna = antenna;
                    myParam.ScanTime = 10000;
                    Reader.rrlib.SetInventoryPatameter(myParam);

                    // startread
                    Reader.rrlib.StartRead();

                    // continue reading for 10 seconds
                    Log.d(TAG, "Reading...");
                    TimeUnit.SECONDS.sleep(10);

                    // stopread
                    Reader.rrlib.StopRead();

                    // disconnect
                    Reader.rrlib.DisConnect();

                } catch (Exception e) {
                    Log.d(TAG, "Reading Failed");
                }


                // lsTagList is ready

                // save lsTagList in local database

                // Post lsTagList to the server - schedule a JobInfo with PosterJobService

                Log.d(TAG, "Job finished");
            }
        }).start();
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag readTag) {
            String epc = readTag.epcId.toUpperCase();
            InventoryTagMap m;
            Integer findIndex = dtIndexMap.get(epc);
            if (findIndex == null) {
                dtIndexMap.put(epc, dtIndexMap.size());
                m = new InventoryTagMap();
                m.strEPC = epc;
                m.antenna = readTag.antId;
                m.strRSSI = String.valueOf(readTag.rssi);
                m.nReadCount = 1;
                //dtIndexMap
                lsTagList.add(m);
            } else {
                m = lsTagList.get(findIndex);
                m.antenna |= readTag.antId;
                m.nReadCount++;
                m.strRSSI = String.valueOf(readTag.rssi);
            }
        }

        @Override
        public int tagCallbackFailed(int i) {
            return 0;
        }
    }
}