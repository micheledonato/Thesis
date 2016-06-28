package com.devmicheledonato.thesis;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PlaceFile {

    private final String TAG = this.getClass().getSimpleName();

    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    private String personID;
    private static final String ERROR_ID = "error_id";

    public PlaceFile(Context context, String fileName) {
        Log.i(TAG, "LocationFile");
        file = new File(context.getExternalCacheDir(), fileName);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        personID = sharedPref.getString(SignInActivity.PERSON_ID, ERROR_ID);
    }

    public boolean deleteFile() {
        return file.delete();
    }

    public void writeFile(String line) {
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            String[] arrayLine = line.split(",");
            for (int i = 0; i < arrayLine.length; i++) {
                printWriter.println(arrayLine[i]);
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

    public JSONObject fileToJson() {

        JSONObject total = new JSONObject();

        try {
            total.put("userID", personID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] arrayLine = line.split(",");
                try {
                    total.put("lat", arrayLine[0]);
                    total.put("lng", arrayLine[1]);
                    total.put("dateEnter", arrayLine[2]);
                    total.put("dateExit", arrayLine[3]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send to WebServer
        Log.i(TAG, total.toString());
        return total;
    }
}
