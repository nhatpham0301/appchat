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
import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdapterBlockList extends RecyclerView.Adapter<AdapterBlockList.BlockHolder> {

    Context mContext;
    List<ModelUsers> modelUsersList;
    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    public AdapterBlockList(Context mContext, List<ModelUsers> modelUsersList) {
        this.mContext = mContext;
        this.modelUsersList = modelUsersList;
    }

    @NonNull
    @Override
    public BlockHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.row_lock_list, viewGroup, false);

        return new AdapterBlockList.BlockHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockHolder blockHolder, int i) {

        final String friendId = modelUsersList.get(i).getUid();

        blockHolder.name.setText(modelUsersList.get(i).getName());
        Glide.with(mContext).load(modelUsersList.get(i).getImage())
                .placeholder(R.drawable.ic_default_img)
                .into(blockHolder.avatar);
        blockHolder.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "unlock", Toast.LENGTH_SHORT).show();
                PublicFunctions.unlock(friendId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelUsersList.size();
    }

    public class BlockHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView name;
        Button unlock;

        public BlockHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatarTv);
            name = itemView.findViewById(R.id.nameTv);
            unlock = itemView.findViewById(R.id.unlock);
        }
    }
}
