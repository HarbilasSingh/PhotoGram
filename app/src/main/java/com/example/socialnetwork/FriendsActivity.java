package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myFriendsList;

    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth= FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.friends_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Friends");

        myFriendsList = findViewById(R.id.friendsList);
        myFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendsList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }


    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder viewHolder, int position, Friends model) {

                viewHolder.setDate(model.getDate());
                final String userId = getRef(position).getKey();

                UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String fullName = dataSnapshot.child("Full_Name").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                            final String status = dataSnapshot.child("Status").getValue().toString();
                            final String username = dataSnapshot.child("Username").getValue().toString();
//                            final String type;
//
//                            if(dataSnapshot.hasChild("userState")){
//                                type = dataSnapshot.child("userState").child("Type").getValue().toString();
//
//                                if(type.equals("online")){
//                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
//                                }
//                                else{
//                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
//                                }
//                            }

                            viewHolder.setFull_Name(fullName);
                            viewHolder.setProfImage(getApplicationContext(), profileImage);
                            viewHolder.setStatus(status);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                      "View profile",
                                      "Send Message"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select an option...");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("Visit_User_Id", userId);
                                                startActivity(profileIntent);
                                            }
                                            if(which == 1){
                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("Visit_User_Id", userId);
                                                chatIntent.putExtra("FullName", fullName);
                                                chatIntent.putExtra("Username", username);
                                                chatIntent.putExtra("profImage", profileImage);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };
        firebaseRecyclerAdapter.startListening();
        myFriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

//        ImageView onlineStatusView;


        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
//            onlineStatusView = itemView.findViewById(R.id.all_users_online_icon);
        }

        public void setProfImage(Context ctx, String profImage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profImage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFull_Name(String full_Name){
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(full_Name);
        }

        public void setStatus(String status){
            TextView myStatus = mView.findViewById(R.id.all_users_profile_status);
            myStatus.setText(status);
        }

        public void setDate(String date)
        {
            TextView myDate = mView.findViewById(R.id.all_users_profile_date);
            myDate.setVisibility(View.VISIBLE);
            myDate.setText("Friends since: " + date);
        }
    }
}
