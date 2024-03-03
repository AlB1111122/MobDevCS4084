package ul.ie.cs4084.app.dataClasses;

import java.util.ArrayList;
import java.util.HashSet;

public class Account implements DBobject {
    private final String id;
    private String username;
    private String profilePictureUrl;
    private HashSet<String> followedTags;
    private HashSet<String> blockedTags;
    public Account(String id){
        this.id = id;
        this.profilePictureUrl = "gs://socialmediaapp-38b04.appspot.com/profilePictures/defaultProfile.jpg";
    }

    public void blockTag(String tag){
        if(!blockedTags.contains(tag)) {
            this.blockedTags.add(tag);
        }
    }

    public void followTag(String tag){
        if(!blockedTags.contains(tag)) {
            this.followedTags.add(tag);
        }
    }

    public void setAttributes(String username, String profilePictureUrl, HashSet<String> followedTags, HashSet<String> blockedTags){
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.followedTags = followedTags;
        this.blockedTags = blockedTags;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setBlockedTags(HashSet<String> blockedTags) {
        this.blockedTags = blockedTags;
    }

    public void setFollowedTags(HashSet<String> followedTags) {
        this.followedTags = followedTags;
    }

    public String getId(){
        return this.id;
    }

    public String getUsername(){
        return this.username;
    }

    public String getProfilePictureUrl() {
        return this.profilePictureUrl;
    }

    public HashSet<String> getFollowedSet() {
        return followedTags;
    }

    public HashSet<String> getBlockedSet() {
        return blockedTags;
    }

    public ArrayList<String> getFollowedTags() {
        return new ArrayList<String>(followedTags);
    }

    public ArrayList<String> getBlockedTags() {
        return new ArrayList<String>(blockedTags);
    }
}

