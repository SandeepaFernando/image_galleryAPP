package com.example.thumbnailapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int PERMMISSION_RESULT = 1;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private RecyclerView mThumbnailRecyclerView;
    private MediaStoreAdapter mMediaStoreAdapter;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    android.hardware.Camera camera;
    FrameLayout camFrame;
    ShowLivePreviewCameraWindow showCameraWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camFrame = findViewById(R.id.camera_view);

        mThumbnailRecyclerView = findViewById(R.id.thumbnailRecyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mThumbnailRecyclerView.setLayoutManager(gridLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaStoreAdapter);
        mMediaStoreAdapter.setOnClickListener(new MediaStoreAdapter.OnClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (mMediaStoreAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                    Log.i("LONGCLICK", "clickedAfterLong");
                } else {
                    // read the inbox which removes bold from the row
                    Log.i("LONGCLICK", "clicked");

                }
            }

            @Override
            public void OnItemLongClick(View view, Uri uri, int position) {
                Log.i("LONGCLIK", "long clicked");
                enableActionMode(position);
            }
        });

        actionModeCallback = new ActionModeCallback();

        verifyUserPermission();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.i("CAMERAPERMISSION", "Granted");

            camFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent camintent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivity(camintent);
                    //camera.stopPreview();
                    //camera.release();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){

            verifyUserPermission();

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            camera_window();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
        }

    }

    private void verifyUserPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[1]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[2]) == PackageManager.PERMISSION_GRANTED){

                LoaderManager.getInstance(this).initLoader(MEDIASTORE_LOADER_ID, null, this);
                //getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
                camera_window();

            } else {
                Toast.makeText(this, "Permision Denyed", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMMISSION_RESULT);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mMediaStoreAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mMediaStoreAdapter.changeCursor(null);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mMediaStoreAdapter.toggleSelection(position);
        int count = mMediaStoreAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    public class ActionModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            Log.i("TEST", "====================Testing===================");
            Tools.setSystemBarColor(MainActivity.this, R.color.colorPrimaryDark);
            actionMode.getMenuInflater().inflate(R.menu.menu_upload, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            int id = menuItem.getItemId();
            if (id == R.id.action_delete) {
                //uploadIMGs();
                actionMode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMediaStoreAdapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(MainActivity.this, R.color.colorPrimary);

        }
    }


    public void camera_window() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //camera = Camera.open();

            showCameraWindow = new ShowLivePreviewCameraWindow(this, camera);
            camFrame.addView(showCameraWindow);
        }
    }


}
