package se.kth.id1212.hangmangame.net;

import java.io.Serializable;

import common.Response;

/**
 * Interface for the Observer pattern. Tells the view if changes to the game, or if connection is lost to the server.
 */
public interface IGameObserver extends Serializable {

    void gameChanges(Response response);
}