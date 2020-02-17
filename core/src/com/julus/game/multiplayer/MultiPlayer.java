package com.julus.game.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.julus.game.systems.PlayerSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiPlayer {

    private Socket socket;
    private String id;
    public static HashMap<String, Vector3> friendlyPlayers;
    private final float UPDATE_TIME = 1 / 20f;
    private float timer;

    public MultiPlayer() {
        connectSocket();
        friendlyPlayers = new HashMap<>();
        configSocketEvents();
    }

    private void connectSocket() {
        try {
            socket = IO.socket("http://192.168.0.104:8080");
            socket.connect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    id = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerID = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player Connect: " + playerID);
                    friendlyPlayers.put(playerID, new Vector3(0, 5, 0));

                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting New PlayerID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    id = data.getString("id");
                    friendlyPlayers.remove(id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected PlayerID");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerID = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Double z = data.getDouble("z");
                    if (friendlyPlayers.get(playerID) != null) {
                        friendlyPlayers.get(playerID).set(x.floatValue(), y.floatValue(), z.floatValue());
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting Player coordinates");
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray players = (JSONArray) args[0];
                try {
                    for (int i = 0; i < players.length(); i++) {
                        Vector3 coopPlayerPosition = new Vector3();
                        coopPlayerPosition.x = ((Double) players.getJSONObject(i).getDouble("x")).floatValue();
                        coopPlayerPosition.y = ((Double) players.getJSONObject(i).getDouble("y")).floatValue();
                        coopPlayerPosition.z = ((Double) players.getJSONObject(i).getDouble("z")).floatValue();
                        friendlyPlayers.get(players.getJSONObject(i).getString("id")).set(coopPlayerPosition);
                    }
                } catch (JSONException e) {

                }
            }
        });
    }

    public void updateServer(float dt) {
        timer += dt;
        if (timer >= UPDATE_TIME && friendlyPlayers != null && PlayerSystem.characterComponent != null ) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", PlayerSystem.characterComponent.characterDirection.x);
                data.put("y", PlayerSystem.characterComponent.characterDirection.y);
                data.put("z", PlayerSystem.characterComponent.characterDirection.z);
                socket.emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data");
            }
        }
    }
}
