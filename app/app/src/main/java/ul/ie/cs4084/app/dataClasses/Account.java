package ul.ie.cs4084.app.dataClasses;

import java.util.Vector;

public class Account implements DBobject {
    private final String id;
    private String username;
    private String profilePictureUrl;
    private Vector<String> followedTags;
    private Vector<String> blockedTags;
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

    public void setAttributes(String username, String profilePictureUrl, Vector<String> followedTags, Vector<String> blockedTags){
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

    public void setBlockedTags(Vector<String> blockedTags) {
        this.blockedTags = blockedTags;
    }

    public void setFollowedTags(Vector<String> followedTags) {
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

    public Vector<String> getFollowedTags() {
        return followedTags;
    }

    public Vector<String> getBlockedTags() {
        return blockedTags;
    }
}
