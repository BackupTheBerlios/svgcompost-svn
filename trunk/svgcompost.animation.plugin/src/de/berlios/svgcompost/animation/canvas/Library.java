package de.berlios.svgcompost.animation.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGStylable;

import de.berlios.svgcompost.animation.anim.composite.Parallel;
import de.berlios.svgcompost.animation.anim.composite.Sequence;
import de.berlios.svgcompost.animation.anim.easing.Easing;
import de.berlios.svgcompost.animation.anim.easing.Quadratic;
import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.KeyframeAnim;
import de.berlios.svgcompost.animation.anim.skeleton.Skeleton;

public class Library {
	
	private static Logger log = Logger.getLogger(Library.class);

	public static final String inkscapeURI = "http://www.inkscape.org/namespaces/inkscape";
//	public static final NameSpace inkscapeNS = new NameSpace( "inkscape", inkscapeURI );
	public static final String groupmode = "groupmode";
	public static final String layer = "layer";

	protected Canvas libraryCanvas;

	protected HashMap<String,Skeleton> models = new HashMap<String,Skeleton>();
//	protected HashMap<String,Walk> walks = new HashMap<String,Walk>();
	
	public Library( Canvas canvas ) {
		this.libraryCanvas = canvas;
	}
	
	/*
	public Walk getWalk( String walkName, String modelName ) {
		Walk walk = walks.get( walkName );
		if( walk != null )
			return walk;
		Skeleton model = getModel( modelName );
		walk = loadPosesForWalk( walkName, model );
		model.discardKeys();
		return walk;
	}
	*/

	// TODO: simplify
	// FIXME: limbs connect to wrong points
	public Parallel createWalkAnim( CanvasNode stage, String posesId, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = getModel(modelName);
		CanvasNode posesNode = stage.addSymbolInstance(posesId, posesId);
		List<CanvasNode> poses = posesNode.getChildListCopy();

		for(CanvasNode keyframe : poses)
			keyframe.getSkeletonLink().applySkeleton(model);

		ArrayList<Bone> feet = new ArrayList<Bone>();
		for( int i=0; i<model.connectorSize(); i++ ) {
			feet.add( model.getConnector(i).getTarget() );
		}

		int noOfPoses = poses.size();
		int lastPose = noOfPoses-1;
		
		for(CanvasNode frame : poses)
			for( Skeleton skeleton : frame.getSkeletonLink().skeletons )
				skeleton.calcLimbMatrices(frame.getSkeletonLink());			
		for(int i=0; i<poses.size(); i++)
			for( Skeleton skeleton : poses.get(i).getSkeletonLink().skeletons )
				skeleton.setupLimbTweening(poses, i);

		Point2D.Float originalStart = calcCenterPoint( feet, poses.get(0), posesNode );
		Point2D.Float originalEnd = calcCenterPoint( feet, poses.get(lastPose), posesNode );
		AffineTransform trafo = skewYbyXscaleX( originalStart, start, originalEnd, end );
		
		Point2D.Float xy_new = new Point2D.Float();
		for( int i=0; i<model.size(); i++ ) {
			Bone topLevelBone = model.get(i);
			log.debug("topLevelBone: "+topLevelBone.getName());
			for( int j=0; j<noOfPoses; j++ ) {
				SkeletonLink frameLink = poses.get(j).getSkeletonLink(); 
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

		// set up the 
		
		Parallel par = new Parallel();
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( poses.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
			
		for(CanvasNode frame : poses)
			for( Skeleton skeleton : frame.getSkeletonLink().skeletons )
				skeleton.calcKeyMatrices(frame.getSkeletonLink());			
		for(int i=0; i<poses.size(); i++)
			for( Skeleton skeleton : poses.get(i).getSkeletonLink().skeletons )
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
	
	/*
	public Parallel createWalkAnim( CanvasNode stage, String posesId, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = getModel(modelName);
		CanvasNode poses = stage.addSymbolInstance(posesId, posesId);
		for(CanvasNode pose : poses.getChildListCopy())
			model.addRootKey(pose.getChild(model.getName()));
		model.setup();
		ArrayList<Bone> feet = new ArrayList<Bone>();
		for( int i=0; i<model.connectorSize(); i++ ) {
			feet.add( model.getConnector(i).getTarget() );
		}
		int noOfPoses = poses.getSize();
		int lastPose = noOfPoses-1;
		
		Point2D.Float originalStart = calcCenterPoint( feet, 0, poses );
		Point2D.Float originalEnd = calcCenterPoint( feet, lastPose, poses );
		AffineTransform trafo = Walk.skewYbyXscaleX( originalStart, start, originalEnd, end );
		
		Point2D.Float xy_new = new Point2D.Float();
		for( int i=0; i<model.size(); i++ ) {
			Bone topLevelBone = model.get(i);
			log.debug("topLevelBone: "+topLevelBone.getName());
			for( int j=0; j<noOfPoses; j++ ) {
				CanvasNode node = topLevelBone.getKey(j);
				Point2D.Float xy = node.getLocalXY( poses );
				trafo.transform(xy, xy_new);
				// TODO: read the position the connectors connect to BEFORE the feet get moved
				topLevelBone.setRecursiveLocalXY(xy_new, j, poses);
			}
		}
		
		CanvasNode startRect = stage.addSymbolInstance("redrect", "start");
		trafo.transform(originalStart, xy_new);
		startRect.setLocalXY(xy_new, poses);
		log.debug("start transformed: "+xy_new);
		log.debug("startRect.getGlobalXY(): "+startRect.getGlobalXY());

		model.discardKeys();
		Parallel par = createAnimFromKeyframesWithoutConnectors(poses.getChildListCopy());
		return par;
	}
	*/

	public static Point2D.Float calcCenterPoint( ArrayList<Bone> bones, CanvasNode frame, CanvasNode system ) {
		float x = 0;
		float y = 0;
		for (Bone bone : bones) {
			Point2D.Float xy = frame.getSkeletonLink().getNodeForBone(bone).getLocalXY( system );
			x += xy.x;
			y += xy.y;
		}
		x /= bones.size();
		y /= bones.size();
		return new Point2D.Float( x, y );
	}

	/*
	protected Walk loadPosesForWalk( String walkName, Skeleton model ) {
		CanvasNode posesGroup = libraryCanvas.getRoot().addSymbolInstance( walkName, walkName );
		int noOfPoses = posesGroup.getSize();
		Walk walk = new Walk( noOfPoses );
		walk.name = walkName;
		walk.modelName = model.getName();
		CanvasNode[] keyMc = new CanvasNode[noOfPoses];
		for (int i = 0; i < noOfPoses; i++) {
			keyMc[i] = posesGroup.get( i );
			walk.poseIds[i] = keyMc[i].getSymbolId();
			walk.wrapperAbs[i] = keyMc[i].getXY();
		}
		loadPosesIntoModel( walk, model, keyMc );
		posesGroup.removeNode();
		return walk;
	}
	*/

	/*
	protected void loadPosesIntoModel( Walk walk, Skeleton model, CanvasNode[] keyMc ) {
		ArrayList<CanvasNode> keyframes = new ArrayList<CanvasNode>();
		for (CanvasNode keyframe : keyMc)
			keyframes.add(keyframe);
		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
		modelsByName.put(model.getName(), model);
		
		loadKeyFramesIntoModels( keyframes, modelsByName, null );
		
		model.setup();
		
		Bone leftFoot = model.getBone( "elfyLeftFoot" );
		Bone rightFoot = model.getBone( "elfyRightFoot" );
		
		log.debug("keyMc.length:"+keyMc.length);
		log.debug("leftFoot: "+leftFoot.getKey(keyMc.length-1));
		
		int noOfPoses = keyMc.length;
		for (int i = 0; i < noOfPoses; i++) {
			walk.footAbs[i][0] = leftFoot.getKey(i).getGlobalXY();
			walk.footAbs[i][1] = rightFoot.getKey(i).getGlobalXY();
			walk.centerAbs[i] = new Point2D.Float( (walk.footAbs[i][0].x+walk.footAbs[i][1].x)/2, (walk.footAbs[i][0].y+walk.footAbs[i][1].y)/2 );			
		}
		Point2D.Float start = walk.centerAbs[0];
		Point2D.Float end = walk.centerAbs[noOfPoses-1];
		loadPositionsIntoWalk( start, end, walk, keyMc );
	}
	*/
	
	/*
	protected void loadPositionsIntoWalk( Point2D.Float start, Point2D.Float end, Walk walk, CanvasNode[] keyMc ) {
		AffineTransform lineSystem = Walk.orthogonalSystem( start, end );
		
		int noOfPoses = keyMc.length;
		for (int i = 0; i < noOfPoses; i++) {
			Point2D.Float origin = keyMc[i].getXY();
			walk.centerOffset[i] = new Point2D.Float( walk.centerAbs[i].x-origin.x, walk.centerAbs[i].y-origin.y );
			walk.centerRel[i] = new Point2D.Float();
			walk.footRel[i] = new Point2D.Float[] { new Point2D.Float(), new Point2D.Float() };
			
			try {
				lineSystem.inverseTransform( walk.centerAbs[i], walk.centerRel[i] );
				lineSystem.inverseTransform( walk.footAbs[i][0], walk.footRel[i][0] );
				lineSystem.inverseTransform( walk.footAbs[i][1], walk.footRel[i][1] );
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
	}
	*/
	
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
		model = Skeleton.createModelRoot( modelNode );
		
		model.addConnector( "elfyLeftUpperLeg", "elfyLeftLowerLeg", "elfyLeftFoot" );
		model.addConnector( "elfyRightUpperLeg", "elfyRightLowerLeg", "elfyRightFoot" );
		
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
		
		for(CanvasNode keyframe : keyframes)
			for(Skeleton skeleton : modelsByName.values())
				keyframe.getSkeletonLink().applySkeleton(skeleton);
			
		SkeletonLink.setupTweening(keyframes);

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
			tweenAnim.setEasing( Easing.getStandard() );
		else
			tweenAnim.setEasing( Quadratic._inOut );
//		String align = frameElement.getAttribute( "align" );
		return tweenAnim;
	}

	/*
	public Parallel createAnimFromKeyframeIds( CanvasNode stage, ArrayList<String> keyframeIds ) {
		ArrayList<CanvasNode> keyframes = new ArrayList<CanvasNode>();
		
		for (int i = 0; i < keyframeIds.size(); i++) {
			String keyframeId = keyframeIds.get(i);
			setDisplayAttributeToInline( stage.getCanvas().getSourceDoc().getElementById(keyframeId) );
			CanvasNode keyframe = stage.addSymbolInstance( keyframeId, keyframeId );
			log.debug( "Adding keyframe: "+keyframe.getPath() );
			keyframes.add( keyframe );
		}
		return createAnimFromKeyframes( keyframes );
	}
	*/

	/*
	public Parallel createAnimFromKeyframes( ArrayList<CanvasNode> keyframes ) {
		log.debug("createAnimFromKeyframes");
		
		Parallel par = new Parallel();
		
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		loadKeyFramesIntoModels(keyframes, modelsByName, seqsByModel);
		
		log.debug( "seqsByModel.size(): "+seqsByModel.size() );
		log.debug( "seqsByModel.keySet(): "+seqsByModel.keySet().size() );
		for(Skeleton model : seqsByModel.keySet())
			model.setup();
	
		for(CanvasNode keyframe : keyframes)
			keyframe.setVisible(false);
		
		if( log.isDebugEnabled() ) {
			log.debug( "Total length of main par: "+par.getDurationinMillis() );
			log.debug( "Total length of scene: "+par.getDurationinMillis() );
			log.debug( "Total sequence levels in scene: "+par.getSize() );
		}
		
		return par;
	}
	*/

	/*
	public Parallel createAnimFromKeyframesWithoutConnectors( ArrayList<CanvasNode> keyframes ) {
		log.debug("createAnimFromKeyframes");
		
		Parallel par = new Parallel();
		
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ) );
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		loadKeyFramesIntoModels(keyframes, modelsByName, seqsByModel);
		
		log.debug( "seqsByModel.size(): "+seqsByModel.size() );
		log.debug( "seqsByModel.keySet(): "+seqsByModel.keySet().size() );
		for(Skeleton model : seqsByModel.keySet())
			model.setupWithoutConnectors();
	
		for(CanvasNode keyframe : keyframes)
			keyframe.setVisible(false);
		
		if( log.isDebugEnabled() ) {
			log.debug( "Total length of main par: "+par.getDurationinMillis() );
			log.debug( "Total length of scene: "+par.getDurationinMillis() );
			log.debug( "Total sequence levels in scene: "+par.getSize() );
		}
		
		return par;
	}
	*/

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
	
	/*
	private static void loadKeyFramesIntoModels(ArrayList<CanvasNode> keyframes, HashMap<String, Skeleton> modelsByName, HashMap<Skeleton, Sequence> seqsByModel) {
		log.debug("# of models: "+modelsByName.size());
		for (int key = 0; key < keyframes.size(); key++) {
			log.debug( "Loading keyframe #"+key );
			CanvasNode keyframe = keyframes.get( key );
						
			// Search for model keys.
			for( Skeleton model : modelsByName.values() ) {
				// If the model isn't wrapped, the frame itself is treated as the wrapper. 
				CanvasNode modelKey = keyframe.getChild( model.getName() );
				if( modelKey == null ) {
					log.debug("model key for '"+model.getName()+"' is null: "+keyframe.getChild( model.getName() ));
					modelKey = keyframe;
				}
				log.debug("model key for '"+model.getName()+"': "+modelKey.getPath());
				
				model.addRootKey( modelKey );
				if( key != keyframes.size()-1 ) {
					if( seqsByModel != null )
						seqsByModel.get(model).addAnim( createTweeningAnim( model, keyframes, key ) );
				}
			}
		}
	}
	*/


	/*
	private static KeyframeAnim createTweeningAnim(Skeleton model, ArrayList<CanvasNode> keyframes, int key) {
		CanvasNode keyframe = keyframes.get(key);
		KeyframeAnim tweenAnim = new KeyframeAnim( model, key, keyframe, keyframes.get(key+1) );
		SVGElement frameElement = (SVGElement) keyframe.getCanvas().getSourceDoc().getElementById( keyframe.getSymbolId() );
		String duration = frameElement.getAttribute( "duration" );
		if( duration == null || duration.equals( "" ) )
			tweenAnim.setDurationInSeconds( 1 );
		else
			tweenAnim.setDurationInSeconds( Double.parseDouble( duration ) );
		String easing = frameElement.getAttribute( "easing" );
		if( easing == null || easing.equals( "" ) )
			tweenAnim.setEasing( Easing.getStandard() );
		else
			tweenAnim.setEasing( Quadratic._inOut );
		return tweenAnim;
	}
	*/
	
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
