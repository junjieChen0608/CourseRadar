package UtilityClass;

import java.util.HashMap;

/**
 * Created by yang on 10/2/17.
 */

public class InstructorInfo {
    private String name;
    private String email;
    private HashMap<String, Long> totalRatingInfo;

    /**
     * two pieces of information were stored in each totalRatingInfo
     * totalRatings (Long)
     * overallQuality (Long)
     * @param name instructor's full name
     * @param email instructor's email
     * @param totalRatingInfo as described above
     */
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
