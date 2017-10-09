package UtilityClass;

import java.util.HashMap;

/**
 * Created by yang on 10/2/17.
 */

public class InstructorInfo {
    private String name;
    private String email;
    private HashMap<String, Long> totalRatingInfo;

    public InstructorInfo(String name, String email, HashMap<String, Long> totalRatingInfo) {
        this.name = name;
        this.email = email;
        this.totalRatingInfo = totalRatingInfo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getTotalRatings() {
        return totalRatingInfo.get("totalRatings");
    }

    public Long getOverallQuality() {
        return totalRatingInfo.get("overallQuality");
    }


}
