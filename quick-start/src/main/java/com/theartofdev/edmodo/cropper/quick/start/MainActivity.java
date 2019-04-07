// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.edmodo.cropper.quick.start;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static RequestQueue mVolleyQueue;
    static ImageLoader mVolleyImageLoader;
    Button sendButton;
    static Context context;
    public static Bitmap image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendButton = findViewById(R.id.send);
        mVolleyQueue = Volley.newRequestQueue(getApplicationContext());
        mVolleyImageLoader = new ImageLoader(mVolleyQueue, new BitmapLruCache());
        mVolleyQueue.start();
        context = this;
        setContentView(R.layout.activity_main);
    }

    public static RequestQueue getVolleyRequestQueue() {
        return mVolleyQueue;
    }

    public static ImageLoader getVolleyImageLoader() {
        return mVolleyImageLoader;
    }

    public static Context getContext() {
        return context;
    }

    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * Start pick image activity with chooser.
     */
    public void onSelectImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Decoupage")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(Config.LARGEUR_REQUISE, Config.HAUTEUR_REQUISE)
                .setCropMenuCropButtonIcon(R.drawable.ic_launcher)
                .start(this);
    }

    public void OnSendImageClick(View view) {
        // Start Result activity
        Intent myIntent = new Intent(this,
                ResultActivity.class);
        myIntent.putExtras(this.getIntent());
        startActivity(myIntent);

    }

    public void OnSettingsClick(View view) {
        // Start Result activity
        Intent myIntent = new Intent(this,
                SettingsActivity.class);
        myIntent.putExtras(this.getIntent());
        startActivity(myIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle some changes on Settings
        HashMap<String, String> params = new HashMap<>();
        params.put("number", String.valueOf(R.xml.pref_data_sync));
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                Config.INDEXING_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //TODO
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // Le code suivant est appelé lorsque Volley n'a pas réussi à récupérer le résultat de la requête
                Log.d("error settings value", "Error while getting JSON: " + error.getMessage());

            }

        });
        mVolleyQueue.add(jsonObjReq);
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ImageView view = findViewById(R.id.quick_start_cropped_image);
                view.setImageURI(result.getUri());
                image = ((BitmapDrawable) view.getDrawable()).getBitmap();
                Toast.makeText(
                        this, "Cropping successful: ", Toast.LENGTH_LONG)
                        .show();
//                sendButton.setEnabled(true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
//                sendButton.setEnabled(false);
            }
        }
    }

}
