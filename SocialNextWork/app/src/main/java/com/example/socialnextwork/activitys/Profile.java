package com.example.socialnextwork.activitys;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.fragments.ChatListFragment;
import com.example.socialnextwork.fragments.FriendsFragment;
import com.example.socialnextwork.fragments.UserFragment;
import com.example.socialnextwork.models.ModelUsers;
import com.example.socialnextwork.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseAuth firebaseAuth;
    FirebaseUser user, fUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, reference;

    //storage
    StorageReference storageReference;
    //path where images f user profile and cover will be stored
    String storagePath = "avatar_user/";

    //progress dialog
    ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //ARRAYS OF PERMISSIONS TO BE REQUESTED
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri uri_image;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    ActionBar actionBar;

    String mUID;

    // drawerLayout navigation view
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    NavigationView navigationView;
    View header_navigation;

    EditText name_edit;
    Button ok, cancel;

    CircleImageView imgAvatar;
    TextView name, email;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
        initNavigationView();
        addControl();
        eventInNavigationView();

        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transaction (default on star)
        actionBar.setTitle("Chat"); // change actionbar title
        ChatListFragment fragment1 = new ChatListFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();


        checkUserStatus();

        // update token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void initNavigationView() {

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        header_navigation = navigationView.getHeaderView(0);

        imgAvatar = header_navigation.findViewById(R.id.drawer_imgAvatar);
        name = header_navigation.findViewById(R.id.drawer_txtName);
        email = header_navigation.findViewById(R.id.drawer_txtGmail);


        drawerLayout = findViewById(R.id.activity_main_drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void showDialogEditName() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_name);
        dialog.setCanceledOnTouchOutside(false);

        name_edit = dialog.findViewById(R.id.name);
        ok = dialog.findViewById(R.id.update);
        cancel = dialog.findViewById(R.id.cancel);

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("name");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.getValue(String.class);
                name_edit.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String value = name_edit.getText().toString();

                if (!TextUtils.isEmpty(value)) {

                    reference = FirebaseDatabase.getInstance().getReference("Users")
                            .child(fUser.getUid());

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", value);

                    reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            dialog.dismiss();
                            Toast.makeText(Profile.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            dialog.dismiss();
                            Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(Profile.this, "Please enter name...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void eventInNavigationView() {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ModelUsers user = dataSnapshot.getValue(ModelUsers.class);
                String name_string = user.getName();
                String email_string = user.getEmail();
                String image = user.getImage();

                name.setText(name_string);
                email.setText(email_string);
                Glide.with(getApplicationContext()).load(image)
                        .placeholder(R.drawable.avatar_default).into(imgAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // update avatar
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarDialog();
            }
        });

        // edit name
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialogEditName();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* This method will be called after picking image from camera or Gallery */
        if (resultCode == RESULT_OK) {
            //image is picked from gallery, get uri or image
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                uri_image = data.getData();
                uploadAvatar(uri_image);
            }

            //image is picked from camera, get uri or image
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadAvatar(uri_image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadAvatar(Uri uri) {
        // show progress

        pd.setMessage("Loading...");
        pd.show();

        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid() + System.currentTimeMillis();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(Profile.this, "Image Updated...", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(Profile.this, "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        } else {
                            //error
                            pd.dismiss();
                            Toast.makeText(Profile.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(Profile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void showAvatarDialog() {

        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    //Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {

                    //Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });

        builder.create().show();
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

    }

    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        uri_image = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /* This method called when user press Allow or Deny from permission request dialog
            here we will handle permission cases (allowed & denied)
         */


        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // picking from camera, first check if camera & storage permission allowed or not
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {

                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permissions denied
                        Toast.makeText(this, "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // picking from camera, firt check if storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {

                        //permission enabled
                        pickFromGallery();
                    } else {
                        //permissions denied
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }

    public void updateToken(String token) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private void addControl() {

        profileOrCoverPhoto = "image";

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item clicks
                    switch (menuItem.getItemId()) {
                        case R.id.nav_chat:
                            actionBar.setTitle("Chats"); // change actionbar title
                            ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;
                        case R.id.nav_friend_list:
                            //profile fragment transaction
                            //home fragment transaction
                            actionBar.setTitle("Friends"); // change actionbar title
                            FriendsFragment friendsFragment = new FriendsFragment();
                            FragmentTransaction ftFriend = getSupportFragmentManager().beginTransaction();
                            ftFriend.replace(R.id.content, friendsFragment, "");
                            ftFriend.commit();
                            return true;
                        case R.id.nav_user:
                            actionBar.setTitle("User"); // change actionbar title
                            UserFragment fragment3 = new UserFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void checkUserStatus() {
        // get current user
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // user is signed in stay here
            // set email of logged in user
            mUID = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

        } else {
            // user not signed in, go to main activity
            startActivity(new Intent(Profile.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        // check on start of app
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        PublicFunctions.showStatus("offline");
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        PublicFunctions.showStatus("online");
        super.onResume();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.info:
                break;
            case R.id.message_wait:
                Intent intentMessageWait = new Intent(getApplicationContext(), MessWait.class);
                startActivity(intentMessageWait);
                break;
            case R.id.friend_request:
                Intent intentFriendRequest = new Intent(getApplicationContext(), FriendRequestActivity.class);
                startActivity(intentFriendRequest);
                break;
            case R.id.block_list:
                Intent intentBlockList = new Intent(getApplicationContext(), BlockListActivity.class);
                startActivity(intentBlockList);
                break;
            case R.id.logout:
                PublicFunctions.showStatus("offline");
                FirebaseAuth.getInstance().signOut();
                checkUserStatus();
                break;
        }

        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
