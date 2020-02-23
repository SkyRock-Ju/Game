package com.julus.game.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.julus.game.systems.PlayerSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiPlayer {

    private Socket socket;
    private static String myId;
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
            if (!socket.connected()) {
                System.out.println("not connected!");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
                    myId = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID: " + myId);
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
                    if (!playerID.equals(myId)) {
                        friendlyPlayers.put(playerID, new Vector3(0, 5, 0));
                    }

                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting New PlayerID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    myId = data.getString("id");
                    friendlyPlayers.remove(myId);
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
                    if (!playerID.equals(myId) && friendlyPlayers.get(playerID) != null) {
                        Double x = data.getDouble("x");
                        Double y = data.getDouble("y");
                        Double z = data.getDouble("z");
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
                        if (friendlyPlayers.get(players.getJSONObject(i).getString("id")) != null) {
                            friendlyPlayers.get(players.getJSONObject(i).getString("id")).set(coopPlayerPosition);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting players array");
                }
            }
        });
    }

    public void updateServer(float dt) {
        timer += dt;
        if (timer >= UPDATE_TIME && friendlyPlayers != null && PlayerSystem.camera != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", PlayerSystem.camera.position.x);
                data.put("y", PlayerSystem.camera.position.y);
                data.put("z", PlayerSystem.camera.position.z);
                System.out.println("" + PlayerSystem.camera.position.x +
                        PlayerSystem.camera.position.y +
                        PlayerSystem.camera.position.z);
                socket.emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data");
            }
        }
    }
}
