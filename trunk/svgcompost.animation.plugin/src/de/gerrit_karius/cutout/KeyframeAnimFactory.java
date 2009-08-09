package de.gerrit_karius.cutout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGElement;

import de.gerrit_karius.cutout.anim.composite.Parallel;
import de.gerrit_karius.cutout.anim.composite.Scene;
import de.gerrit_karius.cutout.anim.composite.Sequence;
import de.gerrit_karius.cutout.anim.easing.Easing;
import de.gerrit_karius.cutout.anim.easing.Quadratic;
import de.gerrit_karius.cutout.anim.skeleton.KeyframeAnim;
import de.gerrit_karius.cutout.anim.skeleton.Skeleton;
import de.gerrit_karius.cutout.canvas.Canvas;
import de.gerrit_karius.cutout.canvas.CanvasNode;
import de.gerrit_karius.cutout.util.TreeWalker;

public class KeyframeAnimFactory {

	// TODO: read anim params, like duration and easing
	// TODO: create a camera anim
	// TODO: maybe fill in a blank key or something,
	// so that you can have different keys for different models
	// by leaving out some models on some keys?
	// TODO: better model search on keys,
	// try to find models that are not at the top-level

	private static Logger log = Logger.getLogger(KeyframeAnimFactory.class);

	public static Parallel createAnimFromKeyframeIds( Canvas canvas, ArrayList<String> keyframeIds ) {
		ArrayList<CanvasNode> keyframes = new ArrayList<CanvasNode>();
		
		CanvasNode stage = canvas.getRoot().addEmptyChild( "stage" );
		for (int i = 0; i < keyframeIds.size(); i++) {
			CanvasNode keyframe = stage.addSymbolInstance( keyframeIds.get(i), keyframeIds.get(i) );
			log.debug( "Locating keyframe: "+keyframeIds.get(i) );
			log.debug( "Adding keyframe: "+keyframe.getName() );
			keyframes.add( keyframe );
			keyframe.setVisible( false );
		}
//		TreeWalker.traverseCanvasTree(stage, 0);
		return createAnimFromKeyframes( canvas, keyframes );
	}
	
	public static Parallel createAnimFromKeyframes( Canvas canvas, ArrayList<CanvasNode> keyframes ) {
		
		Parallel par = new Parallel();
//		Scene scene = new Scene( canvas );
//		scene.addAnim( par );
				
		
		
//		CanvasNode defFrame = keyframes.remove( 0 );
//		HashMap<String,Skeleton> modelsByName = extractModelsFromDefFrame( defFrame );
		HashMap<String,Skeleton> modelsByName = extractModelsFromDeclaration( keyframes.get( 0 ) );
		
		HashMap<Skeleton,Sequence> seqsByModel = addSequencesForModels( par, modelsByName.values() );
		
		loadKeyFramesIntoModels(keyframes, modelsByName, seqsByModel);
		
		log.debug( "seqsByModel.size(): "+seqsByModel.size() );
		log.debug( "seqsByModel.keySet(): "+seqsByModel.keySet().size() );
		for(Skeleton model : seqsByModel.keySet()) {
			model.loadRootKeys();
		}
		
		if( log.isDebugEnabled() ) {
			log.debug( "Total length of main par: "+par.getDuration() );
//			log.debug( "Total length of scene: "+scene.getDuration() );
			log.debug( "Total sequence levels in scene: "+par.getSize() );
		}
		
		return par;
	}

	private static void loadKeyFramesIntoModels(ArrayList<CanvasNode> keyframes, HashMap<String, Skeleton> modelsByName, HashMap<Skeleton, Sequence> seqsByModel) {
		for (int key = 0; key < keyframes.size(); key++) {
			log.debug( "Loading keyframe #"+key );
			CanvasNode keyframe = keyframes.get( key );
						
			// Search for model keys.
			for( Skeleton model : modelsByName.values() ) {
				CanvasNode modelKey = keyframe.getChild( model.getName() );
				if( modelKey == null )
					modelKey = keyframe;
				
				model.addRootKey( modelKey );
				if( key != keyframes.size()-1 ) {
					seqsByModel.get(model).addAnim( createTweeningAnim( model, keyframes, key ) );
				}
			}
//			for( int i=0; i < keyframe.getSize(); i++ ) {
//				CanvasNode child = keyframe.getChild( i );
//				Skeleton model = modelsByName.get( child.getName() );
//				if( model == null )
//					continue;
//				
//				model.addRootKey( child );
//				if( key != keyframes.size()-1 ) {
//					seqsByModel.get(model).addAnim( createTweeningAnim( model, keyframes, key ) );
//				}
//			}
		}
	}

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
//		String align = frameElement.getAttribute( "align" );
		return tweenAnim;
	}
	
//	private static HashMap<String,Skeleton> extractModelsFromDefFrame( CanvasNode defFrame ) {
//		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
//		for( int i=0; i < defFrame.getSize(); i++ ) {
//			log.debug( "create model for: "+defFrame.getChild(i).getName() );
//			Skeleton model = Skeleton.createModelRoot( defFrame.getChild(i) );
//			modelsByName.put( model.getName(), model );
//		}
//		return modelsByName;
//	}
	
	private static HashMap<String,Skeleton> extractModelsFromDeclaration( CanvasNode defFrame ) {
		HashMap<String,Skeleton> modelsByName = new HashMap<String,Skeleton>();
		SVGElement frameElement = (SVGElement) defFrame.getCanvas().getSourceDoc().getElementById( defFrame.getSymbolId() );
		String modelDeclaration = frameElement.getAttribute("model");
		if( modelDeclaration == null || "".equals( modelDeclaration ) )
			log.error( "couldn't find 'model' declaration in first keyframe: "+defFrame.getName() );
		String modelName = modelDeclaration;
		Skeleton model = defFrame.getCanvas().getLibrary().getModel(modelName);
//		Skeleton model = defFrame.getCanvas().getLibrary().getModel(modelName);
//		if( model == null ) {
//			CanvasNode modelNode = defFrame.getCanvas().symbolNode( defFrame, modelName, modelName );
//			model = Skeleton.createModelRoot( modelNode );
//			modelNode.removeNode();
//			defFrame.getCanvas().getLibrary().putModel(modelName, model);
//		}

//		log.debug( "model: "+model );
		modelsByName.put( model.getName(), model );
		// TODO: add support for several models
		// TODO: make the modelsByName list global
//		for( int i=0; i < defFrame.getSize(); i++ ) {
//			log.debug( "create model for: "+defFrame.getChild(i).getName() );
//			Skeleton model = Skeleton.createModelRoot( defFrame.getChild(i) );
//			modelsByName.put( model.getName(), model );
//		}
		return modelsByName;
	}
	
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
