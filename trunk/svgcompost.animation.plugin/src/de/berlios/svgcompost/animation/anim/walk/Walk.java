package de.berlios.svgcompost.animation.anim.walk;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.Skeleton;
import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class Walk {
	
	private static Logger log = Logger.getLogger(Walk.class);

	public String name;
	public Point2D.Float line;
	public Point2D.Float[] centerAbs;
	public Point2D.Float[][] footAbs;
	public Point2D.Float[] wrapperAbs;
	public Point2D.Float[] centerOffset;
	public String[] poseIds;
	public String modelName;
	int noOfPoses;
	
	public Point2D.Float[] centerRel;
	public Point2D.Float[][] footRel;

	public Point2D.Float centerStart;
	public Point2D.Float centerEnd;

	public Walk( int noOfPoses ) {
		initArrays( noOfPoses );
	}
	
	protected void initArrays( int noOfPoses ) {
		wrapperAbs = new Point2D.Float[noOfPoses];
		centerOffset = new Point2D.Float[noOfPoses];
		centerRel = new Point2D.Float[noOfPoses];
		footRel = new Point2D.Float[noOfPoses][2];
		poseIds = new String[noOfPoses];
		centerAbs = new Point2D.Float[noOfPoses];
		footAbs = new Point2D.Float[noOfPoses][2];
	}
	
	public void loadPoses( CanvasNode posesGroup, Skeleton model ) {
		noOfPoses = posesGroup.getSize();
		initArrays( noOfPoses );
//		walk.name = posesInput;
		modelName = model.getName();
		Bone leftFoot = model.getBone( "elfyLeftFoot" );
		Bone rightFoot = model.getBone( "elfyRightFoot" );

		CanvasNode[] keyMc = new CanvasNode[noOfPoses];
//		walk.poseIds = new String[noOfPoses];
		for (int i = 0; i < noOfPoses; i++) {
			keyMc[i] = posesGroup.get( i );
			poseIds[i] = keyMc[i].getSymbolId();
			wrapperAbs[i] = keyMc[i].getXY();
			footAbs[i][0] = leftFoot.getKey(i).getGlobalXY();
			footAbs[i][1] = rightFoot.getKey(i).getGlobalXY();
			centerAbs[i] = new Point2D.Float( (footAbs[i][0].x+footAbs[i][1].x)/2, (footAbs[i][0].y+footAbs[i][1].y)/2 );			
			Point2D.Float origin = keyMc[i].getXY();
			centerOffset[i] = new Point2D.Float( centerAbs[i].x-origin.x, centerAbs[i].y-origin.y );
		}
		centerStart = centerAbs[0];
		centerEnd = centerAbs[noOfPoses-1];
	}
	
	public void placePoses( CanvasNode stage, Point2D.Float start, Point2D.Float end ) {
		Canvas canvas = stage.getCanvas();
		CanvasNode[] keyMc = new CanvasNode[noOfPoses];
		
		
//		Point2D.Float startPoint = new Point2D.Float( centerOffset[noOfPoses-1] );
		
		for (int i = 0; i < noOfPoses; i++) {
			CanvasNode poseNode = canvas.getRoot().addSymbolInstance( poseIds[i], "pose"+i );
			keyMc[i] = poseNode;
			poseNode.setXY( centerAbs[i].x, centerAbs[i].y );
		}

		AffineTransform lineSystem = Walk.orthogonalSystem( start, end );
		
		Point2D.Float centerAbs = new Point2D.Float();
		for (int i = 0; i < noOfPoses; i++) {
			lineSystem.transform( centerRel[i], centerAbs );
			centerAbs.x -= centerOffset[i].x;
			centerAbs.y -= centerOffset[i].y;
			CanvasNode poseNode = canvas.getRoot().addSymbolInstance( poseIds[i], "pose"+i );
			keyMc[i] = poseNode;
			poseNode.setXY( centerAbs.x, centerAbs.y );
		}
	}
	
	public static void placeSteps( Canvas canvas, String posesId, Point2D.Float start, Point2D.Float end ) {
		canvas.getRoot().addSymbolInstance("bigrect","bigrect");
		ArrayList<CanvasNode> steps = new ArrayList<CanvasNode>();
		CanvasNode poses = canvas.getRoot().addSymbolInstance(posesId, posesId);
		int i=0;
		while( poses.getChild( "step"+i ) != null ) {
			steps.add( poses.getChild( "step"+i ) );
			i++;
		}
		log.debug("steps.size(): "+steps.size());
		Point2D.Float originalStart = steps.get(0).getXY();
		Point2D.Float originalEnd = steps.get(steps.size()-1).getXY();
		AffineTransform trafo = Walk.skewXbyYscaleY( originalStart, start, originalEnd, end );
		Point2D.Float xy_new = new Point2D.Float();
		for( CanvasNode node : steps ) {
			Point2D.Float xy = node.getXY();
			trafo.transform(xy, xy_new);
			node.setXY(xy_new);
		}

	}
	
	public static AffineTransform skewXbyYscaleY( Point2D.Float a_old, Point2D.Float a_new, Point2D.Float b_old, Point2D.Float b_new ) {
		Point2D.Float d_old = new Point2D.Float( b_old.x - a_old.x, b_old.y - a_old.y );
		Point2D.Float d_new = new Point2D.Float( b_new.x - a_new.x, b_new.y - a_new.y );
		float scaleX = 1; //d_new.x / d_old.x;
		float scaleY = d_new.y / d_old.y;
		float skewXbyY = (d_new.x - d_old.x) / d_old.y;
		float skewYbyX = 0; //(d_new.y - d_old.y) / d_old.x;
		AffineTransform trafo = AffineTransform.getTranslateInstance( -a_old.x, -a_old.y );
		trafo.preConcatenate( new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 ) );
		trafo.preConcatenate( AffineTransform.getTranslateInstance( a_new.x, a_new.y ) );
		
		if( new Double(Double.NEGATIVE_INFINITY).equals(trafo.getTranslateX()) ) {
			log.error( "d_old: "+d_old );
			log.error( "d_new: "+d_new );
			log.error( "is NaN" );
		}
		return trafo;
	}
	
	public static AffineTransform skewYbyXscaleX( Point2D.Float a_old, Point2D.Float a_new, Point2D.Float b_old, Point2D.Float b_new ) {
		Point2D.Float d_old = new Point2D.Float( b_old.x - a_old.x, b_old.y - a_old.y );
		Point2D.Float d_new = new Point2D.Float( b_new.x - a_new.x, b_new.y - a_new.y );
		float scaleX = d_new.x / d_old.x;
		float scaleY = 1; //d_new.y / d_old.y;
		float skewXbyY = 0; //(d_new.x - d_old.x) / d_old.y;
		float skewYbyX = (d_new.y - d_old.y) / d_old.x;
		AffineTransform trafo = AffineTransform.getTranslateInstance( -a_old.x, -a_old.y );
		trafo.preConcatenate( new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 ) );
		trafo.preConcatenate( AffineTransform.getTranslateInstance( a_new.x, a_new.y ) );
		return trafo;
	}
	
	protected static AffineTransform skewXbyYscaleY( Point2D.Float a, Point2D.Float b_old, Point2D.Float b_new ) {
		Point2D.Float d_old = new Point2D.Float( b_old.x - a.x, b_old.y - a.y );
		Point2D.Float d_new = new Point2D.Float( b_new.x - a.x, b_new.y - a.y );
		float scaleX = 1; //d_new.x / d_old.x;
		float scaleY = d_new.y / d_old.y;
		float skewXbyY = (d_new.x - d_old.x) / d_old.y;
		float skewYbyX = 0; //(d_new.y - d_old.y) / d_old.x;
		AffineTransform trafo = AffineTransform.getTranslateInstance( -a.x, -a.y );
		trafo.preConcatenate( new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 ) );
		trafo.preConcatenate( AffineTransform.getTranslateInstance( a.x, a.y ) );
		return trafo;
	}
	
	protected static AffineTransform skewYbyXscaleX( Point2D.Float a, Point2D.Float b_old, Point2D.Float b_new ) {
		Point2D.Float d_old = new Point2D.Float( b_old.x - a.x, b_old.y - a.y );
		Point2D.Float d_new = new Point2D.Float( b_new.x - a.x, b_new.y - a.y );
		float scaleX = d_new.x / d_old.x;
		float scaleY = 1; //d_new.y / d_old.y;
		float skewXbyY = 0; //(d_new.x - d_old.x) / d_old.y;
		float skewYbyX = (d_new.y - d_old.y) / d_old.x;
		AffineTransform trafo = AffineTransform.getTranslateInstance( -a.x, -a.y );
		trafo.preConcatenate( new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 ) );
		trafo.preConcatenate( AffineTransform.getTranslateInstance( a.x, a.y ) );
		return trafo;
	}
	
	/**
	 * Calcs a transform that transforms absolute coordinates into coordinates along a line,
	 * where x=0 means at the beginning of that line, and x=1 at the end.
	 * y coords are the absolute distance to the line at 90 degs.
	 * @param start
	 * @param end
	 * @return The transform representing the system.
	 */
	public static AffineTransform orthogonalSystem( Point2D.Float start, Point2D.Float end ) {
		float dx = end.x-start.x;
		float dy = end.y-start.y;
		float d = (float) Math.sqrt( dx*dx+dy*dy );
//		Point2D.Float line = new Point2D.Float( dx, dy );
		float cos = dx/d;
		float sin = dy/d;
		AffineTransform trafo = AffineTransform.getScaleInstance( d, 1 );
		AffineTransform rotate = new AffineTransform(cos,sin,-sin,cos,start.x,start.y);
		trafo.preConcatenate( rotate );
		return trafo;
	}
}
