package com.example.socialnextwork.activitys;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterChatList;
import com.example.socialnextwork.models.ModelChat;
import com.example.socialnextwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessWait extends AppCompatActivity {

    RecyclerView recyclerView;
    List<ModelUsers> usersList;
    List<String> messWaitList;
    AdapterChatList adapterChatList;
    DatabaseReference reference;
    FirebaseUser fUser;
    TextView txtMessWait;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_wait);

        addControl();
        addEvent();
    }

    private void addEvent() {
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("MessageWait");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messWaitList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    String id = ds.getValue().toString();
                    messWaitList.add(id);
                }
                loadChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadChat() {

        txtMessWait.setVisibility(View.GONE);
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    for (String id : messWaitList)
                    {
                        if (modelUsers.getUid() != null && modelUsers.getUid().equals(id)) {
                            usersList.add(modelUsers);
                            break;
                        }
                    }

                    if(usersList.size() == 0)
                    {
                        txtMessWait.setVisibility(View.VISIBLE);
                    } else {

                        txtMessWait.setVisibility(View.GONE);
                        adapterChatList = new AdapterChatList(MessWait.this, usersList);
                        recyclerView.setAdapter(adapterChatList);
                    }

                    for (int i =0; i < usersList.size(); i++)
                    {
                        lassMess(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lassMess(final String uid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
                .child(PublicFunctions.myIDandUserID(fUser.getUid(), uid));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lassMessage = "default";
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat == null)
                        continue;

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender ==null || receiver == null)
                        continue;

                    lassMessage = chat.getMessage();

                }
                adapterChatList.setLastMessage(uid, lassMessage);
                adapterChatList.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addControl()
    {

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Message Wait");

        recyclerView = findViewById(R.id.recyclerViewMessWait);
        recyclerView.setHasFixedSize(true);
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();
        messWaitList = new ArrayList<>();
        txtMessWait = findViewById(R.id.txtMessWait);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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
        PublicFunctions.showStatus("online");
        super.onResume();
    }
}
