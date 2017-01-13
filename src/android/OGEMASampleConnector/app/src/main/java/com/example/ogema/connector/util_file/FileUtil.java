package com.example.ogema.connector.util_file;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dnestle on 24.08.2015.
 */
public class FileUtil {
    private static final String DEBUG_TAG = "FileUtil";

    public static boolean writePublicFile(String name, String data) {
        if(!isExternalStorageWritable()) return false;
        File topDir = Environment.getExternalStorageDirectory();
        File file = new File(topDir, name);

        Log.e(DEBUG_TAG, "Writing file:"+file.getAbsolutePath());

        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter(file, false);
            out = new BufferedWriter(fstream);
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                try {out.close();}catch (IOException e) {}
            }
        }
        return true;
    }

    public static BufferedReader getBufferedReader(String name) {
        if(!isExternalStorageReadable()) return null;
        File topDir = Environment.getExternalStorageDirectory();
        File file = new File(topDir, name);
        try {
            FileReader fstream = new FileReader(file);
            Log.e(DEBUG_TAG, "Reading file:"+file.getAbsolutePath());
            return new BufferedReader(fstream);
        } catch (IOException e) {
            return null;
        }
    }

    public static String readPublicFile(String name) {

        BufferedReader out = getBufferedReader(name);
        if(out == null) return null;
        char[] cbuf = new char[4096];
        String s = null;
        try {
            int len = out.read(cbuf, 0, 4095);
            Log.e(DEBUG_TAG, "Read bytes:"+len);
            s = new String(cbuf, 0, len);
         } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                try {out.close();}catch (IOException e) {}
            }
        }
        return s;

    }

    public static boolean writeConfigJSON(Object config) {
        Gson gson = new Gson();
        String json = gson.toJson(config);
        return FileUtil.writePublicFile("wlanConfig.json", json);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
