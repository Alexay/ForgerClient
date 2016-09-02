package alex.forgerclient;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.novoda.merlin.MerlinsBeard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import alex.forgerclient.util.DrawableDataGetter;
import alex.forgerclient.util.VolleyMultipartRequest;
import alex.forgerclient.util.VolleySingleton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonUpload;

    private ImageView contentImageView;
    private ImageView styleImageView;

    private Drawable drawable;

    private int PICK_IMAGE_REQUEST = 1;
    private int THE_BUTTON_THAT_CHOSE;


    /**
     *  Make sure to change this variable to your own server's URL!
     */
    private final String UPLOAD_URL = "http://localhost:51731/";

    int PERMISSION_INTERNET;
    int PERMISSION_WRITE_EXTERNAL_STORAGE;

    public boolean checkAndAskForPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        PERMISSION_INTERNET);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }

        return (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        contentImageView = (ImageView) findViewById(R.id.contentImageView);
        styleImageView = (ImageView) findViewById(R.id.styleImageView);

        buttonUpload.setOnClickListener(this);
        contentImageView.setOnClickListener(this);
        styleImageView.setOnClickListener(this);

        checkAndAskForPermissions();
    }


    private void uploadImage() {

        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Processing...", "Please wait, this may take a few minutes...", false, false);
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            // Dismiss the loader.
                            loading.dismiss();

                            // Write the response to a temporary file.
                            File temp = new File(getFilesDir() + "/output.png");
                            FileOutputStream fileOutputStream = new FileOutputStream(temp);
                            fileOutputStream.write(response.data);
                            fileOutputStream.close();

                            Intent nextScreen = new Intent(getApplicationContext(), ResultActivity.class);
                            startActivity(nextScreen);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("content", new DataPart("file_content.png", DrawableDataGetter.getFileDataFromDrawable(getBaseContext(), contentImageView.getDrawable()), "image/png"));
                params.put("style", new DataPart("file_style.png", DrawableDataGetter.getFileDataFromDrawable(getBaseContext(), styleImageView.getDrawable()), "image/png"));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(3000000, 0, 0));
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(volleyMultipartRequest);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                drawable = Drawable.createFromStream(inputStream, filePath.toString());

                if (THE_BUTTON_THAT_CHOSE == 1) contentImageView.setImageDrawable(drawable);
                else if (THE_BUTTON_THAT_CHOSE == 2) styleImageView.setImageDrawable(drawable);
            } catch (FileNotFoundException e) {
                System.err.println("Error when getting image data: " + e);
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v == contentImageView) {
            THE_BUTTON_THAT_CHOSE = 1;
            if (checkAndAskForPermissions())
                showFileChooser();
            else
                Toast.makeText(MainActivity.this, "Error: permissions not granted, please go to your settings and grant all permissions to this application.", Toast.LENGTH_LONG).show();
        }

        if (v == styleImageView) {
            THE_BUTTON_THAT_CHOSE = 2;
            if (checkAndAskForPermissions())
                showFileChooser();
            else
                Toast.makeText(MainActivity.this, "Error: permissions not granted, please go to your settings and grant all permissions to this application.", Toast.LENGTH_LONG).show();
        }

        if (v == buttonUpload) {

            // Check that the user has granted relevant permissions
            if (checkAndAskForPermissions()) {

                // Check that there are images in both ImageViews
                if ((contentImageView.getDrawable() != null) && (styleImageView.getDrawable() != null)) {

                    // Check that the device can connect to the internet.
                    MerlinsBeard merlinsBeard = MerlinsBeard.from(this);
                    if (merlinsBeard.isConnected()) {

                        // Finally, upload the images.
                        uploadImage();
                    }
                    else
                        Toast.makeText(MainActivity.this, "Error: your device is not connected to the Internet.", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(MainActivity.this, "Error: please select the images.", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(MainActivity.this, "Error: permissions not granted, please go to your settings and grant all permissions to this application.", Toast.LENGTH_LONG).show();
        }
    }
}
