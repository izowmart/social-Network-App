package com.marttech.socialnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName,userProName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfileImage;
    private Button updateSettingsBtn;
    private ProgressDialog mProgressDialog;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference userProfImgRef;

    private String currentUID;
    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        userProfImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);

        userName = findViewById(R.id.settings_username);
        userProName = findViewById(R.id.settings_fullname);
        userGender = findViewById(R.id.settings_gender);
        userCountry = findViewById(R.id.settings_country);
        userDOB = findViewById(R.id.settings_dob);
        userRelation = findViewById(R.id.settings_relationship);
        userStatus = findViewById(R.id.settings_status);
        userProfileImage= findViewById(R.id.settings_profile_image);
        updateSettingsBtn = findViewById(R.id.update_settings_btn);

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(SettingsActivity.this)
                            .load(myProfileImage)
                            .placeholder(R.drawable.profile)
                            .into(userProfileImage);
                    userName.setText(myUserName);
                    userProName.setText(myProfileName);
                    userCountry.setText(myCountry);
                    userDOB.setText(myDOB);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                    userStatus.setText(myProfileStatus);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_PICK);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                mProgressDialog.setTitle("Updating Profile image");
                mProgressDialog.setMessage("Please wait ....");
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfImgRef.child(currentUID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
//   when the image has successfully uploaded, get its download Url
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Profile image stored successfully to Firebase storage....", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadedUrl = uri.toString();

                                    settingsUserRef.child("profileimage").setValue(downloadedUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        mProgressDialog.dismiss();
                                                        Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                                        startActivity(selfIntent);
                                                        Toast.makeText(SettingsActivity.this, "Your profile image has been stored to database successfully ", Toast.LENGTH_SHORT).show();

                                                    }else {
                                                        mProgressDialog.dismiss();
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "An error occurred: "+message, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
        }
    }


    private void validateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)){
            userName.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(profilename)) {
            userProName.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(status)) {
            userStatus.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(dob)) {
            userDOB.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(country)) {
            userCountry.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(gender)) {
            userGender.setError("Field can't be empty");
        }else if(TextUtils.isEmpty(relation)) {
            userRelation.setError("Field can't be empty");
        }else{
            mProgressDialog.setTitle("Updating Profile");
            mProgressDialog.setMessage("Please wait ....");
            mProgressDialog.show();
            updateAccountInformation(username,profilename,status,dob,country,gender,relation);
        }
    }

    private void updateAccountInformation(String username, String profilename, String status, String dob, String country, String gender, String relation) {

        HashMap userMap = new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profilename);
        userMap.put("dob",dob);
        userMap.put("status",status);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings updated successfully", Toast.LENGTH_SHORT).show();
                }else{
                    mProgressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Error occured while updating account settings information", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
