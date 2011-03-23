package game.level;

import game.Start;

import java.util.ArrayList;
import java.util.List;

import jme3tools.converters.ImageToAwt;

import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

@SuppressWarnings("deprecation")
public class Level
{
	private Start stage;
	private TerrainQuad terrain;
	Material mat_terrain;

	public Level(Start s)
	{
		stage = s;
		makeFloor();
	}

	private void makeFloor() 
	{
	    Material terrain_mat;
	    PhysicsNode landscape;
	
	    /** 1. Create terrain material and load four textures into it. */
	    terrain_mat = new Material(stage.getAssetManager(), "assets/Level/Terrain.j3md");
	
	    /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
	    terrain_mat.setTexture("m_Alpha", stage.getAssetManager().loadTexture("assets/Level/alphamap.png"));
	
	    /** 1.2) Add GRASS texture into the red layer (m_Tex1). */
	    Texture grass = stage.getAssetManager().loadTexture("assets/Level/grass.jpg");
	    grass.setWrap(WrapMode.Repeat);
	    terrain_mat.setTexture("m_Tex1", grass);
	    terrain_mat.setFloat("m_Tex1Scale", 64f);
	
	    /** 1.3) Add DIRT texture into the green layer (m_Tex2) */
	    Texture dirt = stage.getAssetManager().loadTexture("assets/Level/dirt.jpg");
	    dirt.setWrap(WrapMode.Repeat);
	    terrain_mat.setTexture("m_Tex2", dirt);
	    terrain_mat.setFloat("m_Tex2Scale", 32f);
	
	    /** 1.4) Add ROAD texture into the blue layer (m_Tex3) */
	    Texture rock = stage.getAssetManager().loadTexture("assets/Level/road.jpg");
	    rock.setWrap(WrapMode.Repeat);
	    terrain_mat.setTexture("m_Tex3", rock);
	    terrain_mat.setFloat("m_Tex3Scale", 128f);
	
	    /** 2. Create the height map */
	    AbstractHeightMap heightmap = null;
	    Texture heightMapImage = stage.getAssetManager().loadTexture("assets/Level/mountains512.png");
	    heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
	    heightmap.load();
	
	    /** 3. We have prepared material and heightmap. Now we create the actual terrain:
	     * 3.1) We create a TerrainQuad and name it "my terrain".
	     * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
	     * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
	     * 3.4) At last, we supply the prepared heightmap itself.*/
	    terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
	
	    HeightfieldCollisionShape sceneShape = new HeightfieldCollisionShape(heightmap.getHeightMap());
	    landscape = new PhysicsNode(terrain, sceneShape, 0);
	
	
	    /** 4. The LOD (level of detail) depends on were the camera is: */
	    List<Camera> cameras = new ArrayList<Camera>();
	    cameras.add(stage.getCamera());
	    TerrainLodControl control = new TerrainLodControl(terrain, cameras);
	    terrain.addControl(control);
	
	    /** 5. We give the terrain its material, position & scale it, and attach it. */
	    terrain.setMaterial(terrain_mat);
	    landscape.setLocalTranslation(0, -100, 0);
	    stage.getRootNode().attachChild(landscape);
	    stage.getBulletAppState().getPhysicsSpace().add(landscape);
	}
}