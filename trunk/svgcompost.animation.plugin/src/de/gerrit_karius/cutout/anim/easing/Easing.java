package de.gerrit_karius.cutout.anim.easing;

/**
 * Immutable easing functions.
 * t: current time, b: beginning value, c: change in value, d: duration
 * special function for b=0, c=1, d=1, 0<t<1
 * 
 * Back
 * Bounce
 * Circ
 * Cubic
 * Elastic
 * Expo
 * Linear
 * Quad
 * Quart
 * Quint
 * Sine
 * @author gerrit
 *
 */
public abstract class Easing {
	
	private static Easing standard;
	
	public static Easing getStandard() {
		if( standard == null )
			standard = new Quadratic( Easing.EASE_IN );
		return standard;
	}

	public static double applyEasing( Easing easing, double percentage ) {
		if( easing != null )
			return easing.valueOf( percentage );
		else
			return percentage;
	}
	
	public static final int EASE_IN = 0;
	public static final int EASE_OUT = 1;
	public static final int EASE_IN_OUT = 2;
	/**
	 * This is the only field common for all easings.
	 * Subclass constructors must set this field.
	 */
	protected int align = EASE_IN_OUT;
	/**
	 * Calculates the function value at a given step for the easing function f.
	 * For simple functions, it should hold that f(0)=0 and f(1)=1.
	 * @param percentage
	 * @return The tweening value at the given timing percentage.
	 */
	public double valueOf( double percentage ) {
		switch( align ) {
		case Easing.EASE_IN:
			return easeIn(percentage);
		case Easing.EASE_OUT:
			return easeOut(percentage);
		case Easing.EASE_IN_OUT:
			return easeInOut(percentage);
		default:
			return percentage;
		}
	}
	/**
	 * Subclasses must primarily override this function.
	 * @param percentage
	 * @return The tweening value at the given timing percentage.
	 */
	public double easeIn( double percentage ) {
		return percentage;
	}
	public double easeOut( double percentage ) {
		return 1 - easeIn( 1 - percentage );
	}
	public double easeInOut( double percentage ) {
		if( percentage < 0.5 )
			return easeIn( percentage * 2 ) * 0.5;
		else
			return easeOut( percentage * 2 - 1 ) * 0.5 + 0.5;
	}
	public static int getAlign( Easing easing ) {
		return easing != null ? easing.align : EASE_IN_OUT;
	}
}
