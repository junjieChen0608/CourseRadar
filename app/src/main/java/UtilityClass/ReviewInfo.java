package UtilityClass;

import java.util.HashMap;

/**
 * Created by yang on 10/5/17.
 */

public class ReviewInfo {

    private String name;
    private HashMap<String, Object> reviewDetail;

    /**
     * Four piece of information were stored in each reviewDetail:
     * comment (String)
     * assignmentDifficult (Long)
     * lectureQuality (Long)
     * overallQuality (Long)
     * @param name revierwer's name, used for development purpose only
     * @param reviewDetail as described above
     */
    public ReviewInfo(String name, HashMap<String, Object> reviewDetail) {
        this.name = name;
        this.reviewDetail = reviewDetail;
    }

    /**
     * This is ony used for debugging for self development
     * because all reviews remain anonymous, we don't show reviewer's name
     * @return student's name in this review
     */
    public String getName() {
        return name;
    }

    public String getComment() {
        return (String) reviewDetail.get("comment");
    }

    public Long getAssignmentDifficulty() {
        return (Long) reviewDetail.get("assignmentDifficulty");
    }

    public Long getLectureQuality() {
        return (Long) reviewDetail.get("lectureQuality");
    }

    public Long getOverallQuality() {
        return (Long) reviewDetail.get("overallQuality");
    }

    public Long getTotalLikes() { return (Long) reviewDetail.get("likes"); }

}
