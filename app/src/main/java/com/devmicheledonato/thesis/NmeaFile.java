package com.devmicheledonato.thesis;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NmeaFile {

    private final String TAG = this.getClass().getSimpleName();

    public static final String NMEA_FILENAME = "NmeaFile";

    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    public NmeaFile(Context context) {
        Log.i(TAG, NMEA_FILENAME);
        file = new File(context.getExternalCacheDir(), NMEA_FILENAME);

    }

    public boolean deleteFile() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public void writeFile(String line) {
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            printWriter.print(line);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

