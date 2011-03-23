package objects.weapons;

public abstract class Weapon
{
	public abstract void fire(boolean keyPressed);
	
	public abstract void reload(boolean keyPressed);
	
	public abstract void simpleUpdate(float tpf);
	
	public abstract void zoom(boolean keyPressed);
}