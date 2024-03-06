package ul.ie.cs4084.app.dataClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;

public class Comment{
    private final String id;
    private final DocumentReference post;
    private final DocumentReference poster;
    private String body;
    private HashSet<DocumentReference> upvotes;
    private HashSet<DocumentReference> downvotes;

    public Comment(String id, DocumentReference post, DocumentReference poster, String body, HashSet<DocumentReference> upvotes, HashSet<DocumentReference> downvotes){
        this.id = id;
        this.post = post;
        this.poster = poster;
        this.body = body;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public String getId() {
        return id;
    }

    public DocumentReference getPoster() {
        return poster;
    }

    public DocumentReference getPost() {
        return post;
    }

    public String getBody() {
        return body;
    }

    public ArrayList<DocumentReference> getUpvotes() {
        return new ArrayList<DocumentReference>(upvotes);
    }

    public ArrayList<DocumentReference> getDownvotes() {
        return new ArrayList<DocumentReference>(downvotes);
    }

    public void addUpvote(DocumentReference upvoter, FirebaseFirestore db){
        if(upvotes.add(upvoter)){
            db.collection(post.getPath()).document(id).update("upvotes", FieldValue.arrayUnion(upvoter));
        }
    }

    public void removeUpvote(DocumentReference upvoter, FirebaseFirestore db){
        if(upvotes.remove(upvoter)){
            db.collection("Comments").document(id).update("upvotes", FieldValue.arrayRemove(upvoter));
        }
    }

    public void addDownvote(DocumentReference downvoter, FirebaseFirestore db){
        if(downvotes.add(downvoter)){
            db.collection(post.getPath()).document(id).update("downvotes", FieldValue.arrayUnion(downvoter));
        }
    }

    public void removeDownvote(DocumentReference downvoter, FirebaseFirestore db){
        if(downvotes.remove(downvoter)){
            db.collection("Comments").document(id).update("downvotes", FieldValue.arrayRemove(downvoter));
        }
    }

    public void setBody(String body, FirebaseFirestore db) {
        this.body = body;
        db.collection(post.getPath()).document(id).update("body", body);
    }
}
