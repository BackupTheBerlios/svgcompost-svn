package de.gerrit_karius.cutout.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import de.gerrit_karius.cutout.anim.skeleton.Bone;
import de.gerrit_karius.cutout.anim.skeleton.RotationMatrixTweener;

/**
 * Links a CanvasNode to a Bone inside a specific Skeleton.
 * @author gerrit
 *
 */
public class BoneLink {

	private static Logger log = Logger.getLogger(BoneLink.class);

	protected Bone bone;
	private CanvasNode canvasNode;
	
	protected CanvasNode frame;
	
	protected AffineTransform keyMatrix;
	protected RotationMatrixTweener tweener;
	
	protected AffineTransform limbKeyMatrix;
	protected Point2D.Float[] limbPoint;
	
	public BoneLink( CanvasNode canvasNode ) {
		this.canvasNode = canvasNode;
	}
	
	public void setFrame(CanvasNode frame) {
		this.frame = frame;
	}

	public void setBone(Bone bone) {
		this.bone = bone;
	}

//	public Bone getBone() {
//		return bone;
//	}

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


}
