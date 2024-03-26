package ul.ie.cs4084.app.dataClasses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;

public class Factory {

    private final Executor executor;

    public Factory(Executor executor){
        this.executor = executor;
    }

    public void createNewPost(
            FirebaseFirestore db,
            DocumentReference parentBoard,
            DocumentReference profile,
            String title,
            String body,
            GeoPoint geotag,
            HashSet<String> tags,
            final Callback<DocumentReference> callback
    ){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> docData = new HashMap<>();
                docData.put("parentBoard", parentBoard);
                docData.put("profile", profile);
                docData.put("title", title);
                docData.put("body", body);
                docData.put("geotag", geotag);
                docData.put("tags", new ArrayList<String>(tags));
                docData.put("upvotes", new ArrayList<String>());
                docData.put("downvotes", new ArrayList<String>());
                db.collection("posts")
                    .add(docData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG,  "new post successfully written!");
                        callback.callback(documentReference);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing post"));
            }
        });
    }

    public void createNewComment(
            DocumentReference post,
            DocumentReference poster,
            String body,
            final Callback<DocumentReference> callback
    ){
        executor.execute(() -> {
            Map<String, Object> docData = new HashMap<>();
            docData.put("post", post);
            docData.put("poster", poster);
            docData.put("body", body);
            docData.put("upvotes", new ArrayList<String>());
            docData.put("downvotes", new ArrayList<String>());
            Database.add(docData,post.getPath()+"/comments")
                    .addOnSuccessListener(documentReference -> callback.callback(documentReference));
        });
    }

    public void createNewBoard(
            FirebaseFirestore db,
            String name,
            String description,
            String relatedImageUrl,
            ArrayList<String> rules,
            HashSet<DocumentReference> moderators,
            HashSet<String> tags,
            final Callback<DocumentReference> callback
    ) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> docData = new HashMap<>();
                docData.put("name", name);
                docData.put("description", description);
                docData.put("relatedImageUrl", relatedImageUrl);
                docData.put("rules", rules);
                docData.put("tags", new ArrayList<String>(tags));
                docData.put("moderators", new ArrayList<DocumentReference>(moderators));
                db.collection("boards")
                        .add(docData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "new board successfully written!");
                            callback.callback(documentReference);
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing board"));
            }
        });
    }
}
