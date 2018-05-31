package compsci290.edu.duke.coconutproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.interfaces.UserExistListener;
import compsci290.edu.duke.coconutproject.models.User;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isUserLoggedIn()) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }

        // Initialize View, Title & Logo
        setContentView(R.layout.login_activity);
        ImageView appLogo = (ImageView) findViewById(R.id.app_logo);
        appLogo.setScaleType (ImageView.ScaleType.FIT_CENTER);
        appLogo.setImageResource(R.drawable.bluebuddy_logo);
        TextView appTitle = (TextView) findViewById(R.id.app_title);
        appTitle.setText("BlueBuddy");
        appTitle.setTypeface(MainActivity.font);
        TextView appSubtitle = (TextView) findViewById(R.id.app_subtitle);
        appSubtitle.setText("devilishly social.");
        appSubtitle.setTypeface(MainActivity.font);

        // Initialize Firebase Auth object
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setTypeface(MainActivity.font);
        loginButton.setReadPermissions("email", "public_profile");
        // callback from Facebook that hands off token to Firebase



        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            processUserProfile(currentProfile);
                            mProfileTracker.stopTracking();
                        }
                    };
                } else {
                    processUserProfile(Profile.getCurrentProfile());
                }

                // handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());



                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    public boolean isUserLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void processUserProfile(final Profile profile) {
        // See if the user already exists within the Firebase user database
        UserExistListener userListener = new UserExistListener() {
            public void onHandleUserExist(boolean userExists) {
                if (!userExists) {

                    // User newUser = new User(profile.getFirstName()+profile.getLastName()+profile.getId().substring(0, 1), profile.getFirstName(), profile.getLastName(), profile.getId(), null, new ArrayList<Meeting>(), new ArrayList<Meeting>());
                    User newUser = new User(profile.getFirstName()+profile.getLastName()+profile.getId().substring(0, 1), profile.getFirstName(), profile.getLastName(), profile.getId(), null, profile.getProfilePictureUri(240, 240).toString(), null, null);

                    DatabaseManager.writeToUserDatabase(newUser, newUser.getUserInfoMap());
                }
                // pass along Firebase User/Facebook profile information
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            }
        };
        DatabaseManager.doesUserExist(userListener, profile.getId());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

}
