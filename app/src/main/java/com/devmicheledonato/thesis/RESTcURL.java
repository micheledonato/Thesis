package com.devmicheledonato.thesis;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
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

                        String message = null;
                        if (error instanceof NoConnectionError) {
                            message = "NoConnectionError";
                        } else if (error instanceof ServerError) {
                            message = "ServerError";
                        } else if (error instanceof AuthFailureError) {
                            message = "AuthFailureError";
                        } else if (error instanceof ParseError) {
                            message = "ParseError";
                        } else if (error instanceof NetworkError) {
                            message = "NetworkError";
                        } else if (error instanceof TimeoutError) {
                            message = "TimeoutError";
                        }

                        Log.i(TAG, "Error message: " + message);
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

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        //Volley does retry for you if you have specified the policy.
        int socketTimeout = 30000; // 30 seconds
        int numAttempt = 3;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, numAttempt, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

//        Attempt 1:
//        time = time + (time * Back Off Multiplier);
//        time = 30.000 + 30.000 = 60.000
//        socketTimeout = time;
//        Request dispatched with Socket Timeout of 1 min
//
//        Attempt 2:
//        time = time + (time * Back Off Multiplier);
//        time = 60.000 + 60.000 = 120.000
//        socketTimeout = time;
//        Request dispatched with Socket Timeout of 2 min
//
//        Attempt 3:
//        time = time + (time * Back Off Multiplier);
//        time = 120.000 + 120.000 = 240.000
//        socketTimeout = time;
//        Request dispatched with Socket Timeout of 4 min

        ThesisApplication.getInstance().addToRequestQueue(request, tag);
    }
}
