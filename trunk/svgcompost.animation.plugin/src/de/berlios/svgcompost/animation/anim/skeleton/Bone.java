package de.berlios.svgcompost.animation.anim.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.berlios.svgcompost.animation.canvas.BoneLink;
import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.canvas.SkeletonLink;

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
	
	/*
	protected int numberOfKeys;
	protected int numberOfTweenings;
	
	public AffineTransform[] keyMatrix;
	public CanvasNode[] keyMc;
	public RotationMatrixTweener[] tweener;
	*/
	
	public Bone( String name ) {
		this.name = name;
	}

	/*
	protected void setArrayLengths() {
		this.numberOfKeys = root.numberOfKeys;
		this.numberOfTweenings = numberOfKeys - 1;
		
		keyMatrix = new AffineTransform[numberOfKeys];
		keyMc = new CanvasNode[numberOfKeys];
		
		tweener = new RotationMatrixTweener[numberOfTweenings >= 0 ? numberOfTweenings : 0];

		for (int i = 0; i < numberOfTweenings; i++)
			tweener[i] = new RotationMatrixTweener( null, null, null );

		for (int i = 0; i < size(); i++)
			get(i).setArrayLengths();
	}
	*/
	
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
	
	public void calcKeyMatrices( SkeletonLink keyframeLink ) {
		
		CanvasNode keyNode = keyframeLink.getNodeForBone(this);
		CanvasNode parentKeyNode = parent == null ? null : keyframeLink.getNodeForBone(parent);
		AffineTransform keyMatrix = keyNode.getGlobalTransform();
		
		if( parent == null )
			keyMatrix = keyNode.getTransform();
		else if( parentKeyNode != null )
			subtractFromMatrix( parentKeyNode.getGlobalTransform(), keyMatrix );
		else
			subtractFromMatrix( keyframeLink.getNodeForBone(skeleton).getGlobalTransform(), keyMatrix );
		keyNode.getBoneLink().setKeyMatrix(keyMatrix);
		
		for (int i = 0; i < size(); i++)
			get(i).calcKeyMatrices( keyframeLink );
	}
	
	public void setupTweening( List<CanvasNode> frames, int key ) {
		if( key < 0 || key >= frames.size()-1 )
			return;
		
		SkeletonLink keyframeLink = frames.get(key).getSkeletonLink();
		BoneLink keyNodeLink = keyframeLink.getNodeForBone(this).getBoneLink();
		
		SkeletonLink nextKeyframeLink = frames.get(key+1).getSkeletonLink();
		BoneLink nextKeyNodeLink = nextKeyframeLink.getNodeForBone(this).getBoneLink();
		
		keyNodeLink.getTweener().load( keyNodeLink.getKeyMatrix(), nextKeyNodeLink.getKeyMatrix() );
		
		for (int i = 0; i < size(); i++)
			get(i).setupTweening( frames, key );
	}
	
	public void tween( SkeletonLink tweeningKeyLink, SkeletonLink activeKeyLink, double percentage ) {

		CanvasNode keyNode = activeKeyLink.getNodeForBone(this);
		if( keyNode == null )
			return;

		AffineTransform tween = tweeningKeyLink.getLinkForBone(this).getTweener().tween( percentage );
		
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
	
	/*
	public CanvasNode getKey( int i ) {
		return keyMc[i];
	}
	
	public int getNumberOfKeys() {
		return numberOfKeys;
	}
	*/
	
	/*
	protected void setupTweening() {
		for (int i = 0; i < numberOfKeys; i++) {
			// normal
			if( keyMc[i] != null ) {
				keyMatrix[i] = keyMc[i].getGlobalTransform();
				if( parent == null )
					keyMatrix[i] = keyMc[i].getTransform();
				else if( parent.keyMc[i] != null )
					subtractFromMatrix( parent.keyMc[i].getGlobalTransform(), keyMatrix[i] );
				else
					subtractFromMatrix( root.keyMc[i].getGlobalTransform(), keyMatrix[i] );
			}
			else {
				// no key found for the current index
				// Clone the other key, if this one doesn't exist.
				// TODO: handle null pointers
				//log.error( "keyMc["+i+"] == null at "+name );
			}
			
		}

		for (int i = 0; i < numberOfTweenings; i++)
			tweener[i].load( keyMatrix[i], keyMatrix[i+1] );

		for (int i = 0; i < size(); i++)
			get(i).setupTweening();
	}
	*/
	
	/**
	 * Tweens the cluster structure node.
	 */
	/*
	protected void tweenStructure() {
		tweenGlobal();
		for (int i = 0; i < size(); i++)
			get(i).tweenStructure();
	}
	
	protected void tweenGlobal() {
		int key = root.activeKey;
		double percentage = root.currentPercentage;
		if( keyMc == null && log.isDebugEnabled() )
			log.debug( "keyMc == null? "+(keyMc==null) );
		if( keyMc[key] == null )
			return;
//		log.debug( "root.currentTweening: "+root.currentTweening );
		AffineTransform tween = (AffineTransform) tweener[root.currentTweening].tween( percentage );
		
		if( parent != null ) {
			// Add the parent Bone's current CanvasNode matrix, to get a global transform.
			if( parent.keyMc[key] != null )
				tween.preConcatenate( parent.keyMc[key].getGlobalTransform() );
			else
				tween.preConcatenate( root.keyMc[key].getGlobalTransform() );
			// Subtract the Bone's CanvasNode's parent's matrix, to get a local transform.
			subtractFromMatrix( keyMc[key].getParent().getGlobalTransform(), tween );
		}
//		log.debug( "Set transform on key: "+key );

		keyMc[key].setTransform( tween );
	}
	*/
	
	/*
	public void applyTransformOnWrapperLevel( AffineTransform transform, int key, Bone start, Bone end ) {
		if( end == start )
			return;
		Bone node;
		if( start.level < end.level ) {
			node = end;
			end = start;
			start = node;
		}
		node = start.parent;
		while( node != null ) {
			node.applyTransformOnWrapperLevel( transform, key );
			if( node == end )
				break;
			else
				node = node.parent;
			//TODO: also shift child ModelNodes
		}
	}
	*/	

	/*
	public void applyTransformOnWrapperLevel( AffineTransform transform, int key ) {
		AffineTransform wrapperTrafo = root.keyMc[key].getGlobalTransform();
		AffineTransform fromWrapper = keyMc[key].getGlobalTransform();
		subtractFromMatrix( wrapperTrafo, fromWrapper );
		AffineTransform globalTrafo = (AffineTransform) fromWrapper.clone();
		globalTrafo.preConcatenate( transform );
		globalTrafo.preConcatenate( wrapperTrafo );
		subtractFromMatrix( keyMc[key].getParent().getGlobalTransform(), globalTrafo );
		keyMc[key].setTransform( globalTrafo );
	}
	*/

	/**
	 * Sets the global position of the bone. All child bones save their position
	 * on the parent bone and recall that position once the bone has shifted.
	 * This is done recursively, so that all descendant bones maintain their position
	 * on the shifted ancestor.
	 * @param newPosition the position the Bone is shifted to.
	 * @param i
	 */
//	public void setRecursiveGlobalXY( Point2D.Float newPosition, int i ) {
//		for (int j = 0; j < size(); j++)
//			children.get(j).savePosition( i );
//		keyMc[i].setGlobalXY( newPosition );
//		for (int j = 0; j < size(); j++)
//			children.get(j).recallPosition( i );
//	}
	
	public void setRecursiveLocalXY( Point2D.Float newPosition, SkeletonLink frameLink, CanvasNode local ) {
		CanvasNode node = frameLink.getNodeForBone(this); 
		for (Bone child : children)
			child.savePosition( frameLink );
		node.setLocalXY( newPosition, local );
		for (Bone child : children)
			child.recallPosition( frameLink );
	}
	
	// TODO: make this a temporary method scope variable.
	private Point2D.Float position;
	
	public void savePosition( SkeletonLink frameLink ) {
		CanvasNode node = frameLink.getNodeForBone(this); 
		position = node.getLocalXY( parent != null ? frameLink.getNodeForBone(parent) : node.getParent() );
		if( children == null )
			return;
		for (Bone child : children)
			child.savePosition( frameLink );
	}
	
	public void recallPosition( SkeletonLink frameLink ) {
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
