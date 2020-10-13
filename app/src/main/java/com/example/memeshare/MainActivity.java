package com.example.memeshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.memeshare.json.ApiHandler;
import com.example.memeshare.json.Meme;

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

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, ImgUrl);
        intent.putExtra(Intent.EXTRA_SUBJECT, CapUrl +" meme");
        startActivity(Intent.createChooser(intent, "Share Using"));

    }
}