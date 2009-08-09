package de.berlios.svgcompost.animation.anim.walk;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import de.berlios.svgcompost.animation.anim.Anim;
import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.Skeleton;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class WalkDef extends Anim {

//	Point2D.Float[] center;
//	Point2D.Float[] centerRel;
//	Point2D.Float[][] foot;
//	Point2D.Float[][] footRel;
//	Point2D.Float start;
//	Point2D.Float end;
//	Point2D.Float line;
	protected String posesInput;
	
	protected ArrayList<String> poseIds;
	protected ArrayList<Float> poseDurations;
	
	public WalkDef() {
		poseIds = new ArrayList<String>();
		poseDurations = new ArrayList<Float>();
	}
	
	public String modelInput;
	
	public WalkDef model( String modelInput ) {
		this.modelInput = modelInput;
		return this;
	}
	public WalkDef pose( String poseInput ) {
		poseIds.add( poseInput );
		return this;
	}
	
	public WalkDef poses( String posesInput ) {
		this.posesInput = posesInput;
		return this;
	}
	
//	public WalkLoader dur( String durationInput ) {
//		float poseDuration = java.lang.Float.parseFloat( durationInput );
//		int index = poseIds.size()-1;
//		while( poseDurations.size() < index )
//			poseDurations.add( 0f );
//		poseDurations.add( index, poseDuration );
//		return this;
//	}
	
	protected Walk walk;
	
	protected int noOfPoses;
	
	protected Skeleton model;
	protected CanvasNode posesGroup;
	protected CanvasNode[] keyMc;
	protected Point2D.Float start;
	protected Point2D.Float end;
//	protected Point2D.Float[] center;
//	protected Point2D.Float[][] foot;
	
	public void prepare() {
		
		if( posesInput == null )
			return;
		
		loadPosesForWalk();
		
		loadPosesIntoModel();
		
		loadPositionsIntoWalk();
		
//		line = new Point2D.Float( end.x-start.x, end.y-start.y );
		
//		scene.addWalk( walk );
		

		posesGroup.removeNode();
	}
	
	protected CanvasNode[] loadPosesForWalk() {
		posesGroup = scene.getCanvas().getRoot().addSymbolInstance( posesInput, "poses" );
		noOfPoses = posesGroup.getSize();
		walk = new Walk( noOfPoses );
		walk.name = posesInput;
		walk.modelName = modelInput;
		keyMc = new CanvasNode[noOfPoses];
//		walk.poseIds = new String[noOfPoses];
		for (int i = 0; i < noOfPoses; i++) {
			keyMc[i] = posesGroup.get( i );
			walk.poseIds[i] = keyMc[i].getSymbolId();
			walk.wrapperAbs[i] = keyMc[i].getXY();
		}
		return keyMc;
	}
	
	protected void loadPosesIntoModel() {
//		model = scene.getModel( walk.modelName );
		for (int i = 0; i < keyMc.length; i++) {
			model.addRootKey( keyMc[i] );
		}
		model.setup();
		
//		center = new Point2D.Float[noOfPoses];
//		foot = new Point2D.Float[noOfPoses][2];		
//		walk.centerAbs = new Point2D.Float[noOfPoses];
//		walk.footAbs = new Point2D.Float[noOfPoses][2];		
		Bone leftFoot = model.getBone( "elfyLeftFoot" );
		Bone rightFoot = model.getBone( "elfyRightFoot" );
		
		for (int i = 0; i < noOfPoses; i++) {
//			foot[i][0] = leftFoot.getKey(i).getGlobalXY(  );
//			foot[i][1] = rightFoot.getKey(i).getGlobalXY(  );
//			center[i] = new Point2D.Float( (foot[i][0].x+foot[i][1].x)/2, (foot[i][0].y+foot[i][1].y)/2 );			
			walk.footAbs[i][0] = leftFoot.getKey(i).getGlobalXY(  );
			walk.footAbs[i][1] = rightFoot.getKey(i).getGlobalXY(  );
			walk.centerAbs[i] = new Point2D.Float( (walk.footAbs[i][0].x+walk.footAbs[i][1].x)/2, (walk.footAbs[i][0].y+walk.footAbs[i][1].y)/2 );			
		}
//		start = center[0];
//		end = center[noOfPoses-1];
		start = walk.centerAbs[0];
		end = walk.centerAbs[noOfPoses-1];
	}
	
	protected void loadPositionsIntoWalk() {
		AffineTransform lineSystem = Walk.orthogonalSystem( start, end );
		
//		walk.centerOffset = new Point2D.Float[noOfPoses];
//		walk.centerRel = new Point2D.Float[noOfPoses];
//		walk.footRel = new Point2D.Float[noOfPoses][];
		
		for (int i = 0; i < noOfPoses; i++) {
			Point2D.Float origin = keyMc[i].getXY();
//			System.out.println( "origin["+i+"]: "+origin );
//			walk.centerOffset[i] = new Point2D.Float( center[i].x-origin.x, center[i].y-origin.y );
			walk.centerOffset[i] = new Point2D.Float( walk.centerAbs[i].x-origin.x, walk.centerAbs[i].y-origin.y );
			walk.centerRel[i] = new Point2D.Float();
			walk.footRel[i] = new Point2D.Float[] { new Point2D.Float(), new Point2D.Float() };
			
			try {
//				System.out.println( "center["+i+"]: "+center[i] );
//				lineSystem.inverseTransform( center[i], walk.centerRel[i] );
//				lineSystem.inverseTransform( foot[i][0], walk.footRel[i][0] );
//				lineSystem.inverseTransform( foot[i][1], walk.footRel[i][1] );
				lineSystem.inverseTransform( walk.centerAbs[i], walk.centerRel[i] );
				lineSystem.inverseTransform( walk.footAbs[i][0], walk.footRel[i][0] );
				lineSystem.inverseTransform( walk.footAbs[i][1], walk.footRel[i][1] );
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void animate(double percentage) {
	}
}
