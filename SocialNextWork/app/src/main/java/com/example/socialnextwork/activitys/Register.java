package com.example.socialnextwork.activitys;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnextwork.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    Button registerOK;
    EditText edtEmail, edtPass, pass2;
    TextView txtAccount;

    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    FirebaseUser fUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        actionbar();
        addControl();
        addEvent();

        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
    }

    private void addEvent() {

        txtAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

        registerOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input Email, pass
                String email = edtEmail.getText().toString().trim();
                String pass = edtPass.getText().toString().trim();
                String p2 = pass2.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEmail.setError("Invalid Email");
                    edtEmail.setFocusable(true);
                } else if (pass.length() < 6) {
                    edtPass.setError("Pass length at least 6 characters");
                    edtPass.setFocusable(true);
                } else {
                    if(!pass.equals(p2)){
                        pass2.setError("please enter password true");
                        edtPass.setFocusable(true);
                    }else {
                        registerUser(email, pass); //register the user
                    }
                }
            }

            private void registerUser(String email, String password) {
                //email and pass pattern is valid, show progress dialog and start registering user
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressDialog.dismiss();

                                    sendEmailVerificationMessage();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressDialog.dismiss();
                                    String error = task.getException().getMessage();
                                    Toast.makeText(Register.this, "ERROR: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // error, dismiss progress dialog and get and show the error message
                        progressDialog.dismiss();
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendEmailVerificationMessage(){

        fUser = mAuth.getCurrentUser();
        if(fUser != null){

            fUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        Toast.makeText(Register.this, R.string.notify_send_a_email, Toast.LENGTH_LONG).show();
                        setValueDefaultForUser();
                        startActivity(new Intent(Register.this, Login.class));
                        mAuth.signOut();
                    }else {

                        String error = task.getException().getMessage();
                        Toast.makeText(Register.this, "ERROR: " + error, Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    private void addControl() {
        registerOK = findViewById(R.id.btn_registerOK);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        txtAccount = findViewById(R.id.txtAccount);
        pass2 = findViewById(R.id.edtPass2);
    }

    private void actionbar() {
        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // back don't lost data
        return super.onSupportNavigateUp();
    }

    private void setValueDefaultForUser() {

        fUser = mAuth.getCurrentUser();
        String email = fUser.getEmail();
        String uid = fUser.getUid();

        HashMap<Object, String> hashMap = new HashMap<>();
        // put info in hashMap
        hashMap.put("email", email);
        hashMap.put("uid", uid);
        hashMap.put("name", "my name");
        hashMap.put("onlineStatus", "online");
        hashMap.put("typingTo", "none");
        hashMap.put("phone", "default");
        hashMap.put("image", "");
        hashMap.put("status","offline");

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid());
        // put data within hashMap in database
        reference.setValue(hashMap);
    }
}
