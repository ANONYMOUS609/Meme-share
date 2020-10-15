package com.example.memeshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.memeshare.json.ApiHandler;
import com.example.memeshare.json.Meme;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView meme_image;
    private TextView caption;
    private ProgressBar progressBar;
    public static String ImgUrl = "";
    public static String CapUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        caption = findViewById(R.id.caption);
        meme_image = findViewById(R.id.meme);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        fetchMeme();

    }

    public void refresh_btn(View view) {
        fetchMeme();
    }

    public void fetchMeme(){
        ApiHandler apiHandler = GetMethod.getRetrofit().create(ApiHandler.class);
        Call<Meme> call = apiHandler.getMeme();
        call.enqueue(new Callback<Meme>() {
            @Override
            public void onResponse(Call<Meme> call, Response<Meme> response) {

                if(!response.isSuccessful()){
                    Toast.makeText(MainActivity.this, "404 not found", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    call.cancel();
                }
                    Meme meme = response.body();
                assert meme != null;
                caption.setText(meme.getTitle());
                ImgUrl = meme.getUrl();
                CapUrl = meme.getTitle();

                    Glide.with(getApplicationContext()).load(meme.getUrl()).into(meme_image);

            }

            @Override
            public void onFailure(Call<Meme> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                meme_image.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void share(View view) {

//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT, ImgUrl);
//        intent.putExtra(Intent.EXTRA_SUBJECT, CapUrl +" meme");
//        startActivity(Intent.createChooser(intent, "Share Using"));

        Bitmap bitmap = null;
        
        Drawable drawable = meme_image.getDrawable();
        if(drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable) meme_image.getDrawable()).getBitmap();
        }
        else {
            return;
        }
        Uri bmpUri = null;

        try {

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            bmpUri = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", file);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, CapUrl +" meme");
            shareIntent.setType("image/*");
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));

        } catch (Exception e){

        }
    }

//    public static void fixMediaDir() {
//        File sdcard = Environment.getExternalStorageDirectory();
//        if (sdcard == null) { return; }
//        File dcim = new File(sdcard, "DCIM");
//        if (dcim == null) { return; }
//        File camera = new File(dcim, "Camera");
//        if (camera.exists()) { return; }
//        camera.mkdir();
//    }

}