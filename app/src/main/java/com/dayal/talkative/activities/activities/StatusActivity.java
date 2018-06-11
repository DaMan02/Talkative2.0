package com.dayal.talkative.activities.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dayal.talkative.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private EditText statusEdit;
    private Button updateBtn;

    private String mStatus;

    private DatabaseReference statusDatabase;
    private FirebaseUser currUser;

    private Toolbar statToolbar;

    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statToolbar = (Toolbar)findViewById(R.id.status_toolbar);
        setSupportActionBar(statToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Status");

        progress = (ProgressBar)findViewById(R.id.status_progress);
        progress.setIndeterminate(true);
        progress.setVisibility(View.INVISIBLE);

        currUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currUser.getUid();

        statusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        updateBtn = (Button)findViewById(R.id.update_btn);
        statusEdit = (EditText) findViewById(R.id.status_update_id);

        mStatus = getIntent().getStringExtra("current_status");
        statusEdit.setText(mStatus);


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status = statusEdit.getText().toString();
                progress.setVisibility(View.VISIBLE);
                statusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                               progress.setVisibility(View.GONE);
                               finish();

                        }else{
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Unknown error !",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
}
