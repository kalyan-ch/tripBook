package edu.uncc.hw09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import edu.uncc.DataObj.ChatMessage;
import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.UserProfile;

public class ChatActivity extends AppCompatActivity {
    Trip trip;
    DatabaseReference mDb;
    private StorageReference mStr;
    SharedPreferences shpr;
    String logEmail;
    ArrayList<ChatMessage> msg_list;
    LinearLayout rl;
    UserProfile up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        trip = (Trip)getIntent().getSerializableExtra("trip");
        mDb = FirebaseDatabase.getInstance().getReference();
        mStr = FirebaseStorage.getInstance().getReference();
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        logEmail = shpr.getString("user_email","");

        ((TextView)findViewById(R.id.cht_title)).setText("Chatroom for trip: "+trip.getTitle());

        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    UserProfile u = snapshot.getValue(UserProfile.class);
                    if(logEmail.equals(u.getEmail())){
                        up = u;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDb.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msg_list =new ArrayList<ChatMessage>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    ChatMessage cm = snapshot.getValue(ChatMessage.class);
                    if(cm.getTrip_id().equals(trip.getId())){
                        if(cm.getViews()!=null){
                            if(!cm.getViews().contains(up.getUser_id())){
                                msg_list.add(cm);
                            }
                        }else{
                            msg_list.add(cm);
                        }


                    }

                }

                loadMessages();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendTextMessage(View v){
        String msgText = ((EditText)findViewById(R.id.msgText)).getText().toString();
        if("".equals(msgText)){
            return;
        }
        ChatMessage cm = new ChatMessage();
        cm.setId(UUID.randomUUID().toString());
        cm.setMessage(msgText);
        cm.setSenderName(up.getFirstName());
        cm.setTime((new Date()).toString());
        cm.setTrip_id(trip.getId());
        cm.setSender_id(up.getUser_id());
        saveMessage(cm);
    }

    public void saveMessage(ChatMessage cm){
        mDb.child("messages").child(cm.getId()).setValue(cm);
    }

    public void sendImage(View v){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent,2);
    }

    @Override
    @SuppressWarnings("VisibleForTests")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK ){
            try{
                Uri uri = data.getData();
                InputStream is = getContentResolver().openInputStream(uri);
                byte[] inData = getBytes(is);
                StorageReference picPath = mStr.child(UUID.randomUUID().toString());
                UploadTask task = picPath.putBytes(inData);
                task.addOnSuccessListener(ChatActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri url = taskSnapshot.getDownloadUrl();
                        ChatMessage cm = new ChatMessage();
                        cm.setId(UUID.randomUUID().toString());
                        cm.setSenderName(up.getFirstName());
                        cm.setTime((new Date()).toString());
                        cm.setTrip_id(trip.getId());
                        cm.setImg_url(url.toString());
                        cm.setSender_id(up.getUser_id());
                        saveMessage(cm);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void loadMessages(){
        if(msg_list!=null) {
            Collections.sort(msg_list, new MsgComparator());
            if (msg_list.size() > 0) {
                rl = (LinearLayout) findViewById(R.id.chatMsg_layout);
                rl.removeAllViews();
                for (final ChatMessage m : msg_list) {
                    if(m.getMessage()!=null){
                        View v = LayoutInflater.from(this).inflate(R.layout.msg_text_body, null);
                        ((TextView)v.findViewById(R.id.msg_txt_txt)).setText(m.getMessage());
                        ((TextView)v.findViewById(R.id.msg_txt_name)).setText(m.getSenderName());
                        String date = m.getTime();
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                            Date date1 = sdf.parse(date);
                            PrettyTime p= new PrettyTime();
                            ((TextView)v.findViewById(R.id.msg_txt_time)).setText(p.format(date1));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)(v.findViewById(R.id.text_cardV)).getLayoutParams();
                        if(up.getUser_id().equals(m.getSender_id())){
                            params.addRule(RelativeLayout.ALIGN_PARENT_END);
                        }else{
                            params.addRule(RelativeLayout.ALIGN_PARENT_START);
                        }

                        ((ImageButton)v.findViewById(R.id.txt_del_btn)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteMessage(v, m);
                            }
                        });

                        (v.findViewById(R.id.text_cardV)).setLayoutParams(params);
                        rl.addView(v);
                    }else if(m.getImg_url()!=null){
                        View v = LayoutInflater.from(this).inflate(R.layout.msg_img_body, null);
                        ((TextView)v.findViewById(R.id.msg_img_name)).setText(m.getMessage());
                        String date = m.getTime();
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                            Date date1 = sdf.parse(date);
                            PrettyTime p= new PrettyTime();
                            ((TextView)v.findViewById(R.id.msg_img_time)).setText(p.format(date1));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Picasso.with(ChatActivity.this)
                                .load(m.getImg_url())
                                .into((ImageView)v.findViewById(R.id.msg_img_img));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)(v.findViewById(R.id.img_cardV)).getLayoutParams();
                        if(up.getFirstName().equals(m.getSenderName())){
                            params.addRule(RelativeLayout.ALIGN_PARENT_END);
                        }else{
                            params.addRule(RelativeLayout.ALIGN_PARENT_START);
                        }

                        ((ImageButton)v.findViewById(R.id.img_del_btn)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteMessage(v, m);
                            }
                        });

                        (v.findViewById(R.id.img_cardV)).setLayoutParams(params);
                        rl.addView(v);
                    }
                }
            }
        }
    }

    public void deleteMessage(View v,ChatMessage cm){
        cm.addView(up.getUser_id());
        mDb.child("messages").child(cm.getId()).setValue(cm);
    }

    class MsgComparator implements Comparator<ChatMessage> {
        @Override
        public int compare(ChatMessage app1, ChatMessage app2) {
            String prc1 = app1.getTime();
            String prc2 = app2.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            try{
                Date date1 = sdf.parse(prc1);
                Date date2 = sdf.parse(prc1);
                long num1 = date1.getTime();
                long num2 = date2.getTime();
                return (int)(num1-num2);
            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }
    }
}
