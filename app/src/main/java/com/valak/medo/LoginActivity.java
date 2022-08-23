package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailtxt,passwordtxt;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressDialog progressDialog;
    TextView forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(LoginActivity.this);

        mAuth = FirebaseAuth.getInstance();

        emailtxt = findViewById(R.id.email);
        passwordtxt = findViewById(R.id.password);
        forgot_password = findViewById(R.id.forgot_password);

        if (user != null){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });

        findViewById(R.id.createaccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        findViewById(R.id.loginnow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String email = emailtxt.getText().toString();
                String password = passwordtxt.getText().toString();

                if (validateForm(view,email,password)) {

                    progressDialog.setMessage("Logging in...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                finish();
                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                progressDialog.dismiss();
                                Snackbar.make(view, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }


        });

        }

    private boolean validateForm(View view, String email, String password) {
        if (email.isEmpty() || password.isEmpty()){
            if(email.isEmpty())
                emailtxt.setError("Required");
                Snackbar.make(view, "Enter Correct Username/Password", Snackbar.LENGTH_LONG).show();
            if(password.isEmpty())
                passwordtxt.setError("Required");
                Snackbar.make(view, "Enter Correct Username/Password", Snackbar.LENGTH_LONG).show();
        }
        else if (password.length() < 6){
            Snackbar.make(view, "Enter Correct Username/Password", Snackbar.LENGTH_LONG).show();
        }
        else {
            return true;
        }

        return false;
    }

}
