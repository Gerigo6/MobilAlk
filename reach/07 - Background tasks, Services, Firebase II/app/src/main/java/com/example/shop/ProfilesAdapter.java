package com.example.shop;

import static com.example.shop.MainActivity.clickedUser;

import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.User;


public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder> {

    private List<User> profileList;
    private String currentUser;
    StorageReference storageReference;
    String userId;

    FirebaseAuth fAuth;
    private int lastPosition = -1;

    FirebaseFirestore fStore;

    private ProfilesAdapter.OnPostDeletedListener onPostDeletedListener;

    public ProfilesAdapter(List<User> profileList, OnPostDeletedListener listener) {
        this.profileList = profileList;
        this.onPostDeletedListener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User profile = profileList.get(position);
        holder.bindTo(profile);
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView profileIV;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.usernameTV);
            emailTextView = itemView.findViewById(R.id.emailTV);
            profileIV = itemView.findViewById(R.id.profilIV);
        }

        public void bindTo(User profile) {
            nameTextView.setText(profile.getUsername());
            emailTextView.setText(profile.getEmail());

            fAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();

            userId = fAuth.getCurrentUser().getUid();


            StorageReference profileRef = storageReference.child("users/" + profile.getUserID() + "/profile.jpg");
            System.out.println("\n======== CURRENT USER =======\n " + userId + "\n=============================\n\n");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profileIV);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO
                }
            });
            profileIV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    clickedUser = profile.getUserID();
                    onPostDeletedListener.onPostDeleted();
                }
            });


        }
    }

    public interface OnPostDeletedListener {
        void onPostDeleted();
    }
}
