package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.timeline.Keyframe;

/**
 * A jointedLimb is a part of an animated skeleton.
 * The parent-child relationship within the skeleton's jointedLimb tree determines which
 * object should animate within the coordinates of another,
 * even when their graphical nodes have no parent-child relationship in the graphics node tree,
 * or change their hierarchy during animation.
 * This class provides functions to simulate the parent-child relationships during tweening,
 * even if the entire structure is laid out flat on a single level.
 * @author gerrit
 *
 */
public class Bone {
	
	protected String name;
	protected Bone parent;
	protected List<Bone> children = new ArrayList<Bone>();
	protected Skeleton skeleton;
	protected int level;
	
	public Bone( String name ) {
		this.name = name;
	}

	public void add(Bone child) {
		children.add(child);
		child.parent = this;
		child.skeleton = skeleton;
		child.level = level + 1;
		skeleton.registerBone( child.name, child );
	}
	
//	public Bone get(int index) {
//		return children.get(index);
//	}
	
//	public int size() {
//		return children.size();
//	}
	
	public List<Bone> getBones() {
		return children;
	}
	
	public String getName() {
		return name;
	}
	
	public Skeleton getSkeleton() {
		return skeleton;
	}
	
	protected CanvasNode getParentKeyNode( SkeletonKey skeletonKey, CanvasNode keyNode ) {
		// Start with the parent.
		Bone ancestor = parent;
		do {
			// If there's no ancestor bone, use the graphical parent element.
			if( ancestor == null )
				return keyNode.getParent();
			// If there's an ancestor bone with a key, use it.
			if( skeletonKey.getCanvasNode(ancestor) != null )
				return skeletonKey.getCanvasNode(ancestor);
			// If the ancestor exists, but has no key, try its parent and so on.
			ancestor = ancestor.parent;
		} while( true );
	}
	
	protected void calcKeyMatrices( SkeletonKey skeletonKey ) {
		try {
			BoneKey key = skeletonKey.getBoneKey(this);
			CanvasNode keyNode = key.getCanvasNode();
			// Find a suitable parent node.
			CanvasNode parentKeyNode = getParentKeyNode(skeletonKey, keyNode);
			key.setParentNode(parentKeyNode);
			
			AffineTransform keyMatrix = keyNode.getGlobalTransform();
			// Calculate a matrix relative to the virtual parent node.
			subtractFromMatrix( parentKeyNode.getGlobalTransform(), keyMatrix );
				
			skeletonKey.getBoneKey(this).setKeyMatrix(keyMatrix);
			
		} catch (NullPointerException e) {
//			e.printStackTrace();
		}
		
//		for (Bone bone : children)
//			bone.calcKeyMatrices( skeletonKey );
	}
	
	/**
	 * Set up the tweening for this Bone and its children on the specified SkeletonKey.
	 * @param skeletonKey
	 */
	protected void setupTweening( SkeletonKey skeletonKey ) {

		try {
			BoneKey boneKey = skeletonKey.getBoneKey(this);
			BoneKey nextBoneKey = boneKey.nextKey();
			
			if( boneKey.getKeyMatrix() == null )
				calcKeyMatrices(skeletonKey);
			if( nextBoneKey.getKeyMatrix() == null )
				calcKeyMatrices(skeletonKey.nextKey());
			
			boneKey.getTweener().load(
					boneKey.previousKey() == null ? null : boneKey.previousKey().getKeyMatrix(),
					boneKey.getKeyMatrix(),
					nextBoneKey.getKeyMatrix(),
					nextBoneKey.nextKey() == null ? null : nextBoneKey.nextKey().getKeyMatrix()
				);
		} catch (NullPointerException e) {
//			e.printStackTrace();
		}
		
		for (Bone bone : children)
			bone.setupTweening( skeletonKey );
	}
	
	public void tween( SkeletonKey tweeningKey, SkeletonKey activeKey, double percentage ) {

		try {
			BoneKey key = activeKey.getBoneKey(this);
			CanvasNode keyNode = key.getCanvasNode();
			CanvasNode parentKeyNode = key.getParentNode();

			AffineTransform tween = tweeningKey.getBoneKey(this).getTweener().tween( percentage );
			
			// Add the parent Bone's current CanvasNode matrix, to get a global transform.
			tween.preConcatenate( parentKeyNode.getGlobalTransform() );
	
			// Subtract the Bone's CanvasNode's parent's matrix, to get a local transform.
			subtractFromMatrix( keyNode.getParent().getGlobalTransform(), tween );
	
			keyNode.setTransform( tween );
		} catch (NullPointerException e) {
//			e.printStackTrace();
		}
		
		for (Bone bone : children)
			bone.tween( tweeningKey, activeKey, percentage );
	}
	
//	private void shiftKeyMatrix(CanvasNode parentKeyNode, CanvasNode keyNode,
//			CanvasNode anchor1, AffineTransform keyMatrix) {
//		Point2D.Float anchorOnParent = anchor1.projectCenterToLocal(parentKeyNode);
//		Point2D.Float centerOnParent = keyNode.projectCenterToLocal(parentKeyNode);
//		keyMatrix.preConcatenate( AffineTransform.getTranslateInstance(-centerOnParent.x+anchorOnParent.x,-centerOnParent.x+anchorOnParent.y) );
//		// ...or is it concatenate?
//	}

	
	/**
	 * Calculates from matrices source S and target T the relative matrix R so that R.concat(S) == T.
	 * In this process, the source is inverted and subtracted from the target matrix.
	 * @param sourceMatrix
	 * @param targetMatrix
	 */
	public static void subtractFromMatrix( AffineTransform sourceMatrix, AffineTransform targetMatrix ) {
//		AffineTransform targetClone = (AffineTransform) targetMatrix.clone();
//		AffineTransform sourceClone = (AffineTransform) sourceMatrix.clone();
		try {
			sourceMatrix = sourceMatrix.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
//		sourceMatrix.invert();
//		System.out.println( "sourceMatrix: "+sourceMatrix );
//		System.out.println( "targetMatrix: "+targetMatrix );
		targetMatrix.preConcatenate( sourceMatrix );
		
//		AffineTransform relClone = (AffineTransform) targetMatrix.clone();
//		// Check:
//		relClone.preConcatenate( sourceClone );
//		System.out.println( "relClone: "+relClone );
//		System.out.println( "trgClone: "+targetClone );
	}
	

	/**
	 * Sets the position for this Bone on the specified SkeletonKey.
	 * @param newPosition the position the Bone is shifted to.
	 */
	public void setGlobalPosition( Point2D.Float newPosition, SkeletonKey skeletonKey, CanvasNode local ) {
		CanvasNode node = skeletonKey.getCanvasNode(this); 
		for (Bone child : children)
			child.savePosition( skeletonKey );
		node.setLocalXY( newPosition, local );
		for (Bone child : children)
			child.recallPosition( skeletonKey );
	}
	
	// TODO: make this a temporary method scope variable.
	private Point2D.Float position;
	
	public void savePosition( SkeletonKey skeletonKey ) {
		CanvasNode node = skeletonKey.getCanvasNode(this); 
		position = node.getLocalXY( parent != null ? skeletonKey.getCanvasNode(parent) : node.getParent() );
		if( children == null )
			return;
		for (Bone child : children)
			child.savePosition( skeletonKey );
	}
	
	public void recallPosition( SkeletonKey skeletonKey ) {
		CanvasNode node = skeletonKey.getCanvasNode(this); 
		node.setLocalXY( position, parent != null ? skeletonKey.getCanvasNode(parent) : node.getParent() );
		if( children == null )
			return;
		for (Bone child : children)
			child.recallPosition( skeletonKey );
	}
	
	public String toString() {
		return toString(0);
	}
	
	protected String toString( int level ) {
		String string = "";
		for (Bone bone : children) {
			string += bone.toString( level + 1 );
		}
//		string = string.replaceAll( "\n", "\n  " );
		String start = "";
		for (int i = 0; i < level; i++) {
			start += "  ";
		}
		string = start + name + " {\n" + string + start + "}\n";
		return string;
	}

}
