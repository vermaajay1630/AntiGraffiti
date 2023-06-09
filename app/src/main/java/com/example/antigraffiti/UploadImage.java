package com.example.antigraffiti;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UploadImage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] graffitiTypes = {"Good","Racist","Slang","In-appropriate","Religious"};
    Context context;
    String add, downUrl,gType, imageId;
    Location location;
    Uri uri;
    Spinner types;
    Button upload, cancel;
    EditText description;
    TextView currentLoc;
    imageData imageData;
    ImageView preview;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private ProgressBar loadingPB;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_upload_image);
        preview = findViewById(R.id.previewImage);
        cancel = findViewById(R.id.cancelBtn);
        upload = findViewById(R.id.uploadBtn);
        currentLoc = findViewById(R.id.address);
        description = findViewById(R.id.desGraffiti);
        types = findViewById(R.id.dropDown);
        types.setOnItemSelectedListener(this);
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, graffitiTypes);
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        types.setAdapter(ad);



        getAddress();
        displayCapturedImage();
        cancel.setOnClickListener(v -> {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
        });

        
        upload.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                uploadtoFirebase();
            } else {
                Toast.makeText(getApplicationContext(), "Please! enable Internet to report the Graffiti", LENGTH_SHORT).show();
            }
            }
            else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
                    uploadtoFirebase();
                } else {
                    Toast.makeText(getApplicationContext(), "Please! enable Internet to report the Graffiti", LENGTH_SHORT).show();
                }
            }


        });

    }


    //uploading image to firebase storage and storing description, image url, address in Realtime database.


    private void uploadtoFirebase() {
        imageData = new imageData();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("image");
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
        Date now =  new Date();
        String filename =dateFormat.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String downloadUrl =  uri.toString();
                addToDatabase(String.valueOf(uri));
                progressDialog.dismiss();
            }
        })).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });
    }
    private void addToDatabase(String uri) {
        imageId = String.valueOf(UUID.randomUUID());
        String desc = description.getText().toString().trim();
        databaseReference = FirebaseDatabase.getInstance().getReference("graffitiDetails");

        // Check if the address already exists in the database
        Query query = databaseReference.orderByChild("address").equalTo(add);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // The address already exists in the database
                    Toast.makeText(getApplicationContext(), "We Appreciate your Effort! However, this has been already reported. Thanks!", LENGTH_SHORT).show();
                } else {
                    // The address does not exist in the database, so add the data
                    SimpleDateFormat dateFormat =new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
                    Date now =new Date();
                    String filename =dateFormat.format(now);
                    imageData.setImageUrl(uri);
                    imageData.setAddress(add);
                    imageData.setDescription(desc);
                    imageData.setCategory(gType);
                    imageData.setId(imageId);

                    databaseReference.child(imageId).setValue(imageData);
                    Toast.makeText(getApplicationContext(), "Thank you for reporting this.", LENGTH_SHORT).show();
                    Intent mainAct = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainAct);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error here
            }
        });
    }


    //Displaying capture image using the camera

    @SuppressLint("NewApi")
    private void displayCapturedImage() {
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("capturedImageFilePath");
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                preview.setImageBitmap(bitmap);
                OutputStream outStream = null;
                File f = new File(filePath);
                try {
                    outStream = Files.newOutputStream(f.toPath());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                uri = Uri.fromFile(f);
            } else {
                Toast.makeText(this, "Failed to load image file", LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to get image file path", LENGTH_SHORT).show();
        }
    }


    private void getAddress() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            new GetAddressTask().execute(latitude, longitude);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAddressTask extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];
            Geocoder geocoder = new Geocoder(UploadImage.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                return fullAddress;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                currentLoc.setText(result);
                add = result;
            }
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    public void openMap(View view) {
        String loc = currentLoc.getText().toString();
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(loc));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "No map application found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1,
                               int position,
                               long id) {
        gType = graffitiTypes[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}