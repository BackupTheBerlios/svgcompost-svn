package de.gerrit_karius.cutout.anim.chara;

import intrinsic.Math;
import intrinsic.MovieClip;
import intrinsic.flash.geom.Point;
import de.gerrit_karius.character.part.PartInfo;
import de.gerrit_karius.util.McUtil;
import de.gerrit_karius.util.Name;

/**
 * Represents a limb, e.g. an arm or a leg.
 * The limb can optionally have a joint in the middle, and an appendage at the end.
 * The term "appendage" here refers to a part appended to the limb, e.g. a hand or foot,
 * as opposed to the entire limb.
 * @author gerrit
 *
 */
public class JointedLimb extends Limb {

	public MovieClip floatingAnchor;
	
//	public boolean snapToFloatingAnchor = false;
//	public boolean alignToFloatingAnchor = true;
	
//	public boolean hasAppendage;
	public boolean appendageBehindLimb;
	public boolean stretches;
	public boolean bendsClockwise = true;

	public double fullLength() {
		return upperLength() + lowerLength();
	}
	public double upperLength() {
		return lower.anchorMan.getAnchor()._y;
	}
	public double lowerLength() {
		return appendage.anchorMan.getAnchor()._y;
	}
	
	public static JointedLimb create( PartInfo parent, String sideName, String partName, String upperSymbol, String lowerSymbol ) {
		String name = sideName+partName;
		JointedLimb limb = new JointedLimb();
		limb.full = PartInfo.createAnchoredPart( parent, name, null, parent, name );
		limb.upper = PartInfo.createPart( limb.full, Name._upper+name, upperSymbol );
		// TODO: use a generic anchor name for both arms and legs, e.g. "a1".
		limb.lower = PartInfo.createAnchoredPart( limb.full, Name._lower+name, lowerSymbol, limb.upper, partName );
//		System.out.println( "partName: "+partName );
		return limb;
	}
	
	public static JointedLimb createCCW( PartInfo parent, String sideName, String partName, String upperSymbol, String lowerSymbol ) {
		JointedLimb limb = create( parent, sideName, partName, upperSymbol, lowerSymbol );
		limb.bendsClockwise = false;
		return limb;
	}
	/**
	 * Creates a floating anchor on a specified MovieClip.
	 * On subsequent calls to snapToAnchors(), the limb will snap its appendage to this anchor.
	 * @param parent parent MovieClip for the anchor.
	 */
//	public void createAnchor( MovieClip parent ) {
//		int depth = parent.getNextHighestDepth();
//		floatingAnchor = parent.createEmptyMovieClip( "anchor"+depth, depth );
//		snapToFloatingAnchor = true;
//	}
//	
//	public void destroyAnchor() {
//		if( floatingAnchor != null ) {
//			floatingAnchor.removeMovieClip();
//			floatingAnchor = null;
//		}
//	}
	
	public void instantiate() {
//		full.instantiateEmpty( parent, prefix );
//		upper.instantiateWithImage( full.mc, prefix );
//		lower.instantiateWithImage( full.mc, prefix );
//		if( hasAppendage )
//			appendage.instantiateWithImage( full.mc, prefix );
		full.instantiate();
		upper.instantiate();
//		System.out.println( upper.imageMan.image );
		lower.instantiate();
		if( appendage != null ) {
//			System.out.println( "appendage.instantiate();" );
			appendage.instantiate();
		}
	}
	
	/**
	 * Snaps all limb parts to their respective anchors.
	 * If the floating anchor is used, the limb snaps so that the appendage is fitted to the anchor.
	 */
	public void snapToAnchors() {
		full.snapToAnchor();
		upper.snapToAnchor();
		lower.snapToAnchor();
		if( appendage != null ) {
//			System.out.println( "appendage.snapToAnchor();" );
			appendage.snapToAnchor();
		}
	}
	
	/**
	 * Rotates the limb parts so that the appendage is placed at a specific point.
	 * @param anchor The MovieClip anchor that the limb snaps to. 
	 */
	
	/**
	 * Rotates the limb parts so that the appendage is placed at a specific point.
	 * @param p The point that the limb snaps to. 
	 * @param system The coordinate system mc of that point.
	 */
	public void snapToPoint( Point p, MovieClip system ) {

		Point q = (Point) p.clone();
		McUtil.localToLocal( system, appendage.mc._parent, q );
		McUtil.setXYToPoint( appendage.mc, q );
//		McUtil.debug( "y before: "+handOrFoot.mc._y );
		
//		full.snapToAnchor();
		full.mc._rotation = 0;
		
		McUtil.localToLocal( system, full.mc, p );
		Point polar = McUtil.calcPolar( p );
		double radius = polar.x;
		double angle = polar.y;
		full.mc._rotation = - angle * McUtil.rad2deg;
		upper.mc._yscale = 100;

		if( radius > fullLength() ) {
			if( stretches ) {
				double scaleLength = radius / fullLength();
				upper.mc._yscale = 100 * scaleLength;
				lower.mc._yscale = 100 * scaleLength;
			}
			upper.mc._rotation = 0;
			lower.mc._rotation = 0;
		}
		else {
			
			double a = upperLength();
			double b = lowerLength();
			double c = radius;
			double aa = a*a;
			double bb = b*b;
			double cc = c*c;
			double shoulderAngle = Math.acos( (bb - aa - cc) / (-2 * a * c) );
			double elbowAngle = Math.acos( (aa - bb - cc) / (-2 * b * c) );
			if( bendsClockwise ) {
				shoulderAngle *= -1;
				elbowAngle *= -1;
			}
			upper.mc._rotation = - shoulderAngle * McUtil.rad2deg;
			lower.mc._rotation = elbowAngle * McUtil.rad2deg;
		}

		lower.snapToAnchor();
		appendage.snapToAnchor();
	}

}
