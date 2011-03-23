package game;

import game.level.Level;

import java.util.ArrayList;

import objects.character.Actor;
import objects.character.player.Hero;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

public class Start extends SimpleApplication
{
	private BulletAppState bulletAppState;
	private PointLight light = new PointLight();
	
	private ArrayList<Actor> goodGuys = new ArrayList<Actor>();
	private ArrayList<Actor> badGuys = new ArrayList<Actor>();
	
	private int id = -1;
	
	private long time;
	
	public static void main(String args[])
	{
		Start stage = new Start();
		stage.start();
	}
	
	public void simpleInitApp()
	{
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		
		rootNode.addLight(light);
		flyCam.setMoveSpeed(100);
		
		new Level(this);
		
		goodGuys.add(new Hero(this));
		
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);
	}
	
	public void simpleUpdate(float tpf)
	{
		light.setPosition(cam.getLocation());
		time = System.currentTimeMillis();
		
		for (int i = 0; i < goodGuys.size(); i ++)
		{
			goodGuys.get(i).simpleUpdate(tpf);
		}
		
		for (int i = 0; i < badGuys.size(); i ++)
		{
			badGuys.get(i).simpleUpdate(tpf);
		}
	}
	
	public BulletAppState getBulletAppState()
	{
		return bulletAppState;
	}
	
	public AppSettings getSettings()
	{
		return settings;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public ArrayList<Actor> getGoodGuys()
	{
		return goodGuys;
	}
	
	public ArrayList<Actor> getBadGuys()
	{
		return badGuys;
	}
	
	public int getId()
	{
		id += 1;
		return id;
	}
	
	public boolean find(ArrayList<Actor> list, int num)
	{
		for (int i = 0; i < list.size(); i ++)
		{
			if (num == list.get(i).getId())
				return true;
		}
		
		return false;
	}
	
	public Actor search(int num)
	{
		for (int i = 0; i < badGuys.size(); i ++)
		{
			if (num == badGuys.get(i).getId())
				return badGuys.get(i);
		}
		
		for (int i = 0; i < goodGuys.size(); i ++)
		{
			if (num == goodGuys.get(i).getId())
				return goodGuys.get(i);
		}
		
		return null;
	}
	
	public Actor getActor(String s)
	{
		for (int i = 0; i < badGuys.size(); i ++)
		{
			if (s.equals(Integer.toString(badGuys.get(i).getId())))
				return badGuys.get(i);
		}
		
		for (int i = 0; i < goodGuys.size(); i ++)
		{
			if (s.equals(Integer.toString(goodGuys.get(i).getId())))
				return goodGuys.get(i);
		}
		
		return null;
	}
	
	public ArrayList<Actor> getEnemies(int num)
	{
		if (find(badGuys, num))
			return goodGuys;
		
		return badGuys;
	}
	
	public ArrayList<Actor> getFriends(int num)
	{
		if (find(badGuys, num))
			return badGuys;
		
		return goodGuys;
	}
}