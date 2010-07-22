package de.berlios.svgcompost.animation.anim.easing;


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
	
	public Easing() {
	}
	
	public static double applyEasing( Easing easing, double percentage ) {
		if( easing != null )
			return easing.valueAt( percentage );
		else
			return percentage;
	}
	
	public static final int EASE_IN = 0;
	public static final int EASE_OUT = 1;
	public static final int EASE_IN_OUT = 2;

	protected int align = EASE_IN_OUT;
	protected double power = 1;
	
	/**
	 * Calculates the function value at a given step for the easing function f.
	 * For simple functions, it should hold that f(0)=0 and f(1)=1.
	 * @param percentage
	 * @return The tweening value at the given timing percentage.
	 */
	public double valueAt( double percentage ) {
		switch( align ) {
		case Easing.EASE_IN:
			return Math.pow(easeIn(percentage),power);
		case Easing.EASE_OUT:
			return easeOut(Math.pow(percentage,1.0/power));
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
	protected abstract double easeIn( double percentage );
	
	protected abstract double easeOut( double percentage );
	
//	protected double easeOut( double percentage ) {
//		return 1 - easeIn( 1 - percentage );
//	}
	protected double easeInOut( double percentage ) {
		if( percentage < 0.5 )
			return easeIn( percentage * 2 ) * 0.5;
		else
			return easeOut( percentage * 2 - 1 ) * 0.5 + 0.5;
	}
	public int getAlign() {
		return align;
	}
	public void setAlign( int align ) {
		this.align = align;
	}
	public void setPower(double power) {
		this.power = power;
	}
}
