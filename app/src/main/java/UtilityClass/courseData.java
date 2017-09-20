package UtilityClass;

import java.util.HashMap;

/**
 * Created by junjie on 9/12/17.
 */

public class courseData {
    public String credit;
    public HashMap<String, Object> instructors;

    public courseData(){};

    public courseData(String credit, HashMap<String, Object> instructors){
        this.credit = credit;
        this.instructors = instructors;
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Credit: ").append(this.credit).append("\n");
        for(String instructor : instructors.keySet()){
            sb.append(instructor).append(": ").append(instructors.get(instructor)).append("\n");
        }
        return sb.toString();
    }
}
