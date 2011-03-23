package objects.weapons.m16;

import objects.character.Actor;
import objects.character.player.Hero;
import objects.weapons.Weapon;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

public class M16 extends Weapon implements AnimEventListener
{
	private Node model;
	private Node m16;
	private AnimChannel animChannel;
	private AnimControl animControl;
	private Hero user;
	private Vector3f offset = new Vector3f(-.65f, -.2f, .5f);
	
	private boolean firing;
	private boolean reloading;
	private boolean keyPressed;
	private boolean firstHit = true;
	
	private int shotsMax = 30;
	private int shots = shotsMax;
	
	private BitmapText cross, hud;
	private float fireRate = 1.5f;
	private float reloadRate = .85f;
	
	private float sightSpeed = 200;
	private float sightAmount;
	private boolean sight;
	private boolean isSighted;
	private Vector3f sightOffset = new Vector3f(0, .0135f, .5f);
	private float zoomAmount = 125;	//MINIMUM 100, LESS IS MORE ZOOM
	private float criticalSightAmount = 75;
	
	private float hipSpread = .015f;
	private float sightSpread = .001f;
	
	private Geometry mark;
	
	public M16(Hero h)
	{
		user = h;
		
		user.getStage().getCamera().setFrustumPerspective(45, 1.4f, .5f, 100);
		
		Material mat = new Material(user.getStage().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		mat.setTexture("DiffuseMap", user.getStage().getAssetManager().loadTexture(new String("assets/M16/diffuse.png")));
		mat.setTexture("NormalMap", user.getStage().getAssetManager().loadTexture("assets/M16/norm.png"));
		mat.setTexture("SpecularMap", user.getStage().getAssetManager().loadTexture("assets/M16/spec.png"));
		
		model = (Node)user.getStage().getAssetManager().loadModel("assets/M16/m16.mesh.xml");
		model.setMaterial(mat);
		model.getChild(0).setName(Integer.toString(user.getId()));
		TangentBinormalGenerator.generate(model);
		
		animControl = model.getControl(AnimControl.class);
		animChannel = animControl.createChannel();
		animControl.addListener(this);
		animChannel.setAnim("idle");
		
		m16 = new Node();
		m16.attachChild(model);
		model.setLocalTranslation(offset);
	//	model.setLocalScale(.875f);
		user.getStage().getRootNode().attachChild(m16);
		
		initCross();
		setupMark();
	}

	private void setupMark()
	{
		Sphere sphere = new Sphere(8, 8, 0.2f);
		mark = new Geometry(Integer.toString(user.getId()), sphere);
		Material mark_mat = new Material(user.getStage().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
		
	//	user.getStage().getRootNode().attachChild(mark);
	}

	private void initCross()
	{
		BitmapFont guiFont = user.getStage().getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		cross = new BitmapText(guiFont, false);
		cross.setSize(guiFont.getCharSet().getRenderedSize() * 2.5f);
		cross.setText("+");
		cross.setLocalTranslation(user.getStage().getSettings().getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
				user.getStage().getSettings().getHeight() / 2 + cross.getLineHeight() / 2, 0);
		user.getStage().getGuiNode().attachChild(cross);
		
		hud = new BitmapText(guiFont, false);
		hud.setSize(guiFont.getCharSet().getRenderedSize() * 1.25f);
		hud.setText("HUD");
		hud.setLocalTranslation(new Vector3f(5, 300, 0));
		user.getStage().getGuiNode().attachChild(hud);
	}
	
	public void reload(boolean keyPressed)
	{
		if (!firing && !reloading && keyPressed && shots != shotsMax)
		{
			reloading = true;
			animChannel.setAnim("reload", 0);
			animChannel.setSpeed(reloadRate);
		}
	}
	
	public void zoom(boolean keyPressed)
	{
		sight = keyPressed;
	}

	public void fire(boolean k)
	{
		keyPressed = k;
	}

	
	public void simpleUpdate(float tpf)
	{
		m16.setLocalTranslation(user.getLocation());
		m16.setLocalRotation(user.getStage().getCamera().getRotation());

		if (sight && sightAmount < 100)
			sightAmount += (tpf * sightSpeed);

		else if (!sight && sightAmount > 0)
			sightAmount -= (tpf * sightSpeed);

		if (sightAmount > 100)
			sightAmount = 100;

		else if (sightAmount < 0)
			sightAmount = 0;
		
		isSighted = (sightAmount > criticalSightAmount);
		
		Vector3f sightInter = offset.mult((100 - sightAmount) / 100).add(sightOffset.mult(sightAmount / 100));
		model.setLocalTranslation(sightInter);
		
		user.getStage().getCamera().setFrustumPerspective(45 * ((zoomAmount - sightAmount) / 100), 1.4f, .5f, 100);
		
		if (!isSighted)
			cross.setText("+");
		else
			cross.setText("");
		
		hud.setText("ammo: " + shots + "/" + shotsMax + "\nhealth: " + user.getHealth());
		
		if (!reloading)
		{
			if (keyPressed && !firing && shots > 0)
			{
				firing = true;
			}
			
			if (firing)
			{
				if (!animChannel.getAnimationName().equals("fire"))
				{
					animChannel.setAnim("fire", 0);
					animChannel.setSpeed(fireRate);
				}
			}
			
			if (user.isWalking() && !firing && !animChannel.getAnimationName().equals("walk"))
				animChannel.setAnim("walk");
			else if (!user.isWalking() && !firing && !animChannel.getAnimationName().equals("idle"))
				animChannel.setAnim("idle", 0);
			
			CollisionResults results = new CollisionResults();
		//	Ray ray = new Ray(user.getStage().getCamera().getLocation(), user.getStage().getCamera().getDirection());
			Vector3f dir = user.getStage().getCamera().getDirection();
			
			if (isSighted)
				dir = dir.add(new Vector3f((FastMath.rand.nextFloat() - .5f) * sightSpread, (FastMath.rand.nextFloat() - .5f) * sightSpread, (FastMath.rand.nextFloat() - .5f) * sightSpread));
			else
				dir = dir.add(new Vector3f((FastMath.rand.nextFloat() - .5f) * hipSpread, (FastMath.rand.nextFloat() - .5f) * hipSpread, (FastMath.rand.nextFloat() - .5f) * hipSpread));
			
			Ray ray = new Ray(user.getStage().getCamera().getLocation(), dir);
			user.getStage().getRootNode().collideWith(ray, results);
			
			for (int i = 0; i < results.size(); i++)
			{	
				if (results.getCollision(i).getGeometry().getName().equals(Integer.toString(user.getId())))
				{
				//	System.out.println(results.getCollision(i).getGeometry().getName());
				}
				else
				{
				//	mark.setLocalTranslation(results.getCollision(i).getContactPoint());
					
					if (firstHit)
					{
						Actor victim = user.getStage().getActor(results.getCollision(i).getGeometry().getName());
						if (victim != null)
						{
							victim.hurt(25);
						}
						
						firstHit = false;
						
						System.out.println(results.getCollision(i).getGeometry().getName());
						
						mark.setLocalTranslation(results.getCollision(i).getContactPoint());
					//	user.getStage().getRootNode().attachChild(mark);
					}
					break;
				}
			}
			firstHit = false;
		//	user.getStage().getRootNode().detachChild(mark);
		}
	}

	
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName)
	{
		//	UNUSED
	}

	
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)
	{
		if (animName.equals("fire"))
		{
			animChannel.setAnim("idle", 0);
			firing = false;
			firstHit = true;
			
			shots --;
		}
		else if (animName.equals("reload"))
		{
			shots = shotsMax;
			reloading = false;
		}
	}
}