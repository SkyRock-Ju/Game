package com.julus.game.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.julus.game.GameWorld;
import com.julus.game.components.CharacterComponent;
import com.julus.game.components.ModelComponent;
import com.julus.game.components.PlayerComponent;
import com.julus.game.managers.EntityFactory;
import com.julus.game.multiplayer.MultiPlayer;

import java.util.Map;

public class CoopPlayerSystem extends EntitySystem implements EntityListener {

    private ImmutableArray<Entity> entities;
    private Entity coopPlayer;
    private Engine engine;
    private GameWorld gameWorld;
    private Matrix4 ghost = new Matrix4();
    private Vector3 translation = new Vector3();
    ComponentMapper<CharacterComponent> characterComponentCM = ComponentMapper.getFor(CharacterComponent.class);

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
        Entity entity = entities.get(0);
        if (MultiPlayer.friendlyPlayers != null) {
            for (Map.Entry<String, Vector3> coopPlayersPosition : MultiPlayer.friendlyPlayers.entrySet()) {
                spawnCoopPlayer();
                System.out.println("spawned");
                ModelComponent modelComponent = entities.get(0).getComponent(ModelComponent.class);

                characterComponentCM.get(entity).characterDirection.set(-1, 0, 0).rot(modelComponent.instance.transform);
                characterComponentCM.get(entity).walkDirection.set(0, 0, 0);
                characterComponentCM.get(entity).walkDirection.add(coopPlayersPosition.getValue());
                characterComponentCM.get(entity).walkDirection.scl((10f) * delta);
//            characterComponentCM.get(entity).characterController.setWalkDirection(characterComponentCM.get(entity).walkDirection);

                ghost.set(0, 0, 0, 0);
                translation.set(0, 0, 0);
                characterComponentCM.get(entity).ghostObject.getWorldTransform(ghost);
                ghost.getTranslation(translation);

                modelComponent.instance.transform.setTranslation(coopPlayersPosition.getValue());
            }
        }
    }

    @Override
    public void entityAdded(Entity entity) {
        coopPlayer = entity;
    }

    @Override
    public void entityRemoved(Entity entity) {
    }


    private void spawnCoopPlayer() {
        engine.addEntity(EntityFactory.createCoopPlayer(gameWorld.bulletSystem));
    }
}
