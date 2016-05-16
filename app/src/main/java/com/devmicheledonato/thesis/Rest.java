package com.devmicheledonato.thesis;

import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Rest {

    public Rest(){

    }

    public void restValue(){

    }

    public class postValue extends AsyncTask<String, Void, Void> {

        private StringBuffer chaine;

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://31.14.140.186:8080/mobilita-0.0.5-SNAPSHOT/postDisplacement");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                String json = params[0];

                Log.e("JSON:", json);
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(json);
                out.close();

//                int HttpResult = connection.getResponseCode();
//                Log.e("Result:", "" + HttpResult);
//                if(HttpResult == HttpURLConnection.HTTP_OK){
//                }else{
//                    Log.e("Insert response", connection.getResponseMessage());
//                }

                InputStream inputStream = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                chaine = new StringBuffer("");
                chaine.delete(0, chaine.length());
                while ((line = rd.readLine()) != null) {
                    chaine.append(line);
                }
                Log.e("Insert response", chaine.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}
