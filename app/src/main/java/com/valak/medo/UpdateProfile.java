package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner city_spinner,state_spinner;

    private static final String TAG = "signout";
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userProfileDB;
    EditText euserName,euserPhone, euserAddress, euserPincode;
    String userEmail, userName, userPhone, userAddress, userCity, userState, userPincode, key;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        progressDialog = new ProgressDialog(UpdateProfile.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        setupFirebaseListener();
        euserName = findViewById(R.id.profile_username);
        euserPhone = findViewById(R.id.profile_phone);
        euserAddress = findViewById(R.id.profile_address);
        euserPincode = findViewById(R.id.profile_pincode);

        key = "p";
        key = getIntent().getStringExtra("key");

        if (user != null) {
            userEmail = user.getEmail();
        }

        getUserProfile();

        city_spinner = (Spinner) findViewById(R.id.city_spinner);
        city_spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> city_adapter = ArrayAdapter.createFromResource(this, R.array.city_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        city_adapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        city_spinner.setAdapter(city_adapter);

        state_spinner = (Spinner) findViewById(R.id.state_spinner);
        state_spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> state_adapter = ArrayAdapter.createFromResource(this, R.array.state_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        state_adapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        state_spinner.setAdapter(state_adapter);

        findViewById(R.id. update_profile__back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.update_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = euserName.getText().toString();
                userPhone = euserPhone.getText().toString();
                userAddress = euserAddress.getText().toString();
                userPincode = euserPincode.getText().toString();

                if (!(userName.isEmpty() && userPhone.isEmpty() && userAddress.isEmpty()))
                updateProfile();
                else
                    Toast.makeText(UpdateProfile.this, "Fill all information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        Map<String, String> item = new HashMap<>();
        item.put("name", userName);
        item.put("phone", userPhone);
        item.put("address", userAddress);
        item.put("city", userCity);
        item.put("state", userState);
        item.put("pincode", userPincode);

        // Add a new document with a generated ID
        userProfileDB.set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateProfile.this,"Updated Successfully", Toast.LENGTH_SHORT).show();
                        if (key.equals("os"))
                        {
                            startActivity(new Intent(UpdateProfile.this,OrderSummary.class));
                        }
                        else if (key.equals("p")){
                            startActivity(new Intent(UpdateProfile.this,Profile.class));

                        }
                        else {
                            onBackPressed();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfile.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        if(parent.getId() == R.id.city_spinner)
        {
            userCity = parent.getItemAtPosition(pos).toString();
        }
        else if(parent.getId() == R.id.state_spinner)
        {
            userState = parent.getItemAtPosition(pos).toString();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void getUserProfile() {
        userProfileDB = firestore.collection("Users").document(userEmail);

        Source source = Source.DEFAULT;

        userProfileDB.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                String name = document.get("name").toString();
                String phone = document.get("phone").toString();
                if(!(name.isEmpty() && phone.isEmpty())){
                euserName.setText(document.get("name").toString());
                euserPhone.setText(document.get("phone").toString());
                city_spinner.setSelection(5);}
                else {
                    Toast.makeText(UpdateProfile.this,"Error in getting details", Toast.LENGTH_LONG).show();
                }
                if (document.get("address") != null && document.get("pincode") != null){
                    euserAddress.setText(document.get("address").toString());
                    euserPincode.setText(document.get("pincode").toString());
                }
                progressDialog.dismiss();
            }
        });

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
                    Toast.makeText(UpdateProfile.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateProfile.this, LoginActivity.class);
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
            startActivity(new Intent(UpdateProfile.this,Profile.class));
    }
}