package cse442.courseradar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference firebaseDB;
    private Button btnGetData;
    private TextView tvCourseData;
    private EditText etChildPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDB = FirebaseDatabase.getInstance().getReference();
        btnGetData = (Button) findViewById(R.id.get_data);
        btnGetData.setOnClickListener(this);
        tvCourseData = (TextView) findViewById(R.id.course_data);
        etChildPath = (EditText) findViewById(R.id.child_path);
    }

    @Override
    public void onClick(View view) {
        if(view == btnGetData){
            String input = etChildPath.getText().toString().replace(" ", "").toUpperCase();
            Log.d("TEST", "child path: " + input);
            String dept = input.replaceAll("\\d", "");
            String composedPath = dept + "/" + input;
            if(!input.isEmpty()){
                firebaseDB.child(composedPath).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        courseData data = dataSnapshot.getValue(courseData.class);
                        if(data != null){
                            tvCourseData.setText(data.toString());
                        }else{
                            Toast.makeText(MainActivity.this, "Please make sure you input correct course number", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }else{
                Toast.makeText(this, "Please specify query path", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
