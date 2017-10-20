package UtilityClass;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by yang on 10/5/17.
 * custom data type to represent each user's review on certain instructor
 * it is fetched from "ratings" database
 */

public class ReviewInfo implements Parcelable{

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

    protected ReviewInfo(Parcel in) {
        name = in.readString();
    }

    public static final Creator<ReviewInfo> CREATOR = new Creator<ReviewInfo>() {
        @Override
        public ReviewInfo createFromParcel(Parcel in) {
            return new ReviewInfo(in);
        }

        @Override
        public ReviewInfo[] newArray(int size) {
            return new ReviewInfo[size];
        }
    };

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }
}
