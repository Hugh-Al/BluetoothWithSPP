package com.example.huy_a.bluetoothwithspp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {
    BluetoothSPP bt;
    String TAG = "BluetoothArduinoApp";
    boolean status = false;
    TextView dataRecording;
    ArrayList<String> dataLog; //Once record is pressed, all previous elements are cleared.
    ArrayList<String> allData;
    ArrayAdapter<String> adapter;
    Thread t;
    static Boolean recording = false;
    Boolean power = false;
    ListView listView;
    ImageView ledPowerStatus;
    LineChart mChart;
    LineChart lineChart2;
    ArrayList<Entry> tempArray = new ArrayList<Entry>();
    LineData tempData;
    int num = 0;

    public LineData data;
    public LineData blueData;

    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledPowerStatus = (ImageView) findViewById(R.id.ledPowerStatus);
        ledPowerStatus.setImageResource(R.drawable.offled);

//        lineChart2 = (LineChart) findViewById(R.id.lineChart2);


        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setAxisMaximum(3000f);
//        leftAxis.setAxisMinimum(2800f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);






        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                dataRecording.setText(message);
                if(recording){
                    addEntry((float) Float.parseFloat(message));
                    dataLog.add(message);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED)
                Log.d(TAG, "Connected");
                else if(state == BluetoothState.STATE_CONNECTING)
                    Log.d(TAG, "Connecting");
                else if(state == BluetoothState.STATE_LISTEN)
                    Log.d(TAG, "Listening");
                else if(state == BluetoothState.STATE_NONE)
                    Log.d(TAG, "None");
            }
        });
        Button btnConnect = (Button) findViewById(R.id.button);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
        Button powerButton = (Button) findViewById(R.id.button3);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!power){
                    power = true;
                    ledPowerStatus.setImageResource(R.drawable.redled);
                    bt.send("1", true);
                } else {
                    power = false;
                    ledPowerStatus.setImageResource(R.drawable.offled);
                    bt.send("0", true);
                }
            }
        });
        Button plotButton = (Button) findViewById(R.id.plotButton);
        plotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Status of recording is: " + recording, Toast.LENGTH_SHORT).show();
//                blueData = lineChart2.getData();
//                if (blueData != null) {
//
//                    ILineDataSet set = blueData.getDataSetByIndex(0);
//                    // set.addEntry(...); // can be called as well
//
//                    if (set == null) {
//                        set = createSet();
//                        blueData.addDataSet(set);
//                    }
//                    for(int i = 0; i < dataLog.size(); i++){
//                        Entry newEntry = new Entry(i, (float) Float.parseFloat(dataLog.get(i)));
//                        blueData.addEntry(newEntry, 0);
//                    }
////                    blueData.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
//                    blueData.notifyDataChanged();
//                    lineChart2.notifyDataSetChanged();
//                    lineChart2.setVisibleXRangeMaximum(120);
//                    lineChart2.invalidate();
//                }
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        dataRecording = (TextView) findViewById(R.id.dataRecording);
        dataLog = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, dataLog);
        listView.setAdapter(adapter);
    }


    public void recordData(View view){
        // This is only simulation of turning on for 3 seconds, turn off for 1 second, turn on for 3 seconds
        // need to send signal to turn on and off the lights whilst recording is always on going
        if(!recording){
            recording = true;
            Toast.makeText(this, "Now recording: " + recording, Toast.LENGTH_SHORT).show();

            dataLog.clear();
            adapter.notifyDataSetChanged();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recording = false;
                    Toast.makeText(MainActivity.this, "3 seconds after, status is now " + recording, Toast.LENGTH_SHORT).show();
                }
            }, 3000);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recording = true;
                    Toast.makeText(MainActivity.this, "4 seconds after, resume " + recording, Toast.LENGTH_SHORT).show();
                }
            }, 4000);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recording = false;
                    Toast.makeText(MainActivity.this, "7 seconds after, stop " + recording, Toast.LENGTH_SHORT).show();
                }
            }, 7000);


//            recording = false;
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = true;
//                }
//            }, 2000);
//            Toast.makeText(this, "Wait for one second" + recording, Toast.LENGTH_SHORT).show();
//            recording = true;
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = false;
//                }
//            }, 3000);
//            Toast.makeText(this, "Recording for 3 seconds" + recording, Toast.LENGTH_SHORT).show();
//            recording = false;
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = false;
//                }
//            }, 3000);
//            dataLog.clear();
//            adapter.notifyDataSetChanged();
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = true;
//                }
//            }, 2000);
//            dataLog.clear();
//            adapter.notifyDataSetChanged();
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = false;
//                }
//            }, 3000);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = false;
//                }
//            }, 3000);
//            Toast.makeText(this, "Recording for 3 seconds and then delay for one second", Toast.LENGTH_SHORT).show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = true;
//                }
//            }, 10000);
//            Toast.makeText(this, "Resume recording for 3 seconds", Toast.LENGTH_SHORT).show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    recording = false;
//                }
//            }, 3000);
//            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Stop recording", Toast.LENGTH_SHORT).show();
            recording = false;
        }
    }

    private void addEntry(float input) {

//        LineData data = mChart.getData();
        data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) input), 0);
            data.notifyDataChanged();
//            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//                public void onDataReceived(byte[] data, String message) {
//                    dataRecording.setText(message);
//                    if(recording){
//                        dataLog.add(message);
//                        adapter.notifyDataSetChanged();
//                    }
//                }
//            });

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }


    private void addEntry() {

//        LineData data = mChart.getData();
        data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.notifyDataChanged();
//            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//                public void onDataReceived(byte[] data, String message) {
//                    dataRecording.setText(message);
//                    if(recording){
//                        dataLog.add(message);
//                        adapter.notifyDataSetChanged();
//                    }
//                }
//            });

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setDrawCircles(false);
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
//        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
//        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
//        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                //TODO a 7 second timer loop here,
                // TODO Turn on 810nm, loop 3 seconds and record. Pause one second
                // TODO turn off 810nm and turn on 1300nm.
                //TODO loop for another 3 seconds
                Long firstStop = System.currentTimeMillis() + 3000;
                while(System.currentTimeMillis() < firstStop) {
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                bt.send("0", true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                Long secondStop = System.currentTimeMillis() + 3000;
                while(System.currentTimeMillis() < secondStop) {
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
//                for (int i = 0; i < 50; i++) {
//
//                    // Don't generate garbage runnables inside the loop.
//                    runOnUiThread(runnable);
//
//                    try {
//                        Thread.sleep(25);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
            }
        });

        thread.start();
    }
//
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "resultCode: " + resultCode);


        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
//                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            Log.d(TAG, "Starting bluetooth");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                //setup(); // Do I need this?
            }
        }
    }

//    public void setup() {
//        Button btnSend = (Button)findViewById(R.id.button);
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                bt.send("Inside setup function", true);
//            }
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAdd: {
                addEntry();
                Toast.makeText(this, "Add one!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionClear: {
                mChart.clearValues();
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                Toast.makeText(this, "Add realtime!", Toast.LENGTH_SHORT).show();
                feedMultiple();
                break;
            }
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }
}
