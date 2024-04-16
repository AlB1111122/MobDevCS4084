package ul.ie.cs4084.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ul.ie.cs4084.app.dataClasses.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private ArrayList<Post> localDataSet;
    private DocumentReference profileDoc;
    private FirebaseFirestore db;
    private NavController navController;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView postTitle;
        public final TextView postBody;
        public final TextView upCount;
        public final TextView downCount;
        public final Button upvote;
        public final Button downvote;
        public final CardView card;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            postTitle = view.findViewById(R.id.postTitleText);
            postBody = view.findViewById(R.id.postBody);
            upCount = view.findViewById(R.id.upvoteCounter);
            downCount = view.findViewById(R.id.downvoteCounter);
            upvote = view.findViewById(R.id.upvoteButton);
            downvote = view.findViewById(R.id.downvoteButton);
            card = view.findViewById(R.id.card);
        }
    }

    public PostAdapter(ArrayList<Post> dataSet, DocumentReference profileDoc,FirebaseFirestore  db, NavController navController) {
        this.localDataSet = dataSet;
        this.profileDoc = profileDoc;
        this.db = db;
        this.navController = navController;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_timeline_post, viewGroup, false);

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = localDataSet.get(position);

        // Set item views based on your views and data model
        holder.postTitle.setText(p.getTitle());
        holder.postBody.setText(p.getBody());
        holder.upCount.setText(Integer.toString(p.retriveUpvotesSet().size()));
        holder.downCount.setText(Integer.toString(p.retriveDownvotesSet().size()));
        holder.upvote.setOnClickListener(upvote -> {
            p.addUpvote(profileDoc,db);
            holder.upCount.setText(Integer.toString(p.retriveUpvotesSet().size()));
            holder.downCount.setText(Integer.toString(p.retriveDownvotesSet().size()));
        });
        holder.downvote.setOnClickListener(upvote -> {
            p.addDownvote(profileDoc,db);
            holder.upCount.setText(Integer.toString(p.retriveUpvotesSet().size()));
            holder.downCount.setText(Integer.toString(p.retriveDownvotesSet().size()));
        });
        holder.card.setOnClickListener(clicked -> {
            Bundle bundle = new Bundle();
            bundle.putString("postId", p.getId());
            bundle.putSerializable("postObj",p);
            navController.navigate(R.id.action_Home_to_FullscreenPost,bundle);
        });

    }

    public void addPost(Post p){
        localDataSet.add(p);
        this.notifyItemInserted(this.getItemCount());
    }

    public void addAllPosts(ArrayList<Post> posts){
        int start = this.getItemCount();
        int length = posts.size();
        localDataSet.addAll(posts);
        this.notifyItemRangeInserted(start,length);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
