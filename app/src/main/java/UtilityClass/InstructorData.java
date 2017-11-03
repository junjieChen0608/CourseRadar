package UtilityClass;

import java.util.HashMap;

/**
 * Created by dixin on 11/3/17.
 */

public class InstructorData {
    public HashMap<String, HashMap<String, Long>> courses;
    public String email;
    public String firstName;
    public String lastName;
    public HashMap<String, HashMap<String, String>> reviews;

    public InstructorData(){}

    public InstructorData(HashMap<String, HashMap<String, Long>> remoteCourses,
                          String remoteEmail, String remoteFirstName, String remoteLastName,
                          HashMap<String, HashMap<String, String>> remoteReviews){
        courses= remoteCourses;
        email= remoteEmail;
        firstName= remoteFirstName;
        lastName= remoteLastName;
        reviews= remoteReviews;
    }

    @Override
    public String toString() {
        StringBuilder courseinfo= new StringBuilder();
        courseinfo.append("email: ").append(email).append("\n").append("course info").append("\n");
        /*for (String ckey: courses.keySet()){
            courseinfo.append("\t").append(ckey).append('\n');
            HashMap<String, String> temp= courses.get(ckey);
            for(String nkey: temp.keySet()){
                courseinfo.append("\t\t").append(nkey).append(":\t").append(temp.get(nkey)).append("\n");
            }
        }
        courseinfo.append("reviews").append("\n");
        for (String ckey: reviews.keySet()){
            courseinfo.append("\t").append(ckey).append('\n');
            HashMap<String, String> temp= reviews.get(ckey);
            for(String nkey: temp.keySet()){
                courseinfo.append("\t\t").append(nkey).append(":\t").append(temp.get(nkey)).append("\n");
            }
        }*/
        return courseinfo.toString();
    }
}
