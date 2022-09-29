package com.bkm.emojiandroidapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;





public class BitmapUtils {

    private static final String FILE_PROVIDER_AUTHORITY = "com.bkm.emojiandroidapp.fileprovider";


    static Bitmap reSamplePic(Context cnt, String imgPath){
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) cnt.getSystemService(Context.WINDOW_SERVICE);
        // get the diamension from the device
        manager.getDefaultDisplay().getMetrics(metric);
        int targH = metric.heightPixels;
        int targW = metric.widthPixels;

        // get the diamension of original bitmap
        BitmapFactory.Options bmOption = new BitmapFactory.Options();
        bmOption.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath,bmOption);
        int photoW = bmOption.outWidth;
        int photoH = bmOption.outHeight;

        // Calculate the scale factor
        int scaleFactor = Math.min(photoH/targH,photoW/targW);
        // decode image as per saclefactor

        bmOption.inJustDecodeBounds = false;
        bmOption.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(imgPath, bmOption);
    }
    /***
     * Create temperory image file in the cache dir
     * return the temperory image file
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = context.getCacheDir();
        Log.d("createTempImageFile",storageDir.getAbsolutePath());
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    /***
     * Delete the image in the given path
     * @param cnt
     * @param imgPath
     * @return
     */
    static boolean deleteImageFile(Context cnt,String imgPath){
        //Get the file
        File imageFile = new File(imgPath);
        boolean isDeleted = imageFile.delete();
        if(!isDeleted){
            Toast.makeText(cnt, "No file found", Toast.LENGTH_SHORT).show();
        }
        return isDeleted;
    }

    /***
     * Used for saving the image
     * @param cnt
     * @param img
     * @return
     */
    static String saveImage(Context cnt, Bitmap img){

        String savedImagePath = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String imgFileName = "JPEG_"+timeStamp+".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Emojify");
        boolean success = true;
        if( !storageDir.exists()){
            success = storageDir.mkdir();
        }
        if(success){
            File imageFile = new File(storageDir,imgFileName);
            savedImagePath = imageFile.getAbsolutePath();
            OutputStream fOut = null;
            try {
                fOut = new FileOutputStream(imageFile);
                img.compress(Bitmap.CompressFormat.JPEG,100,fOut);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Add image to gallery
            addToGallery(cnt,savedImagePath);
            Toast.makeText(cnt, "Saved to " +savedImagePath, Toast.LENGTH_SHORT).show();

        }
        return savedImagePath;
    }

    /***
     * This function add the image to Gallery
     * @param cnt
     * @param savedImagePath
     */
    private static void addToGallery(Context cnt, String savedImagePath) {
        File file = new File(savedImagePath);
        MediaScannerConnection.scanFile(cnt, new String[]{file.toString()},null,null);
    }
    static void shareImage(Context cnt, String imagePath){
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoUri = FileProvider.getUriForFile(cnt,FILE_PROVIDER_AUTHORITY,imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM,photoUri);
        cnt.startActivity(shareIntent);
    }
}
