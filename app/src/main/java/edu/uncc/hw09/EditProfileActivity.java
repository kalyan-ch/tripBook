package edu.uncc.hw09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import edu.uncc.DataObj.UserProfile;

public class EditProfileActivity extends AppCompatActivity {

    UserProfile up;
    String uploadUrl;
    private StorageReference mStr;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        up = (UserProfile)getIntent().getSerializableExtra("user");
        mStr = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Spinner spinner = (Spinner) findViewById(R.id.edit_gender);
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.gender));
        spinner.setAdapter(spnAdapter);
        setDataEdit();
    }

    public void setDataEdit(){
        ((EditText)findViewById(R.id.edit_fName)).setText(up.getFirstName());
        ((EditText)findViewById(R.id.edit_lName)).setText(up.getLastName());
        int select = 0;
        if("Male".equalsIgnoreCase(up.getGender())){
            select = 0;
        }else if ("Female".equalsIgnoreCase(up.getGender())){
            select = 1;
        }else if ("Other".equalsIgnoreCase(up.getGender())){
            select = 2;
        }
        ((Spinner) findViewById(R.id.edit_gender)).setSelection(select);

        Picasso.with(this).load(up.getDisplayPic()).into((ImageView)findViewById(R.id.edit_dp));
    }

    public void saveChanges(View v){
        String fName = ((EditText)findViewById(R.id.edit_fName)).getText().toString();
        String lName = ((EditText)findViewById(R.id.edit_lName)).getText().toString();
        String gender = ((Spinner) findViewById(R.id.edit_gender)).getSelectedItem().toString();
        if("".equals(fName)){
            Toast.makeText(this, "Enter first Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if("".equals(lName)){
            Toast.makeText(this, "Enter last Name", Toast.LENGTH_SHORT).show();
            return;
        }

        up.setFirstName(fName);
        up.setLastName(lName);
        up.setGender(gender);
        if(uploadUrl!=null){
            up.setDisplayPic(uploadUrl);
        }

        mDatabase.child("users").child(up.getUser_id()).setValue(up);
        finish();

    }

    public void openGallforEdit(View v){
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
                task.addOnSuccessListener(EditProfileActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditProfileActivity.this, "Location Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                        Uri url = taskSnapshot.getDownloadUrl();
                        uploadUrl = url.toString();
                        Picasso.with(EditProfileActivity.this).load(uploadUrl).into((ImageView)findViewById(R.id.edit_dp));
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
}
