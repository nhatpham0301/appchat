package com.example.socialnextwork.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.R;
import com.example.socialnextwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterFriendRequest extends RecyclerView.Adapter<AdapterFriendRequest.RequestHolder> {

    Context mContext;
    List<ModelUsers> modelUsersList;

    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference;

    public AdapterFriendRequest(Context mContext, List<ModelUsers> modelUsersList) {
        this.mContext = mContext;
        this.modelUsersList = modelUsersList;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_friend_request,viewGroup, false);

        return new AdapterFriendRequest.RequestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder requestHolder, int i) {

        requestHolder.name.setText(modelUsersList.get(i).getName());
        Glide.with(mContext).load(modelUsersList.get(i).getImage())
                .placeholder(R.drawable.avatar_default)
                .into(requestHolder.avatar);

        final String idFriend = modelUsersList.get(i).getUid();

        requestHolder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "Accept", Toast.LENGTH_SHORT).show();
                doAccept(idFriend);
            }
        });

        requestHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "Delete", Toast.LENGTH_SHORT).show();
                doDelete(idFriend);
            }
        });
    }

    private void doDelete(String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendRequest").child(id);
        reference.removeValue();

        // xet lai flag cho friend
        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(id);
        reference.setValue(false);
    }

    private void doAccept(final String id) {

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendList");
        reference.push().setValue(id);

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(id).child("FriendList");
        reference.push().setValue(fUser.getUid());

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("FriendRequest").child(id);
        reference.removeValue();

        // xet lai co cho friend
        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(MyUidHisUid(fUser.getUid(), id)).child("FriendRequest")
                .child(id);
        reference.setValue(false);
    }

    @Override
    public int getItemCount() {
        return modelUsersList.size();
    }

    public class RequestHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView name;
        Button accept, delete;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatarTv);
            name = itemView.findViewById(R.id.nameTv);
            accept = itemView.findViewById(R.id.accept);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    private String MyUidHisUid(String myID, String hisID) {
        if (myID.compareTo(hisID) > 0)
            return myID + hisID;
        else
            return hisID + myID;
    }
}
