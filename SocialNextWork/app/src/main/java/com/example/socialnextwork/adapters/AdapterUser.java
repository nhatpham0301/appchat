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

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference;
    Context context;
    List<ModelUsers> usersList;

    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {
        //get data
        final String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getName();
        String status = usersList.get(i).getStatus();

        //set data
        myHolder.mNameTv.setText(userName);
        try {
            Glide.with(context).load(userImage).placeholder(R.drawable.avatar_default).into(myHolder.mAvatarIv);
        } catch (Exception e) {
            Toast.makeText(context, "error get avatar", Toast.LENGTH_SHORT).show();
        }

        // show status online or offline
        if(status.equals("online")){
            myHolder.status.setVisibility(View.VISIBLE);
        }else {
            myHolder.status.setVisibility(View.GONE);
        }

        //handle item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Chat.class);
                intent.putExtra("hisUid", hisUID);
                context.startActivity(intent);
            }
        });

        myHolder.menu_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final PopupMenu popupMenu = new PopupMenu(context, myHolder.menu_option);
                popupMenu.inflate(R.menu.menu_user_list);


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Flags")
                        .child(MyUidHisUid(fUser.getUid(), hisUID))
                        .child("FriendRequest");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Boolean myCheck = false, friendCheck = false;

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String id = data.getKey();

                            if (id.equals(fUser.getUid()))
                                myCheck = data.getValue(Boolean.class);

                            else
                                friendCheck = data.getValue(Boolean.class);

                        }

                        if (myCheck.equals(true)) {

                            popupMenu.getMenu().findItem(R.id.add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.cancel_add_friend).setVisible(true);
                            popupMenu.getMenu().findItem(R.id.accept_add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.delete_friend_request).setVisible(false);
                        } else {

                            popupMenu.getMenu().findItem(R.id.add_friend).setVisible(true);
                            popupMenu.getMenu().findItem(R.id.cancel_add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.accept_add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.delete_friend_request).setVisible(false);
                        }

                        if (friendCheck.equals(true)) {

                            popupMenu.getMenu().findItem(R.id.add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.cancel_add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.accept_add_friend).setVisible(true);
                            popupMenu.getMenu().findItem(R.id.delete_friend_request).setVisible(true);

                        } else {
                            popupMenu.getMenu().findItem(R.id.accept_add_friend).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.delete_friend_request).setVisible(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                reference = FirebaseDatabase.getInstance().getReference("Flags")
                        .child(PublicFunctions.myIDandUserID(fUser.getUid(), hisUID))
                        .child("Block");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Boolean myCheck = false, friendCheck = false;

                        for (DataSnapshot data : dataSnapshot.getChildren()){

                            String id = data.getKey();
                            if (id.equals(fUser.getUid()))
                                myCheck = data.getValue(Boolean.class);
                            else
                                friendCheck = data.getValue(Boolean.class);
                        }

                        if(friendCheck.equals(true)){
                            popupMenu.getMenu().findItem(R.id.unlock).setVisible(true);
                            popupMenu.getMenu().findItem(R.id.block).setVisible(false);
                        }else {
                            popupMenu.getMenu().findItem(R.id.unlock).setVisible(false);
                            popupMenu.getMenu().findItem(R.id.block).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            case R.id.add_friend:
                                Toast.makeText(context, "Add friend", Toast.LENGTH_SHORT).show();
                                sendFriendRequest(hisUID);
                                break;

                            case R.id.cancel_add_friend:
                                Toast.makeText(context, "Cancel add friend", Toast.LENGTH_SHORT).show();
                                cancelAddFriend(hisUID);
                                break;

                            case R.id.accept_add_friend:
                                Toast.makeText(context, "Accept add friend", Toast.LENGTH_SHORT).show();
                                acceptAddFriend(hisUID);

                            case R.id.delete_friend_request:
                                Toast.makeText(context, "Delete friend request", Toast.LENGTH_SHORT).show();
                                deleteFriendRequest(hisUID);
                                break;

                            case R.id.block:
                                Toast.makeText(context, "Block", Toast.LENGTH_SHORT).show();
                                PublicFunctions.block(hisUID);
                                break;

                            case R.id.unlock:
                                Toast.makeText(context, "Unlock", Toast.LENGTH_SHORT).show();
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

    private void deleteFriendRequest(String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendRequest").child(id);
        reference.removeValue();

        // xet lai flag cho friend
        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(id);
        reference.setValue(false);
    }

    private void acceptAddFriend(String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendList").child(id);
        reference.setValue(id);

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(id).child("FriendList").child(fUser.getUid());
        reference.setValue(fUser.getUid());

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendRequest").child(id);
        reference.removeValue();

        // xet lai flag cho friend
        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(id);
        reference.setValue(false);
    }

    private void cancelAddFriend(String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(id).child("FriendRequest").child(fUser.getUid());
        reference.removeValue();

        // xet lai co cho minh
        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(fUser.getUid());
        reference.setValue(false);
    }

    private void sendFriendRequest(String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(id).child("FriendRequest").child(fUser.getUid());
        reference.setValue(fUser.getUid());

        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(fUser.getUid());
        reference.setValue(true);

        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(id);
        reference.setValue(false);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView mAvatarIv, status;
        TextView mNameTv;
        ImageButton menu_option;
        ItemClickListener itemClickListener;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarTv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            menu_option = itemView.findViewById(R.id.menu_option);
            status = itemView.findViewById(R.id.status);
        }


        @Override
        public void onClick(View v) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    private String MyUidHisUid(String myID, String hisID) {
        if (myID.compareTo(hisID) > 0)
            return myID + hisID;
        else
            return hisID + myID;
    }
}
