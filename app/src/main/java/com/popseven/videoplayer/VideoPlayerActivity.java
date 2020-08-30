package com.popseven.videoplayer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.popseven.videoplayer.Model.VideoModel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class VideoPlayerActivity extends AppCompatActivity implements Player.EventListener, PlaybackControlView.VisibilityListener, ScaleGestureDetector.OnScaleGestureListener {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImageButton btnFullscreen;
    private boolean flag = false;
    //private VideoModel videoModel;
    private ArrayList<VideoModel> videopathList = new ArrayList<>();
    private int position;
    private ImageButton btnBack;
    private TextView txtVideoName;
    private RelativeLayout rlActionbar;
    private ScaleGestureDetector mScaleDetector;
    private ImageView volIcon;
    private ProgressBar volumeSlider;
    private LinearLayout volumeSliderContainer;
    private ImageView brightnessIcon;
    private ProgressBar brightnessSlider;
    private LinearLayout brightnessSliderContainer;
    private ImageView volImage;
    private TextView volPercCenterText;
    private LinearLayout volCenterText;
    private ImageView brightnessImage;
    private TextView brigtnessPercCenterText;
    private LinearLayout brightnessCenterText;
    private Display display;
    private Point size;
    private Boolean tested_ok = false;
    private int sWidth, sHeight;
    private long diffX, diffY;
    private float baseX, baseY;
    private boolean immersiveMode, intLeft, intRight, intTop, intBottom, finLeft, finRight, finTop, finBottom;
    private double seekSpeed = 0;
    private int calculatedTime;
    private String seekDur;
    private Boolean screen_swipe_move = false;
    private static final int MIN_DISTANCE = 150;
    private ContentResolver cResolver;
    private Window window;
    private int brightness, mediavolume, device_height, device_width;
    private AudioManager audioManager;
    private TextView txtSeekCurrTime;
    private TextView txtSeekSecs;
    private LinearLayout seekbarCenterText;
    private RelativeLayout relativeLayout;
    private ImageButton btnAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        playerView = findViewById(R.id.playerView);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnBack = findViewById(R.id.btnBack);
        txtVideoName = findViewById(R.id.txtVideoName);
        rlActionbar = findViewById(R.id.rlActionbar);
        volIcon = findViewById(R.id.volIcon);
        volumeSlider = findViewById(R.id.volume_slider);
        volumeSliderContainer = findViewById(R.id.volume_slider_container);
        brightnessIcon = findViewById(R.id.brightnessIcon);
        brightnessSlider = findViewById(R.id.brightness_slider);
        brightnessSliderContainer = findViewById(R.id.brightness_slider_container);
        volImage = findViewById(R.id.vol_image);
        volPercCenterText = findViewById(R.id.vol_perc_center_text);
        volCenterText = findViewById(R.id.vol_center_text);
        brightnessImage = findViewById(R.id.brightness_image);
        brigtnessPercCenterText = findViewById(R.id.brigtness_perc_center_text);
        brightnessCenterText = findViewById(R.id.brightness_center_text);
        txtSeekCurrTime = findViewById(R.id.txt_seek_currTime);
        txtSeekSecs = findViewById(R.id.txt_seek_secs);
        seekbarCenterText = findViewById(R.id.seekbar_center_text);
        relativeLayout = findViewById(R.id.relativeLayout);
        btnAR = findViewById(R.id.btnAR);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        device_height = displaymetrics.heightPixels;
        device_width = displaymetrics.widthPixels;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            position = bundle.getInt("position");
            videopathList = bundle.getParcelableArrayList("videopathList");
            txtVideoName.setText(videopathList.get(position).getTitle());
        }

        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View view) {
                if (flag) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    flag = false;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    flag = true;
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mScaleDetector = new ScaleGestureDetector(this, this);

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tested_ok = false;
                        if (event.getX() < (sWidth / 2)) {
                            intLeft = true;
                            intRight = false;
                        } else if (event.getX() > (sWidth / 2)) {
                            intLeft = false;
                            intRight = true;
                        }
                        int upperLimit = (sHeight / 4) + 100;
                        int lowerLimit = ((sHeight / 4) * 3) - 150;
                        if (event.getY() < upperLimit) {
                            intBottom = false;
                            intTop = true;
                        } else if (event.getY() > lowerLimit) {
                            intBottom = true;
                            intTop = false;
                        } else {
                            intBottom = false;
                            intTop = false;
                        }
                        seekSpeed = (TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) * 0.1);
                        diffX = 0;
                        calculatedTime = 0;
                        seekDur = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(diffX) -
                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffX)),
                                TimeUnit.MILLISECONDS.toSeconds(diffX) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffX)));

                        //TOUCH STARTED
                        baseX = event.getX();
                        baseY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        screen_swipe_move = true;
                        if (event.getX() < (sWidth / 2)) {
                            intLeft = true;
                            intRight = false;
                        } else if (event.getX() > (sWidth / 2)) {
                            intLeft = false;
                            intRight = true;
                        }
                        diffX = (long) (Math.ceil(event.getX() - baseX));
                        diffY = (long) Math.ceil(event.getY() - baseY);
                        double brightnessSpeed = 0.05;
                        if (Math.abs(diffY) > MIN_DISTANCE) {
                            tested_ok = true;
                        }
                        if (Math.abs(diffY) > Math.abs(diffX)) {
                            if (intLeft) {
                                cResolver = getContentResolver();
                                window = getWindow();
//                            try {
//                                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                                brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
//
//                            } catch (Settings.SettingNotFoundException e) {
//                                e.printStackTrace();
//                            }
                                WindowManager.LayoutParams lp = window.getAttributes();
                                brightness = (int) lp.screenBrightness * 255;

                                int new_brightness = (int) (brightness - (diffY * brightnessSpeed));
                                if (new_brightness > 250) {
                                    new_brightness = 250;
                                } else if (new_brightness < 1) {
                                    new_brightness = 1;
                                }
                                double brightPerc = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                                brightnessSliderContainer.setVisibility(View.VISIBLE);
                                brightnessCenterText.setVisibility(View.VISIBLE);
                                brightnessSlider.setProgress((int) brightPerc);
                                if (brightPerc < 30) {
                                    brightnessIcon.setImageResource(R.drawable.ic_baseline_brightness_low_24);
                                    brightnessImage.setImageResource(R.drawable.ic_baseline_brightness_low_24);
                                } else if (brightPerc > 30 && brightPerc < 80) {
                                    brightnessIcon.setImageResource(R.drawable.ic_baseline_brightness_medium_24);
                                    brightnessImage.setImageResource(R.drawable.ic_baseline_brightness_medium_24);
                                } else if (brightPerc > 80) {
                                    brightnessIcon.setImageResource(R.drawable.ic_baseline_brightness_high_24);
                                    brightnessImage.setImageResource(R.drawable.ic_baseline_brightness_high_24);
                                }
                                brigtnessPercCenterText.setText(" " + (int) brightPerc);
                                //Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, (new_brightness));
                                WindowManager.LayoutParams layoutpars = window.getAttributes();
                                layoutpars.screenBrightness = brightness / (float) 255;
                                window.setAttributes(layoutpars);
                            } else if (intRight) {
                                volCenterText.setVisibility(View.VISIBLE);
                                mediavolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                double cal = (double) diffY * ((double) maxVol / (double) (device_height * 3));
                                int newMediaVolume = mediavolume - (int) cal;
                                if (newMediaVolume > maxVol) {
                                    newMediaVolume = maxVol;
                                } else if (newMediaVolume < 1) {
                                    newMediaVolume = 0;
                                }
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                double volPerc = Math.ceil((((double) newMediaVolume / (double) maxVol) * (double) 100));
                                volPercCenterText.setText(" " + (int) volPerc);
                                if (volPerc < 1) {
                                    volIcon.setImageResource(R.drawable.ic_baseline_volume_mute_24);
                                    volImage.setImageResource(R.drawable.ic_baseline_volume_mute_24);
                                    volPercCenterText.setVisibility(View.GONE);
                                } else if (volPerc >= 1) {
                                    volIcon.setImageResource(R.drawable.ic_baseline_volume_up_24);
                                    volImage.setImageResource(R.drawable.ic_baseline_volume_up_24);
                                    volPercCenterText.setVisibility(View.VISIBLE);
                                }
                                volumeSliderContainer.setVisibility(View.VISIBLE);
                                volumeSlider.setProgress((int) volPerc);
                            }
                        } else if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (Math.abs(diffX) > (MIN_DISTANCE + 100)) {
                                tested_ok = true;
                                seekbarCenterText.setVisibility(View.VISIBLE);
                                String totime = "";
                                calculatedTime = (int) ((diffX) * seekSpeed);
                                if (calculatedTime > 0) {
                                    seekDur = String.format("[ +%02d:%02d ]",
                                            TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime)),
                                            TimeUnit.MILLISECONDS.toSeconds(calculatedTime) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime)));
                                } else if (calculatedTime < 0) {
                                    seekDur = String.format("[ -%02d:%02d ]",
                                            Math.abs(TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime))),
                                            Math.abs(TimeUnit.MILLISECONDS.toSeconds(calculatedTime) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime))));
                                }
                                totime = String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition() + (calculatedTime)) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(player.getCurrentPosition() + (calculatedTime))), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition() + (calculatedTime)) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition() + (calculatedTime))));
                                txtSeekSecs.setText(seekDur);
                                txtSeekCurrTime.setText(totime);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        screen_swipe_move = false;
                        tested_ok = false;
                        if(playerView.isControllerVisible()){
                            playerView.hideController();
                        }
                        seekbarCenterText.setVisibility(View.GONE);
                        brightnessCenterText.setVisibility(View.GONE);
                        volCenterText.setVisibility(View.GONE);
                        brightnessSliderContainer.setVisibility(View.GONE);
                        volumeSliderContainer.setVisibility(View.GONE);
                        calculatedTime = (int) (player.getCurrentPosition() + (calculatedTime));
                        player.seekTo(calculatedTime);
                        break;

                }
                return true;
            }
        });

        btnAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Intent intent = new Intent(VideoPlayerActivity.this,ARVideoPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("videoUri", String.valueOf(videopathList.get(position).getData()));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else {
                    Toast.makeText(VideoPlayerActivity.this, "Sorry, Your device is not supported AR.", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();

        for (int i = 0; i < videopathList.size(); i++) {
            MediaSource mediaSource = buildMediaSource(videopathList.get(i).getData());
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        player.setPlayWhenReady(true);
        player.addListener(this);
        playerView.setControllerVisibilityListener(this);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();
        player.setAudioAttributes(audioAttributes, true);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        int latestWindowIndex = player.getCurrentWindowIndex();
        if (latestWindowIndex != position) {
            // item selected in playlist has changed, handle here
            position = latestWindowIndex;
            txtVideoName.setText(videopathList.get(position).getTitle());
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        super.onStop();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        rlActionbar.setVisibility(visibility);
        if (visibility == 8) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        if (playerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }

    }
}