package com.popseven.videoplayer;

import android.Manifest;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.popseven.videoplayer.Adapter.VideoFolderAdapter;
import com.popseven.videoplayer.Adapter.VideoListAdapter;
import com.popseven.videoplayer.Utils.AlertDialogHelper;
import com.popseven.videoplayer.Utils.RecyclerItemClickListener;
import com.popseven.videoplayer.Model.VideoFolderModel;
import com.popseven.videoplayer.Model.VideoModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, VideoFolderAdapter.VideoFolderAdapterListener, VideoListAdapter.VideoListAdapterListener, AlertDialogHelper.AlertDialogListener {

    private static final int REQUEST_PERMISSIONS = 100;
    private static ArrayList<VideoFolderModel> videoList = new ArrayList<>();
    private boolean booleanFolder = false;
    private RecyclerView recyclerViewFolder;
    private VideoFolderAdapter adapter;
    boolean isMultiSelect = false;
    private ArrayList<VideoFolderModel> selectedVideoList = new ArrayList<>();
    private ActionMode mActionMode;
    private Menu context_menu;
    private AlertDialogHelper alertDialogHelper;
    private static ArrayList<VideoModel> allVideoList = new ArrayList<>();
    private static ArrayList<VideoModel> selectedAllVideoList = new ArrayList<>();
    private VideoListAdapter videoListAdapter;
    private SharedPreferences sharedPref;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("com.popseven.videoplayer",MODE_PRIVATE);

        //Storage Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        }

        recyclerViewFolder = findViewById(R.id.recyclerViewFolder);

//        recyclerViewFolder.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
//        recyclerViewFolder.setHasFixedSize(true);

        videoList.clear();
        reloadRecyclerView();

        alertDialogHelper = new AlertDialogHelper(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerViewFolder.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewFolder, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect)
                    multiSelect(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    if (sharedPref.getBoolean("byFolder", true)){
                        selectedVideoList = new ArrayList<VideoFolderModel>();
                    }else {
                        selectedAllVideoList = new ArrayList<>();
                    }
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multiSelect(position);

            }
        }));

    }

    private void reloadRecyclerView(){
        getVideoPath();
        if(sharedPref.getBoolean("isGrid", false)) {
            setGridView();
        }else {
            setListView();
        }
    }

    private void setListView() {
        recyclerViewFolder.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewFolder.setHasFixedSize(true);
        if (sharedPref.getBoolean("byFolder", true)){
            adapter = new VideoFolderAdapter(this,videoList, selectedVideoList,this,0);
            recyclerViewFolder.setAdapter(adapter);
        }else {
            videoListAdapter = new VideoListAdapter(this,allVideoList, selectedAllVideoList,this,0);
            recyclerViewFolder.setAdapter(videoListAdapter);
        }
    }

    private void setGridView() {
        recyclerViewFolder.setLayoutManager(new GridLayoutManager(this,2));
        recyclerViewFolder.setHasFixedSize(true);
        if (sharedPref.getBoolean("byFolder", true)){
            adapter = new VideoFolderAdapter(this,videoList, selectedVideoList,this,1);
            recyclerViewFolder.setAdapter(adapter);
        }else {
            videoListAdapter = new VideoListAdapter(this,allVideoList, selectedAllVideoList,this,1);
            recyclerViewFolder.setAdapter(videoListAdapter);
        }
    }

    @Override
    protected void onResume() {
        reloadRecyclerView();
        invalidateOptionsMenu();
        super.onResume();
    }

    public ArrayList<VideoFolderModel> getVideoPath() {
        videoList.clear();
        booleanFolder = false;
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

        allVideoList.clear();
        for (int i = 0; i < videoList.size(); i++) {
            allVideoList.addAll(videoList.get(i).getVideopathList());
        }

        return videoList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_option_menu,menu);

        menuItem = menu.findItem(R.id.grid_view);
        setIcon();

        return super.onCreateOptionsMenu(menu);
    }

    private void setIcon() {
        if(sharedPref.getBoolean("isGrid", false)) {
            menuItem.setIcon(R.drawable.linear_view);
        }else {
            menuItem.setIcon(R.drawable.grid_view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.grid_view:
                if(sharedPref.getBoolean("isGrid", false)==false) {
                    setGridView();
                    item.setIcon(R.drawable.linear_view);
                    sharedPref.edit().putBoolean("isGrid", true).commit();
                } else {
                    setListView();
                    item.setIcon(R.drawable.grid_view);
                    sharedPref.edit().putBoolean("isGrid", false).commit();
                }
                break;

            case R.id.search:
                Intent intent = new Intent(this,SearchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("allVideoList", allVideoList);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.view:
                final AlertDialog.Builder alertOptions = new AlertDialog.Builder(MainActivity.this);
                String[] list = {"Folder", "Video"};
                alertOptions.setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case 0:
                                sharedPref.edit().putBoolean("byFolder", true).commit();
                                reloadRecyclerView();
                                dialog.dismiss();
                                break;

                            case 1:
                                sharedPref.edit().putBoolean("byFolder", false).commit();
                                reloadRecyclerView();
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                alertOptions.show();
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

    @Override
    public void onVideoSelected(int i) {
        Intent intent = new Intent(this,VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        bundle.putParcelableArrayList("videopathList", allVideoList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void multiSelect(int position) {
        if (mActionMode != null) {

            if (sharedPref.getBoolean("byFolder", true)){
                if (selectedVideoList.contains(videoList.get(position)))
                    selectedVideoList.remove(videoList.get(position));
                else
                    selectedVideoList.add(videoList.get(position));

                if (selectedVideoList.size() > 0)
                    mActionMode.setTitle("" + selectedVideoList.size());
                else
                    mActionMode.setTitle("");
            }else {
                if (selectedAllVideoList.contains(allVideoList.get(position)))
                    selectedAllVideoList.remove(allVideoList.get(position));
                else
                    selectedAllVideoList.add(allVideoList.get(position));

                if (selectedAllVideoList.size() > 0)
                    mActionMode.setTitle("" + selectedAllVideoList.size());
                else
                    mActionMode.setTitle("");
            }

            refreshAdapter();

        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    if (sharedPref.getBoolean("byFolder", true)){
                        alertDialogHelper.showAlertDialog("Delete Folder","Are you want to delete this folder?","DELETE","CANCEL",1,false);
                    }else {
                        alertDialogHelper.showAlertDialog("Delete Video","Are you want to delete this video?","DELETE","CANCEL",1,false);
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            if (sharedPref.getBoolean("byFolder", true)){
                selectedVideoList = new ArrayList<VideoFolderModel>();
            }else {
                selectedAllVideoList = new ArrayList<>();
            }
            refreshAdapter();
        }
    };

    public void refreshAdapter() {
        if (sharedPref.getBoolean("byFolder", true)){
            adapter.selectedVideoList=selectedVideoList;
            adapter.videoList=videoList;
            adapter.notifyDataSetChanged();
        }else {
            videoListAdapter.selectedVideoList=selectedAllVideoList;
            videoListAdapter.videoList=allVideoList;
            videoListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPositiveClick(int from) {
//        if(from==1)
//        {
//            if(selectedVideoList.size()>0)
//            {
//                for(int i=0;i<selectedVideoList.size();i++)
//                    videoList.remove(selectedVideoList.get(i));
//
//                adapter.notifyDataSetChanged();
//
//                if (mActionMode != null) {
//                    mActionMode.finish();
//                }
//                Toast.makeText(getApplicationContext(), "Delete Click", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }
}