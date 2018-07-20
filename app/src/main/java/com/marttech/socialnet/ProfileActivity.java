package com.marttech.socialnet;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView userName,userProName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);


        userName = findViewById(R.id.my_username);
        userProName = findViewById(R.id.my_profile_full_name);
        userGender = findViewById(R.id.my_gender);
        userCountry = findViewById(R.id.my_country);
        userDOB = findViewById(R.id.my_dob);
        userRelation = findViewById(R.id.my_relationship_status);
        userStatus = findViewById(R.id.my_profile_status);
        userProfileImage= findViewById(R.id.my_profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(ProfileActivity.this)
                            .load(myProfileImage)
                            .placeholder(R.drawable.profile)
                            .into(userProfileImage);
                    userName.setText("@"+myUserName);
                    userProName.setText(myProfileName);
                    userCountry.setText("Country"+myCountry);
                    userDOB.setText("DOB"+myDOB);
                    userGender.setText("Gender"+myGender);
                    userRelation.setText("Relationship"+myRelationStatus);
                    userStatus.setText("Status"+myProfileStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
