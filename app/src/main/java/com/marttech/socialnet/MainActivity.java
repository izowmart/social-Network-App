package com.marttech.socialnet;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbreference,postRef,likesRef;
    private CircleImageView navProfileImage;
    private TextView navProfileUsername;
    private ImageButton addNewPostBtn;

    String currentUserId;
    Boolean likeChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        setContentView(R.layout.activity_main);


        dbreference = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        addNewPostBtn = findViewById(R.id.add_new_postBtn);
        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navView.findViewById(R.id.nav_profile_image);
        navProfileUsername = navView.findViewById(R.id.nav_user_full_name);

        dbreference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("fullname")){
                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUsername.setText(fullName);
                    }else                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                    if (dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this)
                                .load(image)
                                .placeholder(R.drawable.profile)
                                .into(navProfileImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        });
        addNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        displayAllUsersPosts();

    }

    private void displayAllUsersPosts() {
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                postRef
                        ) {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                        final String postKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(),model.getPostimage());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());

                        viewHolder.setLikeButtonstatus(postKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPost = new Intent(MainActivity.this,ClickPostActivity.class);
                                clickPost.putExtra("postKey",postKey);
                                startActivity(clickPost);
                            }
                        });
                        viewHolder.likePostBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likeChecker = true;
                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (likeChecker.equals(true)){
                                            if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                                                likesRef.child(postKey).child(currentUserId).removeValue();
                                                likeChecker = false;

                                            }else {
                                                likesRef.child(postKey).child(currentUserId).setValue(true);
                                                likeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);

    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton likePostBtn,commentPostBtn;
        TextView displayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likePostBtn = mView.findViewById(R.id.like_btn);
            commentPostBtn = mView.findViewById(R.id.comment_btn);
            displayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonstatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes =(int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostBtn.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes)+" Likes"));
                    }else {
                        countLikes =(int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostBtn.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(countLikes)+" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        public void setFullname(String fullname){
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = mView.findViewById(R.id.post_profile_img);
            Picasso.with(ctx)
                    .load(profileimage)
                    .into(image);
        }
        public void setTime(String time) {
            TextView postTime = mView.findViewById(R.id.post_time);
            postTime.setText(" "+time);
        }
        public void setDate(String date) {
            TextView postDate = mView.findViewById(R.id.post_date);
            postDate.setText(" "+date);
        }
        public void setDescription(String description) {
            TextView postDescription = mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }
        public void setPostimage(Context ctx, String postimage) {
            ImageView postImg = mView.findViewById(R.id.post_image);
            Picasso.with(ctx)
                    .load(postimage)
                    .into(postImg);
        }
    }

    private void sendUserToPostActivity() {
        Intent addNewIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewIntent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String Current_userId = mAuth.getCurrentUser().getUid();
        dbreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(Current_userId)){

                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(setupIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;
            case R.id.nav_home:
                break;
            case R.id.nav_friends:
                break;
            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                break;
            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                break;

        }
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(settingsIntent);
    }
    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(profileIntent);
    }
    private void sendUserToFindFriendsActivity() {
        Intent profileIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(profileIntent);
    }
}
