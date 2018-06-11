package com.dayal.talkative.activities.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private TextInputLayout loginEmail;
    private TextInputLayout loginPassword;
    private Toolbar loginToolbar;

    private ProgressDialog loginProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users");

        loginToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle("Login Screen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginBtn = (Button)findViewById(R.id.login_btn);
        loginEmail = (TextInputLayout)findViewById(R.id.login_email);
        loginPassword  = (TextInputLayout)findViewById(R.id.login_pass);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uEmail = loginEmail.getEditText().getText().toString();
                String uPass = loginPassword.getEditText().getText().toString();
                if (!(uEmail.isEmpty() || uPass.isEmpty())){
                    loginUser(uEmail,uPass);
                    loginProgress.setTitle("Authenticating");
                    loginProgress.setMessage("Checking credentials");
                    loginProgress.setCanceledOnTouchOutside(false);
                    loginProgress.show();
                }
            }
        });
    }

    private void loginUser(String uEmail, String uPass) {

        mAuth.signInWithEmailAndPassword(uEmail,uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    loginProgress.dismiss();

                    String curr_uid = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mUserDatabse.child(curr_uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });

                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }else{
                    loginProgress.hide();
                    String error = ""; try { throw task.getException(); }
                    catch (FirebaseAuthInvalidUserException e) { error = "Invalid Email!"; }
                    catch (FirebaseAuthInvalidCredentialsException e) { error = "Invalid Password!"; }
                    catch (Exception e) { error = "Default error!"; e.printStackTrace(); }
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
