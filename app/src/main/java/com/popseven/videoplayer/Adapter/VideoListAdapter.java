package com.popseven.videoplayer.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.popseven.videoplayer.Model.VideoModel;
import com.popseven.videoplayer.R;

import java.io.File;
import java.util.ArrayList;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.MyViewHolder> {

    private Context context;
    private VideoListAdapter.VideoListAdapterListener listener;
    private ArrayList<VideoModel> videoList = new ArrayList<>();
    private int type;

    public VideoListAdapter(Context context, ArrayList<VideoModel> videoList, VideoListAdapter.VideoListAdapterListener listener, int type) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
        this.type = type;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgThumb;
        private TextView txtVideoName;
        private TextView txtVideoTime;
        private ImageButton btnMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgThumb = itemView.findViewById(R.id.imgThumb);
            txtVideoName = itemView.findViewById(R.id.txtVideoName);
            txtVideoTime = itemView.findViewById(R.id.txtVideoTime);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    @NonNull
    @Override
    public VideoListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        if (type == 0) {
            view = inflater.inflate(R.layout.item_video_list, viewGroup, false);
        } else {
            view = inflater.inflate(R.layout.item_video_grid, viewGroup, false);
        }
        return new VideoListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoListAdapter.MyViewHolder holder, final int i) {

        Glide.with(context).load(videoList.get(i).getData())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imgThumb);

        holder.txtVideoName.setText(videoList.get(i).getTitle());
        holder.txtVideoTime.setText(videoList.get(i).getDuration());

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), holder.btnMore);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.more_option_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){

                            case R.id.share:
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.setType("video/3gp");
                                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Video");
                                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.valueOf(videoList.get(i).getData())));
                                context.startActivity(Intent.createChooser(sendIntent, "Send Video"));
                                break;

                            case R.id.rename:

                                LayoutInflater li = LayoutInflater.from(context);
                                View promptsView = li.inflate(R.layout.alert_dialog_rename, null);

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                                // set alert_dialog.xml to alertdialog builder
                                alertDialogBuilder.setView(promptsView);

                                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTxtRename);

                                // set dialog message
                                alertDialogBuilder
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                final File file = new File(videoList.get(i).getData().getPath());
                                                file.renameTo(new File(String.valueOf(userInput)));
                                                notifyDataSetChanged();

                                            }
                                        })
                                        .setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                // create alert dialog
                                AlertDialog alertDialog = alertDialogBuilder.create();

                                // show it
                                alertDialog.show();

                                break;

                            case R.id.properties:
                                break;

                            case R.id.delete:

                                AlertDialog diaBox = new AlertDialog.Builder(context)
                                        // set message, title, and icon
                                        .setTitle("Delete")
                                        .setMessage("Do you want to Delete "+videoList.get(i).getTitle())
                                        .setIcon(R.drawable.ic_baseline_delete_24)

                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // Delete Code
                                            }

                                        })
                                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                dialog.dismiss();

                                            }
                                        })
                                        .create();
                                diaBox.show();
                                break;

                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onVideoSelected(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public interface VideoListAdapterListener {
        void onVideoSelected(int i);
    }

}
