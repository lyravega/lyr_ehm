package lyravega.utilities;

import org.lwjgl.util.vector.Vector2f;

/**
 * Provides some vector utility functions to the base, only used to
 * calculate the new locations of child slots.
 * @author lyravega
 */
public class lyr_vectorUtilities {
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

	/**
	 * Converts the position of a point that is in another system to the default system.
	 * <p> Assume there are two planes. The first one has a coordinate system that starts from (0,0)
	 * and has no angle; this is the default plane with its default coordinate system. The other one
	 * has a point of origin that is represented with {@code translatePoint} in the default one, and
	 * it may be sloped in comparison to default and may have a {@code translateAngle}; this is the
	 * other plane and its other coordinate system.
	 * <p> A {@code offsetPoint} on the other plane may have no representation in the default plane,
	 * and its position in the default may need to be calculated. This method calculates this exact
	 * representation, or rather the relative position of that point in the default plane.
	 * <p> For example, offsets may be entered for a weapon slot to spawn child slots based on this
	 * location. However, these offsets will use the weapon slot's location as its origin, and its
	 * angle. In order to make these offsets usable by the hull spec, the relative point of this
	 * offset needs to be calculated.
	 * @param translatePoint of origin, (0f, 0f) will be used if null
	 * @param translateAngle if the other origin is sloped
	 * @param offsetPoint to be used to calculate the relative point
	 * @return
	 */
	public static Vector2f calculateRelativePoint(Vector2f translatePoint, float translateAngle, Vector2f offsetPoint) {
		if (translatePoint == null) translatePoint = new Vector2f(0.0f, 0.0f);
		Vector2f relativePoint = new Vector2f(0.0f, 0.0f);

		final double parentAngleInRadians = translateAngle / 180.0d * PI;
		final float cos = (float) cos(parentAngleInRadians);
		final float sin = (float) sin(parentAngleInRadians);

		relativePoint.set((offsetPoint.x * cos) - (offsetPoint.y * sin), (offsetPoint.x * sin) + (offsetPoint.y * cos));

		return Vector2f.add(relativePoint, translatePoint, relativePoint);
	}
}