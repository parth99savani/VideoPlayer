package com.popseven.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.popseven.videoplayer.Adapter.VideoListAdapter;
import com.popseven.videoplayer.Model.VideoModel;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements VideoListAdapter.VideoListAdapterListener {

    private RecyclerView recyclerViewSearchVideo;
    private static ArrayList<VideoModel> allVideoList = new ArrayList<>();
    private static ArrayList<VideoModel> selectedSearchVideoList = new ArrayList<>();
    private static ArrayList<VideoModel> searchVideoList = new ArrayList<>();
    private VideoListAdapter videoListAdapter;
    private ImageButton btnBack;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchVideoList.clear();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            allVideoList = bundle.getParcelableArrayList("allVideoList");
        }

        recyclerViewSearchVideo = findViewById(R.id.recyclerViewSearchVideo);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);

        recyclerViewSearchVideo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewSearchVideo.setHasFixedSize(true);
        videoListAdapter = new VideoListAdapter(this, searchVideoList, selectedSearchVideoList, this, 0);
        recyclerViewSearchVideo.setAdapter(videoListAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void filterData(String s) {
        searchVideoList.clear();

        for (int i = 0; i < allVideoList.size(); i++) {
            if (allVideoList.get(i).getTitle().toLowerCase().contains(s.toLowerCase())) {
                searchVideoList.add(allVideoList.get(i));
            }
        }

        videoListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onVideoSelected(int i) {
        Intent intent = new Intent(this,VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position", i);
        bundle.putParcelableArrayList("videopathList", searchVideoList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}