package com.popseven.videoplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class ARVideoPlayerActivity extends AppCompatActivity {

    private ModelRenderable videoRenderable;
    private Uri videoUri;
    private float HEIGHT = 0.95f;
    private MediaPlayer mediaPlayer;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_video_player);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoUri = Uri.parse(bundle.getString("videoUri"));

        }

        ExternalTexture texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this,videoUri);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        ModelRenderable
                .builder()
                .setSource(this,R.raw.video_screen)
                .build()
                .thenAccept(modelRenderable -> {
                    videoRenderable = modelRenderable;
                    videoRenderable.getMaterial().setExternalTexture("videoTexture",texture);
                    videoRenderable.getMaterial().setFloat4("keyColor",new Color(0.01843f,1.0f,0.098f));
                });

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();

                texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
                    anchorNode.setRenderable(videoRenderable);
                    texture.getSurfaceTexture().setOnFrameAvailableListener(null);
                });

            }else {
                //anchorNode.setRenderable(videoRenderable);
            }

            float width = mediaPlayer.getVideoWidth();
            float height = mediaPlayer.getVideoHeight();

            anchorNode.setLocalScale(new Vector3(
                    HEIGHT * (width / height), HEIGHT, 0.95f
            ));

            arFragment.getArSceneView().getScene().addChild(anchorNode);

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}