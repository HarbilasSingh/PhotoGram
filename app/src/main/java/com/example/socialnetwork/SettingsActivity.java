package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateButton;
    private CircleImageView userProfImage;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    final static int Gallery_pick = 1;
    private StorageReference UserProfileImageRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadingBar = new ProgressDialog(this);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        userName = findViewById(R.id.settings_username);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_profile_country);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_profile_dob);
        userProfName = findViewById(R.id.settings_profile_full_name);

        UpdateButton = findViewById(R.id.update_account_button);
        userProfImage = findViewById(R.id.settings_profile_image);


        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("Username").getValue().toString();
                    String myProfileName = dataSnapshot.child("Full_Name").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("Status").getValue().toString();
                    String myDOB = dataSnapshot.child("DOB").getValue().toString();
                    String myCountry = dataSnapshot.child("Country").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("Relationship_Status").getValue().toString();

                    Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    userName.setText(myUserName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText(myCountry);
                    userRelation.setText(myRelationshipStatus);
                    userDOB.setText(myDOB);
                    userGender.setText(myGender);
                    userProfName.setText(myProfileName);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data != null ){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("Profile image");
                loadingBar.setMessage("Please wait while we are updating your profile image !!!");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image saved successfully in the Firebase storage!!", Toast.LENGTH_SHORT).show();


                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    settingsUserRef.child("profileImage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SettingsActivity.this, "Profile Image stored in Firebase database successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                    else{
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }

                                                }
                                            });
                                }
                            });

                        }
                    }
                });
            }
            else{
                Toast.makeText(SettingsActivity.this, "Error occurred: Image can't be cropped!! " , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(SettingsActivity.this, "Please write your User Name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profilename)){
            Toast.makeText(SettingsActivity.this, "Please write your Full Name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(SettingsActivity.this, "Please write your Profile Status...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob)){
            Toast.makeText(SettingsActivity.this, "Please write your Date Of Birth...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(SettingsActivity.this, "Please write your Country...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender)){
            Toast.makeText(SettingsActivity.this, "Please write your Gender...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation)){
            Toast.makeText(SettingsActivity.this, "Please write your Relationship Status...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Profile image");
            loadingBar.setMessage("Please wait while we are updating your profile image !!!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, profilename, status, dob, country, gender, relation);
        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation) {
        HashMap userMap = new HashMap();
        userMap.put("Username", username);
        userMap.put("Full_Name", profilename);
        userMap.put("Status", status);
        userMap.put("DOB", dob);
        userMap.put("Country", country);
        userMap.put("Gender", gender);
        userMap.put("Relationship_Status", relation);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings updated successfully !!!!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Error occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
