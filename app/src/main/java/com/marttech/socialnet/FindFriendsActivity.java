package com.marttech.socialnet;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchBtn;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private DatabaseReference allUsersDbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find friends");

        searchResultList = findViewById(R.id.search_result_list);
        searchResultList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        searchResultList.setLayoutManager(linearLayoutManager);

        searchBtn = findViewById(R.id.search_pple_friends_btn);
        searchInputText = findViewById(R.id.search_box_input);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String searchBoxInput = searchInputText.getText().toString();
                searchPeopleFriends(searchBoxInput);
            }
        });
    }

    private void searchPeopleFriends(String searchBoxInput) {
        Toast.makeText(this, "Searching......", Toast.LENGTH_LONG).show();

        Query searchPplAndFriendsQuery = allUsersDbRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_desplay,
                        FindFriendsViewHolder.class,
                        searchPplAndFriendsQuery
                )
        {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
            }
        };

        searchResultList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(Context ctx,String profileimage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx)
                    .load(profileimage)
                    .placeholder(R.drawable.profile)
                    .into(myImage);
        }
        public void setFullname(String fullname){
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setStatus(String status){
            TextView myStatus = mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }

    }

}
