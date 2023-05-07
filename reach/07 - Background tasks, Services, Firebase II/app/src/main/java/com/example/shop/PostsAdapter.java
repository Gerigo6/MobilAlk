package com.example.shop;

import static androidx.core.content.ContextCompat.startActivity;

import static com.example.shop.MainActivity.clickedUser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import model.Post;
import model.User;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>
        implements Filterable {
    // Member variables.
    private ArrayList<Post> mPostData;
    private ArrayList<Post> mPostDataAll;
    private Context mContext;

    private String currentUser;
    StorageReference storageReference;

    FirebaseAuth fAuth;
    private int lastPosition = -1;

    FirebaseFirestore fStore;

    List<String> friends;
    String type;

    private OnPostDeletedListener onPostDeletedListener;



    public PostsAdapter(Context context, ArrayList<Post> postsData,String currentUser, OnPostDeletedListener listener,List<String> friends, String type) {
        this.mPostData = postsData;
        this.mPostDataAll = postsData;
        this.mContext = context;
        this.currentUser = currentUser;
        this.onPostDeletedListener = listener;
        this.friends = friends;
        this.type = type;
    }

    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        return new PostsAdapter.ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PostsAdapter.ViewHolder holder, int position) {

        fStore = FirebaseFirestore.getInstance();

        Collections.sort(mPostData, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return post2.getPostTime().compareTo(post1.getPostTime());
            }
        });
        // Get current sport.
        Post currentPost = mPostData.get(position);

        // Get the current user's userID.
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(type.equals("myFeed")) {

            // Check if the current post was written by the current user.
            if (friends.contains(currentPost.getAuthorID())) {
                // Populate the textviews with data.
                holder.bindTo(currentPost);

                // Add animation.
                if (holder.getAdapterPosition() > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
                    holder.itemView.startAnimation(animation);
                    lastPosition = holder.getAdapterPosition();
                }
            } else {
                // Hide the view holder if the current post was not written by the current user.
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }
        else if(type.equals("myProfile")){
            if (currentPost.getAuthorID().equals(currentUserID)) {
                // Populate the textviews with data.
                holder.bindTo(currentPost);

                // Add animation.
                if (holder.getAdapterPosition() > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fragment_close_enter);
                    holder.itemView.startAnimation(animation);
                    lastPosition = holder.getAdapterPosition();
                }
            } else {
                // Hide the view holder if the current post was not written by the current user.
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }

        }
        else if(type.equals("clickedProfile")){
            if (currentPost.getAuthorID().equals(currentUser)) {
                // Populate the textviews with data.
                holder.bindTo(currentPost);

                // Add animation.
                if (holder.getAdapterPosition() > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fragment_fade_enter);
                    holder.itemView.startAnimation(animation);
                    lastPosition = holder.getAdapterPosition();
                }
            } else {
                // Hide the view holder if the current post was not written by the current user.
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }

        }

    }
    @Override
    public int getItemCount() {
        return mPostData.size();
    }


    /**
     * RecycleView filter
     * **/
    @Override
    public Filter getFilter() {
        return postFilter;
    }

    private Filter postFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Post> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0) {
                // if there is no filter pattern, return all the posts by the current user.
                for(Post post : mPostDataAll) {
                    if(friends.contains(post.getAuthorID())){
                        filteredList.add(post);
                    }
                }
            } else {
                // if there is a filter pattern, return the filtered posts by the current user.
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Post post : mPostDataAll) {
                    if(friends.contains(post.getAuthorID()) && post.getText().toLowerCase().contains(filterPattern)){
                        filteredList.add(post);
                    }
                }
            }

            results.count = filteredList.size();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mPostData = (ArrayList)filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        // Member Variables for the TextViews
        private TextView authorTV;
        private TextView textTV;

        private TextView dateTV;


        private ImageView profileIV;
        private ImageView deleteBin;

        String userId;

        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            authorTV = itemView.findViewById(R.id.authorTV);
            textTV = itemView.findViewById(R.id.textTV);
            dateTV = itemView.findViewById(R.id.dateTV);
            profileIV = itemView.findViewById(R.id.profilIV);
            deleteBin = itemView.findViewById(R.id.deleteBin);

        }

        void bindTo(Post currentPost){
            authorTV.setText(currentPost.getAuthorName());
            textTV.setText(currentPost.getText());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = currentPost.getPostTime().toDate();
            String formattedDate = dateFormat.format(date);
            dateTV.setText(formattedDate);

            fAuth = FirebaseAuth.getInstance();
            fStore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();

            userId = fAuth.getCurrentUser().getUid();

            if(!currentPost.getAuthorID().equals(userId)){
                deleteBin.setVisibility(View.GONE);
            }

            StorageReference profileRef = storageReference.child("users/" + currentPost.getAuthorID() + "/profile.jpg");
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
                    clickedUser = currentPost.getAuthorID();
                    onPostDeletedListener.onPostDeleted();
                }
            });



            deleteBin.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    deletePost(currentPost.getPostID());
                }
            });


        }
    }

    private void deletePost(String postId) {
        // Access the Firestore database and get a reference to the post document to delete.
        DocumentReference postRef = fStore.collection("posts").document(postId);

        // Call the delete() method on the reference to delete the document.
        postRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Remove the deleted post from the ArrayList and notify the adapter of the change.
                        for (int i = 0; i < mPostDataAll.size(); i++) {
                            if (mPostDataAll.get(i).getPostID().equals(postId)) {
                                mPostDataAll.remove(i);
                                notifyItemRemoved(i);
                                break;
                            }
                        }
                        if (onPostDeletedListener != null) {
                            onPostDeletedListener.onPostDeleted();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO
                    }
                });

    }

    public interface OnPostDeletedListener {
        void onPostDeleted();
    }

    public interface OnProfileListener {
        void onProfileClick(String clickedUser);
    }

}



