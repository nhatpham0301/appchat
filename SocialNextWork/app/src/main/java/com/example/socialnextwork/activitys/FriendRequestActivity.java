package com.example.socialnextwork.activitys;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterFriendRequest;
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

public class FriendRequestActivity extends AppCompatActivity {

    TextView no_request;
    RecyclerView recyclerView;
    AdapterFriendRequest adapterFriendRequest;
    List<ModelUsers> modelUsersList;
    List<String> stringListIdFriendRequest;

    FirebaseUser fUser;
    DatabaseReference reference;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        initView();
        loadData();
    }

    private void loadData() {


        DatabaseReference refListIdFriendRequest = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(fUser.getUid())
                .child("FriendRequest");

        // Lay all id trong FriendRequest
        refListIdFriendRequest.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListIdFriendRequest.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    stringListIdFriendRequest.add(data.getValue().toString());
                }


                // get data and show
                if (stringListIdFriendRequest.size() == 0) {
                    no_request.setVisibility(View.VISIBLE);
                } else {

                    reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            modelUsersList.clear();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {

                                ModelUsers modelUsers = data.getValue(ModelUsers.class);
                                for (String id : stringListIdFriendRequest) {
                                    if (id.equals(modelUsers.getUid()))
                                        modelUsersList.add(modelUsers);
                                }
                            }

                            adapterFriendRequest = new AdapterFriendRequest(getApplicationContext(), modelUsersList);
                            recyclerView.setAdapter(adapterFriendRequest);
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

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Friend Request");

        no_request = findViewById(R.id.no_request);
        recyclerView = findViewById(R.id.recycler_friend_request);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        stringListIdFriendRequest = new ArrayList<>();
        modelUsersList = new ArrayList<>();
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
