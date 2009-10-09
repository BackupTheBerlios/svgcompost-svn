package de.berlios.svgcompost.animation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.composite.Scene;
import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.export.Export;
import de.berlios.svgcompost.animation.export.binary.FlagstoneExport;
import de.berlios.svgcompost.animation.timeline.Timeline;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);

//	public static String infile = "res/elfy-walk-connect.svg";
//	public static String infile = "res/hansel-scene-02.svg";

	public static String infile = "res/elfy-timeline-layers-keyframe.svg";
//	public static String infile = "res/steps.svg";

//	public static String placeObject = "elfyEar";
	
	public static String outfile = "res/binary.swf"; //"res/swfml.xml"; //"res/out"; //
	
//	public static CanvasNode circle;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("start application");

		BridgeContext ctx = GraphicsBuilder.readLibrary(infile);

		Canvas canvas = new Canvas( ctx );
		canvas.setLibrary( ctx );
		Export capture = new FlagstoneExport( canvas );
//		SwfmlBasicExport capture = new SwfmlBasicExport( canvas );
//		SwfmlShapeImport shapeImport = new SwfmlShapeImport(importFile);
//		capture.setShapeImport( shapeImport );
		
//		ArrayList<String> layerIds = SvgDocumentParser.parseSvgDocument(doc);
		
		Timeline timeline = canvas.getLibrary().createTimeline();
		Scene scene = canvas.getLibrary().createAnimsForTimeline(timeline);
		
		
//		Scene scene = new Scene( canvas );
//		canvas.getRoot().setXY(canvas.width/2, canvas.height/2);
		// TODO: frame origin is centered in swf symbols and top left for main movie
		// keep that in mind!

//		log.debug("Define walk");
//		Walk elfyWalk = canvas.getLibrary().getWalk("elfyWalkPoses", "elfy");
//		log.debug("Set up walk");
//		WalkSetup walkSetup = new WalkSetup().poses( "elfyWalkPoses" ).start( "100,-100" ).end( "120,300" );
//		walkSetup.setDurationInSeconds( 10 );
//		scene.addAnim( walkSetup );


//		ArrayList<String> keyframeIds = canvas.getLibrary().getFrameIds( "elfyWalkPoses" );
//		Parallel par = canvas.getLibrary().createAnimFromKeyframeIds( canvas.getRoot(), keyframeIds);
//		scene.addAnim(par);
		
//		ArrayList<String> keyframeIds = canvas.getLibrary().getFrameIds( "elfyWalkPoses" );
		
		Point2D.Float start = new Point2D.Float(-50,-200);
		Point2D.Float end = new Point2D.Float(50,-200);
//		Parallel par = canvas.getLibrary().createWalkAnim(canvas.getRoot(), "elfyWalkPoses", "elfy", start, end);
//		Parallel par = canvas.getLibrary().createWalkAnim(timeline.getLayers().get(0), "elfy", start, end);
//		scene.addAnim(par);
		scene.setDurationInSeconds(10);
		
//		Walk.placeSteps(canvas, "steps", start, end);
//		scene.addAnim( new Wait(10) );
		

		AnimControl ctrl = new AnimControl( scene );
		log.debug("scene.getDurationinSeconds() = "+scene.getDurationInSeconds());
		ctrl.setCapture( capture );

		
		int frame = 1;
		while( ctrl.nextFrame() ) {
			log.debug( "Frame: "+(frame++) );
		}
		
//		CompositeGraphicsNode canvasRoot = canvas.getRoot();
//		GraphicsNode gNodeA = canvas.symbolNode( canvasRoot, "elfyPose1", "A" );
//		Canvas.setTransform( gNodeA, AffineTransform.getScaleInstance( 0.2, 0.2 ) );
//		Canvas.setXY( gNodeA, 120, 250 );
		
//		GraphicsNode gNodeA = canvas.symbolNode( canvasRoot, "orangeBone", "A" );
//		Canvas.setXY( gNodeA, 100, 300 );
//		GraphicsNode gNodeB = canvas.symbolNode( canvasRoot, "orangeBone", "B" );
//		Canvas.setXY( gNodeB, 120, 70 );
//		GraphicsNode gNodeA2 = canvas.symbolNode( canvasRoot, "orangeBone", "A2" );
//		Canvas.setTransform( gNodeA2, AffineTransform.getRotateInstance( Math.PI/4 ) );
//		Canvas.setXY( gNodeA2, 200, 400 );
//		System.out.println( Canvas.getTransform( gNodeA2 ) );
//		GraphicsNode gNodeB2 = canvas.symbolNode( canvasRoot, "orangeBone", "B2" );
//		
//		AffineTransform t = Canvas.getTransform( gNodeB );
//		subtractFromMatrix( Canvas.getTransform( gNodeA ), t );
//		// t = B rel to A.
//		System.out.println( "B rel to A: "+t );
//		
//		System.out.println( "A2 global: "+Canvas.getTransform( gNodeA2 ) );
//		
//		t.preConcatenate( Canvas.getTransform( gNodeA2 ) );
//		// global to A2
//		
//		Canvas.setTransform( gNodeB2, t );
//		
//		TreeWalker.traverseGraphicsTree( canvasRoot, 0 );
		
		// Capture a single test frame.
//		capture.captureFrame();

		
//		capture.end();
//		capture.writeSwfmlFile( outfile );
		
		
//		CompositeGraphicsNode canvasRoot = canvas.getRoot();
//		GraphicsNode gNode = canvas.symbolNode( canvasRoot, placeObject, "Copy of Layer 2" );
//		AffineTransform trafo = AffineTransform.getTranslateInstance(250,150);
//		Canvas.setTransform( gNode, trafo );
//		
//		float x = Canvas.getX( gNode );
//		Canvas.setX( gNode, x+100 );
//		
//		Capture capture = new Capture( canvas );
//		capture.captureFrame();
//		capture.captureShapes();
		
		capture.end();
		capture.writeOutput( outfile );
		log.info("done.");
	}

	public static void subtractFromMatrix( AffineTransform sourceMatrix, AffineTransform targetMatrix ) {
//		AffineTransform targetClone = (AffineTransform) targetMatrix.clone();
//		AffineTransform sourceClone = (AffineTransform) sourceMatrix.clone();
		sourceMatrix = getInverse( sourceMatrix );
//		sourceMatrix.invert();
//		System.out.println( "sourceMatrix: "+sourceMatrix );
//		System.out.println( "targetMatrix: "+targetMatrix );
		targetMatrix.concatenate( sourceMatrix );
		
//		AffineTransform relClone = (AffineTransform) targetMatrix.clone();
//		// Check:
//		relClone.concatenate( sourceClone );
//		System.out.println( "relClone: "+relClone );
//		System.out.println( "trgClone: "+targetClone );
	}
	
	public static AffineTransform getInverse(AffineTransform transform) {
		AffineTransform inverse = null;
		try {
			inverse = transform.createInverse();
		} catch(Exception e){
			e.printStackTrace();
		}
		AffineTransform clone = (AffineTransform) transform.clone();
		clone.concatenate( inverse );
		System.out.println( clone );
		return inverse;
	}
}
