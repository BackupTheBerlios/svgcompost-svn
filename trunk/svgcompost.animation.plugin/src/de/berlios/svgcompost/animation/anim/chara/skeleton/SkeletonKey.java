package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.timeline.Keyframe;

/**
 * Represents a keyframe of a Skeleton.
 * @author gerrit
 *
 */
public class SkeletonKey {

	private CanvasNode keyFrame;
	
	protected SkeletonKey previousKey;
	protected SkeletonKey nextKey;
	
	protected Map<Bone,CanvasNode> nodesForBones = new HashMap<Bone,CanvasNode>();
	protected Map<CanvasNode,BoneKey> keysForNodes = new HashMap<CanvasNode,BoneKey>();
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
		return getBoneKey(node);
	}
	
	public BoneKey getBoneKey( CanvasNode node ) {
		return keysForNodes.get(node);
	}
	
	public LimbKey getLimbKey( Limb limb ) {
		return limbKeys.get( limb );
	}
	
	public CanvasNode getKeyframeNode() {
		return keyFrame;
	}
	
	protected void searchForBones( CanvasNode node ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			BoneKey key = new BoneKey(bone, node, this);
			nodesForBones.put(bone, node);
			keysForNodes.put(node, key);
		}

		for( int i = 0; i < node.getSize(); i++ )
			searchForBones( node.get(i) );
	}
	
	public static void setupTweening( List<Keyframe> frames ) {

		for(Keyframe frame : frames) {
			for( Skeleton skeleton : frame.getSkeletonKeys().keySet() ) {
				skeleton.setupTweening(frame.getSkeletonKey(skeleton));
				skeleton.setupLimbTweening(frame);
			}			
		}
	}
	
	public static void tween( Keyframe tweeningKey, Keyframe activeKey, double percentage ) {
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
