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
 * Collection of util methods for file reading and writing
 */
public class FileUtil {
    private static final String DEBUG_TAG = "FileUtil";

    /**Write file overwriting any existing file
     *
     * @param name file to be created in external storage directory
     * @param data content of new file. Note that if an existing file is longer than data
     *             the part "behind data" from the old file may remain to exist.
     * @return true on success, otherwise an the method throws an exception
     */
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

    private static BufferedReader getBufferedReader(String name) {
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

    /**Read entire content of file into String
     *
     * @param name file in external storage directory
     * @return content of file read
     */
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

    /**Write object into file as json
     *
     * @param config object to be serialized into file
     * @return true on suscess
     */
    public static boolean writeConfigJSON(Object config) {
        Gson gson = new Gson();
        String json = gson.toJson(config);
        return FileUtil.writePublicFile("wlanConfig.json", json);
    }

    /**Read object from json file
     *
     * @param file file in external storage directory
     * @param classToRead
     * @return object of type T read or null if not successful
     */
    public static <T extends Object> T readConfigJson(String file, Class<T> classToRead) {
        Gson gson = new Gson();
        BufferedReader out = null;
        out = FileUtil.getBufferedReader("wlanConfig.json");
        T result = gson.fromJson(out, classToRead);
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {}
        }
        return result;
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
