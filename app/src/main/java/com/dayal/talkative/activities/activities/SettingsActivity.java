package com.dayal.talkative.activities.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dayal.talkative.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_REQ_CODE = 1;
    private DatabaseReference database;
    private FirebaseUser currentUser;

    private TextView nameTV;
    private TextView statusTV;
    private CircleImageView dpImage;
    private CircleImageView editPicBtn;
    private Button updateBtn;

    private String currUid;

    private ProgressDialog imgProgress;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nameTV = (TextView)findViewById(R.id.prof_name);
        statusTV = (TextView)findViewById(R.id.status_text);
        dpImage = (CircleImageView)findViewById(R.id.sett_pic);
        editPicBtn = (CircleImageView)findViewById(R.id.edit_pic_btn);
        updateBtn = (Button)findViewById(R.id.update_stat_btn);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currUid  = currentUser.getUid();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance().getReference().child("Users").child(currUid);
        database.keepSynced(true);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String  profName = dataSnapshot.child("name").getValue().toString();
               String profStatus = dataSnapshot.child("status").getValue().toString();
               final String displayPic = dataSnapshot.child("image").getValue().toString();
               String thumbNail = dataSnapshot.child("thumbnail").getValue().toString();

                nameTV.setText(profName);
                statusTV.setText(profStatus);

                if (!displayPic.equals("default")){
                    Picasso.with(SettingsActivity.this).load(displayPic).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.headicon).into(dpImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(displayPic).placeholder(R.drawable.headicon).into(dpImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // TODO check internet connection
        editPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // start picker to get image for cropping and then use the image in cropping activity
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);
//
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent,"Select image"),GALLERY_REQ_CODE);

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = statusTV.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("current_status",status_value);
                startActivity(statusIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                      .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File thumb_path = new File(resultUri.getPath());

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(65)
                            .compressToBitmap(thumb_path);
                } catch (IOException e) {

                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mStorageRef.child("profile_pics").child(currUid + ".jpg");
                final StorageReference thumb_filePath = mStorageRef.child("profile_pics").child("thumbs").child(currUid + ".jpg");

                imgProgress = new ProgressDialog(this);
                imgProgress.setMessage("Uploading...");
                imgProgress.setCanceledOnTouchOutside(false);
                imgProgress.show();

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            @SuppressWarnings("VisibleForTests")
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    @SuppressWarnings("VisibleForTests")
                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){

                                        Map<String,Object> updateHashmap = new HashMap<>();
                                        updateHashmap.put("image",downloadUrl);
                                        updateHashmap.put("thumbnail",thumb_download_url);

                                        database.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> thumb_task) {
                                                if (thumb_task.isSuccessful()) {

                                                    imgProgress.dismiss();

                                                }
                                            }
                                        });

                                    }

                                }
                            });


                        } else {
                            imgProgress.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error uploading image !", Toast.LENGTH_SHORT).show();
                        }
                    }


                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
