package ul.ie.cs4084.app.dataClasses;

import java.util.ArrayList;
import java.util.HashSet;

public class Post implements DBobject{
    private final String id;
    private final String parentBoardId;
    private final String profileId;
    private String title;
    private String body;
    private HashSet<String> tags;
    //private String geotag;
    private HashSet<String> upvotes;
    private HashSet<String> downvotes;
    private String imageUrl;

    public Post(
            String id,
            String parentBoardId,
            String profileId,
            String title,
            HashSet<String> tags,
            HashSet<String> upvotes,
            HashSet<String> downvotes
    ){
        this.id = id;
        this.parentBoardId = parentBoardId;
        this.profileId = profileId;
        this.title = title;
        this.tags = tags;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public void addTag(String tag){
        if(!tags.contains(tag)){
            tags.add(tag);
        }
    }

    public void addUpvote(String upvoter){
        upvotes.add(upvoter);
    }

    public void addDownvote(String downvoter){
        downvotes.add(downvoter);
    }

    public String getId(){
        return id;
    }

    public String getParentBoardId() {
        return parentBoardId;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public HashSet<String> getTagsSet() {
        return tags;
    }

    public HashSet<String> getUpvotesSet() {
        return upvotes;
    }

    public HashSet<String> getDownvotesSet() {
        return downvotes;
    }
    //for writing to the db
    public ArrayList<String> getTags() {
        return new ArrayList<String>(tags);
    }

    public ArrayList<String> getUpvotes() {
        return new ArrayList<String>(upvotes);
    }

    public ArrayList<String> getDownvotes() {
        return new ArrayList<String>(downvotes);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setBody(String body){
        this.body = body;
    }

    public void setTags(HashSet<String> tags){
        this.tags = tags;
    }

    public void setUpvotes(HashSet<String> upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(HashSet<String> downvotes) {
        this.downvotes = downvotes;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
