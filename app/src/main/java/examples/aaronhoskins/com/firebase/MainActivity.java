package examples.aaronhoskins.com.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String EMAIL = "email";
    private static final String USER_CHILD = "user";
    FirebaseAuth firebaseAuth;
    CallbackManager callbackManager;
    EditText etEmail;
    EditText etPassword;
    TextView tvLogInStatus;
    LoginButton loginButton;

    //DB
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();//Firebase
        callbackManager = CallbackManager.Factory.create();//Facebook

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvLogInStatus = findViewById(R.id.tvLogInResult);

        //Facebook
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        updateFBUI(loginResult.getAccessToken().getUserId());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("TAG", "onError: ", exception);
                    }
                });

        //Firebase DB
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(USER_CHILD);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TAG", "onDataChange: DATA HAS CHANGED");
                Iterator<DataSnapshot> iter =  dataSnapshot.getChildren().iterator();
                while(iter.hasNext()) {
                    Log.d("TAG", "onDataChange: " + iter.next().getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAG", "onCancelled: ", databaseError.toException());
            }
        });
        saveUserToDB(new User("User Name", "username@domain.com"));

    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            tvLogInStatus.setText("USER " + user.getEmail() + " is logged IN");
        } else {
            tvLogInStatus.setText("USER " + etEmail.getText().toString() + " FAILED TO LOGIN");
        }
    }

    private void updateFBUI(String user) {
        if(user != null) {
            tvLogInStatus.setText("USER " + user + " is logged IN");
        } else {
            tvLogInStatus.setText("USER " + etEmail.getText().toString() + " FAILED TO LOGIN");
        }
    }

    public void onClick(View view) {
        String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        switch(view.getId()) {
            case R.id.btnSignIn:
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "signInWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "signInWithEmail:failure", task.getException());

                                    updateUI(null);
                                }

                            }
                        });
                break;
            case R.id.btnSignUp:
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "createUserWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                    updateUI(null);
                                }
                            }
                        });
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void saveUserToDB(User user) {
        String databaseKey = user.getKey() == null ? myRef.push().getKey() : user.getKey();
        Log.d("TAG", "saveUserToDB: KEY = " + databaseKey + " - Saving to DB");
        myRef.child(databaseKey).setValue(user);
        myRef.child(databaseKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TAG", "onDataChange: " + dataSnapshot.getKey() + " changed! ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAG", "onCancelled: ", databaseError.toException());
            }
        });
    }

    private void updateUser(User user) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty(user.getUserName()))
            myRef.child(user.getKey()).child("name").setValue(user.getUserName());

        if (!TextUtils.isEmpty(user.getUserEmail()))
            myRef.child(user.getKey()).child("email").setValue(user.getUserEmail());
    }

















}
