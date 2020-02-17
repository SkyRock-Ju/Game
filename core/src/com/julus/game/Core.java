package com.julus.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.julus.game.multiplayer.MultiPlayer;
import com.julus.game.screens.MainMenuScreen;

public class Core extends ApplicationAdapter {

    public static final float VIRTUAL_WIDTH = 960;
    public static final float VIRTUAL_HEIGHT = 540;
    private Screen screen;
    private MultiPlayer multiPlayer;

    @Override
    public void create() {
        new Assets();
        Gdx.input.setCatchBackKey(true);
        setScreen(new MainMenuScreen(this));
        new Settings().load();
        multiPlayer = new MultiPlayer();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        screen.render(Gdx.graphics.getDeltaTime());
        multiPlayer.updateServer(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        screen.resize(width, height);
    }

    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.hide();
            this.screen.dispose();
        }
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void dispose() {
        Assets.dispose();
        Settings.save();
    }
}
