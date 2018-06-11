package com.dayal.talkative.activities.activities;

import android.app.ProgressDialog;
import java.text.SimpleDateFormat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dayal.talkative.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView profileName, profileStatus, profileFriendsCount;
    private Button sendReqBtn, declineBtn;

    private DatabaseReference mDatabase;
    private DatabaseReference friendReqDatabase;
    private DatabaseReference friendsDatabase;
    private DatabaseReference notificationDatabase;

    private FirebaseUser currUser;

    private ProgressDialog mProgress;

    private String friend_curr_state;
    private String sendBtnToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.profile_displayName);
        profileStatus = (TextView) findViewById(R.id.profile_status);
        profileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        sendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        declineBtn = (Button) findViewById(R.id.profile_decline_btn);

        declineBtn.setVisibility(View.INVISIBLE);

        final String userId = getIntent().getStringExtra("user_id");


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        currUser = FirebaseAuth.getInstance().getCurrentUser();

        friend_curr_state = "not_friends";

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dName = dataSnapshot.child("name").getValue().toString();
                String uStatus = dataSnapshot.child("status").getValue().toString();
                String uProfPic = dataSnapshot.child("image").getValue().toString();

                profileName.setText(dName);
                profileStatus.setText(uStatus);
                Picasso.with(ProfileActivity.this).load(uProfPic).placeholder(R.drawable.headicon).into(mProfileImage);

                if(currUser.getUid().equals(userId)){
                   sendReqBtn.setVisibility(View.INVISIBLE);      // remove btn for my profile

                }

                // -----------------------------FRIEND LIST/ REQUEST FEATURE --------------------
                friendReqDatabase.child(currUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if (req_type.equals("received")){
                                friend_curr_state = "req_received";      // req received
                                sendReqBtn.setText("Accept Friend Request");
                                declineBtn.setVisibility(View.VISIBLE);

                            }else if (req_type.equals("sent")){
                                friend_curr_state = "req_sent";      // req sent
                                sendReqBtn.setText("Cancel Friend Request");

                            }else {
                                friendsDatabase.child(currUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(userId)){     // if user01 & user02 are friends

                                            friend_curr_state = "friends";
                                            sendReqBtn.setText("Remove from Friends' list");
                                            declineBtn.setVisibility(View.INVISIBLE);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendBtnToast = "Success";
        Log.w("log","default state: " + friend_curr_state);
        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                       // sendReqBtn = acceptReqBtn = CancelReqBtn

                sendReqBtn.setEnabled(false);
                // ------------------------ NOT FRIENDS ---------------------------------
                if (friend_curr_state.equals("not_friends")) {                           //  not friends & send_req btn clicked
                    // currUser = the user(user01),  userId =  user whose profile curr user is viewing(user02)

                    friendReqDatabase.child(currUser.getUid()).child(userId).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendReqDatabase.child(userId).child(currUser.getUid()).child("request_type").setValue("received")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        sendBtnToast = "Request sent";
                                        sendReqBtn.setEnabled(true);
                                        friend_curr_state = "req_sent";      // req sent
                                        sendReqBtn.setText("Cancel Friend Request");

                                        HashMap<String, String> notifiData = new HashMap<String, String>();
                                        notifiData.put("from",currUser.getUid());
                                        notifiData.put("type","request");

                                        notificationDatabase.child(userId).push().setValue(notifiData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });

                                    }
                                });

                            }else{
                                        sendReqBtn.setEnabled(true);
                                        sendBtnToast = "Oops, could not send Request!";

                            }
                        }
                    });
                }
                // ---------------------------------- CANCEL REQ  -------------------------------------
                if( friend_curr_state == "req_sent") {                         // req sent & cancel btn clicked

                    friendReqDatabase.child(currUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendReqDatabase.child(userId).child(currUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendReqBtn.setEnabled(true);
                                    friend_curr_state = "not_friends";
                                    sendReqBtn.setText("Send Friend Request");
                                    sendBtnToast = "Friend Request Cancelled";
                                }
                            });
                        }
                    });
                }

                //- ----------------------------------- req received ----------------------
                if (friend_curr_state == "req_received") {    // req received & accept btn clicked
                      final String currDate = SimpleDateFormat.getDateTimeInstance().format(new Date());
                      friendsDatabase.child(currUser.getUid()).child(userId).setValue(currDate)
                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {

                              friendsDatabase.child(currUser.getUid()).child(userId).setValue(currDate)
                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void aVoid) {

                                      friendReqDatabase.child(currUser.getUid()).child(userId).removeValue()
                                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void aVoid) {
                                              friendReqDatabase.child(userId).child(currUser.getUid()).removeValue()
                                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void aVoid) {
                                                      sendReqBtn.setEnabled(true);
                                                      friend_curr_state = "friends";
                                                      sendReqBtn.setText("Remove from Friends' list");
                                                      declineBtn.setVisibility(View.INVISIBLE);
                                                      sendBtnToast = "Great ! You added a new friend";
                                                  }
                                              });
                                          }
                                      });
                                  }
                              });
                          }
                      });
                }
                // -------------------------------------------- UN-FRIEND -------------------------------

                if (friend_curr_state.equals("friends")) {                   // friends & un-friend clicked

                    friendsDatabase.child(currUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendsDatabase.child(userId).child(currUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendReqBtn.setEnabled(true);
                                    friend_curr_state = "not_friends";
                                    sendReqBtn.setText("Send Friend Request");
                                    sendBtnToast = "Friend Removed !";
                                }
                            });
                        }
                    });
                }

                Toast.makeText(ProfileActivity.this, sendBtnToast, Toast.LENGTH_SHORT).show();
                Log.w("log","Toast msg: " + sendBtnToast + " ,friend_state: " + friend_curr_state);
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                declineBtn.setEnabled(false);

                friendsDatabase.child(currUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsDatabase.child(userId).child(currUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friend_curr_state = "not_friends";
                                declineBtn.setVisibility(View.INVISIBLE);
                                sendReqBtn.setText("Send Friend Request");
                                // TODO toast friend removed
                            }
                        });
                    }
                });
            }
        });


    }
}
