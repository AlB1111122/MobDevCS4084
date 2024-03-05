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
    private HashSet<String> upvotes;
    private HashSet<String> downvotes;

    public Comment(String id, DocumentReference post, DocumentReference poster, String body, HashSet<String> upvotes, HashSet<String> downvotes){
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

    public ArrayList<String> getUpvotes() {
        return new ArrayList<String>(upvotes);
    }

    public ArrayList<String> getDownvotes() {
        return new ArrayList<String>(downvotes);
    }

    public void addUpvote(String upvoter, FirebaseFirestore db){
        if(upvotes.add(upvoter)){
            db.collection(post.getPath()).document(id).update("upvotes", FieldValue.arrayUnion(upvoter));
        }
    }

    public void addDownvote(String downvoter, FirebaseFirestore db){
        if(downvotes.add(downvoter)){
            db.collection(post.getPath()).document(id).update("downvotes", FieldValue.arrayUnion(downvoter));
        }
    }

    public void setBody(String body, FirebaseFirestore db) {
        this.body = body;
        db.collection(post.getPath()).document(id).update("body", body);
    }
}
