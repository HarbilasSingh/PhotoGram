package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.ForegroundLinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private ImageButton selectImage, sendMessageButton, attachFiles;
    private TextView userMessageInput;

    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private String messageReceiverName, messageReceiverId, messageReceiverUsername, messageReceiverImage, messageSenderId, saveCurrentDate, saveCurrentTime;

    private TextView msgToolbarName, msgToolbarLastSeen;
    private CircleImageView msgToolbarImage;
    private DatabaseReference RootRef, UsersRef, MsgRef;
    private FirebaseAuth mAuth;
    private String checker = "", myUrl = "";
    private StorageTask UploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;
//    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadingBar = new ProgressDialog(this);

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("Visit_User_Id").toString();
        messageReceiverName = getIntent().getExtras().get("FullName").toString();
        messageReceiverUsername = getIntent().getExtras().get("Username").toString();
        messageReceiverImage = getIntent().getExtras().get("profImage").toString();

        InitializeFields();

        DisplayReceiverInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        attachFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "Pdf Files",
                        "Word documents",
                        "Videos"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Send...");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image.."), 438);
                        }

                        if(which == 1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select Pdf file.."), 438);
                        }

                        if(which == 2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Word file.."), 438);
                        }

                        if(which == 3){
                            checker = "video";
                        }
                    }
                });
                builder.show();
            }
        });

        FetchMessages();
    }


    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void seenMessage(String id){
//        MsgRef = FirebaseDatabase.getInstance().getReference().child("Messages");
//        MsgRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            loadingBar.setTitle("Sending file ...");
            loadingBar.setMessage("Please wait while we sending the file !!!");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            fileUri = data.getData();

            if(!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

                final String message_push_id = user_message_key.getKey();

                final StorageReference filePath = storageReference.child(message_push_id + "." + checker);

                Calendar CalForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                saveCurrentDate = currentDate.format(CalForDate.getTime());

                Calendar CalForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                saveCurrentTime = currentTime.format(CalForTime.getTime());

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.storage.UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadURL = uri.toString();

                                    Map messageFileBody = new HashMap();
                                    messageFileBody.put("message", downloadURL);
                                    messageFileBody.put("name", fileUri.getLastPathSegment());
                                    messageFileBody.put("time", saveCurrentTime);
                                    messageFileBody.put("date", saveCurrentDate);
                                    messageFileBody.put("type", checker);
                                    messageFileBody.put("from", messageSenderId);
                                    messageFileBody.put("to", messageReceiverId);
                                    messageFileBody.put("messageID", message_push_id);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageFileBody);
                                    messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageFileBody);

                                    RootRef.updateChildren(messageBodyDetails);

                                    loadingBar.dismiss();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {

                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + "% Uploading...");
                    }
                });

            }
            else if(checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

                final String message_push_id = user_message_key.getKey();

                final StorageReference filePath = storageReference.child(message_push_id + ".jpg");

                UploadTask = filePath.putFile(fileUri);

                Calendar CalForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                saveCurrentDate = currentDate.format(CalForDate.getTime());

                Calendar CalForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                saveCurrentTime = currentTime.format(CalForTime.getTime());

                UploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderId);
                            messageImageBody.put("to", messageReceiverId);
                            messageImageBody.put("messageID", message_push_id);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageImageBody);
                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(Task task) {
                                    if(task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Image sent successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error occurred: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            else{
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this, "Nothing selected!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendMessage() {
        updateUserStatus("online");

        String msgText = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(msgText)){
            Toast.makeText(ChatActivity.this, "Enter the message first...", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

            String message_push_id = user_message_key.getKey();

            Calendar CalForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            saveCurrentDate = currentDate.format(CalForDate.getTime());

            Calendar CalForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(CalForTime.getTime());

            Map messageTextBody = new HashMap();
                messageTextBody.put("message", msgText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderId);
                messageTextBody.put("to", messageReceiverId);
                messageTextBody.put("messageID", message_push_id);

            Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody);
                messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Task task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }
                        else{
                            Toast.makeText(ChatActivity.this, "Error occurred: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }
                    }
                });

        }
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

        UsersRef.child(messageSenderId).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {
        msgToolbarName.setText(messageReceiverName);
        Picasso.with(ChatActivity.this).load(messageReceiverImage).placeholder(R.drawable.profile).into(msgToolbarImage);

        RootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String type = dataSnapshot.child("userState").child("Type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("Date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("Time").getValue().toString();

                    if(type.equals("online")){
                        msgToolbarLastSeen.setText("Online");
                    }
                    else{
                        msgToolbarLastSeen.setText("Last Seen at " + lastTime + " on " + lastDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        chatToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        attachFiles = findViewById(R.id.attach_file_button);

        sendMessageButton = findViewById(R.id.send_msg_button);
        selectImage = findViewById(R.id.select_image_file_button);
        userMessageInput = findViewById(R.id.input_message);

        msgToolbarName = findViewById(R.id.Message_Toolbar_FullName);
        msgToolbarLastSeen = findViewById(R.id.Message_Toolbar_last_seen);
        msgToolbarImage = findViewById(R.id.Message_Toolbar_ProfImage);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);
    }
}
