package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView personProfName, personProfuserName, personProfStatus, personProfCountry, personProfGender, personProfDob, personProfRelationshipStatus;
    private CircleImageView personProfImage;
    private Button sendFriendRequestBtn, declineFriendRequestBtn;

    private DatabaseReference FriendReqRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("Visit_User_Id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("Username").getValue().toString();
                    String myProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("Status").getValue().toString();
                    String myDOB = dataSnapshot.child("DOB").getValue().toString();
                    String myCountry = dataSnapshot.child("Country").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("Relationship_Status").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(personProfImage);
                    personProfuserName.setText("@" + myUserName);
                    personProfStatus.setText(myProfileStatus);
                    personProfCountry.setText("Country: " + myCountry);
                    personProfRelationshipStatus.setText("Relationship Status: " + myRelationshipStatus);
                    personProfDob.setText("DOB: " + myDOB);
                    personProfGender.setText("Gender: " + myGender);
                    personProfName.setText(myProfileName);

                    MaintenanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendRequestBtn.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestBtn.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToPerson();
                    }

                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnfriendAnExistingFriend();
                    }
                }
            });
        }
        else{
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendAnExistingFriend() {
        FriendsRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendsRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestBtn.setText("FOLLOW");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar CalForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(CalForDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FriendReqRef.child(senderUserId).child(receiverUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    FriendReqRef.child(receiverUserId).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendFriendRequestBtn.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        sendFriendRequestBtn.setText("UNFOLLOW");

                                                                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                                        declineFriendRequestBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest() {
        FriendReqRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendReqRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendFriendRequestBtn.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        sendFriendRequestBtn.setText("FOLLOW");

                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFriendRequestBtn.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void MaintenanceOfButtons() {
        FriendReqRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String req_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(req_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                sendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");

                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                            else if(req_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                sendFriendRequestBtn.setText("ACCEPT FRIEND REQUEST");

                                declineFriendRequestBtn.setVisibility(View.VISIBLE);
                                declineFriendRequestBtn.setEnabled(true);

                                declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else{
                            FriendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserId)){
                                        CURRENT_STATE = "friends";
                                        sendFriendRequestBtn.setText("UNFOLLOW");

                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFriendRequestBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToPerson() {
        FriendReqRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendReqRef.child(receiverUserId).child(senderUserId).child("request_type")
                                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendFriendRequestBtn.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        sendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");

                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFriendRequestBtn.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void InitializeFields() {
        personProfName = findViewById(R.id.person_profile_full_name);
        personProfuserName = findViewById(R.id.person_profile_user_name);
        personProfStatus = findViewById(R.id.person_profile_status);
        personProfCountry = findViewById(R.id.person_profile_country);
        personProfGender = findViewById(R.id.person_profile_gender);
        personProfDob = findViewById(R.id.person_profile_dob);
        personProfRelationshipStatus = findViewById(R.id.person_profile_relationship_status);
        personProfImage = findViewById(R.id.person_profile_pic);

        sendFriendRequestBtn = findViewById(R.id.person_send_friend_request_button);
        declineFriendRequestBtn = findViewById(R.id.person_decline_friend_request_button);

        CURRENT_STATE = "not_friends";
    }
}
