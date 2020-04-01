package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

import javax.swing.DesktopManager;

public class MyGdxGame extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Texture anvilImage;
	private Sound dropSound;
	private Sound loseSound;
	private Music rainMusic;
	private OrthographicCamera camera; // захватывает по размеру экрана
	private SpriteBatch batch; // отвечает за отрисовку изображений на экране
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private Array<Rectangle> anvils;
	private long lastDropTime;
	private long lastAnvilTime;


	@Override
	public void create() {
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		anvilImage = new Texture(Gdx.files.internal("anvil.png"));

		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		loseSound = Gdx.audio.newSound(Gdx.files.internal("you_lose.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		rainMusic.setLooping(true);
		rainMusic.play();

		camera  = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480); // это для desktop версии

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();

		anvils = new Array<Rectangle>();
		spawnAnvil();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop); // добавляем параметры новой капли в массив
		lastDropTime = TimeUtils.nanoTime();
	}

	private void spawnAnvil() {
		Rectangle anvil = new Rectangle();
		anvil.x = MathUtils.random(0, 800-64);
		anvil.y = 480;
		anvil.width = 64;
		anvil.height = 64;
		anvils.add(anvil); // добавляем параметры новой наковальни в массив
		lastAnvilTime = TimeUtils.nanoTime();
	}


	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		// отрисовка корзины
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);

		// отрисовка капелек. перебираем массив raindrops и каждый объект из него
		// записываем в переменную raindrop
		for (Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);

		}
		// отрисовка наковален. перебираем массив anvils и каждый объект из него
		// записываем в переменную anvil
		for (Rectangle anvil: anvils) {
			batch.draw(anvilImage, anvil.x, anvil.y);
		}
		batch.end();


		// перемещение корзины при касании пальцем и мышкой
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0); // куда мы ткнули
			camera.unproject(touchPos); // считывает нажатие на смартфоне
			bucket.x = touchPos.x - 64 / 2; // середина корзины будет у курсора/пальца
		}


		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		}

		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			bucket.x += 200 * Gdx.graphics.getDeltaTime();
		}

		if(bucket.x < 0 ) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800-64;

		// если с последнего падения капли прошло 1000000000 нано секунд,
		// то будет создана новая капля
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) {
			spawnRaindrop();
		}

		if(TimeUtils.nanoTime() - lastAnvilTime > 4000000000L) {
			spawnAnvil();
		}

		// создаем цикл для итерирования капель, т.е. будем отслеживать каждую созданную
		// каплю, и заставлять ее падать вниз, в случае падения в ведро или за пределы
		// экрана мы будем эту каплю удалять и при попадании в ведро проигрывать звук
		for(Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop_iter = iter.next();
			raindrop_iter.y -= 400 * Gdx.graphics.getDeltaTime();
			if(raindrop_iter.y + 64 < 0) iter.remove();
			if(raindrop_iter.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}

		// создаем цикл для итерирования наковален, т.е. будем отслеживать каждую созданную
		// наковальню, и заставлять ее падать вниз, в случае падения в ведро или за пределы
		// экрана мы будем эту наковальню удалять и при попадании в ведро проигрывать звук
		for(Iterator<Rectangle> iter = anvils.iterator(); iter.hasNext(); ) {
			Rectangle anvil_iter = iter.next();
			anvil_iter.y -= 400 * Gdx.graphics.getDeltaTime();
			if (anvil_iter.y + 64 < 0) iter.remove();
			if (anvil_iter.overlaps(bucket)) {
				loseSound.play();
				iter.remove();
				bucketImage.dispose();
				bucket.setPosition(1000, 10000000);
                rainMusic.dispose();
			}
		}
	}

	// dispose() - позволяет удалить неиспользуемые в игре объекты/ресурсы
	@Override
	public void dispose() {
        batch.dispose();
		dropImage.dispose();
		anvilImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();


	}
}

