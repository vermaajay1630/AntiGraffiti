package com.example.antigraffiti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    Button login;
    EditText email, password;
    TextView fpass, signup;
    private FirebaseAuth mAuth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.signInBtn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        fpass = findViewById(R.id.pasresetText);
        signup = findViewById(R.id.signUp);
        mAuth = FirebaseAuth.getInstance();

        //Redirect user to Registeration page
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), Registeration.class);
                startActivity(reg);
            }
        });

        //login user
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uemail = email.getText().toString().trim();
                String upass = password.getText().toString().trim();

                if(uemail.isEmpty() || upass.isEmpty()){
                    Toast.makeText(Login.this, "Please fill all the fields ", Toast.LENGTH_SHORT).show();
                }
                else{
                 mAuth.signInWithEmailAndPassword(uemail,upass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(main);
                            }
                            else{
                                Toast.makeText(Login.this, "User Not Found! Try Registering", Toast.LENGTH_SHORT).show();
                            }
                     }
                 });
                }
            }
        });


        fpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPassword();
            }
        });
    }

    private void ResetPassword() {
        String userEmail = email.getText().toString().trim();
        if(userEmail.isEmpty()){
            Toast.makeText(this, "Please insert registered Email", Toast.LENGTH_SHORT).show();
            email.requestFocus();
        }
        else{
            mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Email Sent Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "No email found", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
        }
    }
}