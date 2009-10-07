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

import de.berlios.svgcompost.animation.anim.Anim;
import de.berlios.svgcompost.animation.anim.chara.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.chara.skeleton.KeyframeAnim;
import de.berlios.svgcompost.animation.anim.chara.skeleton.Skeleton;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonFactory;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;
import de.berlios.svgcompost.animation.anim.composite.Parallel;
import de.berlios.svgcompost.animation.anim.composite.Scene;
import de.berlios.svgcompost.animation.anim.composite.Sequence;
import de.berlios.svgcompost.animation.anim.easing.Quadratic;
import de.berlios.svgcompost.animation.timeline.Keyframe;
import de.berlios.svgcompost.animation.timeline.Layer;
import de.berlios.svgcompost.animation.timeline.Timeline;

public class Library {
	
	private static Logger log = Logger.getLogger(Library.class);

	public static final String INKSCAPE_URI = "http://www.inkscape.org/namespaces/inkscape";
//	public static final NameSpace inkscapeNS = new NameSpace( "inkscape", INKSCAPE_URI );
	public static final String INKSCAPE_GROUPMODE = "INKSCAPE_GROUPMODE";
	public static final String INKSCAPE_LAYER = "INKSCAPE_LAYER";

	protected Canvas libraryCanvas;

	protected HashMap<String,Skeleton> models = new HashMap<String,Skeleton>();
	
	public Library( Canvas canvas ) {
		this.libraryCanvas = canvas;
	}
	
	public Anim createAnimsForTimeline( Timeline timeline ) {
		Scene scene = new Scene( timeline.getCanvas() );
		for (Layer layer : timeline.getLayers()) {
			scene.addAnim(createAnimsForLayer(layer));
		}
		return scene;
	}
	
	public Parallel createAnimsForLayer( Layer layer ) {
		Parallel par = new Parallel();
		
		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for (Keyframe keyframe : layer.getKeyframes()) {
			HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframe.getNode() );
			for(Skeleton skeleton : modelsByName.values()) {
				keyframe.getNode().applySkeleton(skeleton, skeletonKeys);
			}
			skeletonKeys = keyframe.getNode().getSkeletonKeys();
		}
		
			
		for (Keyframe keyframe : layer.getKeyframes()) {
			for(Skeleton skeleton : keyframe.getNode().getSkeletonKeys().keySet()) {
				skeleton.setupTweening(keyframe.getNode().getSkeletonKey(skeleton));
				skeleton.setupLimbTweening(keyframe.getNode());
			}
		}
	
		for(Keyframe keyframe : layer.getKeyframes())
			keyframe.getNode().setVisible(false);
		
		return par;
	}

	
	public Timeline createTimeline() {
		CanvasNode root = libraryCanvas.renderDocument(libraryCanvas.getSourceDoc());
		log.debug("Library.createTimeline( "+root.getSize()+" )");
		Canvas canvas = root.getCanvas();
		Timeline timeline = new Timeline();
		timeline.setCanvas(libraryCanvas);
		for (CanvasNode node : root.getChildren()) {
			Element element = canvas.getSourceCtx().getElement( node.getGraphicsNode() );
			log.debug( "element = "+element.getAttribute("id") );
			if( element != null && element.getAttributeNS( INKSCAPE_URI, INKSCAPE_GROUPMODE ).equals( INKSCAPE_LAYER ) ) {
				Layer layer = createLayer( node );
				timeline.addLayer(layer);
			}
		}
		return timeline;
	}
	
	public static Layer createLayer( CanvasNode root ) {
		Canvas canvas = root.getCanvas();
		Layer layer = new Layer();
		for (CanvasNode node : root.getChildren()) {
			Element element = canvas.getSourceCtx().getElement( node.getGraphicsNode() );
			if( element != null && hasClass( element, "keyframe" ) ) {
				double time = 0;
				try {
					time = Double.parseDouble(element.getAttribute("time"));
				}catch (Exception e) {
				}
				Keyframe keyframe = new Keyframe( node, time );
				layer.addKeyframe(keyframe);
			}
		}
		return layer;
	}
	
	public static boolean hasClass( Element element, String className ) {
		String classAtt = element.getAttribute("class");
		String[] classes = classAtt.split(" ");
		for (int i = 0; i < classes.length; i++) {
			if( classes[i].equals( className ) )
				return true;
		}
		return false;
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
		KeyframeAnim tweenAnim = new KeyframeAnim( model, /*keyframes, key,*/ keyframe, keyframes.get(key+1) );
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

	public Parallel createWalkAnim( CanvasNode stage, String posesId, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = getModel(modelName);
		CanvasNode posesNode = stage.addSymbolInstance(posesId, posesId);
		List<CanvasNode> keyframes = posesNode.getChildren();

		// Apply skeleton to all keyframes.
		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for(CanvasNode keyframe : keyframes) {
			keyframe.applySkeleton(model,skeletonKeys);
			skeletonKeys = keyframe.getSkeletonKeys();
		}

		// Make a list of 2 (or more) feet inside the skeleton. 
		ArrayList<Bone> feet = new ArrayList<Bone>();
		for( int i=0; i<model.connectorSize(); i++ ) {
			feet.add( model.getConnector(i).getTarget() );
		}

		int numberOfKeyframes = keyframes.size();
		int lastKeyframe = numberOfKeyframes-1;
		
		// Set up limb tweening for all keyframes.
		// The transformation of the feet and body will mess up the the limb positions,
		// so they need to be saved in advance.
		for(CanvasNode keyframe : keyframes) {
			for( Skeleton skeleton : keyframe.getSkeletonKeys().keySet() )
				skeleton.setupLimbTweening(keyframe);
		}

		// Transform the original start and end location of the walk to the new ones.
		Point2D.Float originalStart = calcCenterPoint( feet, keyframes.get(0), posesNode );
		Point2D.Float originalEnd = calcCenterPoint( feet, keyframes.get(lastKeyframe), posesNode );
		AffineTransform trafo = skewYbyXscaleX( originalStart, start, originalEnd, end );
		
		// Use the transform to change the position of all bones in the keyframes,
		// starting from the top level bones (usually body and feet).
		Point2D.Float xy = null;
		for(CanvasNode keyframe : keyframes) {
			for( Bone topLevelBone : model.getBones() ) {
				SkeletonKey frameLink = keyframe.getSkeletonKey(model); 
				CanvasNode node = frameLink.getNodeForBone(topLevelBone);
				xy = node.getLocalXY( posesNode );
				trafo.transform(xy,xy);
				topLevelBone.setRecursiveLocalXY(xy, frameLink, posesNode);
			}
		}

		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		for(CanvasNode frame : keyframes)
			for( Skeleton skeleton : frame.getSkeletonKeys().keySet() )
				skeleton.setupTweening(frame.getSkeletonKey(skeleton));

		for(int i=0; i<keyframes.size()-1; i++)
			for(Skeleton skeleton : modelsByName.values())
				seqsByModel.get(skeleton).addAnim( createTweeningAnim( skeleton, keyframes, i ) );
	
		for(CanvasNode keyframe : keyframes)
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
	 * Parses an SVG element to determine its INKSCAPE_LAYER children, which are
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
				if( child.getAttributeNS( INKSCAPE_URI, INKSCAPE_GROUPMODE ).equals( INKSCAPE_LAYER ) ) {
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
			log.error( "couldn't find 'model' declaration in keyframe: "+referencingFrame.getName() );
			return modelsByName;
		}
		log.debug("Found model declaration: "+modelReference);
		String[] skeletons = modelReference.split(" ");
		
		for (int i = 0; i < skeletons.length; i++) {
			String modelName = skeletons[i];
			Skeleton model = getModel(modelName);
//			log.debug( "model: "+model );
			modelsByName.put( model.getName(), model );
		}
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
