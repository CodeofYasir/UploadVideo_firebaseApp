package com.s.uploadvideo_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class downloadVideoAct extends AppCompatActivity {

    private MediaController ctlr;
    private VideoView video;

    private EditText imageNameET;

    private Button downloadBtn;

    private Dialog objectDialog;
    private FirebaseFirestore objectFirebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_video);

        imageNameET = findViewById(R.id.videoNameET);
        video = findViewById(R.id.videoToDownloadVV);

        downloadBtn = findViewById(R.id.downloadVideoBtn);

        objectDialog = new Dialog(this);
        objectDialog.setCancelable(false);

        objectDialog.setContentView(R.layout.please_wait_dialog);

        objectFirebaseFirestore = FirebaseFirestore.getInstance();

        video = (VideoView) findViewById(R.id.videoToDownloadVV);
        ctlr = new MediaController(this);

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageUrl();
            }
        });

    }


    private void getImageUrl()
    {
        objectDialog.show();
        objectFirebaseFirestore.collection("BSCSLinks")
                .document(imageNameET.getText().toString())
                .get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            String url=documentSnapshot.getString("url");

                            getWindow().setFormat(PixelFormat.TRANSLUCENT);
                            video.setVideoPath(url);
                            video.setMediaController(ctlr);
                            ctlr.setAnchorView(video);
                            video.requestFocus();
                            video.start();
                            objectDialog.dismiss();
                        }
                        else
                        {
                            objectDialog.dismiss();
                            Toast.makeText(downloadVideoAct.this, "No such document exist", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                objectDialog.dismiss();
                                Toast.makeText(downloadVideoAct.this, "Fails to get url"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }
}
