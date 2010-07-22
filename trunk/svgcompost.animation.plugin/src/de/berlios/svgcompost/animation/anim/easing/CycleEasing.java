package de.berlios.svgcompost.animation.anim.easing;


public class CycleEasing extends Easing{
	
//	public static CycleEasing flyQuad = new CycleEasing( Quadratic._out, Quadratic._in, 0.5, 0 );
//	public static CycleEasing flyCubic = new CycleEasing( Cubic._out, Cubic._in, 0.5, 0 );
//	public static CycleEasing flyQuint = new CycleEasing( Quintic._out, Quintic._in, 0.5, 0 );
	
//	public static CycleEasing fly = new CycleEasing( Quad.easeOut, Quad.easeIn, 0.5, 0 );
//	public static CycleEasing bounce = new CycleEasing( Quad.easeIn, Quad.easeOut, 0.5, 0 );
	
	public CycleEasing(Easing forth, Easing back, double ratio, double offset) {
		easeBack = back;
		easeForth = forth;
		this.offset = offset;
		this.ratio = ratio;
	}
	
	public Easing easeForth;
	public Easing easeBack;
	/**
	 * The ratio of the first Easing, e.g. 0.7 if the first Easing is used 70% of the time.
	 */
	public double ratio;
	/**
	 * Offset of the entire cycle.
	 */
	public double offset;
	
	public double valueAt( double percentage ) {
//		System.out.println( "easeForth: "+easeForth );
		percentage = easeIn( percentage );
//		System.out.println( "percentage out: "+percentage );
		return percentage;

	}
	
	protected double easeIn( double percentage ) {
		percentage += offset;
		percentage %= 1;
		if( percentage < ratio ) {
//			System.out.println( "easeForth" );
			return easeForth.valueAt( percentage / ratio );
		}
		else {
//			System.out.println( "easeBack" );
			return 1 - easeBack.valueAt( (percentage - ratio) / (1 - ratio) );
		}
	}

	@Override
	protected double easeOut(double percentage) {
		// TODO Auto-generated method stub
		return 0;
	}

}
