package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.util.xml.Labels;

public class SquashyLimb implements Limb {
	
	protected Bone limb;
	protected Bone target;
	
	/**
	 * 
	 * @param tweeningKeyLink The first of the two keys that are tweened.
	 * @param activeKeyLink The key (first or second) that is changed to create the tweens.
	 * @param percentage The percentage of tweening.
	 */
	public void tween( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {
		CanvasNode keyNode = activeKeyLink.getNodeForBone(limb);
		skewSquashAlignWithPoint( keyNode, keyNode.getChild(Labels.ANCHOR2), activeKeyLink.getNodeForBone(target) );
				
	}

	public SquashyLimb(Bone limb, Bone target) {
		this.limb = limb;
		this.target = target;
	}

	/**
	 * Applies a transformation to the node so that the align point in the node's
	 * own coordinate space aligns with the align-with point.
	 * @param node
	 * @param alignPoint
	 * @param alignWith
	 */
	public static void skewSquashAlignWithPoint(CanvasNode node, CanvasNode alignPoint, CanvasNode alignWith) {
		Point2D.Float alignLocal = alignPoint.projectCenterToLocal( node );
		Point2D.Float alignWithLocal = alignWith.projectCenterToLocal( node );
		float squash = alignLocal.y == 0 ? 0 : alignWithLocal.y / alignLocal.y;
		float skew = (alignWithLocal.x - alignLocal.x) / alignWithLocal.y;
		AffineTransform trafo = AffineTransform.getScaleInstance( 1, squash );
		trafo.preConcatenate( AffineTransform.getShearInstance( skew, 0 ) );
		trafo.preConcatenate( node.getTransform() );
		node.setTransform( trafo );
	}

	@Override
	public Bone getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readRotationPoint(SkeletonKey keyframeLink) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSkeleton(Skeleton skeleton) {
		// TODO Auto-generated method stub
		
	}

}
