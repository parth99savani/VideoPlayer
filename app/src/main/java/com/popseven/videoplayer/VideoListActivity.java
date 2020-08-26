package com.popseven.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.popseven.videoplayer.Adapter.VideoFolderAdapter;
import com.popseven.videoplayer.Adapter.VideoListAdapter;
import com.popseven.videoplayer.Model.VideoModel;

import java.util.ArrayList;

public class VideoListActivity extends AppCompatActivity implements VideoListAdapter.VideoListAdapterListener {

    private String strFolder;
    private ArrayList<VideoModel> videopathList = new ArrayList<>();
    private RecyclerView recyclerViewVideo;
    private VideoListAdapter adapter;
    private boolean isGrid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            strFolder = bundle.getString("strFolder");
            videopathList = bundle.getParcelableArrayList("videopathList");
            getSupportActionBar().setTitle(strFolder);
        }

        recyclerViewVideo = findViewById(R.id.recyclerViewVideo);
        recyclerViewVideo.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewVideo.setHasFixedSize(true);
        adapter = new VideoListAdapter(this,videopathList,this,0);
        recyclerViewVideo.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.videolist_option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.grid_view:
                if(isGrid==false)
                {
                    recyclerViewVideo.setLayoutManager(new GridLayoutManager(this,2));
                    recyclerViewVideo.setHasFixedSize(true);
                    adapter = new VideoListAdapter(this,videopathList,this,1);
                    recyclerViewVideo.setAdapter(adapter);
                    item.setIcon(R.drawable.linear_view);
                    isGrid = true;
                } else {
                    recyclerViewVideo.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
                    recyclerViewVideo.setHasFixedSize(true);
                    adapter = new VideoListAdapter(this,videopathList,this,0);
                    recyclerViewVideo.setAdapter(adapter);
                    item.setIcon(R.drawable.grid_view);
                    isGrid = false;
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
}