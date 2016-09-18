package com.ocrsplitter.ocrsplitter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    Button loginButton;
    Button registerButton;

    EditText userNameET;
    EditText passwordET;

    TextView errorNameET;
    TextView errorPasswordET;

    @TargetApi(24)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                }
                else {
                    // User is signed out
                }
            }
        };

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        userNameET = (EditText) findViewById(R.id.usernameEditText);
        passwordET = (EditText) findViewById(R.id.passwordEditText);

        errorNameET = (TextView) findViewById(R.id.errorEmailText);
        errorPasswordET = (TextView) findViewById(R.id.errorPasswordText);

        errorNameET.setText("");
        errorNameET.setTextColor(Color.RED);
        errorPasswordET.setText("");
        errorPasswordET.setTextColor(Color.RED);


        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                register();
            }
        });

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 5);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    protected void login() {
        errorNameET.setText("");

        mAuth.signInWithEmailAndPassword(userNameET.getText().toString(), passwordET.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            errorNameET.setText("Invalid username or password");
                            System.out.println(task.getException());
                        }
                        else {
                            goToNextScreen();
                        }

                        // ...
                    }
                });
    }

    protected void register() {
        errorNameET.setText("");

        if (passwordET.getText().toString().length() < 6) {
            errorNameET.setText("Password is too short");
            return;
        }

        mAuth.createUserWithEmailAndPassword(userNameET.getText().toString(), passwordET.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user.
                        if (!task.isSuccessful()) {
                            errorNameET.setText("Can't register");
                            System.out.println(task.getException());
                        }
                        else {
                            goToNextScreen();
                        }

                        // ...
                    }
                });
    }

    protected void goToNextScreen() {
        System.out.println("Next Screen");
        Intent goToNextScreen = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(goToNextScreen);
    }
}