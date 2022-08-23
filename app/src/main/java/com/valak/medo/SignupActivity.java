package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private EditText signupemail,signuppassword, cnfsignuppassword;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        progressDialog = new ProgressDialog(SignupActivity.this);
        mAuth = FirebaseAuth.getInstance();
        signupemail = findViewById(R.id.emailsignup);
        signuppassword = findViewById(R.id.passwordsignup);
        cnfsignuppassword = findViewById(R.id.cnfpasswordsignup);

        findViewById(R.id.alreadyhaveaccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, ForgotPassword.class));
            }
        });

        findViewById(R.id.signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String emailsignup = signupemail.getText().toString();
                String passwordsignup = signuppassword.getText().toString();
                String cnfpasswordsignup = cnfsignuppassword.getText().toString();

                if (validateForm(view,emailsignup,passwordsignup,cnfpasswordsignup))
               {
                   progressDialog.setMessage("Creating Account...");
                   progressDialog.setCancelable(false);
                   progressDialog.show();

                   mAuth.createUserWithEmailAndPassword(emailsignup, passwordsignup)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Please fill Profile.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, SignupForm.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressDialog.dismiss();
                                    Snackbar.make(view, "Account Creation Failed.", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });}

            }
        });
    }

    private boolean validateForm(View view, String emailsignup, String passwordsignup, String cnfpasswordsignup) {

        if (emailsignup.isEmpty() || passwordsignup.isEmpty() || cnfpasswordsignup.isEmpty()){
            if(emailsignup.isEmpty())
                signupemail.setError("Required");
            if(passwordsignup.isEmpty())
                signuppassword.setError("Required");
            if(cnfpasswordsignup.isEmpty())
                cnfsignuppassword.setError("Required");
        }
        else if (passwordsignup.length() < 6){
            Snackbar.make(view, "Password length must be at least 6.", Snackbar.LENGTH_LONG).show();
        }
        else if (!(passwordsignup.equals(cnfpasswordsignup))) {
            signuppassword.setError("Password doesn't match.");
            cnfsignuppassword.setError("Password doesn't match.");
            Snackbar.make(view, "Password doesn't match. Try again.", Snackbar.LENGTH_LONG).show();
        }
        else {
            return true;
        }

        return false;
    }

}