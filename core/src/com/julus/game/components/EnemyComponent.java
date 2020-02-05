package com.julus.game.components;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {


    public int health = 2;

    public enum STATE {
        IDLE,
        FLEEING,
        HUNTING
    }

    public void hit () {
        health--;
    }

    public STATE state = STATE.IDLE;

    public EnemyComponent(STATE state){
        this.state = state;
    }
}
