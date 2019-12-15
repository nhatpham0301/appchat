package com.example.socialnextwork.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterFriends;
import com.example.socialnextwork.adapters.AdapterUser;
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

public class FriendsFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterFriends adapterFriends;
    List<ModelUsers> modelUsersList;
    List<String> stringListIdFriend;

    DatabaseReference reference;
    FirebaseAuth fAuth;
    FirebaseUser fUser;

    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        initView();

        fAuth = FirebaseAuth.getInstance();

        getAllFriend();

        return view;
    }

    private void searchFriend(final String str){

        fUser = fAuth.getCurrentUser();
        DatabaseReference refListIdFriend = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid())
                .child("FriendList");

        //get All data from path
        refListIdFriend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListIdFriend.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getValue().toString();
                    stringListIdFriend.add(id);
                }

                // Lay thong tin tat ca Friend tu stringListIdFriend
                if (stringListIdFriend.size() == 0)
                    return;
                else {
                    reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            modelUsersList.clear();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                ModelUsers modelUsers = data.getValue(ModelUsers.class);

                                for (String id : stringListIdFriend) {
                                    if (id.equals(modelUsers.getUid())
                                    && ( modelUsers.getName().toLowerCase().contains(str.toLowerCase()) ||
                                            modelUsers.getEmail().toLowerCase().contains(str.toLowerCase()) )) {
                                        modelUsersList.add(modelUsers);
                                    }
                                }

                                adapterFriends = new AdapterFriends(getContext(), modelUsersList);
                                recyclerView.setAdapter(adapterFriends);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllFriend() {

        fUser = fAuth.getCurrentUser();
        DatabaseReference refListIdFriend = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid())
                .child("FriendList");

        // Lay danh sach ID cua tat ca Friend
        refListIdFriend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListIdFriend.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getValue().toString();
                    stringListIdFriend.add(id);
                }

                // Lay thong tin tat ca Friend tu stringListIdFriend
                if (stringListIdFriend.size() == 0)
                    return;
                else {
                    reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            modelUsersList.clear();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                ModelUsers modelUsers = data.getValue(ModelUsers.class);

                                for (String id : stringListIdFriend) {
                                    if (id.equals(modelUsers.getUid())) {
                                        modelUsersList.add(modelUsers);
                                    }
                                }

                                adapterFriends = new AdapterFriends(getContext(), modelUsersList);
                                recyclerView.setAdapter(adapterFriends);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void initView() {

        recyclerView = view.findViewById(R.id.recycler_list_friend);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        stringListIdFriend = new ArrayList<>();
        modelUsersList = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true); //  to show menu option in fragment
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
                    searchFriend(s);
                } else {
                    // search text empty, get all users
                    getAllFriend();
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
                    searchFriend(s);
                } else {
                    // search text empty, get all users
                    getAllFriend();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

}
