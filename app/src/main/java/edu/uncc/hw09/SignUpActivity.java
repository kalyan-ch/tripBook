package edu.uncc.hw09;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import edu.uncc.DataObj.UserProfile;

public class SignUpActivity extends AppCompatActivity {
    private String uploadUrl;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");
        Spinner spinner = (Spinner) findViewById(R.id.signUp_gender);
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.gender));
        spinner.setAdapter(spnAdapter);
        mStr = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void createLogin(View v){
        final String email = ((EditText)findViewById(R.id.signUp_Email)).getText().toString();
        if("".equals(email)){
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = ((EditText)findViewById(R.id.signUp_Password)).getText().toString();
        if("".equals(password)){
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        String password1 = ((EditText)findViewById(R.id.signUp_cnfPsw)).getText().toString();
        if("".equals(password1)){
            Toast.makeText(this, "Enter confirm password", Toast.LENGTH_SHORT).show();
            return;
        }
        final String fName = ((EditText)findViewById(R.id.signUp_fName)).getText().toString();
        if("".equals(fName)){
            Toast.makeText(this, "Enter first Name", Toast.LENGTH_SHORT).show();
            return;
        }
        final String lName = ((EditText)findViewById(R.id.signUp_LName)).getText().toString();
        if("".equals(lName)){
            Toast.makeText(this, "Enter last Name", Toast.LENGTH_SHORT).show();
            return;
        }
        final String gender = ((Spinner) findViewById(R.id.signUp_gender)).getSelectedItem().toString();
        if(password.equals(password1)){
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("demo", "createUserWithEmail:onComplete:" + task.isSuccessful());
                    if (task.isSuccessful()) {
                        UserProfile up = new UserProfile();

                        up.setUser_id(UUID.randomUUID().toString());
                        up.setFirstName(fName);
                        up.setLastName(lName);
                        up.setGender(gender);
                        up.setDisplayPic(uploadUrl);
                        up.setEmail(email);

                        mDatabase.child("users").child(up.getUser_id()).setValue(up);

                        Toast.makeText(SignUpActivity.this, "New user created. Login to continue", Toast.LENGTH_SHORT).show();

                        finish();

                    }else{

                        Toast.makeText(SignUpActivity.this, "user creation failed"+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void cancel(View v){
        finish();
    }

    public void openGallery(View v){
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
                task.addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SignUpActivity.this, "Avatar uploaded successfully", Toast.LENGTH_SHORT).show();
                        Uri url = taskSnapshot.getDownloadUrl();
                        uploadUrl = url.toString();
                        Picasso.with(SignUpActivity.this).load(uploadUrl).into((ImageView)findViewById(R.id.signUp_avtPrvw));
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
