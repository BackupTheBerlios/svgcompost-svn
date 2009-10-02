package de.berlios.svgcompost.animation.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGStylable;

import de.berlios.svgcompost.animation.anim.chara.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.chara.skeleton.KeyframeAnim;
import de.berlios.svgcompost.animation.anim.chara.skeleton.Skeleton;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonFactory;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;
import de.berlios.svgcompost.animation.anim.composite.Parallel;
import de.berlios.svgcompost.animation.anim.composite.Sequence;
import de.berlios.svgcompost.animation.anim.easing.Quadratic;

public class Library {
	
	private static Logger log = Logger.getLogger(Library.class);

	public static final String inkscapeURI = "http://www.inkscape.org/namespaces/inkscape";
//	public static final NameSpace inkscapeNS = new NameSpace( "inkscape", inkscapeURI );
	public static final String groupmode = "groupmode";
	public static final String layer = "layer";

	protected Canvas libraryCanvas;

	protected HashMap<String,Skeleton> models = new HashMap<String,Skeleton>();
	
	public Library( Canvas canvas ) {
		this.libraryCanvas = canvas;
	}
	
	// TODO: simplify
	// FIXME: limbs connect to wrong points
	public Parallel createWalkAnim( CanvasNode stage, String posesId, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = getModel(modelName);
		CanvasNode posesNode = stage.addSymbolInstance(posesId, posesId);
		List<CanvasNode> poses = posesNode.getChildListCopy();

		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for(CanvasNode keyframe : poses) {
			keyframe./*getSkeletonKeys().*/applySkeleton(model,skeletonKeys);
			skeletonKeys = keyframe.getSkeletonKeys();
		}

		ArrayList<Bone> feet = new ArrayList<Bone>();
		for( int i=0; i<model.connectorSize(); i++ ) {
			feet.add( model.getConnector(i).getTarget() );
		}

		int noOfPoses = poses.size();
		int lastPose = noOfPoses-1;
		
		for( Skeleton skeleton : poses.get(0).getSkeletonKeys().keySet() )
			skeleton.setupLimbTweening(poses);

		Point2D.Float originalStart = calcCenterPoint( feet, poses.get(0), posesNode );
		Point2D.Float originalEnd = calcCenterPoint( feet, poses.get(lastPose), posesNode );
		AffineTransform trafo = skewYbyXscaleX( originalStart, start, originalEnd, end );
		
		Point2D.Float xy_new = new Point2D.Float();
		for( int i=0; i<model.size(); i++ ) {
			Bone topLevelBone = model.get(i);
			log.debug("topLevelBone: "+topLevelBone.getName());
			for( int j=0; j<noOfPoses; j++ ) {
				SkeletonKey frameLink = poses.get(j).getSkeletonKey(model); 
				CanvasNode node = frameLink.getNodeForBone(topLevelBone);
				Point2D.Float xy = node.getLocalXY( posesNode );
				trafo.transform(xy, xy_new);
				// TODO: read the position the connectors connect to BEFORE the feet get moved
				topLevelBone.setRecursiveLocalXY(xy_new, frameLink, posesNode);
			}
		}
		
		CanvasNode startRect = stage.addSymbolInstance("redrect", "start");
		trafo.transform(originalStart, xy_new);
		startRect.setLocalXY(xy_new, posesNode);
		log.debug("start transformed: "+xy_new);
		log.debug("startRect.getGlobalXY(): "+startRect.getGlobalXY());

		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( poses.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
			
		for(CanvasNode frame : poses)
			for( Skeleton skeleton : frame.getSkeletonKeys().keySet() )
				skeleton.calcKeyMatrices(frame.getSkeletonKey(skeleton));			
		for(int i=0; i<poses.size(); i++)
			for( Skeleton skeleton : poses.get(i).getSkeletonKeys().keySet() )
				skeleton.setupTweening(poses, i);

		for(int i=0; i<poses.size()-1; i++)
			for(Skeleton skeleton : modelsByName.values())
				seqsByModel.get(skeleton).addAnim( createTweeningAnim( skeleton, poses, i ) );
	
		for(CanvasNode keyframe : poses)
			keyframe.setVisible(false);
		
		return par;
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
	
	public static Point2D.Float calcCenterPoint( ArrayList<Bone> bones, CanvasNode frame, CanvasNode system ) {
		float x = 0;
		float y = 0;
		for (Bone bone : bones) {
			Point2D.Float xy = frame.getSkeletonKey(bone.getSkeleton()).getNodeForBone(bone).getLocalXY( system );
			x += xy.x;
			y += xy.y;
		}
		x /= bones.size();
		y /= bones.size();
		return new Point2D.Float( x, y );
	}

	/**
	 * Creates a new model skeleton by parsing the hierarchy of a graphics node
	 * with the specified id.
	 * @param modelName The id of the SVG element representing the model.
	 */
	public Skeleton getModel( String modelName ) {
		Skeleton model = models.get( modelName );
		if( model != null )
			return model;
		log.debug("Model '"+modelName+"' referenced for the first time.");
		CanvasNode modelNode = libraryCanvas.getRoot().addSymbolInstance( modelName, modelName );
		model = SkeletonFactory.createSkeleton( modelNode );
		
		modelNode.removeNode();
		models.put(modelName, model);
		return model;
	}

	/**
	 * Parses an SVG element to determine its layer children, which are
	 * interpreted as animation frames.
	 * @param symbolId
	 * @return
	 */
	public ArrayList<String> getFrameIds( String symbolId ) {
		Document doc = libraryCanvas.getSourceDoc();
		log.debug("doc: "+doc);
		ArrayList<String> layerIds = new ArrayList<String>();
		Element symbolElement = doc.getElementById( symbolId ); 
		log.debug("symbolElement: "+symbolElement );
		if( symbolElement == null )
			log.warn( "Couldn't find symbol with id "+symbolId );
		NodeList list = symbolElement.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node node = list.item( i );
			if( node instanceof Element ) {
				Element child = (Element) node;
				if( child.getAttributeNS( inkscapeURI, groupmode ).equals( layer ) ) {
					layerIds.add( child.getAttribute( "id" ) );
				}
			}
		}		
		return layerIds;
	}
	
	public Parallel createAnimFromKeyframeIds( CanvasNode stage, ArrayList<String> keyframeIds ) {
		ArrayList<CanvasNode> keyframes = new ArrayList<CanvasNode>();
		for (String keyframeId : keyframeIds) {
			setDisplayAttributeToInline( stage.getCanvas().getSourceDoc().getElementById(keyframeId) );
			keyframes.add( stage.addSymbolInstance( keyframeId, keyframeId ) );
		}
		return createAnimFromKeyframes( keyframes );
	}
	
	/**
	 * Creates parallel animation sequences over a set of keyframes.
	 * All referenced models get their own animation sequence. 
	 * @param keyframes
	 * @return
	 */
	public Parallel createAnimFromKeyframes( ArrayList<CanvasNode> keyframes ) {
		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for(CanvasNode keyframe : keyframes) {
			for(Skeleton skeleton : modelsByName.values())
				keyframe./*getSkeletonKeys().*/applySkeleton(skeleton, skeletonKeys);
			skeletonKeys = keyframe.getSkeletonKeys();
		}
			
		SkeletonKey.setupTweening(keyframes);

		for(int i=0; i<keyframes.size()-1; i++)
			for(Skeleton skeleton : modelsByName.values())
				seqsByModel.get(skeleton).addAnim( createTweeningAnim( skeleton, keyframes, i ) );
	
		for(CanvasNode keyframe : keyframes)
			keyframe.setVisible(false);
		
		return par;
	}
	
	/**
	 * Creates an animation for the specified model, from the specified keyframe in the given keyframe sequence
	 * to the next keyframe in the list.
	 * @param model
	 * @param keyframes
	 * @param key
	 * @return
	 */
	private static KeyframeAnim createTweeningAnim(Skeleton model, List<CanvasNode> keyframes, int key) {
		CanvasNode keyframe = keyframes.get(key);
		KeyframeAnim tweenAnim = new KeyframeAnim( model, keyframes, key, keyframe, keyframes.get(key+1) );
		SVGElement frameElement = (SVGElement) keyframe.getCanvas().getSourceDoc().getElementById( keyframe.getSymbolId() );
		String duration = frameElement.getAttribute( "duration" );
		if( duration == null || duration.equals( "" ) )
			tweenAnim.setDurationInSeconds( 1 );
		else
			tweenAnim.setDurationInSeconds( Double.parseDouble( duration ) );
		String easing = frameElement.getAttribute( "easing" );
		if( easing == null || easing.equals( "" ) )
			tweenAnim.setEasing( Quadratic._inOut );
		else
			tweenAnim.setEasing( Quadratic._inOut );
//		String align = frameElement.getAttribute( "align" );
		return tweenAnim;
	}

	/**
	 * When editing keyframes, all but one are usually made invisible.
	 * In that case, however, no graphics nodes are constructed by the Batik framework.
	 * In order to access the keyframes, all must be made visible by setting the display attribute
	 * to inline mode. 
	 */
	private static void setDisplayAttributeToInline( Element element ) {
		if( element instanceof SVGStylable ) {
			CSSStyleDeclaration style = ((SVGStylable)element).getStyle();
			if( style.getPropertyValue("display").equals("none") )
				style.setProperty("display", "inline", "");
		}
		else {
			if( element.getAttribute("display").equals("none") )
				element.setAttribute("display", "inline");
		}
	}
	
	/**
	 * Reads all model references from the specified frame.
	 */
	private HashMap<String,Skeleton> extractModelsFromDeclaration( CanvasNode referencingFrame ) {
		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
		SVGElement frameElement = (SVGElement) referencingFrame.getCanvas().getSourceDoc().getElementById( referencingFrame.getSymbolId() );
		String modelReference = frameElement.getAttribute("model");
		
		if( modelReference == null || "".equals( modelReference ) ) {
			log.error( "couldn't find 'model' declaration in first keyframe: "+referencingFrame.getName() );
			return modelsByName;
		}
		log.debug("Found model declaration: "+modelReference);
		
		String modelName = modelReference;
		Skeleton model = getModel(modelName);
//		log.debug( "model: "+model );
		modelsByName.put( model.getName(), model );
		// TODO: add support for several models
		// TODO: make the modelsByName list global
		return modelsByName;
	}
	
	/**
	 * Adds a new animation sequence for each model to the given parallel animation.
	 * @param par
	 * @param models
	 * @return
	 */
	private static HashMap<Skeleton,Sequence> addSequencesForModels( Parallel par, Collection<Skeleton> models ) {
		HashMap<Skeleton,Sequence> seqsByModel = new HashMap<Skeleton,Sequence>();
		for( Skeleton model : models ) {
			Sequence modelKeySeq = new Sequence();
			par.addAnim( modelKeySeq );
			seqsByModel.put( model, modelKeySeq );
		}
		return seqsByModel;
	}

}
