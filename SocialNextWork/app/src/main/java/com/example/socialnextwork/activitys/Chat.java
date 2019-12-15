package com.example.socialnextwork.activitys;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterChat;
import com.example.socialnextwork.models.ModelChat;
import com.example.socialnextwork.models.ModelUsers;
import com.example.socialnextwork.notifications.APIService;
import com.example.socialnextwork.notifications.Client;
import com.example.socialnextwork.notifications.Data;
import com.example.socialnextwork.notifications.Response;
import com.example.socialnextwork.notifications.Sender;
import com.example.socialnextwork.notifications.Token;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class Chat extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    TextView txtName, txtUserStatus, txtBlock;
    ImageView profileIv;
    EditText editMessage;
    ImageButton btnSend, btnChatCamera, btnChatPhoto, btnStickerChat;
    LinearLayout chatLayout;

    String hisUid;
    public String myUid;
    String hisImage;
    String type;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    APIService apiService;
    boolean notify = false;

    String cameraPermissions[];
    String storagePermissions[];
    FirebaseUser user;

    Uri uri_image;
    String fileChatPhoto = "PhotoChat/";
    String fileSticker = "Sticker/";

    StorageReference storageReference;
    String profileOrCoverPhoto;
    DatabaseReference databaseReference;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int STICKER_PICK = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        addControl();
        addEvent();
    }

    private void addEvent() {
        FirebaseUser myUser = firebaseAuth.getInstance().getCurrentUser();

        myUid = myUser.getUid();
        // send Uid his form Users click
        final Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        // search user to get that users's info
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required info is received
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name ="" + ds.child("name").getValue();
                    hisImage ="" + ds.child("image").getValue();
                    String typingStatus ="" + ds.child("typingTo").getValue();
                    String onlineStatus = "" + ds.child("onlineStatus").getValue();

                    if(typingStatus.equals(myUid))
                    {
                        txtUserStatus.setText("typing...");
                    } else {

                        if(onlineStatus.equals("online")) {
                            txtUserStatus.setText(onlineStatus);
                        } else {
                            // covert timestamp to proper time date
                            //convert time stamp to dd//mm//yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            txtUserStatus.setText("Last seen at: " + dateTime);
                        }
                    }


                    //set data
                    txtName.setText(name);

                    try {
                        //image received, set it to imageview
                        //Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);
                        Glide.with(Chat.this).load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);

                    } catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;
                // get text from edit text
                String message = editMessage.getText().toString().trim();
                // check if text is empty or not
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(Chat.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    //text not empty
                    type = "text";
                    sendMessage(message);
                }

                //reset edit text after sending message
                editMessage.setText("");
            }
        });

        //check edit text change
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0)
                {
                    checkTypingStatus("none");
                    btnStickerChat.setVisibility(View.VISIBLE);
                    btnChatCamera.setVisibility(View.VISIBLE);
                    btnChatPhoto.setVisibility(View.VISIBLE);

                } else {
                    checkTypingStatus(hisUid);
                    btnStickerChat.setVisibility(View.GONE);
                    btnChatCamera.setVisibility(View.GONE);
                    btnChatPhoto.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnChatCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!checkCameraPermission()){
                    requestCameraPermission();
                }
                else {
                    pickFromCamera();
                }
            }
        });

        btnChatPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!checkStoragePermission()){
                    requestStoragePermission();
                }
                else {
                    pickFromallery();
                }

            }
        });

        btnStickerChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Chat.this, StickerSelect.class);
                startActivityForResult(intent1, STICKER_PICK);
            }
        });

        readMessages();

        seenMessages();

        eventBlock();
    }

    private void eventBlock() {

        final List<String> listidBlock = new ArrayList<>();

        chatLayout.setVisibility(View.VISIBLE);

        txtBlock.setVisibility(View.GONE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(myUid).child("BlockList");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listidBlock.clear();

                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    listidBlock.add(data.getValue().toString());
                }

                if(listidBlock.size() == 0) {

                    return;
                } else {

                    for(String id : listidBlock)
                    {
                        if(id.equals(hisUid))
                        {
                            chatLayout.setVisibility(View.GONE);
                            txtBlock.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users")
                .child(hisUid).child("BlockList");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listidBlock.clear();

                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    listidBlock.add(data.getValue().toString());
                }

                if(listidBlock.size() == 0) {

                    return;
                } else {

                    for(String id : listidBlock)
                    {
                        if(id.equals(myUid))
                        {
                            chatLayout.setVisibility(View.GONE);
                            txtBlock.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pickFromallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission()
    {
        //request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE );

    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        uri_image = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void requestCameraPermission()
    {
        //request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE );

    }

    private boolean checkCameraPermission(){
        //check if storage permission is enabled or not
        // return true if enable
        // return false if not enabled

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
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
                        pickFromallery();
                    } else {
                        //permissions denied
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
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

            if(requestCode == STICKER_PICK)
            {
                int sticker = data.getIntExtra("Stiker_Chat", 0);
                String send_Sticker = sticker + "";
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                String timestamp = String.valueOf(System.currentTimeMillis());

                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("sender", myUid);
                hashMap.put("receiver",hisUid);
                hashMap.put("message",send_Sticker);
                hashMap.put("timestamp",timestamp);
                hashMap.put("isSeen",false);
                hashMap.put("type","sticker");
                databaseReference.child("Chats").child(MyUidHisUid(myUid,hisUid)).push().setValue(hashMap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void uploadProfileCoverPhoto(Uri uri) {
        // show progress
        pd.setMessage("Loading...");
        pd.show();

        user = firebaseAuth.getCurrentUser();


        //path and name of image to be stored in firebase storage
        // storagePath = Users_Profile_Cover_Imgs/
        //ex: Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        // ex: Users_Profile_Cover_Imgs/cover_e12f3456f789.jpg

        String filePathAndName = fileChatPhoto + "" + myUid + System.currentTimeMillis();

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
                            String messPhoto = downloadUri.toString();
                            type = "image";
                            sendMessage(messPhoto);
                            pd.dismiss();
                            Toast.makeText(Chat.this, "Upload", Toast.LENGTH_SHORT).show();


                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(Chat.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog
                        Toast.makeText(Chat.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private String MyUidHisUid(String myID, String hisID)
    {
        if(myID.compareTo(hisID) > 0)
            return myID + hisID;
        else
            return hisID + myID;
    }

    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats")
                .child(MyUidHisUid(myUid,hisUid));
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
//                  //  ModelChat chat = ds.getValue(ModelChat.class);
                     if(myUid != null) {
                    HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                    hasSeenHashMap.put("isSeen",true);
                    ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats").child(MyUidHisUid(myUid,hisUid));
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    //if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                    //      chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid) ){
                    chatList.add(chat);
                    //}

                    adapterChat = new AdapterChat(Chat.this, chatList, hisImage, hisUid);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        hashMap.put("type",type);
        databaseReference.child("Chats").child(MyUidHisUid(myUid,hisUid)).push().setValue(hashMap);


        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers user = dataSnapshot.getValue(ModelUsers.class);
                if(notify)
                {
                    senNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Chat list
        final DatabaseReference dbChatList = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid).child(hisUid);
        dbChatList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists())
                    dbChatList.child("id").setValue(hisUid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference dbChatList1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(hisUid).child(myUid);
        dbChatList1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    dbChatList1.child("id").setValue(myUid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // Message Wait
        DatabaseReference dbFriend = FirebaseDatabase.getInstance().getReference("Users")
                .child(myUid).child("FriendList");
        dbFriend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Boolean flag = false;

                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String id = ds.getValue().toString();
                    if(hisUid.equals(id))
                    {
                        flag = true;
                        return;
                    }
                }

                if(!flag.equals(true))
                {
                    DatabaseReference dbMessWait = FirebaseDatabase.getInstance().getReference("Users")
                            .child(hisUid).child("MessageWait").child(myUid);
                    dbMessWait.setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void senNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name + ":" + message, "New Message", hisUid, R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(Chat.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addControl() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        txtName = findViewById(R.id.txtNameChat);
        txtUserStatus = findViewById(R.id.txtUserStatus);
        txtBlock = findViewById(R.id.txtBlock);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnChatCamera = findViewById(R.id.btn_ChatCamera);
        btnChatPhoto = findViewById(R.id.btn_PhotoCamera);
        btnStickerChat = findViewById(R.id.btn_StickerChat);
        chatLayout = findViewById(R.id.chatLayout);
        pd = new ProgressDialog(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        /* On clicking user form users list we have passed that user's
        UID using intent. So get that uid here to get the profile picture,
        name and start chat with that
         */

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storageReference = getInstance().getReference();
        profileOrCoverPhoto = "image";
        databaseReference = firebaseDatabase.getReference("Users");



    }

    private void checkUserStatus()
    {
        // get current user
        FirebaseUser user1 = firebaseAuth.getInstance().getCurrentUser();
        if(user1 != null)
        {
            myUid = user1.getUid();

        }
        else {
            // user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status)
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing)
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestamp = String.valueOf(System.currentTimeMillis());

        checkOnlineStatus(timestamp);
        checkTypingStatus("none");
        userRefForSeen.removeEventListener(seenListener);

        PublicFunctions.showStatus("offline");
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        PublicFunctions.showStatus("online");
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide searchView, as we don't need it here
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
