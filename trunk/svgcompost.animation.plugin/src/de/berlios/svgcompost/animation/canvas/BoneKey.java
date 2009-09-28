package de.berlios.svgcompost.animation.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.CatmullRomTweener;
import de.berlios.svgcompost.animation.anim.skeleton.RotationMatrixTweener;

/**
 * Links from a keyframe CanvasNode to a Bone inside a specific Skeleton.
 * @author gerrit
 *
 */
public class BoneKey {

	private static Logger log = Logger.getLogger(BoneKey.class);

	protected Bone bone;
	private CanvasNode canvasNode;
	
//	protected CanvasNode frame;
	protected List<CanvasNode> frames;
	protected int key;
	
	protected AffineTransform keyMatrix;
	protected RotationMatrixTweener tweener;
	
	protected AffineTransform limbKeyMatrix;
	protected CatmullRomTweener limbTweener;
	protected Point2D.Float[] limbPoint;
	
	public BoneKey( CanvasNode canvasNode ) {
		this.canvasNode = canvasNode;
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
	
	/**
	 * Returns a link to the same bone in a keyframe
	 * relative to this bone's keyframe.
	 */
	public BoneKey getRelativeKey( int d ) {
		if(frames == null)
			return null;
		int i= d + key;
		if( i<0 || i>=frames.size())
			return null;
		else
			return frames.get(i).getSkeletonKey(bone.getSkeleton()).getLinkForBone(bone);
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

	public CatmullRomTweener getLimbTweener() {
		return limbTweener;
	}

	public AffineTransform getLimbKeyMatrix() {
		return limbKeyMatrix;
	}

	public void setLimbKeyMatrix(AffineTransform limbKeyMatrix) {
		this.limbKeyMatrix = limbKeyMatrix;
	}

	public Point2D.Float[] getLimbPoint() {
		return limbPoint;
	}

	public void setLimbPoint(Point2D.Float[] limbPoint) {
		this.limbPoint = limbPoint;
	}

	public void setFrames(List<CanvasNode> frames, int key) {
		this.frames = frames;
		this.key = key;
	}


}
