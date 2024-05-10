import java.util.Date;

public class PopularFragment {
    private String postTitle;
    private int upvotes;
    private int downvotes;
    private Date timestamp;

    public PopularFragment(String postTitle, int upvotes, int downvotes, Date timestamp) {
        this.postTitle = postTitle;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Method to calculate upvotes to downvotes ratio
    public double calculateUpvoteRatio() {
        if (downvotes == 0) {
            return upvotes;
        } else {
            return (double) upvotes / downvotes;
        }
    }

    @Override
    public String toString() {
        return "PopularFragment{" +
                "postTitle='" + postTitle + '\'' +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", upvoteRatio=" + calculateUpvoteRatio() +
                ", timestamp=" + timestamp +
                '}';
    }
}
