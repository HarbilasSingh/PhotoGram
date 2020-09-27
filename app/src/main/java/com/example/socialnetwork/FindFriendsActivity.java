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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;
    private RecyclerView SearchResultList;
    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SearchButton = findViewById(R.id.search_button);
        SearchInputText = findViewById(R.id.Search_box_input);
        SearchResultList = findViewById(R.id.friends_list);

        mToolbar = findViewById(R.id.find_friends_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SearchBoxInput = SearchInputText.getText().toString();
                SearchFriends(SearchBoxInput);
            }
        });
    }

    private void SearchFriends(String searchBoxInput) {
        Toast.makeText(FindFriendsActivity.this, "Searching....", Toast.LENGTH_LONG).show();
        Query findFriendsQuery = allUsersDatabaseRef.orderByChild("Full_Name").startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(findFriendsQuery, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FindFriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(FindFriendsViewHolder viewHolder, final int position, FindFriends model) {
                viewHolder.setFull_Name(model.getFull_Name());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("Visit_User_Id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImage(Context ctx, String profileImage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFull_Name(String full_Name){
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(full_Name);
        }

        public void setStatus(String status){
            TextView myStatus = mView.findViewById(R.id.all_users_profile_status);
            myStatus.setText(status);
        }

    }
}
