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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyMessagesActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPersonalChatsList;
    private DatabaseReference UsersRef, MessagesRef;
    private String CurrentUserID;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);

        mToolbar = findViewById(R.id.my_messages_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Direct");

        myPersonalChatsList = findViewById(R.id.my_messages_list);
        myPersonalChatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPersonalChatsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(CurrentUserID);

        DisplayAllMessageContacts();

    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("Time", saveCurrentTime);
        currentStateMap.put("Date", saveCurrentDate);
        currentStateMap.put("Type", state);

        UsersRef.child(CurrentUserID).child("userState").updateChildren(currentStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }

    private void DisplayAllMessageContacts() {

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(MessagesRef, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends, MyMessagesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, MyMessagesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final MyMessagesViewHolder viewHolder, int position, FindFriends model) {

                final String userId = getRef(position).getKey();

                UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            final String fullName = dataSnapshot.child("Full_Name").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                            final String status = dataSnapshot.child("Status").getValue().toString();
                            final String username = dataSnapshot.child("Username").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("Type").getValue().toString();

                                if(type.equals("online")){
                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else{
                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            viewHolder.setFull_Name(fullName);
                            viewHolder.setProfileImage(getApplicationContext(), profileImage);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent myMessageIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                    myMessageIntent.putExtra("Visit_User_Id", userId);
                                    myMessageIntent.putExtra("FullName", fullName);
                                    myMessageIntent.putExtra("Username", username);
                                    myMessageIntent.putExtra("profImage", profileImage);
                                    startActivity(myMessageIntent);

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public MyMessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_messages_display_layout, parent, false);

                return new MyMessagesViewHolder(view);
            }
        };
        myPersonalChatsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class MyMessagesViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView onlineStatusView;

        public MyMessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            onlineStatusView = itemView.findViewById(R.id.all_users_online_icon);
        }

        public void setFull_Name(String full_Name){

            TextView myMsgFullName = mView.findViewById(R.id.my_messages_full_name);
            myMsgFullName.setText(full_Name);
        }

        public void setProfileImage(Context ctx, String profileImage){

            CircleImageView myMsgProfileImg = mView.findViewById(R.id.my_messages_profile_image);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(myMsgProfileImg);
        }

    }
}
