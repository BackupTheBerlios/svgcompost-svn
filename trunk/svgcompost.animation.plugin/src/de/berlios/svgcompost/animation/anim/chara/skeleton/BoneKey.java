package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * Represents a keyframe of a Bone.
 * @author gerrit
 *
 */
public class BoneKey {

//	private static Logger log = Logger.getLogger(BoneKey.class);

	protected Bone bone;
	private CanvasNode canvasNode;
	protected SkeletonKey skeletonKey;
	
	protected AffineTransform keyMatrix;
	protected RotationMatrixTweener tweener;
	
	public BoneKey( CanvasNode canvasNode, SkeletonKey skeletonKey ) {
		this.canvasNode = canvasNode;
		this.skeletonKey = skeletonKey;
	}
	
//	public void setFrame(CanvasNode frame) {
//		this.frame = frame;
//	}

	public void setBone(Bone bone) {
		this.bone = bone;
	}

//	public Bone getBone() {
//		return bone;
//	}
	
	public BoneKey nextKey() {
		SkeletonKey nextSkeletonKey = skeletonKey.nextKey();
		return nextSkeletonKey==null?null:nextSkeletonKey.getBoneKey(bone);
	}
	public BoneKey previousKey() {
		SkeletonKey previousSkeletonKey = skeletonKey.previousKey();
		return previousSkeletonKey==null?null:previousSkeletonKey.getBoneKey(bone);
	}


	public AffineTransform getKeyMatrix() {
		return keyMatrix;
	}

	public void setKeyMatrix(AffineTransform keyMatrix) {
		this.keyMatrix = keyMatrix;
	}

	public RotationMatrixTweener getTweener() {
		if( tweener == null )
			tweener = new RotationMatrixTweener( null, null, canvasNode );
		return tweener;
	}

	public void setSkeletonKey(SkeletonKey skeletonKey) {
		this.skeletonKey = skeletonKey;
	}

	public SkeletonKey getSkeletonKey() {
		return skeletonKey;
	}


}
