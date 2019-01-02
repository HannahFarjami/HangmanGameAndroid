package se.kth.id1212.hangmangame.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import se.kth.id1212.hangmangame.R;


/**
 * First view when app starts, user puts in server ip and server port.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void newGame(View v){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            System.out.println(task.getResult().getToken());
                        }
                        else System.out.println("Failed to generate token!");
                    }
                });

        EditText port = (EditText)findViewById(R.id.port);
        EditText serverIP = (EditText)findViewById(R.id.serverIP);
        Intent i = new Intent(MainActivity.this, GameActivity.class);
        i.putExtra("SERVER_IP",serverIP.getText().toString());
        i.putExtra("SERVER_PORT",port.getText().toString());
        startActivity(i);
    }
}
