package com.example.socialnextwork.activitys;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnextwork.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private  static final int RC_SIGN_IN = 100;

    EditText edtEmail_LG,edtPass_LG;
    TextView txtNotHaveAcc, txtForgotPass;
    Button btnOK_LG;
    SignInButton mGoogleLoginBtn;
    GoogleSignInClient mGoogleSingInClient;

    private FirebaseAuth mAuth;
    DatabaseReference reference;
    FirebaseUser fUser;

    // progress dialog
    ProgressDialog progressDialog;

    private boolean checkEmailVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        actionbar();
        addControl();
        addEvent();

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }

    private void addEvent() {

        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPassDialog();
            }
        });

       txtNotHaveAcc.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(Login.this, Register.class));
               finish();
           }
       });

        // init dialog
        progressDialog = new ProgressDialog(this);

       btnOK_LG.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                //input data
               String email = edtEmail_LG.getText().toString().trim();
               String pass = edtPass_LG.getText().toString().trim();

               if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
               {
                   // invalid email pattern set error
                   edtEmail_LG.setError("Invalid Email");
                   edtEmail_LG.setFocusable(true);
               }
               else {
                   // vaild email pattern
                   loginUser(email,pass);
               }
           }
       });

       //handle google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSingInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }

    private void showRecoverPassDialog() {
        // AlterDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        // set layout liner layout
        LinearLayout linearLayout = new LinearLayout(this);
        // view to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        /* sets the min width of a EditView to fit a text of n 'M' letter regardless
        of the actual text extension and text size
         */
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // input email
            String email = emailEt.getText().toString().trim();
            beginRecover(email);

            }
        });
        // buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismiss dialog
                dialog.dismiss();
            }
        });

        // show dialog
        builder.create().show();
    }

    private void beginRecover(String email) {
        progressDialog.setMessage("Sending email ...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(Login.this, "Email send", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(Login.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // get and show proper error message
                progressDialog.dismiss();
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.setMessage("Logging In ...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // dismiss dialog
                            progressDialog.dismiss();

                            verifyEmailAddress();
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                // error, get and show error message
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void verifyEmailAddress(){

        fUser = mAuth.getCurrentUser();
        checkEmailVerify = fUser.isEmailVerified();
        if(checkEmailVerify){
            //setValueDefaultForUser();
            startActivity(new Intent(Login.this, Profile.class));
            finish();
        }else {
            Toast.makeText(this, R.string.please_check_email, Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
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

    private void addControl() {
        edtEmail_LG = findViewById(R.id.edtEmail_LG);
        edtPass_LG = findViewById(R.id.edtPass_LG);
        txtNotHaveAcc = findViewById(R.id.txtAccount_LG);
        txtForgotPass = findViewById(R.id.txtForgotPass);
        btnOK_LG = findViewById(R.id.btn_loginOK);
        mGoogleLoginBtn = findViewById(R.id.btn_google);
    }

    private void actionbar() {
        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //before mAuth
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSingInClient = GoogleSignIn.getClient(this,gso);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // back don't lost data
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInINtent
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e)
            {
                // Google Sign In failed, update UI approriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null );
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();

                            // if user is signing in first time then get and show user info from google account
                            if (task.getResult().getAdditionalUserInfo().isNewUser())
                            {
                                // Get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();
                                String uri_image = user.getPhotoUrl().toString();

                                String name = user.getDisplayName();

                                // when user is registered store user info in firebase realtime database too
                                // using HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                // put info in hashMap
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name",name); // will add later (e.g.edit.profile)
                                hashMap.put("onlineStatus","online");
                                hashMap.put("typingTo","none");
                                hashMap.put("phone","default");
                                hashMap.put("image", uri_image);
                                hashMap.put("status","offline");

                                //firebase database isntance
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                // path to store user data named "Users"
                                DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
                                // put data within hashMap in database
                                databaseReference.child(uid).setValue(hashMap);
                            }

                            // show user email in toast
                            Toast.makeText(Login.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            // go to profile activity logged in
                            startActivity(new Intent(Login.this,Profile.class));
                            finish();
                            //updateUI(user);
                        }   else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(Login.this, "Login Failed ...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // get and show error message
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
