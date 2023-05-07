package com.example.shop;


import static com.example.shop.MainActivity.clickedUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import model.Post;
import model.User;

public class FeedActivity extends AppCompatActivity implements PostsAdapter.OnPostDeletedListener{
    private static final String LOG_TAG = FeedActivity.class.getName();

    ImageView profileIV;
    ImageView profileClick;
    ImageView searchProfile;
    ImageView logOut;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    // POST
    EditText postET;
    Button postButton;
    String postId;
    private ArrayList<Post> mPostsData;
    private PostsAdapter mAdapter;
    private CollectionReference mPosts;
    private boolean viewRow = true;
    int postNum = 0;
    int itemLimit = 40;
    private int gridNumber = 1;

    StorageReference storageReference;
    RecyclerView recyclerView;

    List<String> friends = new ArrayList<>();

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        getSupportActionBar().hide();


        profileClick = findViewById(R.id.profileClick);

        profileClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedUser = "";
                startProfile();
            }
        });

        searchProfile = findViewById(R.id.searchProfile);

        searchProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedUser = "";
                startSearchProfile();
            }
        });

        logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedUser = "";
                showLogoutDialog();
            }
        });

        //POST

/*
        postET = findViewById(R.id.postET);
        postButton = findViewById(R.id.postButton);
*/
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();
        clickedUser = "";
/*
        StorageReference profileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileIV);
            }
        });
*/

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null && value.exists()) {
                    List<String> friendsList = (List<String>) value.get("friends");
                    if (friendsList != null) {
                        friends.clear();
                        friends.addAll(friendsList);
                    }
                }
                Log.d(LOG_TAG, "IDE BEGYUTTEM " + userId);
            }
        });

        // recycle view
        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(
                this, gridNumber));
        // Initialize the ArrayList that will contain the data.
        mPostsData = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new PostsAdapter(this, mPostsData, userId, this,friends, "myFeed");
        mRecyclerView.setAdapter(mAdapter);

        mPosts = fStore.collection("posts");
        queryData();
    }

    private void startProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    private void startFeed() {
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    private void startSearchProfile() {
        Intent intent = new Intent(this, SearchProfileActivity.class);
        startActivity(intent);
    }

    public void logout() {
        fAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Close the current activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                profileIV.setImageURI(imageUri);
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();
                profileIV.setImageURI(croppedImageUri);
                uploadImageToFirebase(croppedImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(LOG_TAG, "Error while cropping image: " + error.getMessage(), error);
            }
        }
    }

    // upload an image to firebase
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileIV);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FeedActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("kijelentezés.");
        builder.setMessage("biztosan ki szeretne jelentkezni?");
        builder.setPositiveButton("igen.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.setNegativeButton("nem.", null);
        builder.show();
    }

    // POSTS

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction == null)
                return;

            switch (intentAction) {
                case Intent.ACTION_POWER_CONNECTED:
                    itemLimit = 40;
                    queryData();
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    itemLimit = 40;
                    queryData();
                    break;
            }
        }
    };

    private void queryData() {
        mPostsData.clear();
        mPosts.orderBy("authorID").limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Post post = document.toObject(Post.class);
                mPostsData.add(post);
            }
            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });
    }
    @Override
    public void onPostDeleted() {
        startProfile();
    }

    @Override
    public void onBackPressed() {
        startFeed();
        super.onBackPressed();
    }

}


