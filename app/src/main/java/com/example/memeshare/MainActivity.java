package com.example.memeshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.memeshare.json.ApiHandler;
import com.example.memeshare.json.Meme;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView meme_image;
    private TextView caption;
    private ProgressBar progressBar;
    public static String ImgUrl = "";
    public static String CapUrl = "";
    public Uri bmpUri, mImageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
//    private StorageTask mStorageTask;
    Task<Uri> uriTask;
    Bitmap uploadBmp, bmp;
    ProgressBar horizontal_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        caption = findViewById(R.id.caption);
        meme_image = findViewById(R.id.meme);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        horizontal_bar = findViewById(R.id.horizontal_bar);

        storageReference = FirebaseStorage.getInstance().getReference("uploads/");
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

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
            public void onResponse(@NonNull Call<Meme> call, @NonNull Response<Meme> response) {

                if(!response.isSuccessful()){
                    Toast.makeText(MainActivity.this, "404 not found", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    call.cancel();
                }
                try {
                    Meme meme = response.body();
                    assert meme != null;
                    caption.setText(meme.getTitle());
                    ImgUrl = meme.getUrl();
                    CapUrl = meme.getTitle();

                    Glide.with(getApplicationContext()).load(meme.getUrl()).into(meme_image);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(@NonNull Call<Meme> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Please check your internet connection \n"
                        + t.getMessage(), Toast.LENGTH_SHORT).show();
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

//        Bitmap bitmap;
//        Uri bmpUri;

        Drawable drawable = meme_image.getDrawable();
        if(drawable instanceof BitmapDrawable){
            uploadBmp = ((BitmapDrawable) meme_image.getDrawable()).getBitmap();
        }

        try {

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            uploadBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
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
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        uploadBmp = null;
    }

    public void upload(View view) {

        uploadFile();
    }

    private void uploadFile() {

        Drawable drawable = meme_image.getDrawable();
        if(drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) meme_image.getDrawable()).getBitmap();
        }

//        bmp = ((BitmapDrawable) meme_image.getDrawable().getCurrent()).getBitmap();
        if(bmp != null){
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + ".jpg");
            mImageUri = getImageUri(MainActivity.this, bmp);

            UploadTask uploadTask = reference.putFile(mImageUri);

             uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();

                        assert downloadUri != null;
                        Upload upload = new Upload(CapUrl.trim(), downloadUri.toString());
                        String uploadId = databaseReference.push().getKey();
                        assert uploadId != null;
                        databaseReference.child(uploadId).setValue(upload);
                        Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Toast.makeText(MainActivity.this, "Url failed to load", Toast.LENGTH_SHORT).show();
                    }

                }
            });
//            mStorageTask = reference.putFile(mImageUri)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    horizontal_bar.setProgress(0);
//                                }
//                            }, 700);
//
//                            Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
//
//                            Upload upload = new Upload(CapUrl.trim(),
//                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().toString()
//
//                            );
//                            String uploadId = databaseReference.push().getKey();
//                            databaseReference.child(uploadId).setValue(upload);
//
//                        }
//                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//
//                    double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
//                    horizontal_bar.setProgress((int) progress);
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                }
//            });
        }
        else {
            Toast.makeText(this, "Can't upload", Toast.LENGTH_SHORT).show();
        }
        bmp = null;
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, CapUrl, null);
        return Uri.parse(path);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.get_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.get){
            Intent intent = new Intent(MainActivity.this, FavAct.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}