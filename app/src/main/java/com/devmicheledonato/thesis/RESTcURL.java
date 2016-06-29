package com.devmicheledonato.thesis;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class RESTcURL {

    private final String TAG = this.getClass().getSimpleName();

    private final String BASE_URL = "http://31.14.140.186:8080/mobilita-0.0.7-SNAPSHOT/";
    private static final String USER = "USER";
    private static final String PLACE = "PLACE";
    private static final String DISPLACEMENT = "DISPLACEMENT";

    public RESTcURL() {
    }

    public void postUser(JSONObject jsonObject){
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Error " + error);
                    }
                }
        ) {
//            @Override
//            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//                Log.i(TAG, "Status response " + response.statusCode);
//                return super.parseNetworkResponse(response);
//            }
//
//            @Override
//            protected VolleyError parseNetworkError(VolleyError volleyError) {
//                Log.i(TAG, "Status error " + volleyError.networkResponse.statusCode);
//                return super.parseNetworkError(volleyError);
//            }
        };
        ThesisApplication.getInstance().addToRequestQueue(request, tag);
    }
}
