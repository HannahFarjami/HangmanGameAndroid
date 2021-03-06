package se.kth.id1212.hangmangame.net;



import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import common.Request;
import common.Response;

import static common.RequestType.GUESSLETTER;
import static common.RequestType.GUESSWORD;
import static common.RequestType.NEW_GAME;
import static common.RequestType.QUIT;


/**
 * Class that handle the connection from client to server.
 */
public class ConnectionHandler implements Serializable{

    static final int TIMEOUT = 3000;
    private Socket socket;
    private InputStream fromServer;
    private OutputStream toServer;
    private boolean connected = false;
    private IGameObserver gameObserver;

    public void connect(String host, int port) throws IOException{
        socket = new Socket();
        socket.connect(new InetSocketAddress(host,port),TIMEOUT);
        toServer = socket.getOutputStream();
        fromServer = socket.getInputStream();
        connected = true;
        new Thread(new Listener()).start();
    }

    public void setGameObserver(IGameObserver gameObserver){
        this.gameObserver = gameObserver;
    }

    /**
     * Request the server to set up a knew game
     */
    public void newGame(){
        sendGuess(new Request(NEW_GAME));
    }


    /**
     * Calls the function sendGuess with a guess formated after the decided protocol
     * @param wordToGuess if the user guesses a whole word
     */
    public void sendGuess(String wordToGuess){
        Request request;
        if(wordToGuess.length()>1) {
            request = new Request(GUESSWORD);
            request.setWordToGuess(wordToGuess);
        }else{
            request = new Request(GUESSLETTER);
            request.setLetterToGuess(wordToGuess.charAt(0));
        }
        sendGuess(request);
    }

    /**
     *Send an Request with the type QUIT to tell the server to close its connection to client
     */
    public void quitGame(){
        try {
            sendGuess(new Request(QUIT));
            socket.close();
            connected = false;
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     *
     * Takes the Reequest created in the public functions and sends it to the server.
     * @param request the protocol used for requests
     */
    private void sendGuess(Request request){
        try {
            byte[] temp = calculateAndPrependSizeOfObjectToBeSent(request);
            for (int i = 0;i<temp.length;i++)
                toServer.write(temp[i]);
            toServer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     *
     * This function calculate the size of the Request object and prepend it to an byte array
     * that contains the object itself.
     *
     * @param request the object to be sent
     * @return an array with the object and the length to be sent
     **/
    private byte[] calculateAndPrependSizeOfObjectToBeSent(Request request){
        byte[] objectArray;
        byte[] objectAndLengthArray = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.close();

            objectArray = byteArrayOutputStream.toByteArray();

            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(objectArray.length);

            byte[] byteBufferArray = byteBuffer.array();
            objectAndLengthArray = new byte[byteBufferArray.length+objectArray.length];

            System.arraycopy(byteBufferArray, 0, objectAndLengthArray, 0, byteBufferArray.length);
            System.arraycopy(objectArray,0,objectAndLengthArray,byteBufferArray.length,objectArray.length);

        }catch (IOException e){
            e.printStackTrace();
        }

        return objectAndLengthArray;
    }

    /**
     * Inner class that are listening for communication from the server
     */
    private class Listener implements Runnable{
        boolean run = true;
        ByteBuffer byteBuffer;
        byte[] temp = new byte[4];
        @Override
        public void run() {
            while(run) {
                try {
                    for(int i = 0;i<4;i++){
                        temp[i] = (byte)fromServer.read();
                    }
                    byteBuffer = ByteBuffer.wrap(temp);
                    int size = byteBuffer.getInt(0);
                    byte[] object = new byte[size];
                    for(int i = 0; i<size;i++) {
                        object[i] = (byte) fromServer.read();
                    }
                    Response response = byteArrayToResponseObject(object);
                    gameObserver.gameChanges(response);
                } catch (Exception e) {
                    // gameObserver.connectionLost();
                    run = false;
                }
            }
        }

        private Response byteArrayToResponseObject(byte[] objectByteArray){
            Response response = null;
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectByteArray);
                ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
                response = (Response) in.readObject();
            } catch (IOException e){
                e.printStackTrace();
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            return response;
        }
    }
}
