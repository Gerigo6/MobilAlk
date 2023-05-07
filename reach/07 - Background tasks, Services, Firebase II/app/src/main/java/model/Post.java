package model;

import com.google.firebase.Timestamp;

public class Post {

    private String postID;
    private String authorID;

    private String authorName;
    private String text;

    private Timestamp postTime;


    public Post() {
    }

    public Post(String postID, String authorID, String authorName, String text, Timestamp postTime) {
        this.postID = postID;
        this.authorID = authorID;
        this.authorName = authorName;
        this.text = text;
        this.postTime = postTime;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Timestamp getPostTime() {
        return postTime;
    }

    public void setPostTime(Timestamp postTime) {
        this.postTime = postTime;
    }
}
