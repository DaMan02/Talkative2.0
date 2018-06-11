package com.dayal.talkative.activities.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dayal.talkative.R;
import com.dayal.talkative.activities.adapter.ViewPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mUserRef;

    private ViewPager mViewPager;
    private ViewPagerAdapter pagerAdapter;
    private TabLayout tabLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Talkative");

        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


            mViewPager = (ViewPager) findViewById(R.id.view_pager);
            tabLayout = (TabLayout) findViewById(R.id.main_tabLayout);

            pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(pagerAdapter);

            tabLayout.setupWithViewPager(mViewPager);

            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                    FirebaseUser currentuser = firebaseAuth.getCurrentUser();
                    if (currentuser != null) {

                    } else {
                        sendUserToStart();
                    }
                }
            };
        }
    }

    private void sendUserToStart() {
        startActivity(new Intent(MainActivity.this,StartActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.main_logout_btn : FirebaseAuth.getInstance().signOut();
                    sendUserToStart();
                break;
            case R.id.main_settings :
                Intent settIntent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(settIntent);
                break;
            case R.id.main_all_users:
                startActivity(new Intent(MainActivity.this,UsersActivity.class));
        }
          return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendUserToStart();

        } else {

            mUserRef.child("online").setValue("true");

        }

    }

    @Override
    public void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }
}
