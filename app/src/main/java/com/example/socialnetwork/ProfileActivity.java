package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    private TextView myProfName, myProfuserName, myProfStatus, myProfCountry, myProfGender, myProfDob, myProfRelationshipStatus;
    private CircleImageView myProfImage;

    private DatabaseReference ProfUserRef, FriendsRef, PostsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private Button NoOfPosts, NoOfFriends;
    private int CountFriends = 0, CountPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ProfUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        myProfName = findViewById(R.id.my_profile_full_name);
        myProfuserName = findViewById(R.id.my_profile_user_name);
        myProfStatus = findViewById(R.id.my_profile_status);
        myProfCountry = findViewById(R.id.my_profile_country);
        myProfGender = findViewById(R.id.my_profile_gender);
        myProfDob = findViewById(R.id.my_profile_dob);
        myProfRelationshipStatus = findViewById(R.id.my_profile_relationship_status);
        myProfImage = findViewById(R.id.my_profile_pic);
        NoOfPosts = findViewById(R.id.my_post_button);
        NoOfFriends = findViewById(R.id.my_friends_button);

        NoOfFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendsActivity();
            }
        });

        NoOfPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });

        PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CountPosts = (int) dataSnapshot.getChildrenCount();
                    NoOfPosts.setText(CountPosts + " Posts");
                }
                else{
                    NoOfPosts.setText("NO POSTS");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CountFriends = (int) dataSnapshot.getChildrenCount();
                    NoOfFriends.setText(CountFriends + " Friends");
                }
                else{
                    NoOfFriends.setText("No Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ProfUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("Username").getValue().toString();
                    String myProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("Status").getValue().toString();
                    String myDOB = dataSnapshot.child("DOB").getValue().toString();
                    String myCountry = dataSnapshot.child("Country").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("Relationship_Status").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(myProfImage);
                    myProfuserName.setText("@"+myUserName);
                    myProfStatus.setText(myProfileStatus);
                    myProfCountry.setText("Country: "+myCountry);
                    myProfRelationshipStatus.setText("Relationship Status: "+myRelationshipStatus);
                    myProfDob.setText("DOB: "+myDOB);
                    myProfGender.setText("Gender: "+myGender);
                    myProfName.setText(myProfileName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToMyPostsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(friendsIntent);
    }
}
