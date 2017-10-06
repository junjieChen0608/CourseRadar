package UtilityClass;

/**
 * Created by yang on 10/5/17.
 */

public class ReviewInfo {
    private String name;
    private String review;
    public ReviewInfo(String name, String review) {
        this.name = name;
        this.review = review;
    }
    public String getName() {
        return name;
    }
    public String getReview() {
        return review;
    }

}
