package com.ocrsplitter.ocrsplitter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ArrayList<ReceiptItem> ReceiptList;


    Button loginButton;
    Button registerButton;

    EditText userNameET;
    EditText passwordET;

    TextView errorNameET;
    TextView errorPasswordET;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
                } else {
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        mAuth.addAuthStateListener(mAuthListener);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ocrsplitter.ocrsplitter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ocrsplitter.ocrsplitter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
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
                        } else {
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
                        } else {
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

        ArrayList words = new ArrayList();

        for (Object word : text_data) {
            words.add(word);
        }

        return words;
    }

    /**
    protected ArrayList selectLines(ArrayList<JsonObject> words) {
        ArrayList lines;
        ArrayList current_line;

        ArrayList<JsonObject> words_done;

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

                current_line.add(primary_word);
                words_done.add(primary_word);

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
                current_line =[];
            }
        }
        return lines;
    }
     */

    //public void getTextData(String filename)


    protected void goToNextScreen() {
        System.out.println("Next Screen");
        Intent goToNextScreen = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(goToNextScreen);
    }

    public void createReceipt(JSONObject obj) {
        //if obj.getJSONArray("textAnnotations").get
    }
}
