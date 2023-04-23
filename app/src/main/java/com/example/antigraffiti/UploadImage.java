package com.example.antigraffiti;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UploadImage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] graffitiTypes = {"RACIST","SLANG","InAppropriate","Religious"};
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

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadtoFirebase();
            }
        });

    }


    //uploading image to firebase storage and storing description, image url, address in Realtime database.

//    private void UploadToStorage() {
//        databaseReference =FirebaseDatabase.getInstance().getReference("image");
//        SimpleDateFormat dateFormat =new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
//        Date now =new Date();
//        String filename =dateFormat.format(now);
//        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
//                    String donwloadUrl =uri.toString();
//                    downUrl =   donwloadUrl;
//                    addToDatabase(donwloadUrl);
//                });
//            }
//        });
//
//
//    }

    private void uploadtoFirebase() {
        imageData = new imageData();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("image");
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
        Date now =  new Date();
        String filename =dateFormat.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl =  uri.toString();
                        addToDatabase(String.valueOf(uri));
                    }
                });
            }
        });

    }

    private void addToDatabase(String uri) {
        imageId = String.valueOf(UUID.randomUUID());
        String desc = description.getText().toString().trim();
       databaseReference  =  FirebaseDatabase.getInstance().getReference("graffitiDetails");
        SimpleDateFormat dateFormat =new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
        Date now =new Date();
        String filename =dateFormat.format(now);
        imageData.setImageUrl(uri);
        imageData.setAddress(add);
        imageData.setDescription(desc);
        imageData.setCategory(gType);
        imageData.setId(imageId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(imageId).setValue(imageData);
                Toast.makeText(getApplicationContext(), "Graffiti Added Successfully", LENGTH_SHORT).show();
                Intent mainAct = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainAct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    //Displaying capture image using the camera

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
                    outStream = new FileOutputStream(f);
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
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                String country = address.getCountryName();
                String fullAddress = address.getAddressLine(0);
                currentLoc.setText(fullAddress);
                add = fullAddress;
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