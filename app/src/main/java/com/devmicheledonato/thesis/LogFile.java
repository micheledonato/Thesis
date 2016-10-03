package com.devmicheledonato.thesis;


import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogFile {

    private static final String LOG_FILENAME = "logFile";

    private File logFile;
    // Writer for file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    public LogFile(Context context) {
        logFile = new File(context.getExternalCacheDir(), LOG_FILENAME);
    }

    public void append(String tag, String value) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm:ss", Locale.ITALY);
        String time = simpleDateFormat.format(System.currentTimeMillis());

        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(logFile, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(time + " " + tag + " " + value);
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
