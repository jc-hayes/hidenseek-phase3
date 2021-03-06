package com.cascadia.hidenseek.active;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import com.cascadia.hidenseek.utilities.ConnectionChecks;
import com.cascadia.hidenseek.utilities.LoginManager;
import com.cascadia.hidenseek.model.Match;
import com.cascadia.hidenseek.model.Player;
import com.cascadia.hidenseek.model.PlayerList;
import com.cascadia.hidenseek.network.GetMatchRequest;
import com.cascadia.hidenseek.network.GetPlayerListRequest;
import com.cascadia.hidenseek.network.PutStopRequest;

import java.util.Date;
import java.util.Hashtable;

/**
 * Created by deb on 11/7/16.
 *
 * GameTask.java
 * This sets up a Runnable that checks the player list twice a second and has an
 * abstract method to process the results.  The abstract method sends a message to the handler
 * passed at construction to update the GUI.
 */

public abstract class GameTask implements Runnable {

    protected Context context;
    protected Handler handler; // message handler
    protected Match match;
    protected Player player;
    protected final int DELAY = 500; // delay between checks of player status
    // Keep track of the last status for all the players
    protected Hashtable<Integer, Player> players = new Hashtable<>();
    protected ConnectionChecks connectionChecks;

    // Create the GameTask and provide it with a message handler in the
    // GUI task
    public GameTask(Handler handler, Player player, ConnectionChecks connectionChecks) {
        this.handler = handler;
        this.match = player.getAssociatedMatch();
        this.player = player;
        this.connectionChecks = connectionChecks;
    }

    // Run the task
    public void run() {
        // loop until the user has left the game or it is over
        while (true) {
            // Break out of the loop if the match is over or the player is found
            if ((match.getStatus() == Match.Status.Complete) ||
                    (player.getStatus() == Player.Status.Found)) {
                break;
            }
            // Make sure we have internet connection before updating the Match and Players
            if (connectionChecks.isConnected()) {
                // Do request and update values in match. No callback needed.
                GetPlayerListRequest gplRequest = new GetPlayerList();
                gplRequest.doRequest(match);

                GetMatchRequest gmRequest = new GetMatch();

                gmRequest.doRequest(LoginManager.getMatch().getId());
            }
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private class GetPlayerList extends GetPlayerListRequest {

        @Override
        protected void onException(Exception e) {
        }

        @Override
        protected void onComplete(Match newMatch) {
            if (newMatch == null || newMatch.players == null) // make sure the new data is present
                return;;
            processPlayers(newMatch.players);

            if (match.getType() == Match.MatchType.HideNSeek) {
                int numPlayers = newMatch.players.size();

                for (final Player hider : newMatch.players.values()) {
                    if ((hider.getRole() == Player.Role.Seeker) || !hider.isPlaying() ||
                            (hider.getStatus() == Player.Status.Found)) {
                        numPlayers--;
                    }
                }
                if (numPlayers == 0) {
                    (new PutStopRequest()).doRequest(match);
                }
            }
            // Update the status for each player, and the current player
            players.clear();
            for (Player player : newMatch.players.values()) {
                players.put(new Integer(player.getId()), player);
            }
            player = players.get(new Integer(player.getId()));
        }
    }

    private class GetMatch extends GetMatchRequest {
        @Override
        protected void onException(Exception e) {
        }

        @Override
        protected void onComplete(Match matchUpdate) {
            if (matchUpdate == null) return; // make sure the match object is not null

            Match.Status status = matchUpdate.getStatus();
            if ((status == Match.Status.Complete)) {
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                message.obj = match;
                bundle.putString("event", "game-over");
                message.setData(bundle);
                handler.sendMessage(message);
            }
            PlayerList players = match.players;
            match = matchUpdate;
            match.players = players;

            // Check for the end of match
            Date now = match.getTimeStamp();
            Date matchEnd = match.getEndTime();
            if (now.after(matchEnd)) {
                PutStopRequest putStopRequest = new PutStopRequest();
                putStopRequest.doRequest(match);
            }
        }
    }

    // Process the returned player status for the match
    protected abstract void processPlayers(PlayerList players);

    /* Create a message to send to the Active Activity */
    protected Message createMessage(String event, Player hider) {
        Message message = handler.obtainMessage();
        message.obj = match;
        message.arg1 = hider.getId();
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        message.setData(bundle);
        return message;
    }
}
