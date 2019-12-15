package com.example.socialnextwork.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.activitys.Chat;
import com.example.socialnextwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFriends extends RecyclerView.Adapter<AdapterFriends.FriendsHolder> {

    DatabaseReference reference;
    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    Context mContext;
    List<ModelUsers> listFriend;

    public AdapterFriends(Context mContext, List<ModelUsers> listFriend) {
        this.mContext = mContext;
        this.listFriend = listFriend;
    }

    @NonNull
    @Override
    public FriendsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_users,viewGroup, false);

        return new AdapterFriends.FriendsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsHolder friendsHolder, int i) {

        //get data
        final String hisUID = listFriend.get(i).getUid();
        String userImage = listFriend.get(i).getImage();
        String userName = listFriend.get(i).getName();
        String status = listFriend.get(i).getStatus();

        //set data
        friendsHolder.mNameTv.setText(userName);
        try {
            Glide.with(mContext).load(userImage)
                    .placeholder(R.drawable.avatar_default)
                    .into(friendsHolder.mAvatarIv);

        }
        catch (Exception e) {
        }

        // show status online or offline
        if(status.equals("online")){
            friendsHolder.status.setVisibility(View.VISIBLE);
        }else {
            friendsHolder.status.setVisibility(View.GONE);
        }

        //handle item click
        friendsHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, Chat.class);
                intent.putExtra("hisUid",hisUID);
                mContext.startActivity(intent);
            }
        });

        friendsHolder.menu_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final PopupMenu popupMenu = new PopupMenu(mContext, friendsHolder.menu_option);
                popupMenu.inflate(R.menu.menu_friend_list);


                reference = FirebaseDatabase.getInstance().getReference("Flags")
                        .child(PublicFunctions.myIDandUserID(fUser.getUid(), hisUID))
                        .child("Block");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        boolean myCheck = false;
                        boolean friendCheck = false;

                        for (DataSnapshot data : dataSnapshot.getChildren()){

                            String id = data.getKey();
                            if (id.equals(fUser.getUid()))
                                myCheck = data.getValue(Boolean.class);
                            else
                                friendCheck = data.getValue(Boolean.class);
                        }

                        if(friendCheck){
                            popupMenu.getMenu().findItem(R.id.block).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.unlock).setVisible(true);
                        }else {
                            popupMenu.getMenu().findItem(R.id.block).setVisible(true);
                            popupMenu.getMenu().findItem(R.id.unlock).setVisible(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){

                            case R.id.delete_friend:
                                Toast.makeText(mContext, "Delete friend", Toast.LENGTH_SHORT).show();
                                deleteFriend(hisUID);
                                break;

                            case R.id.block:
                                Toast.makeText(mContext, "Block", Toast.LENGTH_SHORT).show();
                                PublicFunctions.block(hisUID);
                                break;

                            case R.id.unlock:
                                Toast.makeText(mContext, "Unlock", Toast.LENGTH_SHORT).show();
                                PublicFunctions.unlock(hisUID);
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void deleteFriend(String hisUID) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendList").child(hisUID);
        reference.removeValue();

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(hisUID).child("FriendList").child(fUser.getUid());
        reference.removeValue();
    }

    @Override
    public int getItemCount() {
        return listFriend.size();
    }

    public class FriendsHolder extends RecyclerView.ViewHolder {

        CircleImageView mAvatarIv, status;
        TextView mNameTv;
        ImageButton menu_option;

        public FriendsHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarTv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            menu_option = itemView.findViewById(R.id.menu_option);
            status = itemView.findViewById(R.id.status);
        }
    }
}
