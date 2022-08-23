package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


public class Profile extends AppCompatActivity {

    private static final String TAG = "signout";
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userProfileDB;
    String userEmail;
    TextView name,email;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupFirebaseListener();
        name = findViewById(R.id.profile_username);
        email = findViewById(R.id.profile_email);

        progressDialog = new ProgressDialog(Profile.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        if (user != null) {
            userEmail = user.getEmail();
        }

        getUserProfile();

        findViewById(R.id.myorders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,Orders.class));
            }
        });

        findViewById(R.id.mycart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,Cart.class));
            }
        });

        findViewById(R.id.updateProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this,UpdateProfile.class);
                intent.putExtra("key","p");
                startActivity(intent);
            }
        });

        findViewById(R.id.referthisapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                referApp();
            }
        });
        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        findViewById(R.id.homebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,HomeActivity.class));
            }
        });

        findViewById(R.id.upload_prescription).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,PrescriptionUpload.class));
            }
        });

        findViewById(R.id.profilebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,Profile.class));
            }
        });

        findViewById(R.id.cartbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this,Cart.class));
            }
        });
    }

    private void getUserProfile() {
        userProfileDB = firestore.collection("Users").document(userEmail);

        Source source = Source.DEFAULT;

        userProfileDB.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                name.setText("Hello, " + Objects.requireNonNull(document.get("name")).toString());
                email.setText(userEmail);
            }
        });

        progressDialog.dismiss();

    }

    private void referApp() {
        Log.i("Refer this app", "");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("image/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Get medicines at your doorstep.");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Now get Medicines at your door steps. Download Medico, a new world of online pharmacy. Download the app Now.\n https://play.google.com/store/apps/details?id=com.valak.medo");

        Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(),
                BitmapFactory.decodeResource(getResources(), R.drawable.b1), null, null));
        emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        try {
            startActivity(Intent.createChooser(emailIntent, "Refer Medico to your friends and family"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Profile.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("IntentReset")
    private void sendEmail() {
        Log.i("Send Email", "");
        String[] TO = {"vaibhav.kakde19@vit.edu"};

        Intent email = new Intent(Intent.ACTION_SEND);
        email.setData(Uri.parse("mailto:"));
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_EMAIL, TO);
        email.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        email.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(email, "Send Email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Profile.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupFirebaseListener(){
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getEmail());
                }else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(Profile.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Profile.this, LoginActivity.class);
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
}