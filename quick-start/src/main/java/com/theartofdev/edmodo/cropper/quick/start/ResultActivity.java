package com.theartofdev.edmodo.cropper.quick.start;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ResultActivity extends ListActivity {
    private RequestQueue mRequestQueue;
    private ResultListAdapter mAdapter;
    private String resultLocation = "";
    ProgressDialog nDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On récupère notre RequestQueue et notre ImageLoader
        Context app = MainActivity.getContext();
        mRequestQueue = MainActivity.getVolleyRequestQueue();
        ImageLoader imageLoader = MainActivity.getVolleyImageLoader();
        mAdapter = new ResultListAdapter(app, imageLoader);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // chargement avant récupération des résultats
        nDialog = new ProgressDialog(ResultActivity.this);
        nDialog.setMessage("Chargement..");
        nDialog.setTitle("En attente des résultats");
        if(getPingResult(Config.SERVER_IP) == null) {
            nDialog.setMessage("Serveur Injoignable");
        }
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(true);
        nDialog.show();
    }

    public String getIPAdrr() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public void volleyRequestPost(final ServerCallback callback) {
        if (getPingResult(Config.SERVER_IP) != null) {
            HashMap<String, String> params = new HashMap<>();
            params.put("client_ip", getIPAdrr());
            params.put("data", MainActivity.getStringImage(MainActivity.image));
            params.put("method", "CNN");

            Log.e("sending json", params.toString());
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    Config.FILE_UPLOAD_URL, new JSONObject(params), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        resultLocation = response.getString("data_location");
                        // On va créer une Request pour Volley.
                        // JsonArrayRequest hérite de Request et transforme automatiquement les données reçues en un JSONArray
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, "http://" + Config.SERVER_IP + resultLocation, null, new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //En enleve le chargement
                                        nDialog.dismiss();
                                        // Ce code est appelé quand la requête réussi. Étant ici dans le thread principal, on va pouvoir mettre à jour notre Adapter
                                        try {
                                            mAdapter.updateMembers(response);
                                            Log.d("response", response.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Le code suivant est appelé lorsque Volley n'a pas réussi à récupérer le résultat de la requête
                                        Toast.makeText(ResultActivity.this, "Error while getting JSON: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                        jsonObjectRequest.setTag(this);
                        // On ajoute la Request au RequestQueue pour la lancer
                        mRequestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Volley error json object ", "Error: " + error.getMessage());

                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            // Adding request to request queue
            mRequestQueue.add(jsonObjReq);
        } else {
            Log.d("Network error", "Le serveur est injoignable !");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        volleyRequestPost(new ServerCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.i("result", result.toString());
            }
        });
    }

    @Override
    protected void onStop() {
        mRequestQueue.cancelAll(this);
        super.onStop();
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Lorsque l'on clique sur un élément de la liste, cela lancera l'URL du compte GitHub de l'utilisateur sélectionné.
        JSONObject item = mAdapter.getItem(position);
        String url = "http://" + item.optString("result_lib");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public static String getPingResult(String a) {
        String str = "";
        String result = "";
        BufferedReader reader = null;
        char[] buffer = new char[4096];
        StringBuffer output = new StringBuffer();

        try {
            Runtime r = Runtime.getRuntime();
            Process process = r.exec("/system/bin/ping -c 3 " + a);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int i;

            while ((i = reader.read(buffer)) > 0)
                output.append(buffer, 0, i);


            str = output.toString();

            final String[] b = str.split("---");
            final String[] c = b[2].split("rtt");

            if (b.length == 0 || c.length == 0)
                return null;

            if (b.length == 1 || c.length == 1)
                return null;

            result = b[1].substring(1, b[1].length()) + c[0] + c[1].substring(1, c[1].length());

        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ie) {
                }
            }
        }

        return result;
    }

    public String getResultLocation() {
        return resultLocation;
    }
}
