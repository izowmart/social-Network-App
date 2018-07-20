package com.marttech.socialnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail,password,confirmPassword;
    private Button regBtn;
    private FirebaseAuth mAuth;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);

        userEmail = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_password);
        confirmPassword = findViewById(R.id.reg_confirm_password);
        regBtn = findViewById(R.id.register_btn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            Intent registerIntent = new Intent(RegisterActivity.this,MainActivity.class);
            registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(registerIntent);
        }
    }

    private void createNewAccount() {
        String enteredEmail = userEmail.getText().toString();
        String enteredPassword = password.getText().toString();
        String enteredConfirmPass = confirmPassword.getText().toString();

        if(TextUtils.isEmpty(enteredEmail)){
            userEmail.setError("Field Required!");
        }else if (!enteredEmail.contains("@")){
            userEmail.setError("Enter a valid email!");
        }else  if (TextUtils.isEmpty(enteredPassword)){
            password.setError("Field Required!");
        }else if (enteredPassword.length() < 6){
            password.setError("Password must be more than six");
        }else  if (TextUtils.isEmpty(enteredConfirmPass)) {
            confirmPassword.setError("Field Required!");
        }else if (!enteredPassword.equals(enteredConfirmPass)){
            confirmPassword.setError("Passwords not matching");
        }else{
            mProgressDialog.setTitle("Creating New Account");
            mProgressDialog.setMessage("Please wait ....");
            mProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(enteredEmail,enteredPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                mProgressDialog.dismiss();
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                            }else {
                                mProgressDialog.dismiss();
                                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }
    }
}
