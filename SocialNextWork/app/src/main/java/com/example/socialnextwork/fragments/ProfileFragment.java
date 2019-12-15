package com.example.socialnextwork.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.activitys.MainActivity;
import com.example.socialnextwork.R;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifImageButton;
import pl.droidsonroids.gif.GifImageView;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where images f user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //view from xml
    CircleImageView imgAvatar;
    ImageView imgCover;
    TextView txtName, txtEmail, txtPhone, txtNameAvatar;
    FloatingActionButton fabEdit;
    GifImageButton gifImageView;

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

    View view;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);


        init();         // control firebase
        addControl();   // control view
        // Lấy thông tin hiện tại của user đăng nhập khi sử dụng gmail
        // Sử dụng orderByChild để lấy thông tin
        event();

        return view;
    }

    private void event() {
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checkc until required get data
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    // get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    //String cover = "" + ds.child("cover").getValue();


                    //set data
                    txtName.setText(name);
                    txtEmail.setText(email);
                    txtPhone.setText(phone);
                    txtNameAvatar.setText(name);

                    // imageAvatar
                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(imgAvatar);
                        //Glide.with(getActivity()).asGif().load(image).into(gifImageView);
                    }
                    catch (Exception e){
                        // if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_add_image).into(imgAvatar);
                        //Glide.with(getActivity()).asGif().load(R.drawable.ic_add_image).into(gifImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // fab button click
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

    }

    private void showEditProfileDialog() {

        // options to show in dialog
        String options[] = {"Edit profile picture",/*"Edit cover Photo",*/"Edit name","Edit Phone"};

        // alter dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Choose Action");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // handle doalog item clicks
                if (which == 0) {

                    //Edit Profile clicked
                    pd.setMessage("Updating Profile Picture");
                    //Changing profile picture, make sure to assign same value
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();

                }   else if (which == 1){

                    //Edit Name clicked
                    pd.setMessage("Updating Name");
                    //calling method and pass key "name" as parameter to update it's value in database
                    showNamePhoneUpdateDialog("name");
                }   else if (which == 2){

                    //Edit Phone clicked
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();


    }

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update" + key); // update name or phone
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter" + key); // edit name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        // add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //add button in dialog to cancle
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            }
        });
        // create and show dialog
        builder.create().show();


    }

    private void showImagePicDialog() {
        // show dialog containing options camera and gallery to pick the image

        // options to show in dialog
        String options[] = {"Camera","Gallery"};

        // alter dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Pick Image From");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // handle dialog item clicks
                if (which == 0) {

                    //Camera clicked
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else {
                            pickFromCamera();
                        }

                }   else if (which == 1){

                    //Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromallery();
                    }

                }
            }
        });

        builder.create().show();
    }

    private void addControl() {

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        imgAvatar = view.findViewById(R.id.imgAvatar);
        fabEdit = view.findViewById(R.id.fab);

        pd = new ProgressDialog(getActivity());

    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission()
    {
        //request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE );

    }

    private boolean checkCameraPermission(){
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission()
    {
        //request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /* This method called when user press Allow or Deny from permission request dialog
            here we will handle permission cases (allowed & denied)
         */


        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // picking from camera, firt check if camera & storage permission allowed or not
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {

                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
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
                        pickFromallery();
                    } else {
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* This method will be called after picking image from camera or Gallery */
        if(resultCode == RESULT_OK)
        {
            //image is picked from gallery, get uri or image
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                uri_image = data.getData();
                uploadProfileCoverPhoto(uri_image);
            }

            //image is picked from camera, get uri or image
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(uri_image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

     private void uploadProfileCoverPhoto(Uri uri) {
        // show progress

        pd.show();

        //path and name of image to be stored in firebase storage
        // storagePath = Users_Profile_Cover_Imgs/
        //ex: Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        // ex: Users_Profile_Cover_Imgs/cover_e12f3456f789.jpg

        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_"  + user.getUid() + System.currentTimeMillis();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received
                        if(uriTask.isSuccessful())
                        {
                            //image uploaded
                            //add/update url in user's database
                            HashMap<String,Object> results = new HashMap<>();
                            /*First Parameter is profileOrCoverPhoto that has value "image" or "cover"
                            which are keys in user's database where url of image will be saved in one of them
                            Second Parameter contains the url of the image stored in firebase storage
                            ,this url will be saved as value against key "image" or " over"
                             */
                            results.put(profileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfuly
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //url in database of user is added successfuly
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void pickFromallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        uri_image = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void checkUserStatus()
    {
        // get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            // user is signed in stay here
            // set email of logged in user

        }
        else {
            // user not signed in, go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //  to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    // inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

}
