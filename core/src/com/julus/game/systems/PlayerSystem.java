package com.julus.game.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.julus.game.GameWorld;
import com.julus.game.Settings;
import com.julus.game.UI.GameUI;
import com.julus.game.components.*;
import com.julus.game.managers.ControllerWidget;

public class PlayerSystem extends EntitySystem implements EntityListener, InputProcessor {
    private Entity player;
    private PlayerComponent playerComponent;
    private CharacterComponent characterComponent;
    private ModelComponent modelComponent;
    private GameUI gameUI;
    private final Vector3 tmp = new Vector3();
    public static Camera camera;
    private GameWorld gameWorld;
    Vector3 rayFrom = new Vector3();
    Vector3 rayTo = new Vector3();
    ClosestRayResultCallback rayTestCB;
    public Entity gun, dome;
    private float deltaX;
    private float deltaY;
    private Vector3 translation = new Vector3();
    private Matrix4 ghost = new Matrix4();

    boolean movementIsTouched = false;

    public PlayerSystem(GameWorld gameWorld, GameUI gameUI, Camera camera) {
        this.camera = camera;
        this.gameWorld = gameWorld;
        this.gameUI = gameUI;
        rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
    }

    @Override
    public void addedToEngine(Engine engine) {
        engine.addEntityListener(Family.all(PlayerComponent.class).get(), this);
    }

    @Override
    public void update(float delta) {
        if (player == null) return;
        updateMovement(delta);
        updateStatus();
        checkGameOver();
    }

    private void updateMovement(float delta) {
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            if (!movementIsTouched) {
                deltaX = -Gdx.input.getDeltaX() * 0.1f;
                deltaY = -Gdx.input.getDeltaY() * 0.1f;
            }
        } else {
            deltaX = -Gdx.input.getDeltaX() * 0.1f;
            deltaY = -Gdx.input.getDeltaY() * 0.1f;
        }
        tmp.set(0, 0, 0);
        camera.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);
        tmp.set(0, 0, 0);
        characterComponent.characterDirection.set(-1, 0, 0).rot(modelComponent.instance.transform).nor();
        characterComponent.walkDirection.set(0, 0, 0);
        movementIsTouched = false;
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            if (ControllerWidget.getMovementVector().y > 0) {
                movementIsTouched = true;
                characterComponent.walkDirection.add(camera.direction);
            }
            if (ControllerWidget.getMovementVector().y < 0) {
                movementIsTouched = true;
                characterComponent.walkDirection.sub(camera.direction);
            }
            if (ControllerWidget.getMovementVector().x < 0) {
                movementIsTouched = true;
                tmp.set(camera.direction).crs(camera.up).scl(-1);
            }
            if (ControllerWidget.getMovementVector().x > 0) {
                movementIsTouched = true;
                tmp.set(camera.direction).crs(camera.up);
            }
            characterComponent.walkDirection.add(tmp);
            characterComponent.walkDirection.scl(10f * delta);
            characterComponent.characterController.setWalkDirection(characterComponent.walkDirection);
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.W))
                characterComponent.walkDirection.add(camera.direction);
            if (Gdx.input.isKeyPressed(Input.Keys.S))
                characterComponent.walkDirection.sub(camera.direction);
            if (Gdx.input.isKeyPressed(Input.Keys.A))
                tmp.set(camera.direction).crs(camera.up).scl(-1);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                tmp.set(camera.direction).crs(camera.up);
            }
            characterComponent.walkDirection.add(tmp);
            characterComponent.walkDirection.scl(10f * delta);
            characterComponent.characterController.setWalkDirection(characterComponent.walkDirection);
        }
        ghost.set(0, 0, 0, 0);
        translation.set(0, 0, 0);
        translation = new Vector3();
        characterComponent.ghostObject.getWorldTransform(ghost);   //TODO export this
        ghost.getTranslation(translation);
        modelComponent.instance.transform.set(translation.x, translation.y, translation.z, camera.direction.x, camera.direction.y, camera.direction.z, 0);
        camera.position.set(translation.x, translation.y, translation.z);
        camera.update(true);

        dome.getComponent(ModelComponent.class).instance.transform.setToTranslation(translation.x, translation.y, translation.z);

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            characterComponent.characterController.setJumpSpeed(20);
            characterComponent.characterController.jump();
        }
        if (Gdx.input.justTouched() && !gun.getComponent(AnimationComponent.class).inAction())
            fire();
    }

    private void updateStatus() {
        gameUI.healthWidget.setValue(playerComponent.health);
    }

    private void fire() {
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        rayFrom.set(ray.origin);
        rayTo.set(ray.direction).scl(300f).add(rayFrom);
        rayTestCB.setCollisionObject(null);
        rayTestCB.setClosestHitFraction(1f);
        rayTestCB.setRayFromWorld(rayFrom);
        rayTestCB.setRayToWorld(rayTo);
        gameWorld.bulletSystem.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB);
        if (rayTestCB.hasHit()) {
            final btCollisionObject obj = rayTestCB.getCollisionObject();
            if (((Entity) obj.userData).getComponent(EnemyComponent.class) != null) {
                if (((Entity) obj.userData).getComponent(StatusComponent.class).alive &&
                        ((Entity) obj.userData).getComponent(EnemyComponent.class).health > 0) {
                    ((Entity) obj.userData).getComponent(EnemyComponent.class).hit();
                }
                if (((Entity) obj.userData).getComponent(EnemyComponent.class).health == 0 &&
                        ((Entity) obj.userData).getComponent(StatusComponent.class).alive) {
                    ((Entity) obj.userData).getComponent(StatusComponent.class).setAlive(false);
                    PlayerComponent.score += 1;
                }
                Gdx.input.vibrate(50);
            }
        }
        gun.getComponent(AnimationComponent.class).animate("Take 001", 1, 1);
    }

    private void checkGameOver() {
        if (playerComponent.health <= 0 && !Settings.Paused) {
            Settings.Paused = true;
            gameUI.gameOverWidget.gameOver();
        }
    }

    @Override
    public void entityAdded(Entity entity) {
        player = entity;
        playerComponent = entity.getComponent(PlayerComponent.class);
        characterComponent = entity.getComponent(CharacterComponent.class);
        modelComponent = entity.getComponent(ModelComponent.class);
        //
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}