package de.gerrit_karius.cutout.anim.walk;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.gerrit_karius.cutout.anim.Anim;
import de.gerrit_karius.cutout.anim.composite.Parallel;
import de.gerrit_karius.cutout.anim.skeleton.Bone;
import de.gerrit_karius.cutout.anim.skeleton.Skeleton;
import de.gerrit_karius.cutout.canvas.CanvasNode;

public class WalkSetup extends Anim {
	
	private static Logger log = Logger.getLogger(WalkSetup.class);

	protected Parallel keyframeAnim;

//	Point2D.Float[] center;
//	Point2D.Float[] centerRel;
//	Point2D.Float[][] foot;
//	Point2D.Float[][] footRel;
	protected Skeleton model;
	protected CanvasNode[] keyMc;
	protected Point2D.Float start;
	protected Point2D.Float end;
	protected Point2D.Float[] center;
	protected Point2D.Float[][] foot;
	
	float stepLength;
	
	int noOfPoses;
	AffineTransform lineSystem;
	
	Walk walk;
	private String posesInput;
	private String startInput;
	private String endInput;
	
	public WalkSetup start( String startInput ) {
		this.startInput = startInput;
		return this;
	}
	public WalkSetup end( String endInput ) {
		this.endInput = endInput;
		return this;
	}
	public WalkSetup poses( String posesInput ) {
		this.posesInput = posesInput;
		return this;
	}

	/*
	protected void innerInit_old() {
		System.out.println( "WalkSetup.innerInit" );
		
		walk = scene.getWalk( posesInput );
		start = readPoint( startInput );
		end = readPoint( endInput );
		
//		float dx = end.x-start.x;
//		float dy = end.y-start.y;
//		float d = (float) Math.sqrt( dx*dx+dy*dy );
		
//		line = new Point2D.Float( dx, dy );
		lineSystem = Walk.orthogonalSystem( start, end );
		
		noOfPoses = walk.poses.length;
		CanvasNode[] keyMc = new CanvasNode[noOfPoses];
//		int noOfSteps = (int) Math.ceil(d/stepLength);
//		int noOfSteps = walk.poses.length-1;
		
		Point2D.Float center = new Point2D.Float();
		
		Skeleton model = scene.getModel( walk.model );

		for (int i = 0; i < noOfPoses; i++) {
//			int pose = i%noOfPoses;
			lineSystem.transform( walk.centerRel[i], center );
			center.x -= walk.centerOffset[i].x;
			center.y -= walk.centerOffset[i].y;
			CanvasNode poseNode = canvas.symbolNode( canvas.getRoot(), walk.poses[i], "pose"+i );
			keyMc[i] = poseNode;
			poseNode.setXY( center.x, center.y );
			
//			setToTransformedPoint(  );
			
			// TODO: calc abs from rel and line transform
		}
		
		
		model.loadRootKeys( keyMc );
		Point2D.Float position = new Point2D.Float();
		
		String[] footNames = new String[] {"elfyLeftFoot", "elfyRightFoot"};
		String[] legNames = new String[] {"elfyLeftUpperLeg", "elfyRightUpperLeg"};
		for (int i = 0; i < noOfPoses; i++) {
			for (int j = 0; j < footNames.length; j++) {
				Bone foot = model.getPart( footNames[j] );
				Bone leg = model.getPart( legNames[j] );
				lineSystem.transform( walk.footRel[i][j], position );

//				position.x -= walk.centerOffset[i].x;
//				position.y -= walk.centerOffset[i].y;

				position.x -= foot.getKey(i).getX(  );
				position.y -= foot.getKey(i).getY(  );

				Point2D.Float start = foot.getKey(i).getLocalXY( model.getKey(i) );
				Point2D.Float end = foot.getKey(i).getLocalXY( model.getKey(i) );

				setToTransformedPoint( foot, i, position );
				
				Point2D.Float target = foot.getKey(i).getLocalXY( model.getKey(i) );
				
				float x = target.x - start.x;
				float y = target.y - start.y;
				float dx = target.x - end.x;
				float dy = target.y - end.y;
				
				float scaleX = (x + dx) / x;
				float scaleY = (y + dy) / y;
				float skewXbyY = dx / y;
				float skewYbyX = dy / x;
				
				AffineTransform skew = new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 );
//				System.out.println( "skew: "+skew );
				model.applyTransformOnWrapperLevel( skew, i, foot, leg );

//				CanvasNode gNode = leftFoot.getKey(i);
//				CanvasNode parent = Canvas.getParent( gNode );
//				AffineTransform parentSystem = Canvas.getTransform( parent );
//				parentSystem.transform( position, position );
//				Canvas.setXY( gNode, position.x, position.y );
			}
		}
	}
	*/
	
	@Override
	public void prepare() {
		log.error("prepare");
		walk = scene.getCanvas().getLibrary().getWalk( posesInput, "elfy" );
//		System.out.println( "walk.centerOffset: "+walk.centerOffset );
		start = readPoint( startInput );
		end = readPoint( endInput );
		
		lineSystem = Walk.orthogonalSystem( start, end );
		
		noOfPoses = walk.poseIds.length;
		keyMc = new CanvasNode[noOfPoses];
		
//		posesGroup = canvas.symbolNode( canvas.getRoot(), posesInput, "poses" );
//		posesGroup.setX( 1000 );
//		noOfPoses = posesGroup.getSize();

		Point2D.Float centerAbs = new Point2D.Float();
		for (int i = 0; i < noOfPoses; i++) {
			lineSystem.transform( walk.centerRel[i], centerAbs );
			centerAbs.x -= walk.centerOffset[i].x;
			centerAbs.y -= walk.centerOffset[i].y;
			CanvasNode poseNode = scene.getCanvas().getRoot().addSymbolInstance( walk.poseIds[i], "pose"+i );
			keyMc[i] = poseNode;
			poseNode.setXY( centerAbs.x, centerAbs.y );
		}
		
		
		loadPosesIntoModel();
		
		model.discardKeys();
		
		ArrayList<CanvasNode> keyframes = new ArrayList<CanvasNode>();
		for (CanvasNode keyframe : keyMc)
			keyframes.add(keyframe);
		
		keyframeAnim = scene.getCanvas().getLibrary().createAnimFromKeyframes(keyframes);
		keyframeAnim.prepare();
		
//		Skeleton model = scene.getModel( walk.model );
//		model.loadRootKeys( keyMc );
//		
//		Point2D.Float[] center = new Point2D.Float[noOfPoses];
//		foot = new Point2D.Float[noOfPoses][2];
//		Bone leftFoot = model.getPart( "elfyLeftFoot" );
//		Bone rightFoot = model.getPart( "elfyRightFoot" );
//		
//		for (int i = 0; i < noOfPoses; i++) {
//			foot[i][0] = leftFoot.getKey(i).getGlobalXY();
//			foot[i][1] = rightFoot.getKey(i).getGlobalXY();
//			center[i] = new Point2D.Float( (foot[i][0].x+foot[i][1].x)/2, (foot[i][0].y+foot[i][1].y)/2 );			
//		}
//		start = center[0];
//		end = center[noOfPoses-1];
		
		lineSystem = Walk.orthogonalSystem( new Point2D.Float(0,100), new Point2D.Float(400,300) );
		
		String[] footNames = new String[] {"elfyLeftFoot", "elfyRightFoot"};
		String[] legNames = new String[] {"elfyLeftUpperLeg", "elfyRightUpperLeg"};
//		Point2D.Float position = new Point2D.Float();

		for (int i = 0; i < noOfPoses; i++) {
			lineSystem.transform( walk.centerRel[i], center[i] ); // line to global
//			System.out.println( "center["+i+"]: "+center[i] );
			// global = local
			center[i].x -= walk.centerOffset[i].x;
			center[i].y -= walk.centerOffset[i].y;
//			System.out.println( "origin["+i+"]: "+center[i] );
//			System.out.println( "walk.centerOffset["+i+"].y: "+walk.centerOffset[i].y );
//			System.out.println( "keyMc["+i+"]: "+keyMc[i] );
			keyMc[i].setXY( center[i].x, center[i].y );
			
			lineSystem.transform( walk.footRel[i][0], foot[i][0] );
			lineSystem.transform( walk.footRel[i][1], foot[i][1] );
			
			for (int j = 0; j < footNames.length; j++) {
				Bone footNode = model.getBone( footNames[j] );
				Bone leg = model.getBone( legNames[j] );
				
				Point2D.Float a = leg.getKey(i).getLocalXY( model.getKey(i) );
				Point2D.Float b_old = footNode.getKey(i).getLocalXY( model.getKey(i) );
	
				footNode.setRecursiveGlobalXY( foot[i][j], i );
				
				Point2D.Float b_new = footNode.getKey(i).getLocalXY( model.getKey(i) );
				
//				Point2D.Float d_old = new Point2D.Float( b_old.x - a.x, b_old.y - a.y );
//				Point2D.Float d_new = new Point2D.Float( b_new.x - a.x, b_new.y - a.y );
//				float scaleX = 1; //d_new.x / d_old.x;
//				float scaleY = d_new.y / d_old.y;
//				float skewXbyY = (d_new.x - d_old.x) / d_old.y;
//				float skewYbyX = 0; //(d_new.y - d_old.y) / d_old.x;
//				
//				AffineTransform trafo = AffineTransform.getTranslateInstance( -a.x, -a.y );
//				trafo.preConcatenate( new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, 0, 0 ) );
//				trafo.preConcatenate( AffineTransform.getTranslateInstance( a.x, a.y ) );
//				
//				AffineTransform skew = trafo; //new AffineTransform( scaleX, skewYbyX, skewXbyY, scaleY, translateX, translateY );
				
				AffineTransform skew = createSkewTransform( a, b_old, b_new );
				
//				System.out.println( "skew: "+skew );
				model.applyTransformOnWrapperLevel( skew, i, footNode, leg );
//				System.out.println( "a before: "+a );
				skew.transform( a, a );
//				System.out.println( "a after:  "+a );
				
//				System.out.println( "b before: "+b_old );
//				System.out.println( "b target: "+b_new );
				Point2D.Float p = new Point2D.Float();
				skew.transform( b_old, p );
//				System.out.println( "b after:  "+p );
//				System.out.println();
			}
			
		}

	}
	
	protected static AffineTransform createSkewTransform( Point2D.Float a, Point2D.Float b_old, Point2D.Float b_new ) {
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
	
	protected void loadPosesIntoModel() {
		model = scene.getCanvas().getLibrary().getModel( walk.modelName );
		for (int i = 0; i < keyMc.length; i++) {
			model.addRootKey( keyMc[i] );
		}
		model.setup();
		
		center = new Point2D.Float[noOfPoses];
		foot = new Point2D.Float[noOfPoses][2];		
		Bone leftFoot = model.getBone( "elfyLeftFoot" );
		Bone rightFoot = model.getBone( "elfyRightFoot" );
		
		for (int i = 0; i < noOfPoses; i++) {
			foot[i][0] = leftFoot.getKey(i).getGlobalXY();
			foot[i][1] = rightFoot.getKey(i).getGlobalXY();
			center[i] = new Point2D.Float( (foot[i][0].x+foot[i][1].x)/2, (foot[i][0].y+foot[i][1].y)/2 );			
		}
		start = center[0];
		end = center[noOfPoses-1];
	}
	
	/**
	 * Projects a global base point into a gNode's coordinate system, and then sets
	 * x and y of the gNode to the transformed point.
	 */
	public void setToTransformedPoint( Bone model, int i, Point2D.Float globalTranslate ) {
		CanvasNode gNode = model.getKey(i);
		
		Point2D.Float position = gNode.getXY();
		CanvasNode parent = gNode.getParent();
		AffineTransform parentSystem = parent.getGlobalTransform();
		parentSystem.transform( position, position );
		System.out.println( "globTrans: "+globalTranslate );
		position.x += globalTranslate.x;
		position.y += globalTranslate.y;
		try {
			parentSystem.inverseTransform( position, position );
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		gNode.setXY( position.x, position.y );
		
		for (int j = 0; j < model.size(); j++) {
			setToTransformedPoint( model.get(j), i, globalTranslate );
		}
	}
	
	public static Point2D.Float readPoint( String fromString ) {
		String[] parts = fromString.split( "," );
		return new Point2D.Float( Float.parseFloat( parts[0] ), Float.parseFloat( parts[0] ) );
	}
	
	@Override
	protected void animate(double percentage) {
		keyframeAnim.animateAtTime(percentage);
	}
	
	/*
	public static AffineTransform orthogonalSystem( Point2D.Float start, Point2D.Float end ) {
		float dx = end.x-start.x;
		float dy = end.y-start.y;
		float d = (float) Math.sqrt( dx*dx+dy*dy );
//		Point2D.Float line = new Point2D.Float( dx, dy );
		float cos = dx/d;
		float sin = dy/d;
		AffineTransform trafo = AffineTransform.getScaleInstance( d, 1 );
//		AffineTransform trafo = AffineTransform.getScaleInstance( 1/d, 1 );
		AffineTransform rotate = new AffineTransform(cos,sin,-sin,cos,start.x,start.y);
		trafo.preConcatenate( rotate );
		return trafo;
	}
	*/
}
