package com.example.tagmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rfid.trans.ReadTag;
import com.rfid.trans.TagCallback;
import com.rfid.trans.UHFLib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    int baud = 57600;
    TextView connect_result_view;
    int result = 88;
    private Handler mHandler;

    public static class InventoryTagMap  {
        public String strEPC;
        public int antenna;
        public String strRSSI;
        public int nReadCount;
    }

    public static List<InventoryTagMap> lsTagList = new ArrayList<InventoryTagMap>();

    public Map<String, Integer> dtIndexMap =new LinkedHashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_result_view  = findViewById(R.id.connect_result);

        TextView mConnectButton = (TextView) findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {

                    new Thread(new Runnable() {
                        public void run() {

                            result = Reader.rrlib.Connect("/dev/ttyMT0", baud);

                            if (result == 0) {
                                connect_result_view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect_result_view.setText(String.valueOf(result));
                                    }
                                });

                                // start reading
                                lsTagList = new ArrayList<InventoryTagMap>();
                                dtIndexMap =new LinkedHashMap<String, Integer>();
                                MsgCallback callback = new MsgCallback();
                                Reader.rrlib.SetCallBack(callback);
                                Reader.rrlib.StartRead();

                                System.out.print("Its reading...");

                                Reader.rrlib.StopRead();


                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.openport_failed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).start();
                } catch (Exception e)
                {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.openport_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    public class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag readTag) {
            String epc = readTag.epcId.toUpperCase();
            InventoryTagMap m;
            Integer findIndex = dtIndexMap.get(epc);
            if (findIndex == null) {
                dtIndexMap.put(epc,dtIndexMap.size());
                m = new InventoryTagMap();
                m.strEPC = epc;
                m.antenna = readTag.antId;
                m.strRSSI = String.valueOf(readTag.rssi);
                m.nReadCount =1;
                //dtIndexMap
                lsTagList.add(m);
            }
            else
            {
                m= lsTagList.get(findIndex);
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