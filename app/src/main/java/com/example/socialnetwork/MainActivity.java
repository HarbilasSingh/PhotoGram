package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static com.example.socialnetwork.R.*;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private ImageButton addNewPostButton;

    private DatabaseReference UserRef, PostsRef, LikesRef;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;

    Boolean LikeChecker = false;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        addNewPostButton = findViewById(R.id.add_new_post_button);
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawable_layout);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, string.drawer_open, string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigationView);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = navView.findViewById(R.id.nav_username);

        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("Full_Name")){
                        String fullname = dataSnapshot.child("Full_Name").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile Name do not exists...", Toast.LENGTH_SHORT).show();
                    }

                    if (dataSnapshot.hasChild("profileImage")){
                        String image =  dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(drawable.profile).into(NavProfileImage);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile Image do not exists...", Toast.LENGTH_SHORT).show();
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
                UserMenuSelector(item);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();
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

        UserRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }


    private void DisplayAllUsersPosts() {

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(SortPostsInDescendingOrder, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder> (options)
        {
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);

                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostsViewHolder viewHolder, int position, Posts model) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setFullName(model.getFullName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setProfilePicture(getApplicationContext(), model.getProfilePicture());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());

                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
                    }
                });

                viewHolder.LikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true)){
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else{
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
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
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        ImageButton LikeButton, CommentButton;
        TextView DisplayLikes;
        int CountLikes;
        String currentUserId;
        DatabaseReference likesRef;


        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            LikeButton = mView.findViewById(id.like_button);
            CommentButton = mView.findViewById(id.comment_button);
            DisplayLikes = mView.findViewById(id.display_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setLikeButtonStatus(final String postKey){
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        CountLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.like);
                        DisplayLikes.setText(CountLikes+" Likes");
                    }
                    else{
                        CountLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikeButton.setImageResource(drawable.dislike);
                        DisplayLikes.setText(CountLikes+" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullName(String fullName){
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullName);
        }

        public void setProfilePicture(Context ctx, String profilePicture){
            CircleImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profilePicture).into(image);
        }

        public void setTime(String time){
            TextView PostName = mView.findViewById(R.id.post_time);
            PostName.setText(time);
        }

        public void setDate(String date){
            TextView PostDate = mView.findViewById(R.id.post_date);
            PostDate.setText(date);
        }

        public void setDescription(String description){
            TextView PostDescription = mView.findViewById(R.id.P_description);
            PostDescription.setText(description);
        }

        public void setPostImage(Context ctx, String postImage){
            ImageView PostImage = mView.findViewById(R.id.P_image);
            Picasso.with(ctx).load(postImage).into(PostImage);
        }
    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent =  new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();
        }
        else {
            CheckUserExistence();
        }


    }

    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(current_user_id).hasChild("Username")) {
                    SendUserToSetupActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case id.nav_post:
                SendUserToPostActivity();
                break;
            case id.nav_profile:
                SendUserToProfileActivity();
                break;
            case id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case id.nav_messages:
                SendUserToMyMessagesActivity();
                break;
            case id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case id.nav_logout:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }

    private void SendUserToMyMessagesActivity() {
        Intent myMessagesIntent = new Intent(MainActivity.this, MyMessagesActivity.class);
        startActivity(myMessagesIntent);
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }
}
