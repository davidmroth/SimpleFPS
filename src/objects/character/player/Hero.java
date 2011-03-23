package objects.character.player;

import game.Start;
import objects.character.Actor;
import objects.character.npc.Ghoul;
import objects.weapons.Weapon;
import objects.weapons.m16.M16;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

public class Hero extends Actor
{
	private Geometry model;
	private CharacterControl control;

	boolean forward, backward, left, right;
	boolean firstPersonView = true;

	//private float speed = .05f;
	private float speed = .1f;

	private Start stage;
	private int id;

	private int health = 100;

	Vector3f cameraOffset = new Vector3f(0, .65f, 0);

	private Weapon weapon;

	public Hero(Start s)
	{
		stage = s;
		id = stage.getId();
		
		setupModel();
		setupKeys();
		
		weapon = new M16(this);
	}

	private void setupModel()
	{
		Material monkey = new Material(stage.getAssetManager(), "assets/Misc/Light/Lighting.j3md");
		monkey.setTexture("DiffuseMap", stage.getAssetManager().loadTexture("assets/Misc/Misc/Monkey.jpg"));
		
		SphereCollisionShape sphere = new SphereCollisionShape(1);
		control = new CharacterControl(sphere, 5);
		
		model = new Geometry(Integer.toString(id), new Sphere(16, 16, 1));
		model.setMaterial(monkey);
		stage.getRootNode().attachChild(model);
		
		model.addControl(control);
		stage.getBulletAppState().getPhysicsSpace().add(control);
		
		control.setPhysicsLocation(new Vector3f(0, -1, 7.5f));
	}

	private void setupKeys()
	{
		stage.getInputManager().addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
		stage.getInputManager().addListener(actionListener, "forward");
		
		stage.getInputManager().addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
		stage.getInputManager().addListener(actionListener, "backward");
		
		stage.getInputManager().addMapping("left", new KeyTrigger(KeyInput.KEY_A));
		stage.getInputManager().addListener(actionListener, "left");
		
		stage.getInputManager().addMapping("right", new KeyTrigger(KeyInput.KEY_D));
		stage.getInputManager().addListener(actionListener, "right");
		
		stage.getInputManager().addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
		stage.getInputManager().addListener(actionListener, "jump");
		
		stage.getInputManager().addMapping("toggleView", new KeyTrigger(KeyInput.KEY_P));
		stage.getInputManager().addListener(actionListener, "toggleView");
		
		stage.getInputManager().addMapping("spawnEnemy", new KeyTrigger(KeyInput.KEY_I));
		stage.getInputManager().addListener(actionListener, "spawnEnemy");
		
		stage.getInputManager().addMapping("spawnFriend", new KeyTrigger(KeyInput.KEY_Y));
		stage.getInputManager().addListener(actionListener, "spawnFriend");
		
		stage.getInputManager().addMapping("reload", new KeyTrigger(KeyInput.KEY_R));
		stage.getInputManager().addListener(actionListener, "reload");
		
		stage.getInputManager().addMapping("shoot", new MouseButtonTrigger(0));
		stage.getInputManager().addListener(actionListener, "shoot");
		
		stage.getInputManager().addMapping("zoom", new MouseButtonTrigger(1));
		stage.getInputManager().addListener(actionListener, "zoom");
	}

	private ActionListener actionListener = new ActionListener()
	{
		public void onAction(String name, boolean keyPressed, float tpf)
		{
			if (name.equals("forward"))
				forward = keyPressed;
			
			if (name.equals("backward"))
				backward = keyPressed;
			
			if (name.equals("left"))
				left = keyPressed;
			
			if (name.equals("right"))
				right = keyPressed;
			
			if (name.equals("jump") && !keyPressed)
				control.jump();
			
			if (name.equals("toggleView") && !keyPressed)
				firstPersonView = !firstPersonView;
			
			if (name.equals("shoot"))
				weapon.fire(keyPressed);
			
			if (name.equals("reload"))
				weapon.reload(keyPressed);
			
			if (name.equals("zoom"))
				weapon.zoom(keyPressed);

			if (name.equals("spawnEnemy") && !keyPressed)
				stage.getEnemies(id).add(new Ghoul(stage));
			
			if (name.equals("spawnFriend") && !keyPressed)
				stage.getFriends(id).add(new Ghoul(stage));			
		}
	};

	public void simpleUpdate(float tpf)
	{
		weapon.simpleUpdate(tpf);
		
		Vector3f cam = stage.getCamera().getDirection();
		Vector3f camLeft = stage.getCamera().getLeft();
		cam.setY(0);
		camLeft.setY(0);
		cam = cam.normalize().mult(speed);
		camLeft = camLeft.normalize().mult(speed);
		
		Vector3f walk = new Vector3f(0, 0, 0);
		
		if (forward)
			walk.set(walk.add(cam));

		if (backward)
			walk.set(walk.subtract(cam));

		if (left)
			walk.set(walk.add(camLeft));

		if (right)
			walk.set(walk.subtract(camLeft));
			
		control.setWalkDirection(walk);
		control.setViewDirection(cam);
		
		if (firstPersonView)
			stage.getCamera().setLocation(getLocation().add(cameraOffset));
	}

	public Vector3f getLocation()
	{
		return control.getPhysicsLocation();
	}

	public int getHealth()
	{
		return health;
	}

	public int getId()
	{
		return id;
	}

	public Start getStage()
	{
		return stage;
	}

	public void hurt(int damage)
	{
		health -= damage;
		
		if (health < 0)
		{
			speed = 0;
			control.setJumpSpeed(0);
		}
	}

	public boolean isWalking()
	{
		return forward || backward || left || right;
	}

	public boolean isDead()
	{
		return false;
	}
}