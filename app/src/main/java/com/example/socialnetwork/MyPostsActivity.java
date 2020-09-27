package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, UserRef, LikesRef;
    private String currentUserId;

    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {

        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPostsQuery, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(MyPostsViewHolder viewHolder, int position, Posts model) {

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
                        Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                viewHolder.CommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
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
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                                        LikesRef.child(PostKey).child(currentUserId).removeValue();
                                        LikeChecker = false;
                                    }
                                    else{
                                        LikesRef.child(PostKey).child(currentUserId).setValue(true);
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

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);

                return new MyPostsViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageButton LikeButton, CommentButton;
        TextView DisplayLikes;
        int CountLikes;
        String currentUserId;
        DatabaseReference likesRef;

        public MyPostsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            LikeButton = mView.findViewById(R.id.like_button);
            CommentButton = mView.findViewById(R.id.comment_button);
            DisplayLikes = mView.findViewById(R.id.display_likes);

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
                        LikeButton.setImageResource(R.drawable.dislike);
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
}
