package model;

import java.util.ArrayList;
import java.util.List;

public class User {


    private String userID;
    private String userName;
    private String email;
    private String password;
    private String country;
    private String city;
    private String phoneNumber;
    private String gender;
    private String profilePictureUrl;
    private List<String> friends = new ArrayList();

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userID, String username, String email, String password, String country, String city, String phoneNumber, String gender, String profilePictureUrl) {
        this.userID = userID;
        this.userName = username;
        this.email = email;
        this.password = password;
        this.country = country;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.profilePictureUrl = profilePictureUrl;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void addFriends(String friend){
        this.friends.add(friend);
    }

    public void setFriends(List<String> friends){
        this.friends = friends;
    }

    // Getters and setters
    // ...

}
