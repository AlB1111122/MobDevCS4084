package ul.ie.cs4084.app.dataClasses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Database {
    public static Task<DocumentReference> add(Object obj, String collection) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(collection)
                .add(obj)
                .addOnSuccessListener(documentReference -> Log.d(TAG, collection + " successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing " + collection, e));
    }


}
