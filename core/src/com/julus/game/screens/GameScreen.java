package com.julus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.julus.game.Core;
import com.julus.game.GameWorld;
import com.julus.game.Settings;
import com.julus.game.UI.GameUI;

public class GameScreen implements Screen {
    Core game;
    GameUI gameUI;
    GameWorld gameWorld;

    public GameScreen(Core game) {
        this.game = game;
        gameUI = new GameUI(game);
        gameWorld = new GameWorld(gameUI);
        Settings.Paused = false;
        Gdx.input.setInputProcessor(gameUI.stage);
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        /** Updates */
        gameUI.update(delta);
        /** Draw */
        gameWorld.render(delta);
        gameUI.render();
    }

    @Override
    public void resize(int width, int height) {
        gameUI.resize(width, height);
        gameWorld.resize(width, height);
    }

    @Override
    public void dispose() {
        gameWorld.dispose();
        gameUI.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}