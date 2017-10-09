package UtilityClass;

import java.util.HashMap;

/**
 * Created by chandx on 9/19/17.
 */

public class instructorData {
    private HashMap<String, HashMap<String, Long>> courses;
    private String email;
    private String firstName;
    private String lastName;
    private HashMap<String, HashMap<String, String>> reviews;

    public instructorData(){}

    public instructorData(HashMap<String, HashMap<String, Long>> remoteCourses, String remoteEmail, String remoteFirstName, String remoteLastName, HashMap<String, HashMap<String, String>> remoteReviews){
        courses= remoteCourses;
        email= remoteEmail;
        firstName= remoteFirstName;
        lastName= remoteLastName;
        reviews= remoteReviews;
    }

    @Override
    public String toString() {
        StringBuilder courseinfo= new StringBuilder();
        courseinfo.append("name: ").append(firstName).append(" ").append(lastName).append("\n");
        courseinfo.append("email: ").append(email).append("\n").append("course info").append("\n");

        for (String course: courses.keySet()){
            courseinfo.append("\t").append(course).append('\n');
            HashMap<String, Long> qualityList= courses.get(course);
            for(String singleQuality: qualityList.keySet()){
                courseinfo.append("\t\t").append(singleQuality).append(":\t").append(qualityList.get(singleQuality)).append("\n");
            }
        }

        courseinfo.append("reviews").append("\n");
        for (String course: reviews.keySet()){
            courseinfo.append("\t").append(course).append('\n');
            HashMap<String, String> reviewList= reviews.get(course);
            for(String reviewer: reviewList.keySet()){
                courseinfo.append("\t\t").append(reviewer).append(":\t").append(reviewList.get(reviewer)).append("\n");
            }
        }
        return courseinfo.toString();
    }


    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }
}
