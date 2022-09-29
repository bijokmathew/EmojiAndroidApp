package com.bkm.emojiandroidapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_STOARAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.bkm.emojiandroidapp.fileprovider";

    private ImageView mImageView;
    private Button mEmojifyButton;
    private FloatingActionButton mShareFab;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mClearFab;

    private TextView mTitleTextView;
    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind views
        mImageView = findViewById(R.id.image_view_id);
        mEmojifyButton = findViewById(R.id.emojify_btn_id);
        mSaveFab = findViewById(R.id.save_btn_id);
        mShareFab = findViewById(R.id.share_btn_id);
        mClearFab = findViewById(R.id.clear_btn_id);
        mTitleTextView = findViewById(R.id.title_textView_id);
        mEmojifyButton.setOnClickListener(this);
    }

    /**
     * This function is registered with Go button
     * launch camera
     *
     * @param view
     */
    public void emojifyMe(View view) {
        // check for file storage permission for saving the camera image
        Log.d("emojifyMe","inside emojifyMe");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.d("emojifyMe","checkSelfPermission deni");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STOARAGE_PERMISSION);
        } else {
            //lauch camera App
            Log.d("emojifyMe","checkSelfPermission granted");
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_STOARAGE_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    launchCamera();
                }else {
                    Toast.makeText(this,"Permission is denied",Toast.LENGTH_LONG).show();
                }
        }
    }

    private void launchCamera() {
        //`intent to launch camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // check there is an activity to resove the action ACTION_IMAGE_CAPTURE
        Log.d("launchCamera","launchCamera");
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            // create temp file to save photo
            File photoFile = null;
            try{
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex){
                ex.printStackTrace();
            }
            if(photoFile != null){
                // Get the path of temperory file
                mTempPhotoPath = photoFile.getAbsolutePath();
                Log.d("mTempPhotoPath",photoFile.toString());
                // get content URI for the image file
                Uri photoUri = FileProvider.getUriForFile(this,FILE_PROVIDER_AUTHORITY,photoFile);
                // add content uri to camera to store theimage
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                //launch camera
                startActivityIntent.launch(takePictureIntent);
            }

        } else {
            Log.d("launchCamera","No launchCamera");
        }


    }
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        // proccess and set the image to imageview
                        Log.d("registeActivityResult","processAndSetImage");
                        processAndSetImage();
                    } else {
                        Log.d("registeActivityResult","Failed");
                    }
                }
            }
    );

    /***
     * Proccess the captured image and set to imageview
     */
    private void processAndSetImage() {

        Log.d("processAndSetImage","processAndSetImage");
        //Toggle visisblility of views
        mEmojifyButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.VISIBLE);
        mClearFab.setVisibility(View.VISIBLE);

        mResultsBitmap = BitmapUtils.reSamplePic(this,mTempPhotoPath);
        mImageView.setImageBitmap(mResultsBitmap);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.emojify_btn_id){
            Log.d("emojifyMe","onClick");
            emojifyMe(view);
        } else if(view.getId() == R.id.share_btn_id){
            shareMe(view);
        }else if(view.getId() == R.id.save_btn_id){
            saveMe(view);
        }else if(view.getId() == R.id.clear_btn_id){
            clearImage(view);
        }

    }

    private void clearImage(View view) {
        mImageView.setImageResource(0);
        mEmojifyButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);
    }

    private void saveMe(View view) {
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);
        BitmapUtils.saveImage(this,mResultsBitmap);
    }

    private void shareMe(View view) {
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);
        BitmapUtils.saveImage(this,mResultsBitmap);
        BitmapUtils.shareImage(this,mTempPhotoPath);
    }
}