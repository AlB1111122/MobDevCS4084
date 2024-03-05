package ul.ie.cs4084.app.dataClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;

public class Board{

    private final String id;
    private final String name;
    private String description;
    private String relatedImageUrl;
    private ArrayList<String> rules;
    private HashSet<String> moderators;
    private HashSet<String> tags;

    public Board(
            String id,
            String name,
            String description,
            String relatedImageUrl,
            ArrayList<String> rules,
            HashSet<String> moderators,
            HashSet<String> tags
    ){
        this.id = id;
        this.name = name;
        this.description = description;
        this.relatedImageUrl =  relatedImageUrl;
        this.rules = rules;
        this.moderators = moderators;
        this.tags = tags;
    }

    public void addTag(String tag, FirebaseFirestore db){
        DocumentReference boardRef = db.collection("boards").document(id);
        Map<String, Object> docData = new HashMap<>();
        docData.put("parentBoard", boardRef);
        docData.put("tag", tag);
        if(tags.add(tag)){
            Database.add(docData,"boardTags");
        }
    }

    public void setDescription(String description, FirebaseFirestore db) {
        this.description = description;
        db.collection("boards").document(id).update("description", description);
    }

    public void setRelatedImageUrl(String relatedImageUrl, FirebaseFirestore db) {
        this.relatedImageUrl = relatedImageUrl;
        db.collection("boards").document(id).update("relatedImageUrl", relatedImageUrl);
    }

    public void addRule(String rule, FirebaseFirestore db){
        rules.add(rule);
        db.collection("boards").document(id).update("rules", FieldValue.arrayUnion(rule));
    }

    public void removeRule(String rule, FirebaseFirestore db){
        rules.add(rule);
        db.collection("boards").document(id).update("rules", FieldValue.arrayRemove(rule));
    }

    public void addModerator(String moderator, FirebaseFirestore db){
        if(moderators.add(moderator)){
            db.collection("boards").document(id).update("moderators", FieldValue.arrayUnion(moderator));
        }
    }

    public void removeModerators(String moderator, FirebaseFirestore db){
        if(moderators.remove(moderator)){
            db.collection("boards").document(id).update("moderators", FieldValue.arrayUnion(moderator));
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedImageUrl(){
        return relatedImageUrl;
    }

    public ArrayList<String> getRules() {
        return rules;
    }

    public HashSet<String> getModeratorsSet() {
        return moderators;
    }

    public HashSet<String> getTagsSet() {
        return tags;
    }

    public ArrayList<String> getModerators() {
        return new ArrayList<String>(moderators);
    }
}