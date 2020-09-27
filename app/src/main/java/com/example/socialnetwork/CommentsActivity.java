package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText CommentInputText;
    private ImageButton PostCommentButton;
    private RecyclerView CommentsList;

    private DatabaseReference UsersRef, PostsRef;
    private String Post_Key, current_user_id;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();


        Post_Key = getIntent().getExtras().get("PostKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        mToolbar = findViewById(R.id.comments_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comments");

        CommentsList = findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        PostCommentButton = findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("Username").getValue().toString();
                            String ProfileImage = dataSnapshot.child("profileImage").getValue().toString();

                            ValidateComment(userName, ProfileImage);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostsRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {

            @Override
            public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(CommentsViewHolder viewHolder, int position, Comments model) {
                viewHolder.setComUserName(model.getComUserName());
                viewHolder.setComment(model.getComment());
                viewHolder.setComDate(model.getComDate());
                viewHolder.setComTime(model.getComTime());
                viewHolder.setComUserImage(getApplicationContext(), model.getComUserImage());
            }
        };

        firebaseRecyclerAdapter.startListening();
        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static  class CommentsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setComDate(String comDate){
            TextView myDate = mView.findViewById(R.id.comment_date);
            myDate.setText(comDate);
        }

        public void setComTime(String comTime){
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText("Time: "+comTime);
        }

        public void setComUserImage(Context ctx, String comUserImage){
            CircleImageView myProfImage = mView.findViewById(R.id.comment_profile_picture);
            Picasso.with(ctx).load(comUserImage).placeholder(R.drawable.profile).into(myProfImage);
        }

        public void setComUserName(String comUserName){
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+comUserName);
        }

        public void setComment(String comment){
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
    }

    private void ValidateComment(String userName, String profileImage) {
        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(CommentsActivity.this, "Add a Comment...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar CalForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(CalForDate.getTime());

            Calendar CalForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(CalForTime.getTime());
            final String RandomKey = current_user_id + "," + saveCurrentDate + "," + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("ComUid", current_user_id);
            commentsMap.put("Comment", commentText);
            commentsMap.put("ComDate", saveCurrentDate);
            commentsMap.put("ComTime", saveCurrentTime);
            commentsMap.put("ComUserName", userName);
            commentsMap.put("ComUserImage", profileImage);

            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CommentsActivity.this, "Commented successfully..", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CommentsActivity.this, "Error occurred!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
