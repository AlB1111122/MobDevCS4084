package ul.ie.cs4084.app.dataClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Post{
    private final String id;
    private final DocumentReference parentBoard;
    private final DocumentReference profile;
    private String title;
    private String body;
    private HashSet<String> tags;
    private GeoPoint geotag;
    private HashSet<DocumentReference> upvotes;
    private HashSet<DocumentReference> downvotes;
    private String imageUrl;

    public Post(
            String id,
            DocumentReference parentBoard,
            DocumentReference profile,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags,
            HashSet<DocumentReference> upvotes,
            HashSet<DocumentReference> downvotes,
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

    public Post (DocumentSnapshot postDocument){
        this.id = postDocument.getId();
        this.parentBoard = postDocument.getDocumentReference("parentBoard");
        this.profile = postDocument.getDocumentReference("profile");
        this.title = postDocument.getString("title");
        this.body = postDocument.getString("body");
        this.geotag = postDocument.getGeoPoint("geotag");
        this.tags =  new HashSet<String>((List) Objects.requireNonNull(postDocument.get("tags")));
        this.upvotes = new HashSet<DocumentReference>((List) Objects.requireNonNull(postDocument.get("upvotes")));
        this.downvotes = new HashSet<DocumentReference>((List) Objects.requireNonNull(postDocument.get("downvotes")));
        this.imageUrl = postDocument.getString("imageUrl");
    }

    public void addTag(String tag, FirebaseFirestore db){
        if(tags.add(tag)){
            db.collection("posts").document(id).update("tags", FieldValue.arrayUnion(tag));
        }
    }

    public void removeTag(String tag, FirebaseFirestore db){
        if(tags.remove(tag)){
            db.collection("posts").document(id).update("tags", FieldValue.arrayRemove(tags));
        }
    }

    public void addUpvote(DocumentReference upvoter, FirebaseFirestore db){
        removeDownvote(upvoter,db);
        if(upvotes.add(upvoter)){
            db.collection("posts").document(id).update("upvotes", FieldValue.arrayUnion(upvoter));
        }
    }

    public void removeUpvote(DocumentReference upvoter, FirebaseFirestore db){
        if(upvotes.remove(upvoter)){
            db.collection("posts").document(id).update("upvotes", FieldValue.arrayRemove(upvoter));
        }
    }

    public void addDownvote(DocumentReference downvoter, FirebaseFirestore db){
        removeUpvote(downvoter,db);
        if(downvotes.add(downvoter)){
            db.collection("posts").document(id).update("downvotes", FieldValue.arrayUnion(downvoter));
        }
    }

    public void removeDownvote(DocumentReference downvoter, FirebaseFirestore db){
        if(downvotes.remove(downvoter)){
            db.collection("posts").document(id).update("downvotes", FieldValue.arrayRemove(downvoter));
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

    public HashSet<String> retriveTagsSet() {
        return tags;
    }
    public ArrayList<String> getTags() {
        return  new ArrayList<String>(tags);
    }

    public HashSet<DocumentReference> retriveUpvotesSet() {
        return upvotes;
    }

    public HashSet<DocumentReference> retriveDownvotesSet() {
        return downvotes;
    }

    public ArrayList<DocumentReference> getUpvotes() {
        return new ArrayList<DocumentReference>(upvotes);
    }

    public ArrayList<DocumentReference> getDownvotes() {
        return new ArrayList<DocumentReference>(downvotes);
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
