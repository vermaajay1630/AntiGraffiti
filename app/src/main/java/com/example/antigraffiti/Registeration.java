package com.example.antigraffiti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registeration extends AppCompatActivity {

    Button signUp;
    EditText emailu, passwordu, cnfpasswordu;
    TextView signin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        signUp = findViewById(R.id.signUpBtnReg);
        emailu = findViewById(R.id.emailReg);
        passwordu = findViewById(R.id.passwordReg);
        cnfpasswordu = findViewById(R.id.cnPasswordReg);
        signin = findViewById(R.id.signInReg);

        //Redirecting to sign in page.
        signin.setOnClickListener(v -> {
            Intent log = new Intent(getApplicationContext(), Login.class);
            startActivity(log);
        });

        signUp.setOnClickListener(v -> {
            String uemail = emailu.getText().toString().trim();
            String upass = passwordu.getText().toString().trim();
            String cpass = cnfpasswordu.getText().toString().trim();
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Creating Account");
            progressDialog.show();

            if (uemail.isEmpty() || upass.isEmpty() || cpass.isEmpty()) {
                Toast.makeText(Registeration.this, "Please, enter all the fields.", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(uemail, upass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Registeration.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                        Intent log = new Intent(getApplicationContext(), Login.class);
                        startActivity(log);
                        progressDialog.dismiss();
                    } else {
                        Log.e("Registration Error", task.getException().getMessage());
                        Toast.makeText(Registeration.this, "User Already Present with this email! Please try Login.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}