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

public class PostFactory {
    public void createNewPost(
            FirebaseFirestore db,
            String parentBoardId,
            String profileId,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags,
            HashSet<String> upvotes,
            HashSet<String> downvotes
    ){
        Map<String, Object> docData = new HashMap<>();
        docData.put("parentBoardId", parentBoardId);
        docData.put("profileId", profileId);
        docData.put("title", title);
        docData.put("body", body);
        docData.put("geotag", geotag);
        docData.put("upvotes", new ArrayList<String>(upvotes));
        docData.put("downvotes", new ArrayList<String>(downvotes));
        db.collection("posts")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,  "new post successfully written!");
                        createInitialTags(tags,documentReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing post");
                    }
                });
    }

    private void createInitialTags(HashSet<String> tags, DocumentReference parentPost){
        for(String tag: tags) {
            Map<String, Object> docData = new HashMap<>();
            docData.put("parentPost", parentPost);
            docData.put("tag", tag);
            Database.add(docData, "postTags");
        }
    }

}
