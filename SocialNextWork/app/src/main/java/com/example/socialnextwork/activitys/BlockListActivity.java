package com.example.socialnextwork.activitys;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.AdapterBlockList;
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

public class BlockListActivity extends AppCompatActivity {

    TextView no_lock;
    RecyclerView recyclerView;
    AdapterBlockList adapterBlockList;
    List<ModelUsers> modelUsersList;
    List<String> stringListIdBlock;

    DatabaseReference reference;
    FirebaseUser fUser;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        initView();
        getBlockList();
    }

    private void getBlockList() {

        DatabaseReference refListIdFriend = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid())
                .child("BlockList");


        refListIdFriend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                stringListIdBlock.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getValue().toString();
                    stringListIdBlock.add(id);
                }

                // Lay thong tin tat ca Friend tu stringListIdFriend
                if (stringListIdBlock.size() == 0) {
                    no_lock.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    no_lock.setVisibility(View.GONE);
                    reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            modelUsersList.clear();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                ModelUsers modelUsers = data.getValue(ModelUsers.class);

                                for (String id : stringListIdBlock) {
                                    if (id.equals(modelUsers.getUid())) {
                                        modelUsersList.add(modelUsers);
                                    }
                                }

                                adapterBlockList = new AdapterBlockList(getApplicationContext(), modelUsersList);
                                recyclerView.setAdapter(adapterBlockList);
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

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Block List");

        no_lock = findViewById(R.id.no_lock);
        recyclerView = findViewById(R.id.recycler_block_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        modelUsersList = new ArrayList<>();
        stringListIdBlock = new ArrayList<>();
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
