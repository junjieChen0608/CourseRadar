package UtilityClass;

import java.util.HashMap;

/**
 * Created by wiiSo on 9/30/2017.
 */

public class CourseRating {
    public int assignmentDifficulty, lectureQuality, overallQuality, totalRatings;

    public CourseRating(){}

    public CourseRating(int assignmentDifficulty, int lectureQuality, int overallQuality, int totalRatings){
        this.assignmentDifficulty = assignmentDifficulty;
        this.lectureQuality = lectureQuality;
        this.overallQuality = overallQuality;
        this.totalRatings = totalRatings;
    }

    public HashMap<String, Object> toMap(){
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("assignmentDifficulty", assignmentDifficulty);
        ret.put("lectureQuality", lectureQuality);
        ret.put("overallQuality", overallQuality);
        ret.put("totalRatings", totalRatings);
        return ret;
    }
}
