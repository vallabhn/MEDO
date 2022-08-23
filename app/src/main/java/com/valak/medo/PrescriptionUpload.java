package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;

public class PrescriptionUpload extends AppCompatActivity {

    FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference().child(firebaseAuth.getEmail());
    static Uri uri;
    long progresscom;
    private int PICK_IMAGE_REQUEST = 1;
    String patient,doctor,key;
    EditText Patient,Doctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_upload);


        Button btnChoose = findViewById(R.id.btnChoose);
        Button btnupload = findViewById(R.id.btnUpload);


        btnChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });

        btnupload.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    uploadImage();
                }
            });
        }

    public void chooseImage() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));

                    ImageView imageView = findViewById(R.id.imgView);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
    }

    public void uploadImage(){

        final ProgressDialog progressDialog = new ProgressDialog(PrescriptionUpload.this);
        progressDialog.setTitle("Uploading Prescription...");

        Patient = findViewById(R.id.Patient);
        Doctor = findViewById(R.id.Doctor);
        patient=Patient.getText().toString();
        doctor=Doctor.getText().toString();
        key = patient+" at "+ Calendar.getInstance().getTime();

        if (validateForm(patient,doctor)) {

            StorageMetadata storageMetadata = new StorageMetadata.Builder().setCustomMetadata(patient, "by Dr. " + doctor).build();
            if (uri != null) {
                storageReference.child(key).putFile(uri, storageMetadata)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(final UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.show();
                                progresscom = taskSnapshot.getTotalByteCount() / 1000;
                                progressDialog.setMessage(progresscom + "KB");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(PrescriptionUpload.this, "Prescription uploaded successfully!!!", Toast.LENGTH_SHORT).show();

                                AlertDialog alertDialog = new AlertDialog.Builder(PrescriptionUpload.this).create();
                                alertDialog.setCancelable(false);
                                alertDialog.setTitle("Upload Successful!");
                                alertDialog.setMessage("We are processing your order. Grab more items with just few clicks.");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Shop More", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {

                                        startActivity(new Intent(PrescriptionUpload.this, HomeActivity.class));

                                    }
                                });
                                alertDialog.show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(PrescriptionUpload.this, "Try again after sometime.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(PrescriptionUpload.this, "Choose Image", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private boolean validateForm(String patient, String doctor) {

        if (patient.isEmpty() || doctor.isEmpty()){
            if(patient.isEmpty())
                Patient.setError("Required");
            if(doctor.isEmpty())
                Doctor.setError("Required");

            Toast.makeText(PrescriptionUpload.this, "Please fill all details", Toast.LENGTH_SHORT).show();

        }
        else {
            return true;
        }

        return false;
    }


}