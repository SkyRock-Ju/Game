package com.julus.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;


public class AnimationComponent implements Component {
    private AnimationController animationController;
    private boolean inAction = false;
    private int counter = 30;

    public AnimationComponent(ModelInstance instance) {
        animationController = new AnimationController(instance);
        animationController.allowSameAnimation = true;
    }

    public void animate(final String id, final int loops, final int speed) {
        animationController.animate(id, loops, speed, null, 0);
        inAction = true;
    }

    public void animate(String id, float offset, float duration, int loopCount, int speed) {
        animationController.animate(id, offset, duration, loopCount, speed, null, 0);
    }

    public void update(float delta) {
        animationController.update(delta);
        if (counter == 0) {
            counter = 30;
            inAction = false;
        }
        counter--;

    }

    public boolean inAction () {
        return inAction;
    }
}