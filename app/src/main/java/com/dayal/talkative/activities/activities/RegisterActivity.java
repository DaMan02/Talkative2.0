package com.dayal.talkative.activities.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dayal.talkative.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createAc;
    private TextInputLayout name;
    private TextInputLayout email;
    private TextInputLayout password;
    private Toolbar regToolbar;

    private ProgressDialog regProgress;

    private DatabaseReference myDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mLAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        regToolbar = (Toolbar)findViewById(R.id.reg_toolbar);
        setSupportActionBar(regToolbar);
//        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (TextInputLayout)findViewById(R.id.id_name);
        email = (TextInputLayout)findViewById(R.id.id_email);
        password = (TextInputLayout)findViewById(R.id.id_pass);
        createAc = (Button)findViewById(R.id.create_ac);

        //TODO check if user is logged in & internet is available
        createAc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uName = name.getEditText().getText().toString();
                String uEmail = email.getEditText().getText().toString();
                String uPass = password.getEditText().getText().toString();
                if (!(uName.isEmpty() || uEmail.isEmpty() || uPass.isEmpty())){
                    if (uPass.length()<6){
                        password.setHint("Password must contain at least 6 characters");
                        password.setBackgroundColor(Color.parseColor("#E6F50501"));
                    }else{
                        regUser(uName,uEmail,uPass);

                        regProgress.setTitle("Signing up");
                        regProgress.setMessage("Please wait");
                        regProgress.setCanceledOnTouchOutside(false);
                        regProgress.show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }


            }
        });

       }

    private void regUser(final String uName, String uEmail, String uPass) {

        mAuth.createUserWithEmailAndPassword(uEmail,uPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currUser.getUid();

                           // setting up data for the user
                            myDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name",uName);
                            userMap.put("status","Hey, chatting is more fun now.");
                            userMap.put("image","default");
                            userMap.put("thumbnail","default");

                            myDatabaseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        regProgress.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });



                        }else{
                            // toast unsuccessful
                            regProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Failed !  Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }
}
