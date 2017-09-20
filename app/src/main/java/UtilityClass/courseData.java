package UtilityClass;

import java.util.HashMap;

/**
 * Created by junjie on 9/12/17.
 */

public class courseData {
    public String credit;
    public HashMap<String, Object> instructor;

    public courseData(){};

    public courseData(String credit, HashMap<String, Object> instructor){
        this.credit = credit;
        this.instructor = instructor;
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Credit: ").append(this.credit).append("\n");
        this.instructor.forEach((instructor, email) -> sb.append(instructor).append(": ").append(email).append("\n"));
        return sb.toString();
    }
}
