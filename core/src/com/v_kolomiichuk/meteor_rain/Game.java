package com.v_kolomiichuk.meteor_rain;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Game extends ApplicationAdapter {
    OrthographicCamera camera;
    SpriteBatch batch;
    Texture meteorImage, rocketImage, background, play;
    Rectangle meteor, rocket;
    Array<Rectangle> meteors;
    BitmapFont font;

    long lastDropTime, respawnTime;
    int score, maxScore, speed, hp, gameState;

    Preferences prefs;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        meteorImage = new Texture("meteor.png");
        rocketImage = new Texture("rocket.png");
        play = new Texture("play.png");
        background = new Texture("background.jpg");
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 800);
        prefs = Gdx.app.getPreferences("myGame");
        lastDropTime = TimeUtils.nanoTime();
        respawnTime = 1000000000;
        speed = 100;
        hp = 3;
        gameState = 0;

        rocket = new Rectangle();
        rocket.width = 64;
        rocket.height = 88;
        rocket.x = (Gdx.input.getX() / 2) - (rocket.width / 2);
        rocket.y = 0;

        meteors = new Array<>();
        spawnMeteors();
    }

    private void spawnMeteors() {
        meteor = new Rectangle();
        meteor.x = MathUtils.random(0, 480 - 64);
        meteor.y = 800;
        meteor.width = 64;
        meteor.height = 64;
        meteors.add(meteor);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, 480, 800);

        if (gameState == 0) {
            batch.draw(play, 113, 350);
            if (Gdx.input.justTouched())
                gameState = 1;

        } else if (gameState == 1) {
            play();
            if (score > maxScore){
                prefs.putInteger("score", score);
                prefs.flush();}
        } else if (gameState == 2) {
            maxScore = prefs.getInteger("score", 0);

            font.draw(batch, "Your score: " + score, 150, 450);
            font.draw(batch, "Max score: " + maxScore, 150, 400);
            font.draw(batch, "Tap to restart", 150, 350);

            if (Gdx.input.justTouched()) {
                gameState = 1;
                hp = 3;
                speed = 100;
                score = 0;
                respawnTime = 1000000000;
                play();
            }

        }

        batch.end();

        checkSpeed(score);

        if (hp < 1)
            gameState = 2;
    }

    private void checkSpeed(int score) {
        if (score > 10 && score < 20){
            speed = 150;
            respawnTime = 800000000;
        }else if (score >= 20 && score < 30){
            speed = 200;
            respawnTime = 600000000;
        }else if(score >= 30 && score < 50){
            speed = 250;
            respawnTime = 400000000;
        }else if(score >= 50 && score < 100){
            speed = 350;
            respawnTime = 300000000;
        }
    }

    private void play() {
        batch.draw(rocketImage,
                rocket.x,
                rocket.y,

                0,
                0,

                64,
                88,

                1f,
                1f,

                0,

                0,
                0,
                64,
                88,
                false,
                false);

        for (Rectangle meteor : meteors) {
            batch.draw(meteorImage, meteor.x, meteor.y);
            meteor.y -= speed * Gdx.graphics.getDeltaTime();
        }

        font.draw(batch, "Score: " + score, 0, 790);
        font.draw(batch, "HP: " + hp, 400, 790);

        if (TimeUtils.nanoTime() - lastDropTime > respawnTime)
            spawnMeteors();

        Iterator<Rectangle> iterator = meteors.iterator();
        while (iterator.hasNext()) {
            Rectangle meteor = iterator.next();
            if (meteor.y + 64 < 0) {
                score++;
                iterator.remove();
            }

            if (meteor.overlaps(rocket)) {
                hp--;
                iterator.remove();
            }
        }

        if (Gdx.input.isTouched()) {
            rocket.x = getX(Gdx.input.getX());
            if (rocket.x < 0)
                rocket.x = 0;
            if (rocket.x > 416)
                rocket.x = 416;
        }
    }


    private float getX(float f) {
        return ((f / Gdx.graphics.getWidth()) * 480f) - 32f;
    }

    @Override
    public void dispose() {
        batch.dispose();
        meteorImage.dispose();
    }
}
