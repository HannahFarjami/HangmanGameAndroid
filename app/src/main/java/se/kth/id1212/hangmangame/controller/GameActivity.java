package se.kth.id1212.hangmangame.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import common.Response;
import se.kth.id1212.hangmangame.R;
import se.kth.id1212.hangmangame.net.ConnectionHandler;
import se.kth.id1212.hangmangame.net.IGameObserver;

public class GameActivity extends FragmentActivity implements IGameObserver {

    ConnectionHandler connectionHandler;
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        String serverIP = getIntent().getStringExtra("SERVER_IP");
        String port = getIntent().getStringExtra("SERVER_PORT");
        connectionHandler = new ConnectionHandler();
        connectionHandler.setGameObserver(this);
        new ConnectToGameServer().execute(serverIP,port);
    }

    @Override
    public void gameChanges(final Response response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                char[] word = response.getTheWordSoFar();
                LinearLayout lin = (LinearLayout) findViewById(R.id.linearLayout);
                TextView attemptsLeft = (TextView) findViewById(R.id.attemptsLeft);
                TextView totalScore = (TextView) findViewById(R.id.totalScore);
                attemptsLeft.setText(response.getAttemptsLeft()+"");
                totalScore.setText(response.getTotalScore()+"");
                lin.removeAllViews();
                StringBuilder stringBuilder = new StringBuilder();
                for(char letter:word){
                    if(letter==0)
                        stringBuilder.append("_");
                    else
                        stringBuilder.append(letter);
                }
                TextView myText = new TextView(getApplicationContext());
                myText.setText(stringBuilder.toString());
                myText.setTextSize(50);
                lin.addView(myText);

                if (response.isDone()) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    MenuFragment fragment = new MenuFragment();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.menuContainer, fragment);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("RESPONSE",response);
                    fragment.setArguments(bundle);
                    transaction.commit();
                }

            }
        });
    }



    public void sendGuess(View v){
        EditText guess = (EditText)findViewById(R.id.guess);
        new SendGuess().execute(guess.getText().toString());
    }

    public void quitGame(View v){
        connectionHandler.quitGame();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private class ConnectToGameServer extends AsyncTask<String,Void,Void>{

        private Exception e = null;
        @Override
        protected Void doInBackground(String... strings) {
            try{
                connectionHandler.connect(strings[0],Integer.parseInt(strings[1]));
            }catch (IOException e){
                this.e = e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(e!=null){
                connectionError();
            }else{
                FragmentManager fragmentManager = getSupportFragmentManager();
                MenuFragment fragment = new MenuFragment();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.commit();
            }
        }
    }


    private class SendGuess extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            connectionHandler.sendGuess(strings[0]);
            return null;
        }
    }

    public void connectionError(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                CharSequence text = "No connection could be established to the game server";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
        Intent i = new Intent(GameActivity.this, MainActivity.class);
        finish();
        startActivity(i);
    }
}

