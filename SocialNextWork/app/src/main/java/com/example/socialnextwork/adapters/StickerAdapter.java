package com.example.socialnextwork.adapters;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.R;
import com.example.socialnextwork.activitys.StickerSelect;

import java.util.ArrayList;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder>{

    private ArrayList<Integer> mArraySticker;
    private Context mContext;
    private ResultPicture listener;

    public void setListener(ResultPicture listener){
        this.listener = listener;
    }

    public StickerAdapter(Context mContext) {
        this.mContext = mContext;

        mArraySticker = new ArrayList<>();

        mArraySticker.add(R.drawable.ic_sticker_1);
        mArraySticker.add(R.drawable.ic_sticker_2);
        mArraySticker.add(R.drawable.ic_sticker_3);
        mArraySticker.add(R.drawable.ic_sticker_4);
        mArraySticker.add(R.drawable.ic_sticker_5);
        mArraySticker.add(R.drawable.ic_sticker_6);
        mArraySticker.add(R.drawable.ic_sticker_7);
        mArraySticker.add(R.drawable.ic_sticker_8);
        mArraySticker.add(R.drawable.ic_sticker_9);
        mArraySticker.add(R.drawable.ic_sticker_10);
        mArraySticker.add(R.drawable.ic_sticker_11);
        mArraySticker.add(R.drawable.ic_sticker_12);
        mArraySticker.add(R.drawable.ic_sticker_13);
        mArraySticker.add(R.drawable.ic_sticker_14);
        mArraySticker.add(R.drawable.ic_sticker_15);
        mArraySticker.add(R.drawable.ic_sticker_16);
        mArraySticker.add(R.drawable.ic_sticker_17);
        mArraySticker.add(R.drawable.ic_sticker_18);
        mArraySticker.add(R.drawable.ic_sticker_19);
        mArraySticker.add(R.drawable.ic_sticker_20);
        mArraySticker.add(R.drawable.ic_sticker_21);
        mArraySticker.add(R.drawable.ic_sticker_22);
        mArraySticker.add(R.drawable.ic_sticker_23);
        mArraySticker.add(R.drawable.ic_sticker_24);
        mArraySticker.add(R.drawable.ic_sticker_25);
        mArraySticker.add(R.drawable.ic_sticker_26);
        mArraySticker.add(R.drawable.ic_sticker_27);
        mArraySticker.add(R.drawable.ic_sticker_28);
        mArraySticker.add(R.drawable.ic_sticker_29);
        mArraySticker.add(R.drawable.ic_sticker_30);
        mArraySticker.add(R.drawable.ic_sticker_31);
        mArraySticker.add(R.drawable.ic_sticker_32);
        mArraySticker.add(R.drawable.ic_sticker_33);
        mArraySticker.add(R.drawable.ic_sticker_34);
        mArraySticker.add(R.drawable.ic_sticker_35);
        mArraySticker.add(R.drawable.ic_sticker_36);
        mArraySticker.add(R.drawable.ic_sticker_37);
        mArraySticker.add(R.drawable.ic_sticker_38);
        mArraySticker.add(R.drawable.ic_sticker_39);
        mArraySticker.add(R.drawable.ic_sticker_40);
        mArraySticker.add(R.drawable.ic_sticker_41);
        mArraySticker.add(R.drawable.ic_sticker_42);
        mArraySticker.add(R.drawable.ic_sticker_43);
        mArraySticker.add(R.drawable.ic_sticker_44);
        mArraySticker.add(R.drawable.ic_sticker_45);
        mArraySticker.add(R.drawable.ic_sticker_46);
        mArraySticker.add(R.drawable.ic_sticker_47);
        mArraySticker.add(R.drawable.ic_sticker_48);
        mArraySticker.add(R.drawable.ic_sticker_49);
        mArraySticker.add(R.drawable.ic_sticker_50);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ImageView mSicker = new ImageView(mContext);

        int Width = 0;
        Point size = new Point();
        WindowManager w = ((StickerSelect)mContext).getWindowManager();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)    {
            w.getDefaultDisplay().getSize(size);
            Width = size.x;
        }else{
            Display d = w.getDefaultDisplay();
            Width = d.getWidth();
        }

        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(Width/4,Width/4);
        params.rightMargin = 5;
        params.leftMargin = 5;
        params.bottomMargin = 5;
        params.topMargin = 5;
        mSicker.setLayoutParams(params);

        return new ViewHolder(mSicker);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Glide.with(mContext).load(mArraySticker.get(holder.getAdapterPosition())).into(holder.mSicker);
        holder.mSicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnResult(mArraySticker.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArraySticker.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mSicker;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mSicker = (ImageView) itemView;
        }
    }


    public interface ResultPicture{
        void OnResult(Integer mSticker);
    }
}
