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
    private final DocumentReference parentBoard;
    private final DocumentReference profile;
    private String title;
    private String body;
    private HashSet<String> tags;
    private GeoPoint geotag;
    private HashSet<String> upvotes;
    private HashSet<String> downvotes;
    private String imageUrl;

    public Post(
            String id,
            DocumentReference parentBoard,
            DocumentReference profile,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags,
            HashSet<String> upvotes,
            HashSet<String> downvotes,
            String imageUrl
    ){
        this.id = id;
        this.parentBoard = parentBoard;
        this.profile = profile;
        this.title = title;
        this.body = body;
        this.geotag = geotag;
        this.tags = tags;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.imageUrl = imageUrl;
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

    public DocumentReference getParentBoard() {
        return parentBoard;
    }

    public DocumentReference getProfile() {
        return profile;
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
