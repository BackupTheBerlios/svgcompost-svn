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
	
	protected Map<Bone,BoneKey> keysForBones = new HashMap<Bone,BoneKey>();
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
	
	public CanvasNode getCanvasNode( Bone forBone ) {
		BoneKey key = keysForBones.get(forBone);
		return key == null ? null : key.getCanvasNode();
	}
	
	public BoneKey getBoneKey( Bone forBone ) {
		return keysForBones.get(forBone);
	}
	
	public LimbKey getLimbKey( Limb jointedLimb ) {
		return limbKeys.get( jointedLimb );
	}
	
	public CanvasNode getKeyframeNode() {
		return keyFrame;
	}
	
	protected void searchForBones( CanvasNode node ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			BoneKey key = new BoneKey(bone, node, this);
			keysForBones.put(bone, key);
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
