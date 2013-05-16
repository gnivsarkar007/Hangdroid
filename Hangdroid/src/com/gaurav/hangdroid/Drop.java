package com.gaurav.hangdroid;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.Debug;

import android.graphics.Color;
import android.view.View.OnTouchListener;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
/*
This class handles the creation of the drops on the screen.Handles the positioning of the Drops
on the screen, applies the texture,and handles the touch event for the individual Drop sprites.The 
touch event is registered and the onTouched variable is set, which is then used in the main game class to
make relevant changes.
*/
public class Drop {
	private static final String TAG="Class Drop";
	float x,y;
	private TextureRegion mDropRegion;
	private boolean isTouched = false;
	private char mContainChar;
	private Sprite mDropSprite;
	private String mDropName;
	Scene mScene;
	private int id;
		public Drop(float _x, float _y, TextureRegion _textureregion,
			boolean _touched, char _contains, String _name,Scene _scene,int _id) {
		Debug.d("Drop created");
		this.x = _x;
		this.y = _y;
		this.mDropRegion = _textureregion;
		this.isTouched = _touched;
		this.mDropName = new String(_name);
		this.mContainChar =_contains;
		this.mScene=_scene;
		this.id=_id;
		
	this.mDropSprite=new Sprite(x, y, mDropRegion){
		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
				float pTouchAreaLocalX, float pTouchAreaLocalY) {
			if (pSceneTouchEvent.isActionUp()) {
				isTouched= true;
				Debug.d(TAG+" Touched Drop "+isTouched+" "+mDropName);
			}
		return isTouched;
		}
	};
	
	
	this.mDropSprite.setUserData(mDropName);
	Debug.d("Drop created");
	mScene.attachChild(this.mDropSprite);
	Debug.d("Drop created");
	mDropSprite.setScale(0.6f);
	Debug.d("Drop created");
	this.mScene.registerTouchArea(mDropSprite);
	
	
	}

	
	public int getId() {
			return id;
		}


	public float getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isTouched() {
		return isTouched;
	}

	public void setTouched(boolean isTouched) {
		this.isTouched = isTouched;
	}

	public char getmContainChar() {
		return mContainChar;
	}

	public void setmContainChar(char mContainChar) {
		this.mContainChar = mContainChar;
	}

	public Sprite getmDropSprite() {
		return mDropSprite;
	}

	public void setmDropSprite(Sprite mDropSprite) {
		this.mDropSprite = mDropSprite;
	}

	public String getmDropName() {
		return mDropName;
	}

	public void setmDropName(String mDropName) {
		this.mDropName = mDropName;
	}

	public Sprite getSprite(){
		return this.mDropSprite;
	}

	
}
