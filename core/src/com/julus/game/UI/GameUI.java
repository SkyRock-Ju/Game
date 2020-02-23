package com.julus.game.UI;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.julus.game.Assets;
import com.julus.game.Core;
import com.julus.game.managers.ControllerWidget;

public class GameUI {
    private Core game;
    public Stage stage;
    public HealthWidget healthWidget;
    private ScoreWidget scoreWidget;
    private CrosshairWidget crosshairWidget;
    public GameOverWidget gameOverWidget;
    private Label fpsLabel;
    private ControllerWidget controllerWidget;

    public GameUI(Core game) {
        this.game = game;
        stage = new Stage(new FitViewport(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
        setWidgets();
        configureWidgets();
    }

    public void setWidgets() {
        healthWidget = new HealthWidget();
        scoreWidget = new ScoreWidget();
        gameOverWidget = new GameOverWidget(game, stage);
        crosshairWidget = new CrosshairWidget();
        fpsLabel = new Label("", Assets.skin);
        if (Gdx.app.getType() == Application.ApplicationType.Android) controllerWidget = new ControllerWidget();
    }

    public void configureWidgets() {
        healthWidget.setSize(140, 25);
        healthWidget.setPosition(Core.VIRTUAL_WIDTH / 2 - healthWidget.getWidth() / 2, 0);
        scoreWidget.setSize(140, 25);
        scoreWidget.setPosition(0, Core.VIRTUAL_HEIGHT - scoreWidget.getHeight());
        gameOverWidget.setSize(280, 100);
        gameOverWidget.setPosition(Core.VIRTUAL_WIDTH / 2 - 280 / 2, Core.VIRTUAL_HEIGHT / 2);
        crosshairWidget.setPosition(Core.VIRTUAL_WIDTH / 2 - 16, Core.VIRTUAL_HEIGHT / 2 - 16);
        crosshairWidget.setSize(32, 32);

        fpsLabel.setPosition(0, 10);

        stage.addActor(healthWidget);
        stage.addActor(scoreWidget);
        stage.addActor(crosshairWidget);
        stage.addActor(fpsLabel);
        if (Gdx.app.getType() == Application.ApplicationType.Android) controllerWidget.addToStage(stage);
    }

    public void update(float delta) {
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        stage.act(delta);
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    public void dispose() {
        stage.dispose();
    }
}