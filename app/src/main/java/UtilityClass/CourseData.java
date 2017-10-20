package UtilityClass;

import java.util.HashMap;

/**
 * Created by junjie on 9/12/17.
 */

public class CourseData {
    private String credit;
    private HashMap<String, String> instructor;

    public CourseData(){};

    public CourseData(String credit, HashMap<String, String> instructor){
        this.credit = credit;
        this.instructor = instructor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Credit: ").append(this.credit).append("\n");
        for(String instructor : this.instructor.keySet()){
            sb.append(instructor).append(": ").append(this.instructor.get(instructor)).append("\n");
        }
        return sb.toString();
    };

    public HashMap<String, String> getInstructor(){
        return instructor;
    }

    public String getCredit(){
        return credit;
    }
}
