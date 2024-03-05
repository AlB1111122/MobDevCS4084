package ul.ie.cs4084.app.dataClasses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static java.lang.Thread.sleep;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Factory {

    public DocumentReference createNewPost(
            FirebaseFirestore db,
            DocumentReference parentBoard,
            DocumentReference profile,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags
    ) throws InterruptedException {
        Map<String, Object> docData = new HashMap<>();
        docData.put("parentBoard", parentBoard);
        docData.put("profile", profile);
        docData.put("title", title);
        docData.put("body", body);
        docData.put("geotag", geotag);
        docData.put("upvotes", new ArrayList<String>());
        docData.put("downvotes", new ArrayList<String>());
        Task<DocumentReference> dbRefrence =
            db.collection("posts")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,  "new post successfully written!");
                        createInitialTags(tags,documentReference,"Post");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing post");
                    }
                });
        while(!dbRefrence.isComplete()){
            sleep(5);//CHANGE LATER
        }
        return dbRefrence.getResult();
    }

    public DocumentReference createNewComment(DocumentReference post, DocumentReference poster, String body) throws InterruptedException {
        Map<String, Object> docData = new HashMap<>();
        docData.put("post", post);
        docData.put("poster", poster);
        docData.put("body", body);
        docData.put("upvotes", new ArrayList<String>());
        docData.put("downvotes", new ArrayList<String>());
        Task<DocumentReference> dbRefrence = Database.add(docData,post.getPath()+"/comments");
        while(!dbRefrence.isComplete()){
            sleep(5);//CHANGE LATER
        }
        return dbRefrence.getResult();
    }

    public DocumentReference createNewBoard(
            FirebaseFirestore db,
            String name,
            String description,
            String relatedImageUrl,
            ArrayList<String> rules,
            HashSet<DocumentReference> moderators,
            HashSet<String> tags
    ) throws InterruptedException {
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", name);
        docData.put("description", description);
        docData.put("relatedImageUrl", relatedImageUrl);
        docData.put("rules", rules);
        docData.put("moderators",  new ArrayList<DocumentReference>(moderators));

        Task<DocumentReference> dbRefrence = db.collection("boards")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,  "new post successfully written!");
                        createInitialTags(tags,documentReference,"Board");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing post");
                    }
                });
       while(!dbRefrence.isComplete()){
            sleep(5);//CHANGE LATER
        }
        return dbRefrence.getResult();
    }


    private void createInitialTags(HashSet<String> tags, DocumentReference parent, String parentCollection){
        for(String tag: tags) {
            Map<String, Object> docData = new HashMap<>();
            docData.put("parent" + parentCollection, parent);
            docData.put("tag", tag);
            Database.add(docData, parentCollection.toLowerCase()+"Tags");
        }
    }


}
