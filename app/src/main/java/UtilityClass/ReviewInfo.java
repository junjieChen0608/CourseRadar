package UtilityClass;

import java.util.HashMap;

/**
 * Created by yang on 10/5/17.
 */

public class ReviewInfo {
    private String name;
    private HashMap<String, Object> reviewDetail;

    public ReviewInfo(String name, HashMap<String, Object> reviewDetail) {
        this.name = name;
        this.reviewDetail = reviewDetail;
    }

    // TODO:: change get review, and add other getters
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




}
