package com.marttech.socialnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton selectPostImg;
    private Button updatePostBtn;
    private EditText postDescription;

    private static final int GALLERY_PICK = 1;
    private Uri imageUri;

    private StorageReference postImgRef;
    private DatabaseReference userDbReference,postDbReference;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime,postRandomName,downloadUrl, currentUID;
    private ProgressDialog mProgressDialog;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        postImgRef = FirebaseStorage.getInstance().getReference();
        userDbReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postDbReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        mProgressDialog = new ProgressDialog(this);

        selectPostImg = findViewById(R.id.select_img);
        updatePostBtn = findViewById(R.id.post_Btn);
        postDescription = findViewById(R.id.post_description);

        mToolbar = findViewById(R.id.update_post_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        updatePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    private void validatePostInfo() {
         description = postDescription.getText().toString();
        if (imageUri == null){
            Toast.makeText(this, "Please select an image...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please say something about your image", Toast.LENGTH_SHORT).show();
        }else{
            mProgressDialog.setTitle("Add new post");
            mProgressDialog.setMessage("Please wait as we update your new post ....");
            mProgressDialog.show();

            storingImgToFirebaseStorage();
        }
    }

    private void storingImgToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentDate;

        final StorageReference filePath = postImgRef.child("Post Images").child(imageUri.getLastPathSegment() +postRandomName + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Image has been uploaded successfully to storage", Toast.LENGTH_SHORT).show();
//                getting the download url from firebase storage to firebase database
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            savingPostInfoToDatabase();
                        }
                    });


                }else{
                    mProgressDialog.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void savingPostInfoToDatabase() {
        userDbReference.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userFullname = dataSnapshot.child("fullname").getValue().toString();
                    String userProfile = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",currentUID);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",description);
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profileimage",userProfile);
                    postsMap.put("fullname",userFullname);

                    postDbReference.child(currentUID + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "Your new post is updated successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        mProgressDialog.dismiss();
                                        Toast.makeText(PostActivity.this, "Error occurred while updating post", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data !=null){
            imageUri = data.getData();
            Picasso.with(getApplicationContext())
                    .load(imageUri)
                    .placeholder(R.drawable.add_post)
                    .into(selectPostImg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainActivityIntent);
    }
}
