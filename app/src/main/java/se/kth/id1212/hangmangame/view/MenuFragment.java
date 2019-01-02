package se.kth.id1212.hangmangame.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import common.Response;
import se.kth.id1212.hangmangame.R;
import se.kth.id1212.hangmangame.net.ConnectionHandler;

/**
 * Fragment that lays ontop on the GameActivity. In the menu the user can start
 * a new game or quit after a game has ended.
 */
public class MenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container,false);
        FrameLayout root = (FrameLayout) getActivity().findViewById(R.id.menuContainer);
        final Button button = (Button) getActivity().findViewById(R.id.button2);
        button.setVisibility(View.GONE);
        final Button quitButton = (Button) getActivity().findViewById(R.id.quitGame);
        quitButton.setVisibility(View.GONE);
        //root.setBackgroundColor(Color.parseColor("#cfe795"));
        Button newGameButton = (Button) view.findViewById(R.id.button3);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendMessage().execute("new");
                button.setVisibility(View.VISIBLE);
                quitButton.setVisibility(View.VISIBLE);
            }
        });
        Button quitGameButton = (Button) view.findViewById(R.id.button4);
        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendMessage().execute("quit");
            }
        });

        if (getArguments()!=null) {
            Response response = (Response) getArguments().getSerializable("RESPONSE");
            TextView textForWinOrLose = (TextView) view.findViewById(R.id.textForWinOrLose);
            TextView score = (TextView) view.findViewById(R.id.score);
            score.setText(response.getTotalScore()+"");
            if(response.getAttemptsLeft()<0){
                textForWinOrLose.setText("Congratulation, you won!\nThe right word was "+new String(response.getTheWordSoFar())+"\nTo play again press: New Game");
            }else{
                textForWinOrLose.setText("You lose!\nTo play again press: New Game");
            }
        }
        return view;
    }


    /**
     * Private inner class that are responsible to send a either a new game message or a quit message.
     * If a new game is choosen, the fragment should be removec. If quit is choosen, then the application
     * should just exit.
     */
    private class SendMessage extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... strings) {
            GameActivity activity = (GameActivity) getActivity();
            ConnectionHandler connectionHandler = activity.getConnectionHandler();

            switch (strings[0]){
                case "new":
                    connectionHandler.newGame();
                    getFragmentManager().beginTransaction().remove(MenuFragment.this).commit();
                    break;
                case "quit":
                    connectionHandler.quitGame();
                    getActivity().moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
            }
            return null;
        }
    }
}
