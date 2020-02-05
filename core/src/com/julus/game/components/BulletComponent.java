package com.julus.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

public class BulletComponent implements Component {
    public btMotionState motionState;
    public btCollisionObject body;
    public btRigidBody.btRigidBodyConstructionInfo bodyInfo;
}
