public class CommentFragment {
    private int upvotes;
    private int downvotes;
    private String content;
    private String poster;

    public CommentFragment(int upvotes, int downvotes, String content, String poster) {
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.content = content;
        this.poster = poster;
    }

    // Getters and setters
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @Override
    public String toString() {
        return "CommentFragment{" +
                "upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", content='" + content + '\'' +
                ", poster='" + poster + '\'' +
                '}';
    }
}
