package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * Represents a keyframe of a Skeleton.
 * @author gerrit
 *
 */
public class SkeletonKey {

	private static Logger log = Logger.getLogger(SkeletonKey.class);


	private CanvasNode keyFrame;
	
	protected SkeletonKey previousKey;
	protected SkeletonKey nextKey;
	
	protected Map<Bone,CanvasNode> nodesForBones = new HashMap<Bone,CanvasNode>();
	protected Map<Limb,LimbKey> limbKeys = new HashMap<Limb,LimbKey>();
	protected Skeleton skeleton;
	
	public SkeletonKey( Skeleton skeleton, CanvasNode canvasNode ) {
		this.skeleton = skeleton;
		this.keyFrame = canvasNode;
		searchForBones(keyFrame);
		for(Limb limb : skeleton.connectors) {
			LimbKey limbKey = new LimbKey( limb, this );
			limbKeys.put(limb, limbKey);
		}
	}
	
	public CanvasNode getNodeForBone( Bone bone ) {
		return nodesForBones.get(bone);
	}
	
	/**
	 * Finds an instance of the given bone in the key frame that this SkeletonLink belongs to.
	 * @param bone A link to the given bone  in this object's key frame.
	 * @return
	 */
	public BoneKey getBoneKey( Bone bone ) {
		CanvasNode node = getNodeForBone( bone );
		if( node == null )
			return null;
		return node.getBoneKey();
	}
	
	public LimbKey getLimbKey( Limb limb ) {
		return limbKeys.get( limb );
	}
	
	protected void searchForBones( CanvasNode node ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			node.getBoneKey().setBone( bone );
			node.getBoneKey().setSkeletonKey( this );
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

	public SkeletonKey nextKey() {
		return nextKey;
	}
	public SkeletonKey previousKey() {
		return previousKey;
	}
}
