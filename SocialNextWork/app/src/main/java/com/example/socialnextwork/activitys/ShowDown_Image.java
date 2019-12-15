package com.example.socialnextwork.activitys;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnextwork.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ShowDown_Image extends AppCompatActivity {

    ImageView show, save;
    ProgressDialog pd;
    AsyncTask mMyTask;
    String img_Url;
    FirebaseStorage firebaseStorage;
    StorageReference storage, ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_down__image);

        Intent i = getIntent();
        img_Url = i.getStringExtra("Image_Url");
        final String nameUrl = img_Url.substring(91, 132);

        show = findViewById(R.id.img_show);
        save = findViewById(R.id.img_down);
        Glide.with(this).load(img_Url).placeholder(R.drawable.yasuo).into(show);
        pd = new ProgressDialog(this);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                pd.setMessage("Please wait, we are downloading your image file...");
//                mMyTask = new DownloadTask()
//                        .execute(stringToURL(img_Url));
                download(nameUrl, ".jpg" );
                Toast.makeText(ShowDown_Image.this, nameUrl+"", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void  download(final String fileName, final String fileExtension){


        storage = firebaseStorage.getInstance().getReference();
        ref = storage.child("PhotoChat/").child(fileName);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(ShowDown_Image.this, fileName + "", fileExtension + "", DIRECTORY_DOWNLOADS, uri.toString());
            }
        });
    }

    private void downloadFile(Context context, String fileName, String fileExtension, String des, String url) {
        DownloadManager  downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, des, fileName + fileExtension);

        downloadManager.enqueue(request);
    }

    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        protected void onPreExecute() {
            pd.show();
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection connection = null;

            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                return bmp;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        protected void onPostExecute(Bitmap result){
            pd.dismiss();

            if(result!=null){
                saveImageToInternalStorage(img_Url);
                Toast.makeText(ShowDown_Image.this, "Đã tải", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ShowDown_Image.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    protected void saveImageToInternalStorage(String name){

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        File file = wrapper.getDir("Images",MODE_PRIVATE);
        String nameUrl = name.substring(98, 139);
        file = new File(file, nameUrl +".jpg");
        Toast.makeText(wrapper, nameUrl+"", Toast.LENGTH_SHORT).show();


        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);

            stream.flush();
            stream.close();

        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
