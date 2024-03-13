package ul.ie.cs4084.app.dataClasses;


import com.google.firebase.firestore.DocumentReference;

public interface DBCallback<T> {

    public void callback(T t);
}
