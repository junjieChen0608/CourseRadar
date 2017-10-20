package UtilityClass;

import java.util.HashMap;

/**
 * Created by wiiSo on 9/30/2017.
 * represent user's rating on certain instructor,
 */

public class CourseRating {
    public long assignmentDifficulty, lectureQuality, overallQuality, totalRatings;

    public CourseRating(){}

    public CourseRating(long assignmentDifficulty, long lectureQuality, long overallQuality, long totalRatings){
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
