package de.gerrit_karius.cutout.anim;

//import java.lang.Double;

/**
 * Abstract superclass for all value tween calculations.
 * It takes two key values and an easing function,
 * and can then calculate tweens for any given percentage.
 */
public abstract class Tweener {
	public Object target;
	public String fieldName;
	/**
	 * Calculates a tween of the two key values with the given percentage.
	 * @param percentage
	 * @return The calculated tween object.
	 */
	public Object tween( double percentage ) {
		return null;
	}
	public void tweenField( double percentage ) {
//		ObjectUtil.setObject( target, fieldName, tween( percentage ) );
	}
	/**
	 * Selects the appropriate Tweener (point, double or discrete) for the two given values.
	 * @param value1
	 * @param value2
	 * @param easing
	 * @return the newly constructed appropriate Tweener.
	 */
	/*
	public static Tweener createTweener( Object value1, Object value2, Easing easing ) {
		System.out.println( ""+value1+" instanceof Point? "+(value1 instanceof Point2D.Float) );
		System.out.println( ""+value2+" instanceof Point? "+(value2 instanceof Point2D.Float) );
		if( value1 instanceof Point2D.Float && value2 instanceof Point2D.Float )
			return new PointTweener( (Point2D.Float) value1, (Point2D.Float) value2 );
		
//		double double1 = 0, double2 = 0;
//		boolean number1 = false, number2 = false;
//		if( value1 instanceof Number || TopLevel.typeof(value1) == "number" ) {
//			double1 = java.lang.Double.parseDouble( ""+value1 );
//			number1 = true;
//		}
//		if( value2 instanceof Number || TopLevel.typeof(value2) == "number" ) {
//			double2 = java.lang.Double.parseDouble( ""+value2 );
//			number2 = true;
//		}
//		if( number1 && number2 )
//		return new DoubleTweener( double1, double2 );
		if( ASUtil.instanceofNumber( value1 ) && ASUtil.instanceofNumber( value2 ) )
			return new DoubleTweener( ASUtil.doubleValue( (Number) value1 ), ASUtil.doubleValue( (Number) value2 ) );
//		else if( value1 instanceof Double && value2 instanceof Double )
//			return new DoubleTweener( (Double) value1, (Double) value2 );
//		else if( value1 instanceof Number && value2 instanceof Number ) {
//			Number n = (Number) value1;
//			double d = new Integer( ""+n );
//			System.out.println( "n.doubleValue(): "+d );
//			return null;
//		}

		return new DiscreteTweener( value1, value2, Easing.getAlign( easing ) );
	}
	*/
}
