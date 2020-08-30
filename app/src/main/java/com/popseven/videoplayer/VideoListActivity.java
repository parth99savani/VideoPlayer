package com.popseven.videoplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.popseven.videoplayer.Adapter.VideoFolderAdapter;
import com.popseven.videoplayer.Adapter.VideoListAdapter;
import com.popseven.videoplayer.Model.VideoFolderModel;
import com.popseven.videoplayer.Model.VideoModel;
import com.popseven.videoplayer.Utils.AlertDialogHelper;
import com.popseven.videoplayer.Utils.RecyclerItemClickListener;

import java.util.ArrayList;

public class VideoListActivity extends AppCompatActivity implements VideoListAdapter.VideoListAdapterListener, AlertDialogHelper.AlertDialogListener  {

    private String strFolder;
    private ArrayList<VideoModel> videopathList = new ArrayList<>();
    private RecyclerView recyclerViewVideo;
    private VideoListAdapter adapter;
    boolean isMultiSelect = false;
    private ArrayList<VideoModel> selectedVideoList = new ArrayList<>();
    private ActionMode mActionMode;
    private Menu context_menu;
    private AlertDialogHelper alertDialogHelper;
    private SharedPreferences sharedPref;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences("com.popseven.videoplayer",MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            strFolder = bundle.getString("strFolder");
            videopathList = bundle.getParcelableArrayList("videopathList");
            getSupportActionBar().setTitle(strFolder);
        }

        recyclerViewVideo = findViewById(R.id.recyclerViewVideo);

        setRecyclerView();

        alertDialogHelper = new AlertDialogHelper(this);

        recyclerViewVideo.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewVideo, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect)
                    multiSelect(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    selectedVideoList = new ArrayList<VideoModel>();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multiSelect(position);

            }
        }));

    }

    private void setRecyclerView() {
        if(sharedPref.getBoolean("isGrid", false)) {
            setGridView();
        } else {
            setListView();
        }
    }

    private void setListView() {
        recyclerViewVideo.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewVideo.setHasFixedSize(true);
        adapter = new VideoListAdapter(this,videopathList, selectedVideoList,this,0);
        recyclerViewVideo.setAdapter(adapter);
    }

    private void setGridView() {
        recyclerViewVideo.setLayoutManager(new GridLayoutManager(this,2));
        recyclerViewVideo.setHasFixedSize(true);
        adapter = new VideoListAdapter(this,videopathList, selectedVideoList,this,1);
        recyclerViewVideo.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.videolist_option_menu,menu);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onVideoSelected(int i) {
        Intent intent = new Intent(this,VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        bundle.putParcelableArrayList("videopathList", videopathList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void multiSelect(int position) {
        if (mActionMode != null) {
            if (selectedVideoList.contains(videopathList.get(position)))
                selectedVideoList.remove(videopathList.get(position));
            else
                selectedVideoList.add(videopathList.get(position));

            if (selectedVideoList.size() > 0)
                mActionMode.setTitle("" + selectedVideoList.size());
            else
                mActionMode.setTitle("");

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
                    alertDialogHelper.showAlertDialog("Delete Video","Are you want to delete this video?","DELETE","CANCEL",1,false);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            selectedVideoList = new ArrayList<VideoModel>();
            refreshAdapter();
        }
    };

    public void refreshAdapter() {
        adapter.selectedVideoList=selectedVideoList;
        adapter.videoList=videopathList;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPositiveClick(int from) {

    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }
}