package com.example.socialnextwork.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.socialnextwork.activitys.MainActivity;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterUser;
import com.example.socialnextwork.models.ModelGroupChat;
import com.example.socialnextwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUsers> usersList;
    List<String> stringListId;

    FirebaseAuth firebaseAuth;

    View view;

    String uidUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_user, container, false);

        addControl();

        return view;
    }

    private void addControl() {

        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // init user list
        usersList = new ArrayList<>();
        stringListId = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser userUid = firebaseAuth.getCurrentUser();
        uidUser = userUid.getUid();

        // getAll user
        getAllUsers();
    }

    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        //get All data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }
                }

                DatabaseReference refGetListId = FirebaseDatabase.getInstance().getReference("Users")
                        .child(fUser.getUid()).child("FriendList");

                refGetListId.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        stringListId.clear();

                        for (DataSnapshot data : dataSnapshot.getChildren()){

                            String id = data.getValue(String.class);

                            for (int i = 0; i < usersList.size(); i ++)
                            {
                                if (id.equals(usersList.get(i).getUid())){
                                    usersList.remove(usersList.get(i));
                                }
                            }
                        }

                        adapterUser = new AdapterUser(getContext(), usersList);
                        recyclerView.setAdapter(adapterUser);
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

    private void searchUsers(final String search) {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        //get All data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        if(modelUsers.getName().toLowerCase().contains(search.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(search.toLowerCase())){

                            usersList.add(modelUsers);
                        }
                    }
                }

                DatabaseReference refGetListId = FirebaseDatabase.getInstance().getReference("Users")
                        .child(fUser.getUid()).child("FriendList");

                refGetListId.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        stringListId.clear();

                        for (DataSnapshot data : dataSnapshot.getChildren()){

                            String id = data.getValue(String.class);

                            for (int i = 0; i < usersList.size(); i ++)
                            {
                                if (id.equals(usersList.get(i).getUid())){
                                    usersList.remove(usersList.get(i));
                                }
                            }
                        }

                        adapterUser = new AdapterUser(getContext(), usersList);
                        recyclerView.setAdapter(adapterUser);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //  to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    // search menu
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
                    searchUsers(s);
                } else {
                    // search text empty, get all users
                    getAllUsers();
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
                    searchUsers(s);
                } else {
                    // search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

}
