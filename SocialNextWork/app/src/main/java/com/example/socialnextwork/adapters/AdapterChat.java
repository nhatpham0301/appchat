package com.example.socialnextwork.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.R;
import com.example.socialnextwork.activitys.ShowDown_Image;
import com.example.socialnextwork.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{
    public boolean flag = false;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatlist;
    String imageUrl;
    String hisUid;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatlist, String imageUrl, String hisUid) {
        this.context = context;
        this.chatlist = chatlist;
        this.imageUrl = imageUrl;
        this.hisUid = hisUid;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //row_left for receiver, row_right for sender
        if(i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,viewGroup,false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,viewGroup,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        String message = chatlist.get(i).getMessage();
        String timeStamp = chatlist.get(i).getTimestamp();

        //convert time stamp to dd//mm//yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        myHolder.imaChatPhoto.setVisibility(View.GONE);
        myHolder.txtMessage.setVisibility(View.GONE);
        myHolder.txtSeen.setVisibility(View.GONE);
        myHolder.txtSeenImage.setVisibility(View.GONE);
        myHolder.txtTime.setVisibility(View.GONE);
        myHolder.txtTimeImage.setVisibility(View.GONE);
        myHolder.imaSticker.setVisibility(View.GONE);

        //set data
        if(chatlist.get(i).getType().equals("text")) {

            myHolder.txtMessage.setVisibility(View.VISIBLE);
            myHolder.txtSeen.setVisibility(View.VISIBLE);
            //myHolder.txtTime.setVisibility(View.VISIBLE);

            myHolder.txtMessage.setText(message);
            myHolder.txtTime.setText(dateTime);
        }
        else if(chatlist.get(i).getType().equals("image")) {

            myHolder.imaChatPhoto.setVisibility(View.VISIBLE);
            myHolder.txtSeenImage.setVisibility(View.VISIBLE);
            //myHolder.txtTimeImage.setVisibility(View.VISIBLE);

            Glide.with(context).load(chatlist.get(i).getMessage()).into(myHolder.imaChatPhoto);
            myHolder.txtTimeImage.setText(dateTime);
        } else if(chatlist.get(i).getType().equals("sticker")) {

            myHolder.imaSticker.setVisibility(View.VISIBLE);

            Glide.with(context).load(Integer.parseInt(chatlist.get(i).getMessage())).into(myHolder.imaSticker);

        }


        try {

            Picasso.get().load(imageUrl).into(myHolder.profileIv);
        } catch (Exception e) {

        }

        //click to show delete dialog
        myHolder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                // delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });

                //cancle delete
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
                return false;
            }
        });
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(flag == false) {

                    if (chatlist.get(i).getType().equals("text")) {
                        myHolder.txtTime.setVisibility(View.VISIBLE);
                    } else if (chatlist.get(i).getType().equals("image")) {
                        myHolder.txtTimeImage.setVisibility(View.VISIBLE);
                    }
                    flag = true;
                } else {
                    if (chatlist.get(i).getType().equals("text")) {
                        myHolder.txtTime.setVisibility(View.GONE);
                    } else if (chatlist.get(i).getType().equals("image")) {
                        myHolder.txtTimeImage.setVisibility(View.GONE);
                    }
                    flag = false;
                }

            }
        });

        myHolder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                // delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });

                //cancle delete
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

                return false;
            }
        });


        if(i == chatlist.size() - 1) {
            if(chatlist.get(i).isSeen())
            {
                myHolder.txtSeen.setText("Seen");
            }
            else {
                myHolder.txtSeen.setText("Đã chuyển");
            }
        }
        else {
            myHolder.txtSeen.setVisibility(View.GONE);
        }

        if(i == chatlist.size() - 1) {
            if(chatlist.get(i).isSeen())
            {
                myHolder.txtSeenImage.setText("Seen");
            }
            else {
                myHolder.txtSeenImage.setText("Đã chuyển");
            }
        }
        else {
            myHolder.txtSeenImage.setVisibility(View.GONE);
        }

        myHolder.imaChatPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowDown_Image.class);
                intent.putExtra("Image_Url", chatlist.get(i).getMessage());
                context.startActivity(intent);
            }
        });

    }

    private void deleteMessage(int position) {

        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String msgTimeStamp = chatlist.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats")
                .child(MyUidHisUid(myUID,hisUid));
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {

                    if(ds.child("sender").getValue().equals(myUID))
                    {
                        // remove the message from Chats
                        ds.getRef().removeValue();

                        // Set the value of message "This message was deleted..."
//                        HashMap<String,Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This message was deleted...");
//                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You can delete oly your messages...", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatlist.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, imaChatPhoto, imaSticker;
        TextView txtMessage, txtTime, txtSeen, txtTimeImage, txtSeenImage;
        RelativeLayout messageLayout;

        public MyHolder(@NonNull View itemView)
        {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv_chatleft);
            txtMessage = itemView.findViewById(R.id.txtMessage_chatleft);
            txtSeen = itemView.findViewById(R.id.txtisSend_chatleft);
            txtTime = itemView.findViewById(R.id.txtTime_chatleft);
            txtTimeImage = itemView.findViewById(R.id.txtTime_chatleft_Image);
            txtSeenImage = itemView.findViewById(R.id.txtisSend_chatleft_Image);
            messageLayout = itemView.findViewById(R.id.mesageLayout);
            imaChatPhoto = itemView.findViewById(R.id.img_ChatPhotoLeft);
            imaSticker = itemView.findViewById(R.id.img_ChatSticker);
        }
    }

    private String MyUidHisUid(String myID, String hisID)
    {
        if(myID.compareTo(hisID) > 0)
            return myID + hisID;
        else
            return hisID + myID;
    }
}
