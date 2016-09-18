package com.ocrsplitter.ocrsplitter;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import Math.abs;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    Button loginButton;
    Button registerButton;

    EditText userNameET;
    EditText passwordET;

    TextView errorNameET;
    TextView errorPasswordET;

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
    }

    protected void register() {
        mAuth.createUserWithEmailAndPassword(userNameET.getText().toString(), passwordET.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            errorNameET.setText("Can't register");
                        }
                        else {
                            goToNextScreen();
                        }

                        // ...
                    }
                });
    }

    protected ArrayList extractItems(String s) {

        JsonReader reader = Json.createReader(new StringReader(s));

        JsonObject data = reader.readObject();

        reader.close();

        JsonArray text_data = data.getJsonArray("textAnnotation");

        ArrayList words = [];

        for (JsonObject word : text_data) {
            words.add(word);
        }

        return words;
    }

    protected ArrayList selectLines(ArrayList<JsonObject> words) {
        ArrayList lines;
        ArrayList current_line;

        ArrayList words_done;

        int current_line_y = 0;

        for (JsonObject primary_word : words) {

            boolean exclude = false;
            for (JsonObject check_word : words_done) {
                if (primary_word == check_word) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                exclude = false;

                current_line.add(primary_word)
                words_done.add(primary_word)

                JsonArray vertexes = word.getJsonObject("boudingPoly").getJsonArray("vertices");

                current_line_y = vertexes.get(0).getInt("y");
                int current_line_height = vertexes.get(0).getInt("y") - vertexes.get(2).getInt("y");

                for (JsonObject secondary_word : words) {
                    for (JsonObject check_word : words_done) {
                        if (secondary_word == check_word) {
                            exclude = true;
                        }
                    }

                    if (!exclude) {
                        exclude = false;
                        current_word_y = secondary_word.getJsonObject("boundingPoly").getJsonArray("vertices").get(0).getInt("y");
                        if (Math.abs(current_word_y - current_line_y) <= current_line_height) {
                            current_line.add(secondary_word);
                            words_done.add(secondary_word);
                        }
                    }
                }
                lines.add(current_line);
                current_line = [];
            }
        }
        return lines;
    }




    protected void goToNextScreen() {
        System.out.println("TODO");
    }
}