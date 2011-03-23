package objects.character.npc;

import game.Start;
import java.util.ArrayList;
import objects.character.Actor;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.TangentBinormalGenerator;

public class Ghoul extends Actor implements AnimEventListener
{
	private Start stage;
	private int id;
	private Actor target;
	private RigidBodyControl control;
	private CapsuleCollisionShape capsule;
	
	private int health = 100;
	
	private Node model, ghoul;
	private Vector3f modelOffset = new Vector3f(0, -.85f, 0);
	private AnimChannel animChannel;
	private AnimControl animControl;
	
	private float linearDamping = .75f;
	private float chaseDistance = 20;
	
	private long targetTime;
	private long reTargetTime = 7500;
	
	private long attackTime;
	private long reAttackTime = 2500;	//INCLUDES ACTUAL ATTACK DURATION
	
	private float walkForce = 195;
	private float animWalkSpeed = .75f;
	private float walkDistance = 1.95f;
	
	private float attackStartDistance = 1.65f;
	private float attackEndDistance = 2.65f;
	
	private boolean attacking;
	private boolean damageDone;
	private int damage = 10;
	
	private long damageDelay = 975;
	
	private boolean sprinting;
	private long sprintDuration = 4000;
	private long sprintStartTime;
	private long sprintRecharge = 8000;
	private float sprintForce = 650;
	
	private float sprintStartDistance = 3;
	private float sprintEndDistance = 1.5f;
	private float lungeAttackAnimSpeed = 2;
	private long lungeAttackDelay = 600;
	private boolean lungeAttack;
	
	private boolean staggered, staggerShot;
	private int staggerDamage = 20;
	
	private boolean dead;
	
	public Ghoul(Start s)
	{
		stage = s;
		id = stage.getId();
		
		setupModel();
		
		targetTime = stage.getTime() - reTargetTime;
		attackTime = stage.getTime();
		sprintStartTime = stage.getTime();
	}

	private void setupModel()
	{
		Material skin = new Material(stage.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		skin.setTexture("DiffuseMap", stage.getAssetManager().loadTexture(new String("assets/Ghoul/diffuse.png")));
		skin.setTexture("NormalMap", stage.getAssetManager().loadTexture("assets/Ghoul/normal.png"));
		skin.setTexture("SpecularMap", stage.getAssetManager().loadTexture("assets/Ghoul/specular.png"));
		
		model = (Node)stage.getAssetManager().loadModel("assets/Ghoul/ghoul.mesh.xml");
		model.setMaterial(skin);
		model.getChild(0).setName(Integer.toString(id));
		TangentBinormalGenerator.generate(model);
		
		animControl = model.getControl(AnimControl.class);
		animChannel = animControl.createChannel();
		animControl.addListener(this);
		animChannel.setAnim("idle");
		
		ghoul = new Node();
		ghoul.attachChild(model);
		model.setLocalTranslation(modelOffset);
		model.setLocalScale(.875f);
		stage.getRootNode().attachChild(ghoul);
		
		capsule = new CapsuleCollisionShape(.45f, .85f, 1);
		control = new RigidBodyControl(capsule, 50);
		control.setAngularFactor(0);
		control.setLinearDamping(linearDamping);
		stage.getBulletAppState().getPhysicsSpace().add(control);
	}
	
	private void getTarget()
	{
		float distance = chaseDistance;
		Actor bestTarget = null;
		
		ArrayList<Actor> targets = stage.getEnemies(id);
		
		for (int i = 0; i < targets.size(); i ++)
		{
			float newDistance = targets.get(i).getLocation().subtract(getLocation()).length();
			
			if (newDistance < distance && !targets.get(i).isDead())
			{
				distance = newDistance;
				bestTarget = targets.get(i);
			}
		}
		
		if (distance < chaseDistance)
			target = bestTarget;
		else
			target = null;
	}
	
	public Vector3f getLocation()
	{
		return control.getPhysicsLocation();
	}
	
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)
	{
		if (!dead)
		{
			if (animName.contains("stagger"))
			{
				animChannel.setAnim("idle", 0);
				staggered = false;
			}
			else if (animName.equals("walk_start"))
			{
				animChannel.setAnim("walk", 0);
				animChannel.setSpeed(animWalkSpeed);
			}
			else if (animName.equals("walk_end"))
			{
				animChannel.setAnim("idle", 0);
			}
			else if (animName.contains("swipe"))
			{
				attackEnd();
				animChannel.setAnim("idle", 0);
			}
		}
	}
	
	private void attackEnd()
	{
		attacking = false;
	//	attackTime = stage.getTime();
		
		damageDone = false;
	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName)
	{
		//	don't need this
	}
	
	public int getId()
	{
		return id;
	}
	
	public void hurt(int amount)
	{
		if (!dead)
		{
			health -= amount;
			
			if (amount > staggerDamage)
			{
				staggered = true;
				staggerShot = true;
			}
			
			if (health < 0)
			{
				walkForce = 0;
				sprintForce = 0;
				dead = true;
				System.out.println(id + " is dead!");
			}
		}
	}
	
	public boolean isDead()
	{
		return dead;
	}

	public void simpleUpdate(float tpf)
	{
		ghoul.setLocalTranslation(getLocation());
		
		if (target == null)
		{
			if (attacking)
				attacking = false;

			if (sprinting)
				sprinting = false;
		}
		
		if (dead)
		{
			if (!animChannel.getAnimationName().equals("fall"))
			{
				animChannel.setAnim("fall");
				animChannel.setLoopMode(LoopMode.DontLoop);
			}
		}
		else if (staggered)
		{
			if (!animChannel.getAnimationName().contains("stagger") || staggerShot)
			{
				animChannel.setAnim("stagger_" + FastMath.rand.nextInt(2));
				staggerShot = false;
			}
		}
		else if (sprinting)
		{
			if (!animChannel.getAnimationName().equals("run"))
			{
				animChannel.setAnim("run");
				sprintStartTime = stage.getTime();
			}
			
			Vector3f move = target.getLocation().subtract(getLocation());
			move.setY(0);
			move.set(move.normalize());
			control.applyCentralForce(move.mult(sprintForce));
			
			Vector3f targetZero = target.getLocation();
			targetZero.setY(getLocation().getY());
			ghoul.lookAt(targetZero, Vector3f.UNIT_Y);
			
			if (stage.getTime() - sprintStartTime > sprintDuration)
			{
				sprinting = false;
				animChannel.setAnim("walk");
			}
			
			if (target.getLocation().subtract(getLocation()).length() < sprintEndDistance)
			{
				sprinting = false;
				attacking = true;
				animChannel.setAnim("swipe_" + FastMath.rand.nextInt(2), 0);
				animChannel.setSpeed(lungeAttackAnimSpeed);
				attackTime = stage.getTime();
				lungeAttack = true;
			}
		}
		else if (attacking)
		{
			if (!animChannel.getAnimationName().contains("swipe"))
			{
				animChannel.setAnim("swipe_" + FastMath.rand.nextInt(2), 0);
				attackTime = stage.getTime();
			}
			
			if ((stage.getTime() - attackTime > damageDelay || lungeAttack && stage.getTime() - attackTime > lungeAttackDelay) && !damageDone && target.getLocation().subtract(getLocation()).length() < attackEndDistance)
			{
				target.hurt(damage);
				System.out.println("Ghoul attack " + id + " was successful.");
				if (target.isDead())
					getTarget();
				lungeAttack = false;
			}
			
			if (stage.getTime() - attackTime > damageDelay && !damageDone)
			{
				damageDone = true;
			}
		}
		else if (target!= null)
		{
			Vector3f move = target.getLocation().subtract(getLocation());
			move.setY(0);
			move.set(move.normalize());
			control.applyCentralForce(move.mult(walkForce));
			
			Vector3f toTarget = target.getLocation().subtract(getLocation());
			
			if (toTarget.length() > walkDistance && !animChannel.getAnimationName().contains("walk"))
				animChannel.setAnim("walk_start", 0);
			else if (toTarget.length() < walkDistance && !animChannel.getAnimationName().equals("idle") && !animChannel.getAnimationName().equals("walk_end"))
				animChannel.setAnim("walk_end", 0);
			
			Vector3f targetZero = target.getLocation();
			targetZero.setY(getLocation().getY());
			ghoul.lookAt(targetZero, Vector3f.UNIT_Y);
			
			if (toTarget.length() < attackStartDistance && stage.getTime() - attackTime > reAttackTime)
				attacking = true;
			
			if (toTarget.length() > sprintStartDistance && stage.getTime() - sprintStartTime > sprintRecharge)
				sprinting = true;
		}
		
		if (!attacking && stage.getTime() - targetTime > reTargetTime)
		{
			targetTime = stage.getTime();
			getTarget();
		}
	}
}