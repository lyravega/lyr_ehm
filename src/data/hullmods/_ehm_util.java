package data.hullmods;

import org.lwjgl.util.vector.Vector2f;

/**
 * Provides some vector utility functions to the base, only used to
 * calculate the new locations of child slots.
 * @author lyravega
 */
public class _ehm_util {
	private static final double PI = Math.PI;

	private static double reduceSinAngle(double radians)
	{
		radians %= PI * 2.0; 
		if (Math.abs(radians) > PI) radians = radians - (PI * 2.0d);
		if (Math.abs(radians) > PI / 2.0d) radians = PI - radians;
		return radians;
	}

	private static double sin(double radians)
	{
		radians = reduceSinAngle(radians); 
		if (Math.abs(radians) <= PI / 4.0d) return Math.sin(radians); 
		else return Math.cos(PI / 2.0d - radians); 
	}	
	
	private static double cos(double radians)
	{
		return sin(radians + PI / 2.0);
	}
	
	// why the fuck are ships 'sideways'?
	public static Vector2f generateChildLocation(Vector2f parentLocation, float parentAngle, Vector2f childOffset) {
		Vector2f childLocation = new Vector2f(0.0f, 0.0f);

		final double parentAngleInRadians = parentAngle / 180.0d * PI; 
		final float cos = (float) cos(parentAngleInRadians);
		final float sin = (float) sin(parentAngleInRadians);
			
		childLocation.set((childOffset.x * cos) - (childOffset.y * sin), (childOffset.x * sin) + (childOffset.y * cos));

		return new Vector2f(childLocation.x + parentLocation.x, childLocation.y + parentLocation.y);
	}
}