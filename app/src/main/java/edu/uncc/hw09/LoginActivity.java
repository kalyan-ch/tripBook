package edu.uncc.hw09;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.uncc.DataObj.UserProfile;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    GoogleSignInOptions gso;
    GoogleApiClient gApi;
    private DatabaseReference mDatabase;
    ProgressDialog pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestEmail().
                requestIdToken(getString(R.string.key)).
                build();
        gApi = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("demo","connection failed");
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.google_signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        setTitle("Login");
    }

    private void signIn() {
        pg = new ProgressDialog(this);
        pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pg.setCancelable(false);
        pg.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(gApi);
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                String mesage = result.getStatus().getStatusMessage();
                Log.d("demo","failed"+mesage);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("demo", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]

        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {

                            final String userId = acct.getId();
                            mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean flag = true;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        UserProfile u = snapshot.getValue(UserProfile.class);
                                        String uEmail = u.getUser_id();
                                        if(userId.equals(uEmail)){
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if(flag){
                                        UserProfile up = new UserProfile();
                                        up.setFirstName(acct.getGivenName());
                                        up.setLastName(acct.getFamilyName());
                                        up.setEmail(acct.getEmail());
                                        up.setUser_id(acct.getId());
                                        up.setDisplayPic(acct.getPhotoUrl().toString());
                                        mDatabase.child("users").child(up.getUser_id()).setValue(up);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("google","true");
                            intent.putExtra("email", acct.getEmail());
                            startActivity(intent);

                            pg.dismiss();
                        }else{
                            Log.w("demo", "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void login(View v){
        final String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
        String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
        if(!"".equals(email) && !"".equals(password)){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("normal","true");
                        intent.putExtra("email",email);
                        startActivity(intent);
                    }else{
                        Log.w("demo", "signInWithEmail:failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "Enter both email and password", Toast.LENGTH_SHORT).show();
        }

    }

    public void startSignUp(View v){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

}
