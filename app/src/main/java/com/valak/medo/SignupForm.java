package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignupForm extends AppCompatActivity {

    EditText userName, userPhone;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userProfileDB;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    String userEmail,name,phone,smsTxt;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        setupFirebaseListener();

        progressDialog = new ProgressDialog(SignupForm.this);


        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);


        findViewById(R.id.submitSignupForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Updating Profile...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                 name = userName.getText().toString();
                 phone = userPhone.getText().toString();


                userProfileDB = firestore.collection("Users").document(userEmail);

                Map<String, String> item = new HashMap<>();
                item.put("name", name);
                item.put("phone", phone);
                item.put("address", "");
                item.put("city", "");
                item.put("state", "");
                item.put("pincode", "");

                // Add a new document with a generated ID
                userProfileDB.set(item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SignupForm.this,"Profile Created Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupForm.this, HomeActivity.class));
                                smsTxt = "Hi " + name + ", Welcome to Medo, a new world of online pharmacy. Your login ID is " + userEmail ;
                                new SMSTask().execute();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignupForm.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private class SMSTask extends AsyncTask<Void, Void, Void> {
        String result;
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\r\"content\": \""+smsTxt+"\",\r\"from\": \"D7-Rapid\", \r \"to\": "+"91"+phone+"\r }");

                Request request = new Request.Builder()
                        .url("https://d7sms.p.rapidapi.com/secure/send")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("authorization", "Basic eXh5Yjk3ODQ6Qmp3YUNUalo=")
                        .addHeader("x-rapidapi-key", "c6f851075bmshca2f2a62275eca6p1eabd8jsn7bedd08b8236")
                        .addHeader("x-rapidapi-host", "d7sms.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println(response.networkResponse().toString());

            } catch (IOException e){
                e.printStackTrace();
                result = e.toString();
                System.out.println(result);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    private void setupFirebaseListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){

                    userEmail = user.getEmail();

                }else{

                    Toast.makeText(SignupForm.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupForm.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Please fill the form and try again!");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                recreate();
            } });
        alertDialog.show();
    }
}