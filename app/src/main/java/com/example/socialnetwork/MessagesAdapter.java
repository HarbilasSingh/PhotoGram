package com.example.socialnetwork;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabaseRef;
    private DatabaseReference UsersSendersRef;
    private String fullName, profileImage, status, username, RfullName, RprofileImage, Rstatus, Rusername;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView SenderTextMessage, ReceiverTextMessage, SenderMessageTime, ReceiverMessageTime, senderImageTime, receiverImageTime;
        public CircleImageView ReceiverProfileImage;
        public LinearLayout senderMessageLayout, receiverMessageLayout, senderImageLayout, receiverImageLayout;
        private ImageView senderImageView, receiverImageView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            SenderTextMessage = itemView.findViewById(R.id.sender_message_text);
            ReceiverTextMessage = itemView.findViewById(R.id.receiver_message_text);
            ReceiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            SenderMessageTime = itemView.findViewById(R.id.sender_message_time);
            ReceiverMessageTime = itemView.findViewById(R.id.receiver_message_time);
            senderMessageLayout = itemView.findViewById(R.id.sender_message_layout);
            receiverMessageLayout = itemView.findViewById(R.id.receiver_message_layout);

            senderImageLayout = itemView.findViewById(R.id.sender_image_layout);
            senderImageView = itemView.findViewById(R.id.sender_image_view);
            senderImageTime = itemView.findViewById(R.id.sender_image_time);
            receiverImageLayout = itemView.findViewById(R.id.receiver_image_layout);
            receiverImageView = itemView.findViewById(R.id.receiver_image_view);
            receiverImageTime= itemView.findViewById(R.id.receiver_image_time);

            UsersSendersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_users, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
         String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);

        final String fromUserId = messages.getFrom();
        final String toUserId = messages.getTo();
        String fromMessageType = messages.getType();

        UsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String profimage = dataSnapshot.child("profileImage").getValue().toString();

                    Picasso.with(holder.ReceiverProfileImage.getContext()).load(profimage).placeholder(R.drawable.profile).into(holder.ReceiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersSendersRef.child(toUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    fullName = dataSnapshot.child("Full_Name").getValue().toString();
                    profileImage = dataSnapshot.child("profileImage").getValue().toString();
                    status = dataSnapshot.child("Status").getValue().toString();
                    username = dataSnapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        UsersSendersRef.child(fromUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    RfullName = dataSnapshot.child("Full_Name").getValue().toString();
                    RprofileImage = dataSnapshot.child("profileImage").getValue().toString();
                    Rstatus = dataSnapshot.child("Status").getValue().toString();
                    Rusername = dataSnapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.ReceiverTextMessage.setVisibility(View.GONE);
        holder.ReceiverProfileImage.setVisibility(View.GONE);
        holder.ReceiverMessageTime.setVisibility(View.GONE);
        holder.receiverMessageLayout.setVisibility(View.GONE);
        holder.SenderTextMessage.setVisibility(View.GONE);
        holder.SenderMessageTime.setVisibility(View.GONE);
        holder.senderMessageLayout.setVisibility(View.GONE);

        holder.senderImageLayout.setVisibility(View.GONE);
        holder.senderImageTime.setVisibility(View.GONE);
        holder.senderImageView.setVisibility(View.GONE);
        holder.receiverImageTime.setVisibility(View.GONE);
        holder.receiverImageLayout.setVisibility(View.GONE);
        holder.receiverImageView.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){

            if(fromUserId.equals(messageSenderId)){

                holder.SenderTextMessage.setVisibility(View.VISIBLE);
                holder.SenderMessageTime.setVisibility(View.VISIBLE);
                holder.senderMessageLayout.setVisibility(View.VISIBLE);

                holder.SenderTextMessage.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderTextMessage.setTextColor(Color.WHITE);
                holder.SenderTextMessage.setGravity(Gravity.LEFT);
                holder.SenderTextMessage.setText(messages.getMessage());
                holder.SenderMessageTime.setText(messages.getTime());
            }
            else{

                holder.ReceiverTextMessage.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);
                holder.ReceiverMessageTime.setVisibility(View.VISIBLE);
                holder.receiverMessageLayout.setVisibility(View.VISIBLE);

                holder.ReceiverTextMessage.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.ReceiverTextMessage.setTextColor(Color.WHITE);
                holder.ReceiverTextMessage.setGravity(Gravity.LEFT);
                holder.ReceiverTextMessage.setText(messages.getMessage());
                holder.ReceiverMessageTime.setText(messages.getTime());
            }
        }
        else if (fromMessageType.equals("image"))
        {
            if(fromUserId.equals(messageSenderId))
            {

                holder.senderImageView.setVisibility(View.VISIBLE);
                holder.senderImageLayout.setVisibility(View.VISIBLE);
                holder.senderImageTime.setVisibility(View.VISIBLE);

                Picasso.with(holder.senderImageView.getContext()).load(messages.getMessage()).into(holder.senderImageView);
                holder.senderImageTime.setText(messages.getTime());
            }
            else
            {
                holder.receiverImageTime.setVisibility(View.VISIBLE);
                holder.receiverImageLayout.setVisibility(View.VISIBLE);
                holder.receiverImageView.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                Picasso.with(holder.receiverImageView.getContext()).load(messages.getMessage()).into(holder.receiverImageView);
                holder.receiverImageTime.setText(messages.getTime());
            }
        }
        else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
        {
                if(fromUserId.equals(messageSenderId))
                {
                    holder.senderImageView.setVisibility(View.VISIBLE);
                    holder.senderImageLayout.setVisibility(View.VISIBLE);
                    holder.senderImageTime.setVisibility(View.VISIBLE);

                    Picasso.with(holder.senderImageView.getContext())
                            .load("https://firebasestorage.googleapis.com/v0/b/photogram-60975.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=ffc3110a-7423-40dd-b31c-ffb1069ca749")
                            .into(holder.senderImageView);

                    holder.senderImageTime.setText(messages.getTime());


                }
                else
                {
                    holder.receiverImageTime.setVisibility(View.VISIBLE);
                    holder.receiverImageLayout.setVisibility(View.VISIBLE);
                    holder.receiverImageView.setVisibility(View.VISIBLE);
                    holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                    Picasso.with(holder.receiverImageView.getContext())
                            .load("https://firebasestorage.googleapis.com/v0/b/photogram-60975.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=ffc3110a-7423-40dd-b31c-ffb1069ca749")
                            .into(holder.receiverImageView);
                    holder.receiverImageTime.setText(messages.getTime());


                }
        }

        if(fromUserId.equals(messageSenderId))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]{
                                "Download",
                                "Delete for me",
                                "Delete for Everyone",
                                "Cancel"
                        };

                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if(which == 0){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 1){
                                    deleteSentMessages(position, holder);

                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_User_Id", toUserId);
                                    chatIntent.putExtra("FullName", fullName);
                                    chatIntent.putExtra("Username", username);
                                    chatIntent.putExtra("profImage", profileImage);
                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 2){
                                    deleteMessageForEveryone(position, holder);

                                    holder.senderImageLayout.setVisibility(View.GONE);
                                    holder.senderImageTime.setVisibility(View.GONE);
                                    holder.senderImageView.setVisibility(View.GONE);


                                    holder.SenderTextMessage.setVisibility(View.VISIBLE);
                                    holder.SenderMessageTime.setVisibility(View.VISIBLE);
                                    holder.senderMessageLayout.setVisibility(View.VISIBLE);

                                    holder.SenderTextMessage.setBackgroundResource(R.drawable.sender_message_text_background);
                                    holder.SenderTextMessage.setTextColor(Color.WHITE);
                                    holder.SenderTextMessage.setGravity(Gravity.LEFT);
                                    holder.SenderTextMessage.setText(messages.getMessage());
                                    holder.SenderMessageTime.setText(messages.getTime());


//                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
//                                    chatIntent.putExtra("Visit_User_Id", toUserId);
//                                    chatIntent.putExtra("FullName", fullName);
//                                    chatIntent.putExtra("Username", username);
//                                    chatIntent.putExtra("profImage", profileImage);
//                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 3){

                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                    {
                        if (userMessagesList.get(position).getMessage().equals("This message was deleted"))
                        {
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Cancel"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Choose...");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(which == 0)
                                    {
                                        deleteSentMessages(position, holder);

                                        Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                        chatIntent.putExtra("Visit_User_Id", toUserId);
                                        chatIntent.putExtra("FullName", fullName);
                                        chatIntent.putExtra("Username", username);
                                        chatIntent.putExtra("profImage", profileImage);
                                        holder.itemView.getContext().startActivity(chatIntent);
                                    }
                                    else if (which == 1)
                                    {

                                    }

                                }
                            });
                            builder.show();

                        }
                        else
                            {
                            CharSequence options[] = new CharSequence[]{
                                    "Delete for me",
                                    "Delete for Everyone",
                                    "Cancel"
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Choose...");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        deleteSentMessages(position, holder);

                                        Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                        chatIntent.putExtra("Visit_User_Id", toUserId);
                                        chatIntent.putExtra("FullName", fullName);
                                        chatIntent.putExtra("Username", username);
                                        chatIntent.putExtra("profImage", profileImage);
                                        holder.itemView.getContext().startActivity(chatIntent);

                                    }
                                    else if (which == 1) {
                                        deleteMessageForEveryone(position, holder);

                                        Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                        chatIntent.putExtra("Visit_User_Id", toUserId);
                                        chatIntent.putExtra("FullName", fullName);
                                        chatIntent.putExtra("Username", username);
                                        chatIntent.putExtra("profImage", profileImage);
                                        holder.itemView.getContext().startActivity(chatIntent);

                                    }
                                    else if (which == 2) {

                                    }
                                }
                            });
                            builder.show();
                        }
                    }

                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for Everyone",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0){
                                    deleteSentMessages(position, holder);

                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_User_Id", toUserId);
                                    chatIntent.putExtra("FullName", fullName);
                                    chatIntent.putExtra("Username", username);
                                    chatIntent.putExtra("profImage", profileImage);
                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 1){
                                    deleteMessageForEveryone(position, holder);

                                    holder.senderImageLayout.setVisibility(View.GONE);
                                    holder.senderImageTime.setVisibility(View.GONE);
                                    holder.senderImageView.setVisibility(View.GONE);

                                    holder.SenderTextMessage.setVisibility(View.VISIBLE);
                                    holder.SenderMessageTime.setVisibility(View.VISIBLE);
                                    holder.senderMessageLayout.setVisibility(View.VISIBLE);

                                    holder.SenderTextMessage.setBackgroundResource(R.drawable.sender_message_text_background);
                                    holder.SenderTextMessage.setTextColor(Color.WHITE);
                                    holder.SenderTextMessage.setGravity(Gravity.LEFT);
                                    holder.SenderTextMessage.setText(messages.getMessage());
                                    holder.SenderMessageTime.setText(messages.getTime());

//                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
//                                    chatIntent.putExtra("Visit_User_Id", toUserId);
//                                    chatIntent.putExtra("FullName", fullName);
//                                    chatIntent.putExtra("Username", username);
//                                    chatIntent.putExtra("profImage", profileImage);
//                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 2){

                                }
                            }
                        });
                        builder.show();
                    }


                    return true;
                }
            });


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("image")){
                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("name", userMessagesList.get(position).getName());
                        holder.itemView.getContext().startActivity(intent);
                    }
                }
            });


        }

        else
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]{
                                "Download",
                                "Delete for me",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0){

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 1){
                                    deleteReceivedMessages(position, holder);

                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_User_Id", fromUserId);
                                    chatIntent.putExtra("FullName", RfullName);
                                    chatIntent.putExtra("Username", Rusername);
                                    chatIntent.putExtra("profImage", RprofileImage);
                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 2){

                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                    {

                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0){
                                    deleteReceivedMessages(position, holder);

                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_User_Id", fromUserId);
                                    chatIntent.putExtra("FullName", RfullName);
                                    chatIntent.putExtra("Username", Rusername);
                                    chatIntent.putExtra("profImage", RprofileImage);
                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 1){

                                }

                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0){
                                    deleteReceivedMessages(position, holder);

                                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    chatIntent.putExtra("Visit_User_Id", fromUserId);
                                    chatIntent.putExtra("FullName", RfullName);
                                    chatIntent.putExtra("Username", Rusername);
                                    chatIntent.putExtra("profImage", RprofileImage);
                                    holder.itemView.getContext().startActivity(chatIntent);

                                }
                                else if(which == 1){

                                }
                            }
                        });
                        builder.show();
                    }


                    return true;
                }
            });


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("image")){
                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("name", userMessagesList.get(position).getName());
                        holder.itemView.getContext().startActivity(intent);
                    }
                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessages(final int pos, final MessageViewHolder viewholder)
    {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(viewholder.itemView.getContext(), "Deleted successfully...", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(viewholder.itemView.getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteReceivedMessages(final int pos, final MessageViewHolder viewholder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(viewholder.itemView.getContext(), "Deleted successfully...", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(viewholder.itemView.getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int pos, final MessageViewHolder viewholder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        final Map map = new HashMap();
        map.put("message", "This message was deleted");
        map.put("type", "text");

        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getMessageID())
                .updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    rootRef.child("Messages")
                            .child(userMessagesList.get(pos).getTo())
                            .child(userMessagesList.get(pos).getFrom())
                            .child(userMessagesList.get(pos).getMessageID())
                            .updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(viewholder.itemView.getContext(), "Deleted successfully...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else{
                    Toast.makeText(viewholder.itemView.getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
