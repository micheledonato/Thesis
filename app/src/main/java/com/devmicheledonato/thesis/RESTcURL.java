package com.devmicheledonato.thesis;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class RESTcURL {

    private final String TAG = this.getClass().getSimpleName();

    private final String BASE_URL = "http://31.14.135.152:8080/mobilita-0.0.9-SNAPSHOT/";
    private static final String ALIVE = "ALIVE";
    private static final String USER = "USER";
    private static final String PLACE = "PLACE";
    private static final String DISPLACEMENT = "DISPLACEMENT";

    private ThesisApplication app;

    public RESTcURL() {
        app = ThesisApplication.getInstance();
    }

    public void postAlive(JSONObject jsonObject) {
        Log.i(TAG, "postAlive");
        String url = BASE_URL + "postAlive";
        postData(url, ALIVE, jsonObject);
    }

    public void postUser(JSONObject jsonObject) {
        Log.i(TAG, "postUser");
        String url = BASE_URL + "postUser";
        postData(url, USER, jsonObject);
    }

    public void postPlace(JSONObject jsonObject) {
        Log.i(TAG, "postPlace");
        String url = BASE_URL + "postPlace";
        postData(url, PLACE, jsonObject);
    }

    public void postDisplacement(JSONObject jsonObject) {
        Log.i(TAG, "postDisplacement");
        String url = BASE_URL + "postDisplacement";
        postData(url, DISPLACEMENT, jsonObject);
    }

    private void postData(String url, String tag, JSONObject jsonObject) {

        Log.i(TAG, jsonObject.toString());
        app.logFile.append(TAG, jsonObject.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Log.i(TAG, "Response " + response.toString());
                            app.logFile.append(TAG, "Response " + response.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        int statusCode = error.networkResponse.statusCode;
//                        NetworkResponse response = error.networkResponse;
//                        Log.i(TAG, "StatusCode: " + statusCode + " Error response: " + error.toString());
                        if (error != null) {
                            Log.i(TAG, "Error response: " + error.toString());
                            app.logFile.append(TAG, "Error response: " + error.toString());
                        }
                    }
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.i(TAG, "Status response " + response.statusCode);
                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
//                Log.i(TAG, "Status error " + volleyError.networkResponse.statusCode);
//                return super.parseNetworkError(volleyError);

                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 3, 1.0f));
        ThesisApplication.getInstance().addToRequestQueue(request, tag);
    }
}
