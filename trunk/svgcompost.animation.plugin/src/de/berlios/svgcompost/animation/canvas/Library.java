package de.berlios.svgcompost.animation.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGElement;

import de.berlios.svgcompost.animation.anim.chara.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.chara.skeleton.KeyframeAnim;
import de.berlios.svgcompost.animation.anim.chara.skeleton.Skeleton;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonFactory;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;
import de.berlios.svgcompost.animation.anim.composite.Parallel;
import de.berlios.svgcompost.animation.anim.composite.Scene;
import de.berlios.svgcompost.animation.anim.composite.Sequence;
import de.berlios.svgcompost.animation.anim.easing.Cubic;
import de.berlios.svgcompost.animation.anim.easing.Easing;
import de.berlios.svgcompost.animation.anim.easing.Linear;
import de.berlios.svgcompost.animation.anim.easing.Quadratic;
import de.berlios.svgcompost.animation.anim.easing.Sine;
import de.berlios.svgcompost.animation.timeline.Keyframe;
import de.berlios.svgcompost.animation.timeline.Layer;
import de.berlios.svgcompost.animation.timeline.Timeline;
import de.berlios.svgcompost.util.ElementTraversalHelper;
import de.berlios.svgcompost.util.VisibilityHelper;
import de.berlios.svgcompost.xmlconstants.Attributes;
import de.berlios.svgcompost.xmlconstants.Classes;
import de.berlios.svgcompost.xmlconstants.Elements;

public class Library {
	
	public static final String INKSCAPE_URI = "http://www.inkscape.org/namespaces/inkscape";
//	public static final NameSpace inkscapeNS = new NameSpace( "inkscape", INKSCAPE_URI );
	public static final String INKSCAPE_GROUPMODE = "INKSCAPE_GROUPMODE";
	public static final String INKSCAPE_LAYER = "INKSCAPE_LAYER";

	protected Canvas stage;
	protected Canvas libraryCanvas;

	protected HashMap<String,Skeleton> models = new HashMap<String,Skeleton>();
	
	public Library( Canvas stage, Canvas libraryCanvas ) {
		this.stage = stage;
		this.libraryCanvas = libraryCanvas;
	}
	
	public Scene createAnimsForTimeline( Timeline timeline ) {
		Scene scene = new Scene( timeline.getCanvas() );
		for (Layer layer : timeline.getLayers()) {
			scene.addAnim(createAnimsForLayer(layer));
		}
		return scene;
	}
	
	public Parallel createAnimsForLayer( Layer layer ) {
		List<Keyframe> keyframes = layer.getKeyframes();
		
		// Extract skeleton definitions from 1st frame.
		HashMap<String,Skeleton> skeletons = extractModelsFromDeclaration( keyframes.get(0).getNode() ); 
		
		// Apply skeletons to all frames, so that nodes are assigned to the skeletons as bones.
		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for(Keyframe keyframe : keyframes) {
			for(Skeleton skeleton : skeletons.values() )
				keyframe.applySkeleton(skeleton,skeletonKeys);
			skeletonKeys = keyframe.getSkeletonKeys();
		}

//		for(Keyframe keyframe : keyframes) {
//			for( Skeleton skeleton : keyframe.getSkeletonKeys().keySet() ) {
//				skeleton.setupTweening(keyframe.getSkeletonKey(skeleton));
//				skeleton.setupLimbTweening(keyframe);
//			}
//		}

		// Create an animation sequence for every skeleton,
		// and have these sequences run in parallel.
		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ).getNode() );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		// Set up the tweening for all animation sequences.
		for(Keyframe keyframe : keyframes)
			for( Skeleton skeleton : keyframe.getSkeletonKeys().keySet() )
				skeleton.setupTweening(keyframe.getSkeletonKey(skeleton));

		for(Keyframe keyframe : keyframes)
			if( keyframe.hasNext() )
				for(Skeleton skeleton : modelsByName.values())
					seqsByModel.get(skeleton).addAnim( createTweeningAnim( skeleton, keyframe, keyframe.nextKey() ) );
	
		for(Keyframe keyframe : keyframes) {
			VisibilityHelper.setVisibility( stage.getSourceCtx().getElement( keyframe.getNode().getGraphicsNode() ), true );
			keyframe.getNode().setVisible(false);
		}
		
		return par;
	}
	
	public Timeline createTimeline() {
		CanvasNode root = stage.renderDocument(stage.getSourceDoc());
		Canvas canvas = root.getCanvas();
		Timeline timeline = new Timeline();
		timeline.setCanvas(libraryCanvas);
		for (CanvasNode node : root.getChildren()) {
			Element element = canvas.getSourceCtx().getElement( node.getGraphicsNode() );
			if( element != null && hasClass(element, Classes.FRAME) ) {
				Layer layer = createLayer( root );
				timeline.addLayer(layer);
				break;
			}
			if( element != null && hasClass(element, "layer") ) {
				VisibilityHelper.setDisplayToInline(element);
//			if( element != null && element.getAttributeNS( INKSCAPE_URI, INKSCAPE_GROUPMODE ).equals( INKSCAPE_LAYER ) ) {
				Layer layer = createLayer( node );
				timeline.addLayer(layer);
			}
			else {
				node.setVisible(false);
			}
		}
		return timeline;
	}
	
	public static Layer createLayer( CanvasNode root ) {
		Canvas canvas = root.getCanvas();
		Layer layer = new Layer();
//		double time = 0;
		for (CanvasNode node : root.getChildren()) {
			Element element = canvas.getSourceCtx().getElement( node.getGraphicsNode() );
			if( element != null && hasClass( element, Classes.FRAME ) ) {
				VisibilityHelper.setDisplayToInline(element);
//				try {
//					double duration = Double.parseDouble(element.getAttributeNS(Elements.SVGCOMPOST_NAMESPACE_URI, Attributes.DURATION));
//					time += duration;
//				}catch (Exception e) {
//				}
				Keyframe keyframe = new Keyframe( node );
				layer.addKeyframe(keyframe);
			}
		}
		return layer;
	}

	public Parallel createWalkAnim( Layer layer, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = getModel_old(modelName);
		List<Keyframe> keyframes = layer.getKeyframes();
		return createWalkAnim(keyframes, model, start, end);
	}
	
	public Parallel createWalkAnim( List<Keyframe> keyframes, Skeleton model, Point2D.Float start, Point2D.Float end ) {
		// Apply skeleton to all keyframes.
		Map<Skeleton,SkeletonKey> skeletonKeys = null;
		for(Keyframe keyframe : keyframes) {
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
		
		// Set up jointedLimb tweening for all keyframes.
		// The transformation of the feet and body will mess up the the jointedLimb positions,
		// so they need to be saved in advance.
		for(Keyframe keyframe : keyframes) {
			for( Skeleton skeleton : keyframe.getSkeletonKeys().keySet() )
				skeleton.setupLimbTweening(keyframe);
		}

		// Transform the original start and end location of the walk to the new ones.
		Point2D.Float originalStart = calcCenterPoint( feet, keyframes.get(0), keyframes.get(0).getNode() );
		Point2D.Float originalEnd = calcCenterPoint( feet, keyframes.get(lastKeyframe), keyframes.get(lastKeyframe).getNode() );
		AffineTransform trafo = skewYbyXscaleX( originalStart, start, originalEnd, end );
		
		// Use the transform to change the position of all bones in the keyframes,
		// starting from the top level bones (usually body and feet).
		Point2D.Float xy = null;
		for(Keyframe keyframe : keyframes) {
			for( Bone topLevelBone : model.getBones() ) {
				SkeletonKey skeletonKey = keyframe.getSkeletonKey(model); 
				CanvasNode node = skeletonKey.getCanvasNode(topLevelBone);
				xy = node.getLocalXY( keyframe.getNode() );
				trafo.transform(xy,xy);
				topLevelBone.setGlobalPosition(xy, skeletonKey, keyframe.getNode());
			}
		}

		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ).getNode() );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		for(Keyframe frame : keyframes)
			for( Skeleton skeleton : frame.getSkeletonKeys().keySet() )
				skeleton.setupTweening(frame.getSkeletonKey(skeleton));

		for(int i=0; i<keyframes.size()-1; i++)
			for(Skeleton skeleton : modelsByName.values())
				seqsByModel.get(skeleton).addAnim( createTweeningAnim( skeleton, keyframes.get(i), keyframes.get(i+1) ) );
	
		for(Keyframe keyframe : keyframes)
			keyframe.getNode().setVisible(false);
		
		return par;
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
	 * Creates an animation for the specified model, from the specified keyframe in the given keyframe sequence
	 * to the next keyframe in the list.
	 * @param model
	 * @param keyframes
	 * @param key
	 * @return
	 */
	private static KeyframeAnim createTweeningAnim(Skeleton model, Keyframe keyframe1, Keyframe keyframe2) {
		KeyframeAnim tweenAnim = new KeyframeAnim( model, keyframe1, keyframe2 );
		SVGElement frameElement = (SVGElement) keyframe1.getNode().getSourceElement(); // getCanvas().getSourceCtx().getElement( keyframe1.getNode().getGraphicsNode() );
		// Why was this done with IDs?
//		SVGElement frameElement = (SVGElement) keyframe1.getNode().getCanvas().getSourceDoc().getElementById( keyframe1.getNode().getSymbolId() );
		
		// Set duration.
		String duration = frameElement.getAttributeNS( Elements.SVGCOMPOST_NAMESPACE_URI, Attributes.DURATION );
		if( duration == null || duration.equals( "" ) )
			tweenAnim.setDurationInSeconds( 1 );
		else
			tweenAnim.setDurationInSeconds( Double.parseDouble( duration ) );

		Easing easing = parseEasing(frameElement);
		tweenAnim.setEasing(easing);
		return tweenAnim;
	}
	
	public static Easing parseEasing( Element frame ) {
		Element easingElement = ElementTraversalHelper.getFirstChildElementNS(frame, Elements.SVGCOMPOST_NAMESPACE_URI, Elements.EASING);
		if(easingElement == null)
			return null;
		String easingType = easingElement.getAttribute(Attributes.TYPE);
		System.out.println("Library.parseEasing() easingType = "+easingType);
		Easing easing = null;
		if( easingType == null )
			easing = new Quadratic();
		else {
			if( easingType.equals("linear") )
				easing = new Linear();
			else if( easingType.equals("quadratic") )
				easing = new Quadratic();
			else if( easingType.equals("cubic") )
				easing = new Cubic();
			else if( easingType.equals("sine") )
				easing = new Sine();
			else
				easing = new Quadratic();
		}
		String align = easingElement.getAttribute(Attributes.ALIGN);
		if( align.equals("in") )
			easing.setAlign(Easing.EASE_IN);
		else if( align.equals("out") )
			easing.setAlign(Easing.EASE_OUT);
		else if( align.equals("inout") )
			easing.setAlign(Easing.EASE_IN_OUT);
		else
			easing.setAlign(Easing.EASE_IN_OUT);
		try {
			double power = Double.parseDouble(easingElement.getAttribute(Attributes.POWER));
			easing.setPower(power);
		} catch (Exception e) {
		}
		return easing;
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
	
	public static Point2D.Float calcCenterPoint( ArrayList<Bone> bones, Keyframe frame, CanvasNode system ) {
		float x = 0;
		float y = 0;
		for (Bone bone : bones) {
			Point2D.Float xy = frame.getSkeletonKey(bone.getSkeleton()).getCanvasNode(bone).getLocalXY( system );
			x += xy.x;
			y += xy.y;
		}
		x /= bones.size();
		y /= bones.size();
		return new Point2D.Float( x, y );
	}

	/**
	 * Checks a reference to a skeleton.
	 * If the skeleton is not yet active in the library, it is created
	 * from the referenced XML element.
	 * @param useElement The useSkeleton element referencing the skeleton.
	 */
	public Skeleton getModel( Element useElement ) {
		String modelReference = null;
		
		if( useElement.hasAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, SVGConstants.SVG_HREF_ATTRIBUTE) )
			modelReference = useElement.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, SVGConstants.SVG_HREF_ATTRIBUTE);
		else if( useElement.hasAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href") )
			modelReference = useElement.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href");
		
		String prefix = useElement.getAttributeNS(null, Elements.PREFIX);
		boolean makeCamelCase = useElement.getAttributeNS(null, Elements.MAKECAMELCASE).equals("true");
		int index = modelReference.indexOf("#");
		String modelName = index == -1 ? modelReference : modelReference.substring(index+1);
		Skeleton model = models.get( modelName );
		if( model != null )
			return model;

		Element modelElement = libraryCanvas.resolve(modelReference, null);
		model = new SkeletonFactory().createSkeleton( modelElement, prefix, makeCamelCase );
		
		models.put(modelName, model);
		return model;
	}

	public Skeleton getModel_old( String modelReference ) {
		int index = modelReference.indexOf("#"); 
		String modelName = index == -1 ? modelReference : modelReference.substring(index+1);
		Skeleton model = models.get( modelName );
		if( model != null )
			return model;
		CanvasNode modelNode = libraryCanvas.getRoot().addSymbolInstance( modelReference, modelName );
		model = new SkeletonFactory().createSkeleton( modelNode );
		
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
		ArrayList<String> layerIds = new ArrayList<String>();
		Element symbolElement = doc.getElementById( symbolId ); 
		NodeList list = symbolElement.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node node = list.item( i );
			if( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element child = (Element) node;
				if( child.getAttributeNS( INKSCAPE_URI, INKSCAPE_GROUPMODE ).equals( INKSCAPE_LAYER ) ) {
					layerIds.add( child.getAttribute( "id" ) );
				}
			}
		}		
		return layerIds;
	}
	
	/**
	 * Reads all model references from the specified frame.
	 */
	private HashMap<String,Skeleton> extractModelsFromDeclaration( CanvasNode referencingFrame ) {
		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
//		SVGElement frameElement = (SVGElement) referencingFrame.getCanvas().getSourceDoc().getElementById( referencingFrame.getSymbolId() );
		Element frame = referencingFrame.getCanvas().getSourceCtx().getElement(referencingFrame.getGraphicsNode());
		List<Element> childElements = ElementTraversalHelper.getChildElements(frame);
		for (Element element : childElements) {
			String elementName = element.getLocalName();
			String namespaceUri = element.getNamespaceURI();
			if( namespaceUri.equals(Elements.SVGCOMPOST_NAMESPACE_URI) && elementName.equals(Elements.SKELETON) ) {
				String prefix = element.getAttributeNS(null, Elements.PREFIX);
				boolean makeCamelCase = element.getAttributeNS(null, Elements.MAKECAMELCASE).equals("true");
				Skeleton model = new SkeletonFactory().createSkeleton( element, prefix, makeCamelCase ); 
//					getModel(element);
//				System.out.println( "model = "+model );
				modelsByName.put( model.getName(), model );
			}
		}
		return modelsByName;
	}
	
//	private HashMap<String,Skeleton> extractModelsFromDeclaration( CanvasNode referencingFrame ) {
//		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
//		SVGElement frameElement = (SVGElement) referencingFrame.getCanvas().getSourceDoc().getElementById( referencingFrame.getSymbolId() );
//		List<Element> childElements = ElementTraversalHelper.getChildElements(frameElement);
//		for (Element element : childElements) {
//			String elementName = element.getLocalName();
//			String namespaceUri = element.getNamespaceURI();
//			if( namespaceUri.equals(Elements.SVGCOMPOST_NAMESPACE_URI) && elementName.equals(Elements.USESKELETON) ) {
//				Skeleton model = getModel(element);
//				modelsByName.put( model.getName(), model );
//			}
//		}
//		return modelsByName;
//	}
	
//	private HashMap<String,Skeleton> extractModelsFromDeclaration_old( CanvasNode referencingFrame ) {
//		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
//		SVGElement frameElement = (SVGElement) referencingFrame.getCanvas().getSourceDoc().getElementById( referencingFrame.getSymbolId() );
//		String modelReferences = frameElement.getAttribute(Attributes.MODEL);
//		if( modelReferences == null || "".equals( modelReferences ) ) {
//			return modelsByName;
//		}
//		String[] skeletons = modelReferences.split(" ");
//		
//		for (int i = 0; i < skeletons.length; i++) {
//			String skeletonReference = skeletons[i];
//			Skeleton model = getModel_old(skeletonReference);
//			modelsByName.put( model.getName(), model );
//		}
//		return modelsByName;
//	}
	
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
