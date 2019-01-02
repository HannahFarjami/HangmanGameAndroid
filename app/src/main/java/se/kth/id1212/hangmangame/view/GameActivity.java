package se.kth.id1212.hangmangame.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import common.Response;
import se.kth.id1212.hangmangame.R;
import se.kth.id1212.hangmangame.net.ConnectionHandler;
import se.kth.id1212.hangmangame.net.IGameObserver;

/**
 * The Activity that handle the actual game  regarding input/output from/to user.
 * Also takes care of the connection.
 */
public class GameActivity extends FragmentActivity implements IGameObserver {

    ConnectionHandler connectionHandler;
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    /**
     * Creates the game activity and starts the connection with the server
     * @param savedInstanceState
     */
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

    /**
     * This method is called when the server sends a response with a changed game state
     *
     * @param response is the game state sent by the server
     */
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
                        stringBuilder.append("_ ");
                    else
                        stringBuilder.append(letter +" ");
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
        if(guess.length()==0){
            showToast("Guess must be a letter or a word");
            return;
        }
        new SendGuess().execute(guess.getText().toString());
    }

    public void quitGame(View v){
        connectionHandler.quitGame();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    /**
     * Private inner class that are responsible to call the connectionHandler on a new thread to
     * start a new connection with the server.
     * If an error is thrown then call the connectionError method.
     * Sends
     */
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

    /**
     * If there is an error trhown when trying to connect, give the user a message and
     * go back to the main screen.
     */
    public void connectionError(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("No connection could be established to the game server");
            }
        });
        Intent i = new Intent(GameActivity.this, MainActivity.class);
        finish();
        startActivity(i);
    }


    private void showToast(String message){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}

