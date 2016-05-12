package com.devmicheledonato.thesis.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.devmicheledonato.thesis.LocationFile;
import com.devmicheledonato.thesis.R;

import org.json.JSONArray;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class DataFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private final static String error_user = "-Error: No rows for";
    private final static String error_list = "-Error: No rows!";
    private final static String error_generic = "-Error:";

    private StringBuffer chaine;
    private JSONObject userJSON;
    private TextView lista;
    private Button send;
    private Button refresh;

    LocationFile locationFile;


    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationFile = new LocationFile(getActivity(), "location.txt");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_data, container, false);

        lista = (TextView) root.findViewById(R.id.lista);
        send = (Button) root.findViewById(R.id.send);
        refresh = (Button) root.findViewById(R.id.refresh);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                userJSON = new JSONObject();
//                JSONObject newPosition = new JSONObject();
//                JSONArray positions = new JSONArray();
//                Calendar now = (Calendar) Calendar.getInstance();
////                GregorianCalendar now = (GregorianCalendar) GregorianCalendar.getInstance();
//                if (user.getText().toString().trim().equalsIgnoreCase("")
//                        || lat.getText().toString().trim().equalsIgnoreCase("")
//                        || lat.getText().toString().trim().equalsIgnoreCase("")) {
////                    Toast.makeText(MainActivity.this, "Fields empty", Toast.LENGTH_SHORT).show();
//                } else {
//                    try {
//                        userJSON.put("userID", user.getText().toString());
//                        userJSON.put("lastUpdate", now.getTimeInMillis());
//
//                        newPosition.put("lat", Double.parseDouble(lat.getText().toString()));
//                        newPosition.put("lng", Double.parseDouble(lng.getText().toString()));
//                        newPosition.put("date", now.getTimeInMillis());
//                        newPosition.put("dayOfWeek", now.get(Calendar.DAY_OF_WEEK) - 1);
//                        newPosition.put("hourOfDay", now.get(Calendar.HOUR_OF_DAY));
////                        newRow.put("dayOfWeek", ((Calendar) now).get(Calendar.DAY_OF_WEEK));
////                        newRow.put("hourOfDay", ((Calendar) now).get(Calendar.HOUR_OF_DAY));
//                        positions.put(newPosition);
//
//                        userJSON.put("positions", positions);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    new postValue().execute();
//                }
                userJSON = locationFile.fileToJson();
                Log.i(TAG, userJSON.toString());
                new postValue().execute();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new readValue().execute();
            }
        });
        new readValue().execute();

        return root;
    }

    private class readValue extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lista.setText("");
        }

        @Override
        protected String doInBackground(String... strings) {
            chaine = new StringBuffer("");
            try {
                URL url = new URL("http://31.14.140.186:8080/mobilita-0.0.2-SNAPSHOT/get");
                //URL url = new URL("http://31.14.140.186:8080/mobilita-0.0.2-SNAPSHOT/get/nome_utente");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
//                connection.setConnectTimeout(10000);
//                connection.setReadTimeout(10000);
//                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    chaine.append(line);
                }
            } catch (Exception e) {
                // writing exception to log
                e.printStackTrace();
            }
            Log.e("RISULTATO: ", chaine.toString());
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (chaine.toString().contains(error_list))
                lista.setText(chaine.toString());
            else if (chaine.toString().contains(error_generic)) {
                Log.e("RETURN ERROR", chaine.toString());
                lista.setText("Error returned from server");
            } else {
                try {
                    JSONArray list = new JSONArray(chaine.toString());
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject user = list.getJSONObject(i);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
                        Calendar lastUpdate = (Calendar) Calendar.getInstance();
                        lastUpdate.setTimeInMillis(Long.parseLong(user.getString("lastUpdate")));

                        lista.append("UserID: " + user.getString("userID") +
                                " lastUpdate: " + format.format(lastUpdate.getTime()) + "\n");

                        JSONArray positions = new JSONArray(list.getJSONObject(i).getString("positions"));
                        for (int j = 0; j < positions.length(); j++) {
                            JSONObject position = positions.getJSONObject(j);

                            lista.append("\t\tLat: " + position.getDouble("lat") + "\n" +
                                    "\t\tLng: " + position.getDouble("lng") + "\n" +
                                    "\t\tDate: " + position.getString("date") + "\n" +
                                    "\t\tDay: " + position.getInt("dayOfWeek") + "\n" +
                                    "\t\tHour: " + position.getInt("hourOfDay") + "\n");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class postValue extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://31.14.140.186:8080/mobilita-0.0.2-SNAPSHOT/post");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                Log.e("JSON:", userJSON.toString());
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(userJSON.toString());
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
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            Toast.makeText(MainActivity.this, chaine.toString(), Toast.LENGTH_SHORT).show();
            new readValue().execute();
        }
    }
}
