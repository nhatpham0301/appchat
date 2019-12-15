package com.example.socialnextwork.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.R;
import com.example.socialnextwork.activitys.Chat;
import com.example.socialnextwork.models.ModelUsers;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.ChatListHolder> {

    Context mContext;
    List<ModelUsers> modelUsersList;
    HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context mContext, List<ModelUsers> modelUsersList) {
        this.mContext = mContext;
        this.modelUsersList = modelUsersList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_list, viewGroup, false);
        return new AdapterChatList.ChatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder chatListHolder, int i) {

        final String hisUid = modelUsersList.get(i).getUid();
        String name = modelUsersList.get(i).getName();
        String image = modelUsersList.get(i).getImage();
        String status = modelUsersList.get(i).getStatus();

        String lastMessage = lastMessageMap.get(hisUid);


        chatListHolder.name.setText(name);
        Glide.with(mContext).load(image).placeholder(R.drawable.avatar_default).into(chatListHolder.avatar);

        if (status.equals("online")){
            chatListHolder.status.setVisibility(View.VISIBLE);
        }else{
            chatListHolder.status.setVisibility(View.GONE);
        }

        if(lastMessage == null || lastMessage.equals("default")){

            chatListHolder.lastMessage.setVisibility(View.GONE);
        }else {
            chatListHolder.lastMessage.setVisibility(View.VISIBLE);
            chatListHolder.lastMessage.setText(lastMessage);
        }

        chatListHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, Chat.class);
                intent.putExtra("hisUid",hisUid);
                mContext.startActivity(intent);
            }
        });
    }

    public void setLastMessage(String id, String message){

        lastMessageMap.put(id, message);
    }

    @Override
    public int getItemCount() {
        return modelUsersList.size();
    }

    public class ChatListHolder extends RecyclerView.ViewHolder {

        CircleImageView avatar, status;
        TextView name, lastMessage;

        public ChatListHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatarTv);
            status = itemView.findViewById(R.id.status);
            name = itemView.findViewById(R.id.nameTv);
            lastMessage = itemView.findViewById(R.id.last_message);
        }
    }
}
