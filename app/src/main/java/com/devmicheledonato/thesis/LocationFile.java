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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class LocationFile {

    private final String TAG = this.getClass().getSimpleName();

    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

//    FileChannel channel;
//    FileLock lock;
    private String personID;
    private static final String ERROR_ID = "error_id";

    public LocationFile(Context context, String fileName) {
        Log.i(TAG, "LocationFile");
        file = new File(context.getExternalCacheDir(), fileName);
//        channel = null;
//        lock = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        personID = sharedPref.getString(SignInActivity.PERSON_ID, ERROR_ID);
    }

    public boolean deleteFile(){
        return file.delete();
    }

//    public void writeFile(String line) {
//        try {
//            // Get a file channel for the file
//            channel = new RandomAccessFile(file, "rw").getChannel();
//            // Use the file channel to create a lock on the file.
//            // This method blocks until it can retrieve the lock.
//            lock = channel.tryLock();
//            if (lock != null) {
//                Log.i(TAG, "We obtained the lock");
//                write(line);
//            } else {
//                // We didn't get the lock, which means another instance is
//                // running. First, let the user know this.
//                Log.i(TAG, "Another instance is already running");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // Always release the lock
//            // Closing the RandomAccessFile also closes its FileChannel.
//            try {
//                if (lock != null && lock.isValid()) {
//                    lock.release();
//                }
//                if (channel != null) {
//                    channel.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public void readFile() {
//        try {
//            // Get a file channel for the file
//            channel = new RandomAccessFile(file, "rw").getChannel();
//            // Use the file channel to create a lock on the file.
//            // This method blocks until it can retrieve the lock.
//            lock = channel.tryLock();
//            if (lock != null) {
//                Log.i(TAG, "We obtained the lock");
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(20000);
//                        } catch(InterruptedException ex) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }
//                });
//                thread.start();
//            } else {
//                // We didn't get the lock, which means another instance is
//                // running. First, let the user know this.
//                Log.i(TAG, "Another instance is already running");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // Always release the lock
//            // Closing the RandomAccessFile also closes its FileChannel.
//            try {
//                if (lock != null && lock.isValid()) {
//                    lock.release();
//                }
//                if (channel != null) {
//                    channel.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void writeFile(String line) {
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(line);
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
        JSONArray jsonArray = new JSONArray();

        try {
            total.put("userID", personID);
            total.put("positions", jsonArray);
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
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lat", arrayLine[1]);
                    jsonObject.put("lng", arrayLine[2]);
                    jsonObject.put("date", arrayLine[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(jsonObject);
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

    //    public void fileToJson() {
//        file = new File(this.getApplicationContext().getExternalCacheDir(), "location.txt");
//
//        JSONObject total = new JSONObject();
//        JSONArray jsonArray = new JSONArray();
//        try {
//            total.put("User", "Michele");
//            total.put("Location", jsonArray);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        FileReader fileReader = null;
//        BufferedReader bufferedReader = null;
//        try {
//            fileReader = new FileReader(file);
//            bufferedReader = new BufferedReader(fileReader);
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] arrayLine = line.split(",");
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("Date", arrayLine[0]);
//                    jsonObject.put("Lat", arrayLine[1]);
//                    jsonObject.put("Lng", arrayLine[2]);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                jsonArray.put(jsonObject);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (bufferedReader != null)
//                    bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (fileReader != null)
//                    fileReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Send to WebServer
//        Log.i(TAG, total.toString());
//    }

//    private void readJsonObject(JSONObject total){
//        try{
//            String user = total.getString("user");
//            JSONArray jsonArray = total.getJSONArray("Location");
//            for(int i=0; i<jsonArray.length(); i++){
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                String date = jsonObject.getString("Date");
//                String lat = jsonObject.getString("Lat");
//                String lng = jsonObject.getString("Lng");
//            }
//        }catch(JSONException e){
//            e.printStackTrace();
//        }
//    }
}
