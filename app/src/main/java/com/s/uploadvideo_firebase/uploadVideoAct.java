package com.s.uploadvideo_firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class uploadVideoAct extends AppCompatActivity {

    private EditText videoNameET;
    private VideoView Video;
    private MediaController mc;
    private Button uploadBtn;
    private Button selectVideoBtn;

    private Uri objectUri;


    private StorageReference objectStorageReference;
    private FirebaseFirestore objectFirebaseFirestore;

    private Dialog objectDialog;

    private boolean isImageSelected= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);



        objectFirebaseFirestore = FirebaseFirestore.getInstance();
        objectStorageReference = FirebaseStorage.getInstance().getReference("MyVideos");

        objectDialog = new Dialog(this);
        objectDialog.setCancelable(false);

        objectDialog.setContentView(R.layout.please_wait_dialog);



        connectXML();
    }
    private void connectXML()
    {
        try
        {
            videoNameET = findViewById(R.id.videoNameET);
            Video = findViewById(R.id.videoToUploadIV);

            uploadBtn = findViewById(R.id.uploadVideoBtn);
            selectVideoBtn = findViewById(R.id.selectVideoBtn);



            selectVideoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectImageFromGallery();
                }
            });

            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadImageToFirebaseStorage();
                }
            });

        }
        catch (Exception e)
        {
            Toast.makeText(this, "connectXML:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImageFromGallery()
    {
        try
        {
            Intent objectIntent = new Intent();
            objectIntent.setType("video/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(objectIntent, "Select a Video"),123);

        }
        catch (Exception e)
        {
            Toast.makeText(this, "selectImageFromGallery:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==123 && resultCode== RESULT_OK && data !=null)
        {
            objectUri=data.getData();
            Video.setVideoURI(objectUri);
            Video.start();
            isImageSelected = true;


        }
    }

    private void uploadImageToFirebaseStorage()
    {
        try
        {
            if(isImageSelected) {
                if (!videoNameET.getText().toString().isEmpty()) {
                    objectDialog.show();
                    String imageName = videoNameET.getText().toString() + "." + getExtention(objectUri);
                    final StorageReference finalImageRef = objectStorageReference.child(imageName);

                    UploadTask objectUploadTask = finalImageRef.putFile(objectUri);
                    objectUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {
                                objectDialog.dismiss();
                                throw task.getException();
                            }

                            return finalImageRef.getDownloadUrl();
                        }
                    })
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> objectMap = new HashMap<>();
                                        objectMap.put("url", task.getResult().toString());

                                        objectFirebaseFirestore.collection("BSCSLinks")
                                                .document(videoNameET.getText().toString())
                                                .set(objectMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        objectDialog.dismiss();
                                                        Toast.makeText(uploadVideoAct.this, "Video Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        objectDialog.dismiss();
                                                        Toast.makeText(uploadVideoAct.this, "Fails to upload the Video ", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {
                                        objectDialog.dismiss();
                                        Toast.makeText(uploadVideoAct.this, "Message from Firebase" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    objectDialog.dismiss();
                                    Toast.makeText(uploadVideoAct.this, "Firebase storage response:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Please enter the valid name.", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Please Choose Video before uploading", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "uploadImageToFirebaseStorage:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private String getExtention(Uri objectUri)
    {
        try
        {
            ContentResolver objectContentResolver = getContentResolver();
            MimeTypeMap objectMimeTypeMap = MimeTypeMap.getSingleton();

            String extensionOfImage = objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver
                    .getType(objectUri));

            return extensionOfImage;
        }
        catch (Exception e)
        {
            objectDialog.dismiss();
            Toast.makeText(this, "getExtension:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }



}
