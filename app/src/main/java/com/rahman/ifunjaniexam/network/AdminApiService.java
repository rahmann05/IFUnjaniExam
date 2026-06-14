package com.rahman.ifunjaniexam.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminApiService {

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(Exception error, com.android.volley.VolleyError volleyError);
    }

    public static void getApprovalRequests(Context context, ApiCallback callback) {
        String url = Config.BASE_URL + "/admin/requests";
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> callback.onSuccess(response),
                error -> callback.onError(error, error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public static void processApproval(Context context, int reqId, String action, ApiCallback callback) {
        String url = Config.BASE_URL + "/admin/requests/" + reqId;
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        try {
            JSONObject body = new JSONObject();
            body.put("action", action);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> callback.onSuccess(response),
                    error -> callback.onError(error, error)) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Volley.newRequestQueue(context).add(request);
        } catch (Exception e) {
            callback.onError(e, null);
        }
    }
}
