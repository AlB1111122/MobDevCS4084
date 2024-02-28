package ul.ie.cs4084.app.dataClasses;

import java.util.Vector;

public class Post implements DBobject{
    private final String id;
    private final String parentBoardId;
    private final String profileId;
    private String title;
    private String body;
    private Vector<String> tags;
    //private String geotag;
    private Vector<String> upvotes;
    private Vector<String> downvotes;
    private String imageUrl;

    public Post(
            String id,
            String parentBoardId,
            String profileId,
            String title,
            Vector<String> tags,
            Vector<String> upvotes,
            Vector<String> downvotes
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
        if(!upvotes.contains(upvoter)){
            upvotes.add(upvoter);
        }
    }

    public void addDownvote(String downvoter){
        if(!downvotes.contains(downvoter)){
            downvotes.add(downvoter);
        }
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

    public Vector<String> getTags() {
        return tags;
    }

    public Vector<String> getUpvotes() {
        return upvotes;
    }

    public Vector<String> getDownvotes() {
        return downvotes;
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

    public void setTags(Vector<String> tags){
        this.tags = tags;
    }

    public void setUpvotes(Vector<String> upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(Vector<String> downvotes) {
        this.downvotes = downvotes;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
