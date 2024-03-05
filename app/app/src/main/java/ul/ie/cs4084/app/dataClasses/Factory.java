package ul.ie.cs4084.app.dataClasses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Factory {
    public void createNewPost(
            FirebaseFirestore db,
            String parentBoardId,
            String profileId,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags
    ){
        Map<String, Object> docData = new HashMap<>();
        docData.put("parentBoardId", parentBoardId);
        docData.put("profileId", profileId);
        docData.put("title", title);
        docData.put("body", body);
        docData.put("geotag", geotag);
        docData.put("upvotes", new ArrayList<String>());
        docData.put("downvotes", new ArrayList<String>());
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
    }

    public void createNewComment(String postId, String posterId, String body){
        Map<String, Object> docData = new HashMap<>();
        docData.put("postId", postId);
        docData.put("posterId", posterId);
        docData.put("body", body);
        docData.put("upvotes", new ArrayList<String>());
        docData.put("downvotes", new ArrayList<String>());
        Database.add(docData,"posts/" + postId + "/comments");
    }

    public void createNewBoard(
            FirebaseFirestore db,
            String name,
            String description,
            String relatedImageUrl,
            ArrayList<String> rules,
            HashSet<String> moderators,
            HashSet<String> tags
    ){
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", name);
        docData.put("description", description);
        docData.put("relatedImageUrl", relatedImageUrl);
        docData.put("rules", rules);
        docData.put("moderators",  new ArrayList<String>(moderators));
        db.collection("accounts")
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
