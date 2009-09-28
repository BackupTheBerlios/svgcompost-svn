package de.berlios.svgcompost.animation.canvas;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.Skeleton;

/**
 * Provides links to skeletons' bones in a given key frame.
 * @author gerrit
 *
 */
public class SkeletonKey {

	private static Logger log = Logger.getLogger(SkeletonKey.class);


	private CanvasNode keyFrame;
	
	protected SkeletonKey previousKey;
	protected SkeletonKey nextKey;
	
	protected HashMap<Bone,CanvasNode> nodesForBones = new HashMap<Bone,CanvasNode>();
	protected Skeleton skeleton;
	
	public SkeletonKey( Skeleton skeleton, CanvasNode canvasNode ) {
		this.skeleton = skeleton;
		this.keyFrame = canvasNode;
		searchForBones(keyFrame);
	}
	
	public CanvasNode getNodeForBone( Bone bone ) {
		return nodesForBones.get(bone);
	}
	
	/**
	 * Finds an instance of the given bone in the key frame that this SkeletonLink belongs to.
	 * @param bone A link to the given bone  in this object's key frame.
	 * @return
	 */
	public BoneKey getLinkForBone( Bone bone ) {
		CanvasNode node = getNodeForBone( bone );
		if( node == null )
			return null;
		return node.getBoneKey();
	}
	
	protected void searchForBones( CanvasNode node ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			node.getBoneKey().setBone( bone );
			nodesForBones.put(bone, node);
		}
		else
			log.debug( "couldn't find a bone named "+node.getName()+" for skeleton "+skeleton.getName() );
		for( int i = 0; i < node.getSize(); i++ )
			searchForBones( node.get(i) );
	}
	
	public static void setupTweening( List<CanvasNode> frames ) {
		for(CanvasNode frame : frames) {
			for( Skeleton skeleton : frame.getSkeletonKeys().keySet() ) {
				skeleton.calcKeyMatrices(frame.getSkeletonKey(skeleton));			
				skeleton.calcLimbMatrices(frame.getSkeletonKey(skeleton));			
			}
		}
		for(int i=0; i<frames.size(); i++) {
			for( Skeleton skeleton : frames.get(i).getSkeletonKeys().keySet() ) {
				skeleton.setupTweening(frames, i);
				skeleton.setupLimbTweening(frames, i);
			}
		}
	}
	
	public static void tween( CanvasNode tweeningKey, CanvasNode activeKey, double percentage ) {
		for( Skeleton skeleton : tweeningKey.getSkeletonKeys().keySet() ) {
			skeleton.tween(tweeningKey.getSkeletonKey(skeleton), activeKey.getSkeletonKey(skeleton), percentage);
			skeleton.tweenLimbs(tweeningKey.getSkeletonKey(skeleton), activeKey.getSkeletonKey(skeleton), percentage);
		}
	}

	public void setPreviousKey(SkeletonKey previousKey) {
		this.previousKey = previousKey;
		if( previousKey != null ) {
			previousKey.nextKey = this;
		}
	}

}
