package com.example.socialnextwork.activitys;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.socialnextwork.PublicFunctions;
import com.example.socialnextwork.R;
import com.example.socialnextwork.adapters.StickerAdapter;

public class StickerSelect extends AppCompatActivity {

    private RecyclerView mRecycler_Sticker;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_select);

        initView();
        initUI();
    }

    private void initUI() {
        mRecycler_Sticker.setHasFixedSize(true);
        mRecycler_Sticker.setLayoutManager(new GridLayoutManager(getApplicationContext(),4,RecyclerView.VERTICAL,false));
        StickerAdapter mAdapter = new StickerAdapter(this);
        mRecycler_Sticker.setAdapter(mAdapter);
        mAdapter.setListener(new StickerAdapter.ResultPicture() {
            @Override
            public void OnResult(Integer mSticker) {
                Intent intent = new Intent();
                intent.putExtra("Stiker_Chat",mSticker);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    private void initView() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Sticker");

        mRecycler_Sticker = findViewById(R.id.Recycler_Sticker);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
