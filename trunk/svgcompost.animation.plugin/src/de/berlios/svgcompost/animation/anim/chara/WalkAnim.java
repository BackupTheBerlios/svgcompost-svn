package de.berlios.svgcompost.animation.anim.chara;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.naming.spi.Resolver;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.xerces.impl.xpath.XPath.Step;

import de.berlios.svgcompost.animation.anim.Anim;
import de.berlios.svgcompost.animation.anim.easing.Easing;
import de.berlios.svgcompost.animation.canvas.Canvas;

/**
 * Lets a character walk to a specified point, automatically calculating the necessary steps.
 * A Walk object is used to determine the walking pattern.
 * @author Gerrit
 *
 */
public class WalkAnim extends Anim {

	/**
	 * Calculates the percentage of the total distance that has been cleared with the given
	 * number of time steps, provided that walking the total distance
	 * takes the specified number of steps, but the distance of the steps is modified
	 * in the way that the first and last step are scaled according to the given parameters.
	 * @param noOfTimeSteps The total number of time steps (one more than distance steps).
	 * @param pDistStep Percentage of one step of the total distance.
	 * @param compSteps Number of already completed steps.
	 * @param pWithinStep Percentage of execution within the current step.
	 * @param scaleFirst Scaling of the first time step (e.g. one half of a normal distance step).
	 * @param scaleLast Scaling of the last time step (e.g. one half of a normal distance step).
	 * @param part For debugging.
	 * @return
	 */
	public static double pDist( int noOfTimeSteps, double pDistStep, int compSteps, double pWithinStep, double scaleFirst, double scaleLast, String part ) {
		// TODO: include values <0 and >noTimeSteps with mod.
		if( compSteps < 0 )
			return -1;
		// A normal step's percentage of the entire distance. 
		// This normal step would be sufficient to reach 100% with the number of time steps,
		// if the first and last steps weren't scaled.
		// Note: the order of the ifs is important, as the cases are not exclusive.
		if( compSteps == noOfTimeSteps - 1 ) {
			return pDistStep * (scaleFirst + pWithinStep * scaleLast + (compSteps - 1) );
		}
		if( compSteps >= noOfTimeSteps ) {
			// TODO: find out why the actual calculation doesn't work.
			return 1;//pDistStep * (scaleFirst + scaleLast + (compSteps - 2) + pWithinStep);
		}
		else {
			return pDistStep * (scaleFirst + pWithinStep + (compSteps - 1) );
		}
	}
	
	public String fromInput;
	public String toInput;
	public String characterInput;
	
	public CharaPartInterface character;
	public Walk walk;
	public Point2D.Float fromPoint;
	public Point2D.Float toPoint;
	protected Point2D.Float[] fromFoot;
	protected Point2D.Float[] toFoot;

	/**
	 * Percentage of start scale that is reached at the end of the walk.
	 */
	public double pScale = 1;
//	protected double initialScale;

	protected Point2D.Float[] relFootPos;
	protected GraphicsNode[] foot;
	protected Point2D.Float bodyPos;
	protected Point2D.Float headPos;

	public int noOfSteps = 0;
	public int noOfDistSteps;
	protected int noOfTimeSteps;

	protected double stepLength;
	protected Point2D.Float step;
	protected Point2D.Float oddStep;
	
//	public WalkAnim( AnimCharacter character, Point2D.Float from, Point2D.Float to ) {
//		this.character = character;
//		this.fromPoint = from;
//		this.toPoint = to;
//	}
	
	public WalkAnim from( String fromInput ) {
		this.fromInput = fromInput;
		return this;
	}
	public WalkAnim to( String toInput ) {
		this.toInput = toInput;
		return this;
	}
	public WalkAnim target( String characterInput ) {
		this.characterInput = characterInput;
		return this;
	}
	
	private void calcDistance()  {
		
		// When this came after the flip check,
		// scale was set to 100% !!!
		
//		initialScale = Canvas.getScaleY100( character.mc );
		
//		Canvas.debug( "initialScale: "+initialScale );

//		Canvas.debug( "x scale: "+Canvas.getScaleX( character.mc ) );

		
		character.snapToAnchors();

		if( character != null ) {
			if( walk == null )
				walk = character.walk;
//			if( character.mc != null )
//			from = new Point( character.mc._x, character.mc._y );
		}
//		Canvas.debug( "x scale: "+Canvas.getScaleX( character.mc ) );



		// Calc distance.
		Point2D.Float d = new Point2D.Float( toPoint.x-fromPoint.x, toPoint.y-fromPoint.y );
		double distance = fromPoint.distance( toPoint ); // d.length; //

		System.out.println( "characterInput: "+characterInput );
		System.out.println( "character: "+character.getName() );
		System.out.println( "from: "+fromPoint );
		System.out.println( "to: "+toPoint );
		System.out.println( "d: "+d );
		System.out.println( "distance: "+distance );
		
//		Canvas.debug( "distance: "+distance );
//		Canvas.debug( "character.mc: "+character.mc );

//		Canvas.debug( "x scale: "+Canvas.getScaleX( character.mc ) );

//		if( (d.x < 0 && character.facingRight() ) || (d.x > 0 && !character.facingRight() ) ){
//			Canvas.debug( "x scale: "+Canvas.getScaleX( character.mc ) );
//			Canvas.debug( "character needs flip" );
//			Canvas.setScaleX( character.mc, -1 * Canvas.getScaleX( character.mc ) );
//		}
		
		// Put the two legs / feet in an array for better iteration.
		foot = new GraphicsNode[2];
		foot[0] = character.exposed.leg.appendage.getKey(0);
		foot[1] = character.inner.leg.appendage.getKey(0);
		
//		Canvas.debug( "foot[0] == foot[1]? "+(foot[0] == foot[1]) );
//		Canvas.debug( "exposed: "+character.exposed.leg.handOrFoot.mc);
//		Canvas.debug( "inner: "+character.inner.leg.handOrFoot.mc );
//		Canvas.debug( "foot[0] == foot[1]? "+(character.inner == character.exposed) );
//		Canvas.debug( "foot[0] == foot[1]? "+(character.rear == character.front) );
		
		// Reset leg and save the position of foot relative to entire sprite
		// measured in sprite's parent's coordinates.
		
		bodyPos = Canvas.getXY( character.body.getKey(0) );
		headPos = Canvas.getXY( character.head.getKey(0) );

		relFootPos = new Point2D.Float[2];
		fromFoot = new Point2D.Float[2];
		toFoot = new Point2D.Float[2];
		for( int i = 0; i < 2; i++ ) {
			
			fromFoot[i] = new Point2D.Float( 0, 0 );
			Canvas.localToLocal( foot[i], character.mc._parent, fromFoot[i] );
			
			relFootPos[i] = fromFoot[i].subtract( fromPoint );
			toFoot[i] = (Point2D.Float) toPoint.clone();
			toFoot[i].offset( relFootPos[i].x * pScale, relFootPos[i].y * pScale );
//			Canvas.debug("toFoot["+i+"]: "+toFoot[i]);
//			Canvas.debug("fromFoot["+i+"]: "+fromFoot[i]);
//			Canvas.debug("char scale: "+character.mc.transform.matrix.d);
		}
		
		// Set the stepLength. If no value is given, use a default.
		if( walk != null && walk.step0 != null && walk.step1 != null ) {
			if( walk.step0.pLength == walk.step1.pLength )
				stepLength = character.legHeight * walk.step0.pLength;
			else
				stepLength = character.legHeight * 0.5 * (walk.step0.pLength + walk.step1.pLength);
		}
//		else {
//			stepLength = (character.front.leg.full.mc._x - character.rear.leg.full.mc._x) * 1.5;
//			if( stepLength < 0 )
//				stepLength *= -1;
//		}
		stepLength *= character.mc._xscale / 100.0;
//		stepLength = character.legHeight * 2;
		
		

		// test: doubles as step number possible?
		//numberOfSteps = distance != 0 ? Math.ceil( distance / stepLength ) : 1;

		double oddNumberOfSteps = Math.abs( distance / stepLength );
		int ceilNumberOfSteps = (int) Math.ceil( oddNumberOfSteps );
		
		// Set number to ceiling
		// and to an ODD number, as there are to be two half-distance steps.
		// (The first and last step,
		// because this shortens the gap between legs while walking.)

//		Canvas.debug( "stepLength: "+stepLength );
//		Canvas.debug( "oddNumberOfSteps: "+oddNumberOfSteps );
		
		if( noOfSteps == 0 )
			noOfDistSteps = ceilNumberOfSteps;
		else
			noOfDistSteps = noOfSteps - 1;
		if( noOfDistSteps % 2 == 0 )
			noOfDistSteps++;

		// Calculate the adjusted step length.
		step = new Point2D.Float( d.x / noOfDistSteps, d.y / noOfDistSteps );
//		System.out.println( "step: "+step );

		// one odd step at half length
		oddStep = (Point2D.Float) step.clone();
		oddStep.x *= ( 0.5 );
		oddStep.y *= ( 0.5 );

		// Now count the two half-steps as full steps
		// for the legTweening, so that there is
		// an EVEN number of steps and both legs do
		// the same number of steps.
		noOfTimeSteps = noOfDistSteps + 1;
		
		if( duration == 0 )
			duration = noOfTimeSteps / 2 * ( walk.step0.duration + walk.step1.duration );
//		Canvas.debug( "noOfDistSteps: "+noOfDistSteps );
//		Canvas.debug( "noOfTimeSteps: "+noOfTimeSteps );
//		Canvas.debug( "distance: "+distance );
//		Canvas.debug( "duration: "+duration );
	}
	public void innerInit()  {
		System.out.println( "WalkAnim.innerInit()" );
//		character.init();
		
		// Save starting and ending point.
		if( characterInput != null )
			character = scene.getClusterModel( characterInput );
			
		if( fromInput != null )
			fromPoint = (Point2D.Float) Resolver.parseData( fromInput );
		else
			fromPoint = new Point2D.Float( character.mc._x, character.mc._y );
		
		if( toInput != null )
			toPoint = (Point2D.Float) Resolver.parseData( toInput );
		else
			toPoint = fromPoint;
		


//		character.rear.leg.useFloatingAnchor = true;
//		character.front.leg.useFloatingAnchor = true;
//		character.rear.leg.snapToAnchors();
//		character.front.leg.snapToAnchors();
//		
//		character.rear.leg.snapToFloatingAnchor = false;
//		character.front.leg.snapToFloatingAnchor = false;
		
		character.rear.leg.snapToAnchors();
		character.front.leg.snapToAnchors();
		
		character.front.arm.full.mc._rotation = 0;
		character.front.arm.upper.mc._rotation = 0;
		character.front.arm.lower.mc._rotation = 0;
		character.rear.arm.lower.mc._rotation = 0;

		
		calcDistance();
		calcTime();
	}
	
	protected void calcTime() {
		step0Time = walk.step0.duration;
		step1Time = walk.step1.duration;
		totalTime = (step0Time + step1Time) * noOfTimeSteps / 2;
		
		// The percentage of one step pair of the total walk time (static).
		pOneStep = 1 / noOfTimeSteps;
		pStepPair = pOneStep * 2;
		
		// Calc the percentage of the time that each step needs in a cycle of 2 steps (static).
		fullCycle = step0Time + step1Time;
		pStep0Time = step0Time / fullCycle;
		pStep1Time = step1Time / fullCycle;
		
		// The percentage of 1 distance step of the total percentage.
		pDistStep = 1 / noOfDistSteps;
		// The legs move at double speed, since they only move half the time.
		pDistStep2 = pDistStep*2;	
		
		noOfTimeStepsBy2 = noOfTimeSteps / 2;
	}
	
	protected double step0Time = 20, step1Time = 20, totalTime;
	protected double pOneStep, pStepPair;
	protected double fullCycle, pStep0Time, pStep1Time;
	protected double pDistStep, pDistStep2;
	protected int noOfTimeStepsBy2;
	
	public void innerGo( double  percentage ) {
		System.out.println( "WalkAnim.innerGo: "+percentage );
			
		// Number of completed step pairs.
		int compStepPairs = Math.floor( percentage / pStepPair );
		
		// The percentage of execution within the current step pair.
		double pWithinStepPair = (percentage - compStepPairs * pStepPair) / pStepPair;
//		// TODO: find out why there's a near-1 value instead of 0 sometimes: it caused an error at p=0.5
//		if( pWithinStepPair > 0.999999 )
//			pWithinStepPair = 0;

		// One step pair consists of 2 steps.
		// Calc which leg is moving.
		int step;
		Step currentStep;
		
		double pWithinStep;
		if( pWithinStepPair < pStep0Time ) {
			step = 0;
			currentStep = walk.step0;
			pWithinStep = pWithinStepPair / pStep0Time;
		} else {
			step = 1;
			currentStep = walk.step1;
			pWithinStep = ( pWithinStepPair - pStep0Time ) / pStep1Time;
		}

		// The percentage of execution within the current step.
		double pWithinStep0 = step == 0 ? pWithinStep : 1;
		double pWithinStep1 = step == 1 ? pWithinStep : 0;

		double pWithinStepEased, pWithinStep0Eased, pWithinStep1Eased;
		if( walk != null ) {
			pWithinStepEased = Easing.applyEasing( walk.xEasing, pWithinStep );
			pWithinStep0Eased = Easing.applyEasing( walk.step0.xEasing, pWithinStep0 );
			pWithinStep1Eased = Easing.applyEasing( walk.step1.xEasing, pWithinStep1 );
		}
		else {
			pWithinStepEased = pWithinStep;
			pWithinStep0Eased = pWithinStep0;
			pWithinStep1Eased = pWithinStep1;
		}
		
//		System.out.println( "pWithinStep1: "+pWithinStep1 );
//		System.out.println( "pWithinStep1Eased: "+pWithinStep1Eased );

		double pBody = WalkAnim.pDist( noOfTimeSteps, pDistStep, compStepPairs*2+step, pWithinStepEased, 0.5, 0.5, "body" );
		double pFoot0 = WalkAnim.pDist( noOfTimeStepsBy2, pDistStep2, compStepPairs, pWithinStep0Eased, 0.5, 1, "leg0" );
		double pFoot1 = WalkAnim.pDist( noOfTimeStepsBy2, pDistStep2, compStepPairs, pWithinStep1Eased, 1, 0.5, "leg1" );
		
		
		Point bodyPoint = Point.interpolate(toPoint,fromPoint,pBody);
//		System.out.println( "bodyPoint: "+bodyPoint );
		Point foot0Point = Point.interpolate(toFoot[0],fromFoot[0],pFoot0);
		Point foot1Point = Point.interpolate(toFoot[1],fromFoot[1],pFoot1);
//		System.out.println( "pFoot1: "+pFoot1 );
		
//		System.out.println( "bodyPoint: "+bodyPoint );
//		System.out.println( "foot0Point: "+foot0Point );
//		System.out.println( "foot1Point: "+foot1Point );
		Canvas.setXY( character.mc, bodyPoint.x, bodyPoint.y );
		
//		Canvas.debug( "walk.step0Anim: "+walk.step0Anim+", "+walk.step1Anim );
		if( walk != null) {
			double pWeightedStep = pWithinStep / 2;
			if( walk.step0Anim != null )
				walk.step0Anim.go( step == 0 ? pWeightedStep : pWeightedStep + 0.5 );
			if( walk.step1Anim != null )
				walk.step1Anim.go( step == 1 ? pWeightedStep : pWeightedStep + 0.5 );
		}
		
//		if( walk.step0 != null )
			//character.exposed.Leg.mc._yscale = 100 * (1 - walk.step0.yEasing.valueOf( pWithinStep0 ));
//			System.out.println( "scale: "+100 * (1 - walk.step0.yEasing.valueOf( pWithinStep0 )) );

		double yEased = currentStep.yEasing.valueOf( pWithinStep ) * currentStep.pHeight * character.legHeight;
		character.body.mc._y = bodyPos.y - walk.pBodyBounce * yEased;
		character.head.mc._y = headPos.y - walk.pHeadBounce * yEased;
		
		if( pScale != 0 ) {
//			int sgn = Canvas.getScaleX( character.mc () < 0 ? -1 : 1;
//			character.mc._yscale = initialScale * ( 1 + percentage * ( pScale - 1 ) );
//			character.mc._xscale = sgn * Math.abs( character.mc._yscale );
			int sgn = Canvas.getScaleX100( character.mc ) < 0 ? -1 : 1;
			Canvas.setScaleY100( character.mc, initialScale * ( 1 + percentage * ( pScale - 1 ) ) );
			Canvas.setScaleX100( character.mc, sgn * Math.abs( Canvas.getScaleY100( character.mc ) ) );
		}
//		Canvas.debug( "pScale: "+pScale );
//		Canvas.debug( "character.mc._yscal: "+character.mc._yscale );
		
//		Canvas.debug( "foot.mc: "+character.exposed.leg.handOrFoot.mc );
		
//		Canvas.localToLocal( character.mc._parent, character.exposed.leg.handOrFoot.mc._parent, foot0Point );
//		Canvas.localToLocal( character.mc._parent, character.inner.leg.handOrFoot.mc._parent, foot1Point );
//		
//		Canvas.toPoint( character.exposed.leg.handOrFoot.mc, foot0Point );
//		Canvas.toPoint( character.inner.leg.handOrFoot.mc, foot1Point );
		
		if( step == 0 ) {
			double ychange = (walk.step0.pHeight * walk.step0.yEasing.valueOf( pWithinStep ));
			foot0Point.y -= ychange * character.exposed.leg.fullLength();
		}
		if( step == 1 ) {
			double ychange = (walk.step1.pHeight * walk.step1.yEasing.valueOf( pWithinStep ));
//			Canvas.debug( "ychange: "+ychange );
			foot1Point.y -= ychange * character.exposed.leg.fullLength();
		}
		
//		character.exposed.leg.floatingAnchor._x = foot0Point.x - character.mc._x;
//		character.exposed.leg.floatingAnchor._y = foot0Point.y - character.mc._y;
//		character.exposed.leg.snapToFloatingAnchor = true;
//		character.inner.leg.floatingAnchor._x = foot1Point.x - character.mc._x;
//		character.inner.leg.floatingAnchor._y = foot1Point.y - character.mc._y;
//		character.inner.leg.snapToFloatingAnchor = true;
		
		
//		character.exposed.leg.snapToAnchors();
//		character.exposed.leg.snapToPoint( foot0Point, character.mc._parent );
//		character.inner.leg.snapToPoint( foot1Point, character.mc._parent );
		
	}
	
}
