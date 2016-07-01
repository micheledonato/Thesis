package com.devmicheledonato.thesis;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GeofenceFile {

    private final String TAG = this.getClass().getSimpleName();

    public static final String GEOFENCE_FILENAME = "SimpleGeofenceIDs";

    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    public GeofenceFile(Context context) {
        Log.i(TAG, "GeofenceFile");
        file = new File(context.getExternalCacheDir(), GEOFENCE_FILENAME);
    }

    public boolean deleteFile() {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public void writeFile(String line, boolean enter) {
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            if (enter) { // when transition enter write geofence ID,enterDate
                if (file.length() != 0) {
                    printWriter.println();
                }
                printWriter.print(line);
            } else {    // when transition exit write ,exitDate
                printWriter.print(line);
            }
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
