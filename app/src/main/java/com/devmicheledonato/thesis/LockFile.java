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

public class LockFile {

    private final String TAG = this.getClass().getSimpleName();

    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    FileChannel channel;
    FileLock lock;
    String userID;

    public LockFile(Context context) {
        Log.i(TAG, "LockFile");
        file = new File(context.getExternalCacheDir(), "location.txt");
        channel = null;
        lock = null;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        userID = sharedPref.getString("EMAIL", "");
    }

    public void writeFile(String line) {
        try {
            // Get a file channel for the file
            channel = new RandomAccessFile(file, "rw").getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.tryLock();
            if (lock != null) {
                Log.i(TAG, "We obtained the lock");
                write(line);
            } else {
                // We didn't get the lock, which means another instance is
                // running. First, let the user know this.
                Log.i(TAG, "Another instance is already running");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always release the lock
            // Closing the RandomAccessFile also closes its FileChannel.
            try {
                if (lock != null && lock.isValid()) {
                    lock.release();
                }
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readFile() {
        try {
            // Get a file channel for the file
            channel = new RandomAccessFile(file, "rw").getChannel();
            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            lock = channel.tryLock();
            if (lock != null) {
                Log.i(TAG, "We obtained the lock");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(20000);
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                thread.start();
            } else {
                // We didn't get the lock, which means another instance is
                // running. First, let the user know this.
                Log.i(TAG, "Another instance is already running");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always release the lock
            // Closing the RandomAccessFile also closes its FileChannel.
            try {
                if (lock != null && lock.isValid()) {
                    lock.release();
                }
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(String line) {
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

        String lastUpdate = null;

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
                    lastUpdate = arrayLine[0];
                    jsonObject.put("dayOfWeek", "2");
                    jsonObject.put("hourOfDay", "4");
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

        try {
            total.put("userID", userID);
            total.put("lastUpdate", lastUpdate);
            total.put("positions", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send to WebServer
        Log.i(TAG, total.toString());
        return total;
    }
}
