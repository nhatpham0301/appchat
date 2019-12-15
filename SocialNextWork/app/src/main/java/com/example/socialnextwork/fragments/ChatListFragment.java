package com.example.socialnextwork.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {


    private DatabaseReference reference;
    private FirebaseUser fUser;

    View view;
    List<ModelUsers> modelUsersList;
    List<String> stringListID;
    List<String> stringListMessWait;
    AdapterChatList adapterChatList;
    RecyclerView recyclerView;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        initVariable();
        initView();
        loadChatList();
        return view;
    }

    private void loadChatList() {

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){

                    for (DataSnapshot data1 : data.getChildren()){

                        ModelChat modelChat = data1.getValue(ModelChat.class);
                        if(modelChat.getSender().equals(fUser.getUid())){
                            reference = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(fUser.getUid()).child("MessageWait")
                                    .child(modelChat.getReceiver());
                            reference.removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListID.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getKey();
                    if (id.indexOf(fUser.getUid()) != -1) {
                        String idReplace = id.replace(fUser.getUid(), "");

                        stringListID.add(idReplace);
                    }
                }

                getUserNotMessWait(stringListID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserNotMessWait(final List<String> stringListID) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("MessageWait");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    String id = data.getValue().toString();

                    for (int i = 0; i < stringListID.size(); i ++){
                        if (stringListID.get(i).equals(id)){
                            stringListID.remove(i);
                        }
                    }
                }

                getUserChatList(stringListID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserChatList(final List<String> listUserID) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelUsersList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ModelUsers user = snapshot.getValue(ModelUsers.class);

                    // display 1 user form chats
                    for (String id : listUserID) {
                        if (user.getUid().equals(id)) {
                            modelUsersList.add(user);
                        }
                    }
                }

                adapterChatList = new AdapterChatList(getContext(), modelUsersList);
                recyclerView.setAdapter(adapterChatList);

                for (int i = 0; i < modelUsersList.size(); i++) {
                    lastMessage(modelUsersList.get(i).getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String uid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
                .child(PublicFunctions.myIDandUserID(fUser.getUid(), uid));

        Query query = reference.orderByKey().limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String lassMessage = "default";

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat == null)
                        continue;

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender == null || receiver == null)
                        continue;

                    if(chat.getType().equals("text"))
                        lassMessage = chat.getMessage();

                    if(chat.getType().equals("image") && chat.getSender().equals(fUser.getUid()))
                        lassMessage = "You have sent a picture";

                    if(chat.getType().equals("image") && chat.getSender().equals(uid))
                        lassMessage = "You get a picture";

                    if(chat.getType().equals("sticker") && chat.getSender().equals(fUser.getUid()))
                        lassMessage = "You have sent a sticker";

                    if(chat.getType().equals("sticker") && chat.getSender().equals(uid))
                        lassMessage = "You get a sticker";

                }

                adapterChatList.setLastMessage(uid, lassMessage);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initView() {

        recyclerView = view.findViewById(R.id.recycler_chat_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void initVariable() {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        modelUsersList = new ArrayList<>();
        stringListID = new ArrayList<>();
        stringListMessWait = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true); //  to show menu option in fragment
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);

        //Search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim()))
                {
                    //search text contains text, search it
                    searchChat(s);
                } else {
                    // search text empty, get all users
                    loadChatList();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim()))
                {
                    //search text contains text, search it
                    searchChat(s);
                } else {
                    // search text empty, get all users
                    loadChatList();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    private void searchChat(final String s) {

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){

                    for (DataSnapshot data1 : data.getChildren()){

                        ModelChat modelChat = data1.getValue(ModelChat.class);
                        if(modelChat.getSender().equals(fUser.getUid())){
                            reference = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(fUser.getUid()).child("MessageWait")
                                    .child(modelChat.getReceiver());
                            reference.removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListID.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getKey();
                    if (id.indexOf(fUser.getUid()) != -1) {
                        String idReplace = id.replace(fUser.getUid(), "");

                        stringListID.add(idReplace);
                    }
                }

                reference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(fUser.getUid()).child("MessageWait");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()){
                            String id = data.getValue().toString();

                            for (int i = 0; i < stringListID.size(); i ++){
                                if (stringListID.get(i).equals(id)){
                                    stringListID.remove(i);
                                }
                            }
                        }

                        getUserChatListSearch(stringListID, s);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserChatListSearch(final List<String> stringListID, final String s) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelUsersList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ModelUsers user = snapshot.getValue(ModelUsers.class);

                    // display 1 user form chats
                    for (String id : stringListID) {
                        if (user.getUid().equals(id) && ( user.getName().toLowerCase().contains(s.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(s.toLowerCase()) )) {
                            modelUsersList.add(user);
                        }
                    }
                }

                adapterChatList = new AdapterChatList(getContext(), modelUsersList);
                recyclerView.setAdapter(adapterChatList);

                for (int i = 0; i < modelUsersList.size(); i++) {
                    lastMessage(modelUsersList.get(i).getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
