package com.gaurav.hangdroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.StringUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class HangdroidActivity extends BaseGameActivity {

	public static String TAG = "HangDroidActivity";
	private static final short CAMERA_WIDTH = 480;
	private static final short CAMERA_HEIGHT = 800;
	private RepeatingSpriteBackground mBackground;
	private SmoothCamera mCamera;
	public static int dropcount = 0;
	Scene mScene;
	TextureRegion mDropRegion, mBlankRegion;
	ChangeableText mScoretext;
	public Font mScoreFont;
	private Sound bubblePop, victoryClaps, bazinga, defeatBoos;
	PhysicsWorld mPhysicsworld;
	boolean boo = true, victory = false;
	DropsManager allDrops;
	StringLoader mStringPool;
	String mWord;
	Sprite[] mBlanksprite;
	static int blankCount = 0;
	int chances, threshold;
	BitmapTextureAtlas gameAtlas;
	private BitmapTextureAtlas mScoreTextureAtlas;

	public void initWalls() {
		/*
		 * This function loads invisible walls on the 4 sides of the scree.Helps
		 * to set the score and hint.
		 */
		FixtureDef mWallDef = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);
		// left,top,right,bottom
		Rectangle wallLeft = new Rectangle(-1, 0, 1f, CAMERA_HEIGHT);
		Rectangle wallRight = new Rectangle(CAMERA_WIDTH, 0, 1f, CAMERA_HEIGHT);
		Rectangle wallTop = new Rectangle(0, -1, CAMERA_WIDTH, 1f);
		Rectangle wallBottom = new Rectangle(0, CAMERA_HEIGHT - 200,
				CAMERA_WIDTH, 1f);

		PhysicsFactory.createBoxBody(mPhysicsworld, wallLeft,
				BodyType.StaticBody, mWallDef).setUserData("wall_left");
		PhysicsFactory.createBoxBody(mPhysicsworld, wallRight,
				BodyType.StaticBody, mWallDef).setUserData("wall_right");
		PhysicsFactory.createBoxBody(mPhysicsworld, wallTop,
				BodyType.StaticBody, mWallDef).setUserData("wall_top");
		PhysicsFactory.createBoxBody(mPhysicsworld, wallBottom,
				BodyType.StaticBody, mWallDef).setUserData("wall_bottom");

		wallBottom.setVisible(false);
		mScene.attachChild(wallLeft);
		mScene.attachChild(wallRight);
		mScene.attachChild(wallTop);
		mScene.attachChild(wallBottom);

	}

	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}

	public Engine onLoadEngine() {
		// Andengine Function
		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 200,
				200, 1.0f);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		engineOptions.setNeedsSound(true);
		return new Engine(engineOptions);

	}

	public void onLoadResources() {
		// Andengine Function
		gameAtlas = new BitmapTextureAtlas(2048, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/128x128/");

		FontFactory.setAssetBasePath("font/");

		mBackground = new RepeatingSpriteBackground(CAMERA_WIDTH,
				CAMERA_HEIGHT, this.getEngine().getTextureManager(),
				new AssetBitmapTextureAtlasSource(this, "gfx/bg-body.png"));

		loadChances();// function loads font and texture for remaining chances

		/*
		 * Read the file "words" from res/raw/ ...
		 * the file contains a list of 20 words
		 * currently used in the game.
		 */
		try {
			InputStream wordsFile = this.getResources().openRawResource(
					R.raw.words);
			mStringPool = new StringLoader(wordsFile);
		} catch (Exception e) {
			Debug.d(e.toString());
		}

		/*
		 * Load a Random word from the list of words read from "words"
		 */
		int c;
		Random word = new Random();
		if ((c = word.nextInt(21)) == 0) {
			c = word.nextInt(21);
		}

		mWord = new String(mStringPool.getmStringHolder().get(c));
		chances = mWord.length() + 2;
		mBlanksprite = new Sprite[mWord.length()];
		Debug.d("Random values " + Math.random());

		setThreshold();
		//Threshold is the number of chances after which the user is declared to have lost.
		allDrops = new DropsManager();

		SoundFactory.setAssetBasePath("mfx/");
		try {
			this.bubblePop = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "pop.mp3");
			this.bazinga = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "bazinga.mp3");
			this.victoryClaps = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "cheer.mp3");
			this.defeatBoos = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "boo02.mp3");

		} catch (final IOException e) {
			Debug.e("Error", e);
		}
		this.getEngine().getTextureManager().loadTexture(gameAtlas);
	}

	public void loadChances() {
		this.mScoreTextureAtlas = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mScoreFont = FontFactory.createFromAsset(this.mScoreTextureAtlas,
				this, "LCD.ttf", 32, true, Color.WHITE);
		this.mScoretext = new ChangeableText(0, CAMERA_HEIGHT - 50, mScoreFont,
				"Remaining chances " + chances);
		this.mEngine.getTextureManager().loadTexture(this.mScoreTextureAtlas);
		this.mEngine.getFontManager().loadFont(this.mScoreFont);

	}

	public Scene onLoadScene() {
		//Andengine Function
		mScene = new Scene();
		mPhysicsworld = new PhysicsWorld(new Vector2(0, 0), false);
		mScene.registerUpdateHandler(mPhysicsworld);

		initWalls();

		mScene.setBackground(mBackground);

		createAndDrawBubbles();

		loadBlanks();

		loadHint();
		mScene.attachChild(mScoretext);

		mScene.setTouchAreaBindingEnabled(true);
		mScene.registerUpdateHandler(myGameHandler);

		return mScene;

	}

	int count = 0;

	/*
	 * Function to create and render the bubbles/drops on the screen, positioning them as a grid,
	 * in alphabetical order.
	 */
	public void createAndDrawBubbles() {

		for (float y = 10; y <= 460; y += 90) {
			for (float x = 10; x <= 370; x += 90) {
				if (dropcount < 26) {
					allDrops.add(new Drop(x, y, getTexture(++dropcount), false,
							(char) ((int) 'A' + count), "drop" + (dropcount),
							mScene, dropcount));
					Debug.d("alpha " + (char) ((int) 'A' + count));
					count++;
				}
			}
		}
	}

	float offset = 50;

	public void loadBlanks() {
		/*
		 * Loads the blanks on screen.Depends on the length of the word
		 * that the pool of words throws up.
		 */
		for (blankCount = 0; blankCount < mWord.length(); blankCount++) {
			if (blankCount > 0)
				offset = 90;
			Debug.d("Blanks " + blankCount);
			mBlanksprite[blankCount] = new Sprite(5 + (offset * blankCount),
					CAMERA_HEIGHT - 200, getTexture(30));
			Debug.d("Blanks " + blankCount);
			mBlanksprite[blankCount].setScale(0.6f);
			Debug.d("Blanks " + blankCount);
			mScene.attachChild(mBlanksprite[blankCount]);

		}
	}

	int k = 0, victoryCount = 0;
	Sprite temp;

	public void checkAlphabets(Drop d) {
		/*
		 * This is where the actual checking takes place.At each touch, the no. of available chances is reduced
		 * If the touched alphabet is present in the word, it shows up on screen, otherwise the chances remaining reduce and 
		 * if they go below threshold the player loses."victorycount" is used to determine if the player has been correct
		 * the no. of times equal to word length, which indicates he has won.
		 */
		chances--;
		for (k = 0; k < mWord.length(); k++) {
			if (mWord.charAt(k) == d.getmContainChar()) {
				Debug.d("HAHAHA " + (mWord.charAt(k) == d.getmContainChar())
						+ " " + chances);
				victoryCount++;
				d.setTouched(false);

				temp = new Sprite(mBlanksprite[k].getX(),
						mBlanksprite[k].getY(), getTexture(d.getId()));
				temp.setScale(0.6f);
				mScene.attachChild(temp);
				this.bubblePop.play();

				boo = false;

				if (victoryCount == mWord.length())
					victory = true;
				callToast();
			}

		}
	}

	public void callToast() {
		/*
		 * Checking here if the user is to be declared ans victorious or lost.
		 * */
		
		if (victory) {
			Debug.d("Entered Toast Victory");
			this.victoryClaps.play();
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent goSplash = new Intent(this, VictorySplash.class);
			startActivity(goSplash);
			this.finish();

		} else if (chances < threshold && !victory) {
			//If chances remaining go below threshold, and the user has not won, chances are he wont get it right anymore
			//hence declare the game as over.
			Debug.d("Entered Toast Defeat");
			this.defeatBoos.play();
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent goSplash = new Intent(this, GameOver.class);
			startActivity(goSplash);
			this.finish();
			this.onDestroy();
		}
	}

	public void makeThemdisappearonTouch() {
		for (Drop d : allDrops) {
			if (d.isTouched()) {
				/*
				 * This method checks the touched region.It unregisters and removes the drops from the screen.
				 * Also plays corresponding music.
				 */
				mScene.detachChild(d.getmDropSprite());
				mScene.unregisterTouchArea(d.getmDropSprite());
				checkAlphabets(d);
				if (chances < threshold) {
					callToast();
				}

				if (boo) {
					this.bazinga.play();
				}
				d.setTouched(false);
				boo = true;
			}
		}
	}

	public void loadHint() {
		int vowels = 0;
		/*
		 * The best i could find in terms of the hint is the number of vowels present in the word.I have tried
		 * to use only 3,4,5 letter words.So such a vowel-based hint should be enough.
		 */
		for (int k = 0; k < mWord.length(); k++) {
			if (mWord.charAt(k) == 'A' || mWord.charAt(k) == 'E'
					|| mWord.charAt(k) == 'I' || mWord.charAt(k) == 'O'
					|| mWord.charAt(k) == 'U')
				vowels++;
		}
		AlertDialog.Builder mHint = new AlertDialog.Builder(this);
		mHint.setMessage("Word contains " + vowels + " vowels")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		AlertDialog HintAlert = mHint.create();
		HintAlert.show();
	}

	public void setThreshold() {
		threshold = 2;
		if (mWord.length() == 3) {
			threshold = 2;
		}
		if (mWord.length() == 4) {
			threshold = 1;
		}
	}

	public TextureRegion getTexture(int _id) {
		//Returns the texture corresponding texture as per the required alphabet."_id"
		//is the ascii value of corresponding alphabet.
		switch (_id) {
		case 1:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "a.png", 0, 0));
		case 2:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "b.png", 128, 0));
		case 3:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "c.png", 256, 0));
		case 4:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "d.png", 384, 0));
		case 5:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "e.png", 512, 0));
		case 6:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "f.png", 640, 0));
		case 7:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "g.png", 768, 0));
		case 8:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "h.png", 896, 0));
		case 9:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "i.png", 1024, 0));
		case 10:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "j.png", 1152, 0));
		case 11:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "k.png", 1280, 0));
		case 12:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "l.png", 1408, 0));
		case 13:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "m.png", 1536, 0));
		case 14:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "n.png", 1664, 0));
		case 15:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "o.png", 1792, 0));
		case 16:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "p.png", 1920, 0));
		case 17:
			return (BitmapTextureAtlasTextureRegionFactory
					.createFromAsset(gameAtlas, this.getApplicationContext(),
							"q.png", 1152, 128));
		case 18:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "r.png", 0, 128));
		case 19:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "s.png", 128, 128));
		case 20:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "t.png", 256, 128));
		case 21:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "u.png", 384, 128));
		case 22:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "v.png", 512, 128));
		case 23:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "w.png", 640, 128));
		case 24:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "x.png", 768, 128));
		case 25:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "y.png", 896, 128));
		case 26:
			return (BitmapTextureAtlasTextureRegionFactory
					.createFromAsset(gameAtlas, this.getApplicationContext(),
							"z.png", 1024, 128));

		default:
			return (BitmapTextureAtlasTextureRegionFactory.createFromAsset(
					gameAtlas, this.getApplicationContext(), "blank.png",
					1152 + 128, 128));

		}
	}

	//Gamethread begins here.
	private IUpdateHandler myGameHandler = new IUpdateHandler() {

		public void reset() {
			// TODO Auto-generated method stub

		}

		public void onUpdate(float pSecondsElapsed) {
			makeThemdisappearonTouch();
			mScoretext.setText("Remaining chances " + chances);
		}
	};
}