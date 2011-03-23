package objects.character;
import com.jme3.math.Vector3f;

public abstract class Actor
{	
	public abstract void simpleUpdate(float tpf);
	
	public abstract Vector3f getLocation();
	
	public abstract int getId();
	
	public abstract void hurt(int amount);
	
	public abstract boolean isDead();
}