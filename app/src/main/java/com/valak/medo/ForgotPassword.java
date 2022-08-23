package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailtxt;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailtxt = findViewById(R.id.email);
        progressDialog = new ProgressDialog(ForgotPassword.this);


        findViewById(R.id.sendPasswordResetEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String email = emailtxt.getText().toString();

                if (!(email.isEmpty())) {

                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ForgotPassword.this, "Check your mail to reset password.", Toast.LENGTH_SHORT).show();

                                        AlertDialog alertDialog = new AlertDialog.Builder(ForgotPassword.this).create();
                                        alertDialog.setMessage("Password rest link has been send to your email. Please check your mail to reset password.");
                                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(ForgotPassword.this, LoginActivity.class));
                                            } });
                                        alertDialog.show();
                                    }
                                    else {
                                        progressDialog.dismiss();
                                        Snackbar.make(view, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }

                else {
                    Snackbar.make(view, "Enter Correct Email", Snackbar.LENGTH_LONG).show();
                }

            }


        });


        findViewById(R.id.loginbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPassword.this, LoginActivity.class));
            }
        });

        findViewById(R.id.createaccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPassword.this, SignupActivity.class));
            }
        });
    }
}