package com.julus.game.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.julus.game.GameWorld;
import com.julus.game.components.CharacterComponent;
import com.julus.game.components.ModelComponent;
import com.julus.game.components.PlayerComponent;
import com.julus.game.managers.EntityFactory;
import com.julus.game.multiplayer.MultiPlayer;

import java.util.ArrayList;

public class CoopPlayerSystem extends EntitySystem implements EntityListener {

    private ImmutableArray<Entity> entities;
    private Entity coopPlayer;
    private Engine engine;
    private GameWorld gameWorld;
    private ArrayList<String> playersID = new ArrayList<>();

    public CoopPlayerSystem(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    @Override
    public void addedToEngine(Engine e) {
        entities = e.getEntitiesFor(Family.one(CharacterComponent.class).get());
        e.addEntityListener(Family.one(PlayerComponent.class).get(), this);
        this.engine = e;
    }

    @Override
    public void update(float delta) {
        if (!MultiPlayer.friendlyPlayers.isEmpty()) {
                if (playersID.contains(MultiPlayer.friendlyPlayers.keySet().toArray()[0])) {
                    coopPlayer = EntityFactory.createCoopPlayer(gameWorld.bulletSystem);
                    engine.addEntity(coopPlayer);
                    playersID.add((String) MultiPlayer.friendlyPlayers.keySet().toArray()[0]);
                    System.out.println("spawned");
                    Vector3 vector3 = (Vector3) MultiPlayer.friendlyPlayers.values().toArray()[0];
                    System.out.println("x= " + vector3.x + " y= " + vector3.y + " z= " + vector3.z);
                    System.out.println(MultiPlayer.friendlyPlayers.keySet().toArray()[0]);
                }
                coopPlayer.getComponent(ModelComponent.class).instance.transform.setTranslation((Vector3)MultiPlayer.friendlyPlayers.values().toArray()[0]);
            }
    }

    @Override
    public void entityAdded(Entity entity) {
        coopPlayer = entity;
    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
