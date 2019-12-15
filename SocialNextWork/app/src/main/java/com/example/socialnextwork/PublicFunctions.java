package com.example.socialnextwork;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class PublicFunctions {


    public static void block(String id) {

        DatabaseReference reference;
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(myIDandUserID(fUser.getUid(), id))
                .child("Block").child(id);
        reference.setValue(true);

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("BlockList").child(id);
        reference.setValue(id);
    }

    public static void unlock(String id) {

        DatabaseReference reference;
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Flags")
                .child(myIDandUserID(fUser.getUid(), id))
                .child("Block").child(id);
        reference.setValue(true);

        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(fUser.getUid()).child("BlockList").child(id);
        reference.removeValue();
    }


    public static void showStatus(String status) {

        DatabaseReference reference;
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fUser != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(fUser.getUid());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        } else return;
    }

    public static String myIDandUserID(String id1, String id2) {
        if (id1.compareTo(id2) > 0)
            return id1 + id2;
        else
            return id2 + id1;
    }
}
