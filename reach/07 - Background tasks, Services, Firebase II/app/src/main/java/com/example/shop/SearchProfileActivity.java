package com.example.shop;

import static com.example.shop.MainActivity.clickedUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import model.User;

public class SearchProfileActivity extends AppCompatActivity implements ProfilesAdapter.OnPostDeletedListener {

    private static final String TAG = "SearchProfileActivity";
    private RecyclerView recyclerView;
    private ImageView profileClick;
    private ImageView logOut;
    private ProfilesAdapter adapter;
    private List<User> profileList;
    private FirebaseFirestore db;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        profileList = new ArrayList<>();
        adapter = new ProfilesAdapter(profileList, this);

        profileClick = findViewById(R.id.profileClick);

        profileClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedUser = "";
                startProfile();
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

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadProfiles();
        fAuth = FirebaseAuth.getInstance();
    }

    private void loadProfiles() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            profileList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User profile = document.toObject(User.class);
                                profileList.add(profile);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void startProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void startFeed() {
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startFeed();
        super.onBackPressed();
    }
    @Override
    public void onPostDeleted() {
        startProfile();
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

    public void logout() {
        fAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Close the current activity
    }
}

