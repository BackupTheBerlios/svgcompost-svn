package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * A bone is a part of an animated skeleton.
 * The parent-child relationship within the skeleton's bone tree determines which
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
	protected List<Bone> children;
	protected Skeleton skeleton;
	protected int level;
	
	public Bone( String name ) {
		this.name = name;
	}

	public void add(Bone child) {
		if( children == null )
			children = new ArrayList<Bone>();
		children.add(child);
		child.parent = this;
		child.skeleton = skeleton;
		child.level = level + 1;
		skeleton.registerBone( child.name, child );
	}
	
	public Bone get(int index) {
		if( children == null )
			return null;
		return (Bone) children.get(index);
	}
	
	public int size() {
		if( children == null )
			return 0;
		return children.size();
	}
	
	public String getName() {
		return name;
	}
	
	public Skeleton getSkeleton() {
		return skeleton;
	}
	
	protected void calcKeyMatrices( SkeletonKey keyframeLink ) {
		
		CanvasNode keyNode = keyframeLink.getNodeForBone(this);
		CanvasNode parentKeyNode = parent == null ? null : keyframeLink.getNodeForBone(parent);
		AffineTransform keyMatrix = keyNode.getGlobalTransform();
		
		if( parent == null )
			keyMatrix = keyNode.getTransform();
		else if( parentKeyNode != null )
			subtractFromMatrix( parentKeyNode.getGlobalTransform(), keyMatrix );
		else
			subtractFromMatrix( keyframeLink.getNodeForBone(skeleton).getGlobalTransform(), keyMatrix );
		keyNode.getBoneKey().setKeyMatrix(keyMatrix);
		
		for (int i = 0; i < size(); i++)
			get(i).calcKeyMatrices( keyframeLink );
	}
	
	public void setupTweening( SkeletonKey keyframeLink ) {

		if( keyframeLink == null || keyframeLink.nextKey() == null )
			return;
		
		BoneKey boneKey = keyframeLink.getNodeForBone(this).getBoneKey();
		BoneKey nextBoneKey = boneKey.nextKey();
		
		if( boneKey.getKeyMatrix() == null )
			calcKeyMatrices(keyframeLink);
		if( nextBoneKey.getKeyMatrix() == null )
			calcKeyMatrices(keyframeLink.nextKey());
		
		boneKey.getTweener().load( boneKey.getKeyMatrix(), nextBoneKey.getKeyMatrix() );
		
		for (int i = 0; i < size(); i++)
			get(i).setupTweening( keyframeLink );
	}
	
	public void tween( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {

		CanvasNode keyNode = activeKeyLink.getNodeForBone(this);
		if( keyNode == null )
			return;

		AffineTransform tween = tweeningKeyLink.getBoneKey(this).getTweener().tween( percentage );
		
		if( parent != null ) {
			// Add the parent Bone's current CanvasNode matrix, to get a global transform.
			CanvasNode parentKeyNode = activeKeyLink.getNodeForBone(parent);

			if( parentKeyNode != null )
				tween.preConcatenate( parentKeyNode.getGlobalTransform() );
			else
				tween.preConcatenate( activeKeyLink.getNodeForBone(skeleton).getGlobalTransform() );
			// Subtract the Bone's CanvasNode's parent's matrix, to get a local transform.
			subtractFromMatrix( keyNode.getParent().getGlobalTransform(), tween );
		}

		keyNode.setTransform( tween );
		
		for (int i = 0; i < size(); i++)
			get(i).tween( tweeningKeyLink, activeKeyLink, percentage );
	}
	

	
//	/**
//	 * Calculates from matrices source S and target T the relative matrix R so that R.concat(S) == T.
//	 * Actually, it's S.concat(R) == T...
//	 * @param sourceMatrix
//	 * @param targetMatrix
//	 * @return The relative matrix.
//	 */
//	public static AffineTransform calcRelativeMatrix( AffineTransform sourceMatrix, AffineTransform targetMatrix ) {
//		AffineTransform a = (AffineTransform) sourceMatrix.clone();
//		AffineTransform c = (AffineTransform) targetMatrix.clone();
//		try {
//			a = a.createInverse();
//		} catch(Exception e){}
//		c.preConcatenate( a );
//		return c;
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
		} catch(Exception e){
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
	 * Sets the global position of the bone. All child bones save their position
	 * on the parent bone and recall that position once the bone has shifted.
	 * This is done recursively, so that all descendant bones maintain their position
	 * on the shifted ancestor.
	 * @param newPosition the position the Bone is shifted to.
	 */
	public void setRecursiveLocalXY( Point2D.Float newPosition, SkeletonKey frameLink, CanvasNode local ) {
		CanvasNode node = frameLink.getNodeForBone(this); 
		for (Bone child : children)
			child.savePosition( frameLink );
		node.setLocalXY( newPosition, local );
		for (Bone child : children)
			child.recallPosition( frameLink );
	}
	
	// TODO: make this a temporary method scope variable.
	private Point2D.Float position;
	
	public void savePosition( SkeletonKey frameLink ) {
		CanvasNode node = frameLink.getNodeForBone(this); 
		position = node.getLocalXY( parent != null ? frameLink.getNodeForBone(parent) : node.getParent() );
		if( children == null )
			return;
		for (Bone child : children)
			child.savePosition( frameLink );
	}
	
	public void recallPosition( SkeletonKey frameLink ) {
		CanvasNode node = frameLink.getNodeForBone(this); 
		node.setLocalXY( position, parent != null ? frameLink.getNodeForBone(parent) : node.getParent() );
		if( children == null )
			return;
		for (Bone child : children)
			child.recallPosition( frameLink );
	}
	
	public String toString() {
		return toString(0);
	}
	
	protected String toString( int level ) {
		String string = "";
		for (int i = 0; i < size(); i++) {
			string += get( i ).toString( level + 1 );
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
