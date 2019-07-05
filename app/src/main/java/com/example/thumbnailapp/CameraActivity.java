package com.example.thumbnailapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {
    Button bnCamera;
    ImageView previewImage;
    //ImageView imageView;
    Uri image;
    String mCameraFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        bnCamera = findViewById(R.id.camera_button);
        previewImage = findViewById(R.id.preview);

        bnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 0);

                cameraIntent();
                createdirectory();
            }
        });


    }

    public void createdirectory() {
        String path = "/sdcard/PowerApp/";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            Log.i("DIRCREATING", "Folder created");

        }
    }

    private void cameraIntent() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("mm-ss");

        String newPicFile = df.format(date) + ".jpg";
        String outPath = "/sdcard/PowerApp/" + newPicFile;
        File outFile = new File(outPath);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, 2);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                if (data != null) {
                    image = data.getData();
                    previewImage.setImageURI(image);
                    previewImage.setVisibility(View.VISIBLE);
                }
                if (image == null && mCameraFileName != null) {
                    image = Uri.fromFile(new File(mCameraFileName));
                    previewImage.setImageURI(image);
                    previewImage.setVisibility(View.VISIBLE);
                }
//                File file = new File(mCameraFileName);
//                if (!file.exists()) {
//                    file.mkdir();
//                }
            }
        }

    }
}
