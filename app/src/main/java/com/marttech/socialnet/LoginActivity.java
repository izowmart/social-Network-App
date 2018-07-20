package com.marttech.socialnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    
    private Button loginBtn;
    private EditText userEmail, userPassword;
    private TextView newAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private ImageView googleSignInBtn;

    private final static int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private final static String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);

        loginBtn = findViewById(R.id.login_btn);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        newAccount = findViewById(R.id.register_link);
        googleSignInBtn = findViewById(R.id.google_btn);
        
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent); 
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticatingUser();
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this, "Connection to google signin failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

            mProgressDialog.setTitle("Google sign in");
            mProgressDialog.setMessage("Please wait while we allow you to login using your google account....");
            mProgressDialog.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                mProgressDialog.dismiss();
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please waith when we are getting your auth result... ", Toast.LENGTH_SHORT).show();
            }else{
                mProgressDialog.dismiss();
                Toast.makeText(this, "Can't get auth result", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainActivity();

                        } else {
                            mProgressDialog.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            sendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Error occurred"+message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            Intent loginIntent = new Intent(LoginActivity.this,MainActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }
    }

    private void authenticatingUser() {
        String email,password;
        email = userEmail.getText().toString();
        password = userPassword.getText().toString();


        if(TextUtils.isEmpty(email)){
            userEmail.setError("Field Required!");
        }else if (!email.contains("@")){
            userEmail.setError("Enter a valid email!");
        }else  if (TextUtils.isEmpty(password)){
            userPassword.setError("Field Required!");
        }else {

            mProgressDialog.setTitle("Signing in");
            mProgressDialog.setMessage("Please wait ....");
            mProgressDialog.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                mProgressDialog.dismiss();
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "An error occurred" + message, Toast.LENGTH_SHORT).show();
                            }else{
                                mProgressDialog.dismiss();
                                sendUserToMainActivity();
                            }
                        }
                    });

        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
