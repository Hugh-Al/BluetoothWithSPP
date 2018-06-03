package com.example.huy_a.bluetoothwithspp;

import android.util.Pair;

import java.util.ArrayList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Environment;

public class DataSaving {
    FileWriter writer;
    public DataSaving(ArrayList<Pair<Integer, Float>> dataSet810, ArrayList<Pair<Integer, Float>> dataSet1300){

        Long time = System.currentTimeMillis();
        String now = "" + time + ".csv";
        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, now);
    }

    private void writeCsvHeader(String h1, String h2, String h3) throws IOException {
        String line = String.format("%s,%s,%s\n", h1,h2,h3);
        writer.write(line);
    }

    private void writeCsvData(float d, float e, float f) throws IOException {
        String line = String.format("%f,%f,%f\n", d, e, f);
        writer.write(line);
    }
}
