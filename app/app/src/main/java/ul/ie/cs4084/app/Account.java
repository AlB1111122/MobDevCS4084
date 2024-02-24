package ul.ie.cs4084.app;

public class Account {
    private String authUuid;
    private String username;
    private String profilePictureUrl;

    public Account(String authUuid, String username){
        this.authUuid = authUuid;
        this.username = username;
        this.profilePictureUrl = "gs://socialmediaapp-38b04.appspot.com/profilePictures/defaultProfile.jpg";
    }

    public String getAuthUuid(){
        return this.authUuid;
    }

    public String getUsername(){
        return this.username;
    }

    public String getProfilePictureUrl() {
        return this.profilePictureUrl;
    }
}

