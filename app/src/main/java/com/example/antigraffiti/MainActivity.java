package com.example.antigraffiti;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;
    Button cam;
    String currentPhotoPath;
    private File imageFile;
    private FirebaseAuth mAuth;
    myAdapter adapter;
    RecyclerView recyclerView;
    private FirebaseUiException firebaseUiException;
    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cam = findViewById(R.id.openCamera);
        recyclerView =findViewById(R.id.recyclerView);
        cam.setOnClickListener(v -> camOpen());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            REQUEST_PERMISSIONS);
                }
            }
        }



        //recycler setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        FirebaseRecyclerOptions<imageData> options =
                new FirebaseRecyclerOptions.Builder<imageData>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("graffitiDetails").orderByChild("timestamp"), imageData.class)
                        .build();

        adapter = new myAdapter(options);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED
                        & grantResults[1] == PackageManager.PERMISSION_GRANTED & grantResults[2] == PackageManager.PERMISSION_GRANTED &
                        grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("You have denied or More permission. Please go to [Settings] > [Permissions] and allow all")
                            .setTitle("Permissions Denied");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }).setNegativeButton("No, Exit App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();
                }
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED
                        & grantResults[1] == PackageManager.PERMISSION_GRANTED & grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setMessage("You have denied or More permission. Please go to [Settings] > [Permissions] and allow all")
                            .setTitle("Permissions Denied");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }).setNegativeButton("No, Exit App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();
                }
            }
        }
    }

    private void camOpen() {

        String filename = "photo";
        File fileStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageFile = File.createTempFile(filename, ".jpg", fileStorage);
            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.antigraffiti.fileProvider", imageFile);
            currentPhotoPath = imageFile.getAbsolutePath();
            cam.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cam, CAMERA_REQUEST_CODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (currentPhotoPath != null) {
                new UploadTask().execute(currentPhotoPath);
            } else {
                Toast.makeText(this, "Image Not Captured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String filePath = strings[0];
            File file = new File(filePath);
            String url = "https://example.com/upload"; // Replace with your own server URL
            String response = null;

            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                Response httpResponse = client.newCall(request).execute();
                response = httpResponse.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            // Handle the response from the server
            if (response != null) {
                Intent intent = new Intent(getApplicationContext(), UploadImage.class);
                intent.putExtra("capturedImageFilePath", currentPhotoPath);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}