package ul.ie.cs4084.app.dataClasses;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.Executor;

public class Database {
    public static Task<DocumentReference> add(Object obj, String collection) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(collection)
                .add(obj)
                .addOnSuccessListener(documentReference -> Log.d(TAG, collection + " successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing " + collection, e));
    }

    public static void displayPicture(String url, ImageView imageView, Executor executor, Handler mainHandler, Resources theme) {
        try{
            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            final long ONE_MEGABYTE = 1024 * 1024;
            gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> executor.execute(() -> {//runnig on a thred because its slow
                Drawable image = new BitmapDrawable(theme, BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.length
                ));
                //post back to ui thred
                mainHandler.post(() -> imageView.setImageDrawable(image));

            })).addOnFailureListener(exception -> Log.d(TAG, "error fetching PFP"));
        }catch(NullPointerException | IllegalArgumentException e){
            return;
        }
    }
}
