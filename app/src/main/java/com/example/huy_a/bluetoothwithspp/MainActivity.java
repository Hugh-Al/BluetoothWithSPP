package com.example.huy_a.bluetoothwithspp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    boolean powerStatus = false;
    TextView dataRecording;

    ArrayList<String> dataLog; //Once record is pressed, all previous elements are cleared.
    ArrayList<String> allData;
    ArrayAdapter<String> adapter;
    Thread t;
    volatile boolean recording = false;
    boolean power = false;
    
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

    boolean recordBlank = false;
    boolean recordBlank2 = false;

    boolean record810Data = false;
    boolean record1300Data = false;
    ArrayList<Pair<Integer, Float>> dataSet810;
    ArrayList<Pair<Integer, Float>> dataSet1300;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledPowerStatus = (ImageView) findViewById(R.id.ledPowerStatus);
        ledPowerStatus.setImageResource(R.drawable.offled);

        dataSet810 = new ArrayList<Pair<Integer, Float>>();
        dataSet1300 = new ArrayList<Pair<Integer, Float>>();

        listView = (ListView) findViewById(R.id.listView);
//        dataRecording = (TextView) findViewById(R.id.dataRecording);
        dataLog = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, dataLog);
        listView.setAdapter(adapter);


        mChart = (LineChart) findViewById(R.id.chart);
        setupChart();
//        mChart.setOnChartValueSelectedListener(this);
//
//        // enable description text
//        mChart.getDescription().setEnabled(true);
//
//        // enable touch gestures
//        mChart.setTouchEnabled(true);
//
//        // enable scaling and dragging
//        mChart.setDragEnabled(true);
//        mChart.setScaleEnabled(true);
//        mChart.setDrawGridBackground(false);
//
//        // if disabled, scaling can be done on x- and y-axis separately
//        mChart.setPinchZoom(false);
//
//        // set an alternative background color
//        mChart.setBackgroundColor(Color.BLACK);
//
//        LineData data = new LineData();
//        data.setValueTextColor(Color.WHITE);
//
//        // add empty data
//        mChart.setData(data);
//
//        // get the legend (only possible after setting data)
//        Legend l = mChart.getLegend();
//
//        // modify the legend ...
//        l.setForm(LegendForm.LINE);
//        l.setTextColor(Color.WHITE);
//
//        XAxis xl = mChart.getXAxis();
//        xl.setTextColor(Color.WHITE);
//        xl.setDrawGridLines(true);
//        xl.setAvoidFirstLastClipping(true);
//        xl.setEnabled(true);
//
//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTextColor(Color.WHITE);
////        leftAxis.setAxisMaximum(3000f);
////        leftAxis.setAxisMinimum(2800f);
//        leftAxis.setDrawGridLines(true);
//
//        YAxis rightAxis = mChart.getAxisRight();
//        rightAxis.setEnabled(false);






        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
//                dataRecording.setText(message);
                if(recording){
                    addEntry((float) Float.parseFloat(message));
                    dataLog.add(message);
                    adapter.notifyDataSetChanged();
                } else{
                    if(power) {
                        addEntry((float) Float.parseFloat(message));
                    }

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
//        Button btnConnect = (Button) findViewById(R.id.button);
//        btnConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
//                    bt.disconnect();
//                } else {
//                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
//                }
//            }
//        });
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
//                    data = mChart.getData();
//                    if (data != null) {
//                        data.clearValues();
//                        data.notifyDataChanged();
//                        mChart.notifyDataSetChanged();
//                        mChart.invalidate();
//                    }
                    if(mChart.getData() == null){

                    } else{
                        mChart.clearValues();
                        mChart.invalidate();
//                        data = mChart.getData();
//                        data.clearValues();
//                        data.notifyDataChanged();
//                        mChart.notifyDataSetChanged();
//                        mChart.invalidate();
                    }
                    bt.send("0", true);
                }
            }
        });
        Button plotButton = (Button) findViewById(R.id.plotButton);
        plotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Status of recording is: " + recording, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Size of 810 array is: " + dataSet810.size() +
                        " size of 1300 array is: " + dataSet1300.size(), Toast.LENGTH_SHORT).show();
                logData();
            }
        });
    }

    private void logData() {
        File dir = new File(Environment.getExternalStorageDirectory(), "/CSIOLOGGER2");
        boolean dirValidity = false;
        if(!dir.exists()){
            dirValidity = dir.mkdir();
            if (dirValidity) {
                Toast.makeText(this, "Directory was created", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Not created", Toast.LENGTH_SHORT).show();
            }
        } else{ //Directory already created create new files

        }

//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//                Toast.makeText(this, "Did it work?", Toast.LENGTH_SHORT).show();
//
//            } catch (IOException e) {
//                if (file.exists()) try {
//                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//                    writer.write("Example");
//                    writer.write("\n");
//                    writer.write("Text");
//                    writer.write("\n");
//                    writer.flush();
//                    writer.close();
//                    Toast.makeText(this, "Did it work?", Toast.LENGTH_SHORT).show();
//                } catch (IOException e1) {
//                }
//            }
//        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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


    public void recordData(View view){
        if(!power){
            Toast.makeText(this, "Please turn on power", Toast.LENGTH_SHORT).show();
        } else {
            dataSet810.clear();
            dataSet1300.clear();
//            data = mChart.getData();
//            if (data != null) {
//                data.clearValues();
//                data.notifyDataChanged();
//                mChart.notifyDataSetChanged();
//                mChart.invalidate();
//            }
            if(mChart.getData() == null){

            } else{
                mChart.clearValues();
                mChart.invalidate();
//                data = mChart.getData();
//                data.clearValues();
//                data.notifyDataChanged();
//                mChart.notifyDataSetChanged();
//                mChart.invalidate();
            }

            if (!recording) {
                recording = true;
                record810Data = true;
                bt.send("1", true);
                dataLog.clear();
                adapter.notifyDataSetChanged();
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        recording = false;
                        recordBlank = true;
                        bt.send("0", true);
                        Toast.makeText(MainActivity.this, "Turn off 810nm now " + recording, Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recordBlank = false;
                        record810Data = false;
                        record1300Data = true;
                        bt.send("2", true);
                        Toast.makeText(MainActivity.this, "Turn on 1300nm now " + recording, Toast.LENGTH_SHORT).show();
                    }
                }, 5000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recordBlank2 = true;
                        bt.send("0", true);
                        Toast.makeText(MainActivity.this, "Turn off 1300nm " + recording, Toast.LENGTH_SHORT).show();
                    }
                }, 8000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recording = false;
                        record1300Data = false;
                        recordBlank2 = false;
                        bt.send("1", true);
                        Toast.makeText(MainActivity.this, "Turn on 810nm but recording off" + recording, Toast.LENGTH_SHORT).show();
                    }
                }, 10000);

            } else {
                Toast.makeText(this, "Stop recording", Toast.LENGTH_SHORT).show();
                recording = false;
            }
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
            //TODO Add this x value as time??? so we can distinguish which sections?
            if(recordBlank){
                data.addEntry(new Entry(set.getEntryCount(), (float) Math.sin((double)Math.PI/2*3)-1), 0);

            } else if(recordBlank2){
                data.addEntry(new Entry(set.getEntryCount(), (float) Math.sin((double)Math.PI/2*3)-1), 0);

            } else{
                float value = (float) (Math.sin((double)set.getEntryCount()) + Math.random()/10);
                data.addEntry(new Entry(set.getEntryCount(), value), 0);
            }
            if(record810Data){
                dataSet810.add(new Pair(dataSet810.size(), (float) (Math.sin((double)set.getEntryCount()) + Math.random()/10)));
            }
            if(record1300Data){
                dataSet1300.add(new Pair(dataSet1300.size(), (float) (Math.sin((double)set.getEntryCount()) + Math.random()/10)));
            }

//            data.addEntry(new Entry(set.getEntryCount(), (float) input), 0);
            data.notifyDataChanged();

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

//    //Thiz is for fake data
//    private void addEntry() {
//
////        LineData data = mChart.getData();
//        data = mChart.getData();
//
//        if (data != null) {
//
//            ILineDataSet set = data.getDataSetByIndex(0);
//            // set.addEntry(...); // can be called as well
//
//            if (set == null) {
//                set = createSet();
//                data.addDataSet(set);
//            }
//
//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
//            data.notifyDataChanged();
////            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
////                public void onDataReceived(byte[] data, String message) {
////                    dataRecording.setText(message);
////                    if(recording){
////                        dataLog.add(message);
////                        adapter.notifyDataSetChanged();
////                    }
////                }
////            });
//
//            // let the chart know it's data has changed
//            mChart.notifyDataSetChanged();
//
//            // limit the number of visible entries
//            mChart.setVisibleXRangeMaximum(120);
//            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
//
//            // move to the latest entry
//            mChart.moveViewToX(data.getEntryCount());
//
//            // this automatically refreshes the chart (calls invalidate())
//            // mChart.moveViewTo(data.getXValCount()-7, 55f,
//            // AxisDependency.LEFT);
//        }
//    }

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

//    private Thread thread;
//
//    private void feedMultiple() {
//
//        if (thread != null)
//            thread.interrupt();
//
//        final Runnable runnable = new Runnable() {
//
//            @Override
//            public void run() {
//                addEntry();
//            }
//        };
//
//        thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                //TODO a 7 second timer loop here,
//                // TODO Turn on 810nm, loop 3 seconds and record. Pause one second
//                // TODO turn off 810nm and turn on 1300nm.
//                //TODO loop for another 3 seconds
//                Long firstStop = System.currentTimeMillis() + 3000;
//                while(System.currentTimeMillis() < firstStop) {
//                    runOnUiThread(runnable);
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
//                }
//                bt.send("0", true);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//                Long secondStop = System.currentTimeMillis() + 3000;
//                while(System.currentTimeMillis() < secondStop) {
//                    runOnUiThread(runnable);
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (thread != null) {
//            thread.interrupt();
//        }
//    }



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

    public void setupChart() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAdd: {
//                addEntry();
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
//                feedMultiple();
                break;
            }
            case R.id.connectBluetooth:{
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }
}
