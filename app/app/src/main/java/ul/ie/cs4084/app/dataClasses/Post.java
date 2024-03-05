package ul.ie.cs4084.app.dataClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Post{
    private final String id;
    private final String parentBoardId;
    private final String profileId;
    private String title;
    private String body;
    private HashSet<String> tags;
    private GeoPoint geotag;
    private HashSet<String> upvotes;
    private HashSet<String> downvotes;
    private String imageUrl;

    public Post(
            String id,
            String parentBoardId,
            String profileId,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags,
            HashSet<String> upvotes,
            HashSet<String> downvotes
    ){
        this.id = id;
        this.parentBoardId = parentBoardId;
        this.profileId = profileId;
        this.title = title;
        this.body = body;
        this.geotag = geotag;
        this.tags = tags;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public void addTag(String tag, FirebaseFirestore db){
        DocumentReference postRef = db.collection("posts").document(id);
        Map<String, Object> docData = new HashMap<>();
        docData.put("parentPost", postRef);
        docData.put("tag", tag);
        if(tags.add(tag)){
            Database.add(docData,"postTags");
        }
    }

    public void addUpvote(String upvoter, FirebaseFirestore db){
        if(upvotes.add(upvoter)){
            db.collection("posts").document(id).update("upvotes", FieldValue.arrayUnion(upvoter));
        }
    }

    public void addDownvote(String downvoter, FirebaseFirestore db){
        if(downvotes.add(downvoter)){
            db.collection("posts").document(id).update("downvotes", FieldValue.arrayUnion(downvoter));
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

    public GeoPoint getGeotag() {
        return geotag;
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
    //public ArrayList<String> getTags() {
    //    return new ArrayList<String>(tags);
   // }

    public ArrayList<String> getUpvotes() {
        return new ArrayList<String>(upvotes);
    }

    public ArrayList<String> getDownvotes() {
        return new ArrayList<String>(downvotes);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title, FirebaseFirestore db){
        this.title = title;
        db.collection("posts").document(id).update("title", title);
    }

    public void setBody(String body, FirebaseFirestore db){
        this.body = body;
        db.collection("posts").document(id).update("body", body);
    }

    public void setGeotag(GeoPoint geotag, FirebaseFirestore db) {
        this.geotag = geotag;
        db.collection("posts").document(id).update("geotag", geotag);
    }

    public void setImageUrl(String imageUrl, FirebaseFirestore db) {
        this.imageUrl = imageUrl;
        db.collection("posts").document(id).update("imageUrl", imageUrl);
    }
}
