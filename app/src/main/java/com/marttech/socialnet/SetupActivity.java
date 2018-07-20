package com.marttech.socialnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SetupActivity extends AppCompatActivity {

    private EditText username,fullName,countryName;
    private Button saveBtn;
    private CircleImageView profileImg;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private StorageReference userProfImgRef;

    String currentUserId;
    ProgressDialog mProgressDialog;
    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mProgressDialog = new ProgressDialog(this);

        username = findViewById(R.id.setup_username);
        fullName = findViewById(R.id.setup_fullname);
        countryName = findViewById(R.id.setup_country);
        saveBtn = findViewById(R.id.saveBtn);
        profileImg = findViewById(R.id.setup_profile);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInfo();
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_PICK);
            }
        });
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this)
                                .load(image)
                                .placeholder(R.drawable.profile)
                                .into(profileImg);
                    }else {
                        Toast.makeText(SetupActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                final StorageReference filePath = userProfImgRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
//   when the image has successfully uploaded, get its download Url
                            mProgressDialog.dismiss();
                            Toast.makeText(SetupActivity.this, "Profile image stored successfully to Firebase storage....", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadedUrl = uri.toString();

                                    dbReference.child("profileimage").setValue(downloadedUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        mProgressDialog.dismiss();
                                                        Intent selfIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                                        startActivity(selfIntent);
                                                        Toast.makeText(SetupActivity.this, "Your profile image has been stored to database successfully ", Toast.LENGTH_SHORT).show();

                                                    }else {
                                                        mProgressDialog.dismiss();
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "An error occurred: "+message, Toast.LENGTH_SHORT).show();
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

    private void saveAccountSetupInfo() {
        String enteredName = username.getText().toString();
        String enteredfName = fullName.getText().toString();
        String enteredcountryName = countryName.getText().toString();

        if(TextUtils.isEmpty(enteredName)){
            username.setError("Field Required!");
        }else  if(TextUtils.isEmpty(enteredfName)){
            fullName.setError("Field Required!");
        }else if(TextUtils.isEmpty(enteredcountryName)){
            countryName.setError("Field Required!");
        }else{
            mProgressDialog.setTitle("Saving information");
            mProgressDialog.setMessage("Please wait ....");
            mProgressDialog.show();

            HashMap userMap = new HashMap();
            userMap.put("username",enteredName);
            userMap.put("fullname",enteredfName);
            userMap.put("country",enteredcountryName);
            userMap.put("status","Hey there, i am using Poster Social Network, developed by martTech");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");

            dbReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        mProgressDialog.dismiss();
                        Intent intent = new Intent(SetupActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                        Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_LONG).show();
                    }else{
                        mProgressDialog.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "An error occurred"+ message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
                    

        }

    }
}
