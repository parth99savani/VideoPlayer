package com.popseven.videoplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.popseven.videoplayer.Adapter.VideoFolderAdapter;
import com.popseven.videoplayer.Model.VideoFolderModel;
import com.popseven.videoplayer.Model.VideoModel;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, VideoFolderAdapter.VideoFolderAdapterListener {

    private static final int REQUEST_PERMISSIONS = 100;
    private static ArrayList<VideoFolderModel> videoList = new ArrayList<>();
    private boolean booleanFolder;
    private RecyclerView recyclerViewFolder;
    private VideoFolderAdapter adapter;
    private boolean isGrid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Storage Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        }

        recyclerViewFolder = findViewById(R.id.recyclerViewFolder);

        recyclerViewFolder.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewFolder.setHasFixedSize(true);

        videoList.clear();
        reloadRecyclerView();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void reloadRecyclerView(){
        videoList.clear();
        getVideoPath();
        adapter = new VideoFolderAdapter(this,videoList,this,0);
        recyclerViewFolder.setAdapter(adapter);
    }

    public ArrayList<VideoFolderModel> getVideoPath() {
        videoList.clear();

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfVideo = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        if (cursor != null) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                int duration = cursor.getInt(durationColumn);

                Uri data = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                String duration_formatted;
                int sec = (duration / 1000) % 60;
                int min = (duration / (1000 * 60)) % 60;
                int hrs = duration / (1000 * 60 * 60);

                if (hrs == 0) {
                    duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                } else {
                    duration_formatted = String.valueOf(hrs).concat(":".concat(String.format(Locale.UK, "%02d", min).concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                }

                absolutePathOfVideo = cursor.getString(column_index_data);
                Log.e("Column", absolutePathOfVideo);
                Log.e("Folder", cursor.getString(column_index_folder_name));

                for (int i = 0; i < videoList.size(); i++) {
                    if (videoList.get(i).getStrFolder().equals(cursor.getString(column_index_folder_name))) {
                        booleanFolder = true;
                        int_position = i;
                        break;
                    } else {
                        booleanFolder = false;
                    }
                }

                if (booleanFolder) {
                    Log.e("booleanFolder", "true");
                    ArrayList<VideoModel> al_path = new ArrayList<>();
                    al_path.addAll(videoList.get(int_position).getVideopathList());
                    al_path.add(new VideoModel(id, data, title, duration_formatted));
                    videoList.get(int_position).setVideopathList(al_path);

                } else {
                    Log.e("booleanFolder", "false");
                    ArrayList<VideoModel> al_path = new ArrayList<>();
                    al_path.add(new VideoModel(id, data, title, duration_formatted));
                    VideoFolderModel obj_model = new VideoFolderModel();
                    obj_model.setStrFolder(cursor.getString(column_index_folder_name));
                    obj_model.setVideopathList(al_path);

                    videoList.add(obj_model);

                }

            }
        }

//        for (int i = 0; i < videoList.size(); i++) {
//            Log.e("FOLDER", videoList.get(i).getStrFolder());
//            for (int j = 0; j < videoList.get(i).getVideopathList().size(); j++) {
//                Log.e("FILE", videoList.get(i).getVideopathList().get(j));
//            }
//        }

        return videoList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.grid_view:
                if(isGrid==false)
                {
                    recyclerViewFolder.setLayoutManager(new GridLayoutManager(this,2));
                    recyclerViewFolder.setHasFixedSize(true);
                    adapter = new VideoFolderAdapter(this,videoList,this,1);
                    recyclerViewFolder.setAdapter(adapter);
                    item.setIcon(R.drawable.linear_view);
                    isGrid = true;
                } else {
                    recyclerViewFolder.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
                    recyclerViewFolder.setHasFixedSize(true);
                    adapter = new VideoFolderAdapter(this,videoList,this,0);
                    recyclerViewFolder.setAdapter(adapter);
                    item.setIcon(R.drawable.grid_view);
                    isGrid = false;
                }
                break;

            case R.id.search:
                //
                break;

            case R.id.view:
                //
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_rate) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        reloadRecyclerView();
                    } else {
                        Toast.makeText(MainActivity.this, "The app was not allowed to read your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onVideoFolderSelected(VideoFolderModel videoFolderModel) {
        Intent intent = new Intent(this,VideoListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("videopathList", videoFolderModel.getVideopathList());
        bundle.putString("strFolder", videoFolderModel.getStrFolder());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}