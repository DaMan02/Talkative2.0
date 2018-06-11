package com.dayal.talkative.activities.activities;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.dayal.talkative.R;
import com.dayal.talkative.activities.model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mUserToolbar;

    private RecyclerView recyclerView;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUserToolbar = (Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mUserToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        recyclerView = (RecyclerView)findViewById(R.id.users_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_list_row,
                UsersViewHolder.class,
                userDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users users, int position) {
                viewHolder.setName(users.getName());
                viewHolder.setStatus(users.getStatus());
                viewHolder.setThumb_Image(users.getThumb_Image(),getApplicationContext());

                final String userId = getRef(position).getKey();        // get key of current user by id

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profIntent.putExtra("user_id", userId);
                        startActivity(profIntent);

                    }
                });


            }
        } ;

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name) {
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status) {

            TextView userStatusView = (TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setThumb_Image(String thumb_Image, Context ctx) {

            CircleImageView thumbImageView = (CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_Image).placeholder(R.drawable.headicon).into(thumbImageView);
        }
    }
}
