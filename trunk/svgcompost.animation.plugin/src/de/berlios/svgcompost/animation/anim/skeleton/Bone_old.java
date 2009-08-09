package de.berlios.svgcompost.animation.anim.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * Represents a model for a composite cluster of animated objects.
 * The parent-child relationship within the cluster tree determines which
 * object should animate within the coordinates of another,
 * even when their mcs have no parent-child relationship in the mc tree.
 * This class provides functions to simulate the parent-child relationships during tweening,
 * even if the entire structure is laid out flat on a single level.
 * @author gerrit
 *
 */
public class Bone_old {
	
	private static Logger log = Logger.getLogger(Bone_old.class);
	
	protected String name;
	protected Bone_old parent;
	protected ArrayList<Bone_old> children;
	protected Skeleton root;
	protected int level;
	
	protected int numberOfKeys;
	protected int numberOfTweenings;
	
	public AffineTransform[] keyMatrix;
	public CanvasNode[] keyMc;
	public RotationMatrixTweener[] tweener;
	
	public Bone_old( String name ) {
		this.name = name;
	}
	
	protected void setArrayLengths() {
//		log.debug("Set array lengths for "+name+" to "+root.numberOfKeys);
		this.numberOfKeys = root.numberOfKeys;
		this.numberOfTweenings = numberOfKeys - 1;
		
		keyMatrix = new AffineTransform[numberOfKeys];
		keyMc = new CanvasNode[numberOfKeys];
		
//		log.debug( "numberOfKeys: "+numberOfKeys );
		tweener = new RotationMatrixTweener[numberOfTweenings >= 0 ? numberOfTweenings : 0];

		for (int i = 0; i < numberOfTweenings; i++)
			tweener[i] = new RotationMatrixTweener( null, null, null );

		for (int i = 0; i < size(); i++)
			get(i).setArrayLengths();
	}
	
	public void add(Bone_old child) {
		if( children == null )
			children = new ArrayList<Bone_old>();
		children.add(child);
		child.parent = this;
		child.root = root;
		child.level = level + 1;
		root.registerBone_old( child.name, child );
	}
	
	public Bone_old get(int index) {
		if( children == null )
			return null;
		return (Bone_old) children.get(index);
	}
	
	public int size() {
		if( children == null )
			return 0;
		return children.size();
	}
	
	public String getName() {
		return name;
	}
	
	public CanvasNode getKey( int i ) {
		return keyMc[i];
	}
	
	public int getNumberOfKeys() {
		return numberOfKeys;
	}
	
	protected static void createNode( CanvasNode mc, Bone_old parent ) {
		String mcName = mc.getName();
		if( log.isTraceEnabled() ) {
			log.trace( "create bone with name: "+mcName );
			log.trace( "# of children: "+mc.getSize() );
		}
		for (int i = 0; i < mc.getSize(); i++) {
			CanvasNode mcChild = mc.get( i );
			String childName = mcChild.getName();
			if( log.isTraceEnabled() )
				log.trace( "create child bone with name: "+mcName );
			if( childName != null && ! childName.equals( "" ) && ! childName.equals( mcName ) ) {
				Bone_old child = new Bone_old( childName );
				parent.add( child );
				createNode( mcChild, child );
			}
		}
	}
	
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
	 * Tweens the cluster structure node.
	 */
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
			// Add the parent Bone_old's current CanvasNode matrix, to get a global transform.
			if( parent.keyMc[key] != null )
				tween.preConcatenate( parent.keyMc[key].getGlobalTransform() );
			else
				tween.preConcatenate( root.keyMc[key].getGlobalTransform() );
			// Subtract the Bone_old's CanvasNode's parent's matrix, to get a local transform.
			subtractFromMatrix( keyMc[key].getParent().getGlobalTransform(), tween );
		}
//		log.debug( "Set transform on key: "+key );

		keyMc[key].setTransform( tween );
	}
	

	public void applyTransformOnWrapperLevel( AffineTransform transform, int key, Bone_old start, Bone_old end ) {
		if( end == start )
			return;
		Bone_old node;
		if( start.level < end.level ) {
			node = end;
			end = start;
			start = node;
		}
		node = start.parent;
//		System.out.println( "start: "+start.name );
//		System.out.println( "end: "+end.name );
		while( node != null ) {
//			System.out.println( "apply to: "+node.name );
			node.applyTransformOnWrapperLevel( transform, key );
			if( node == end )
				break;
			else
				node = node.parent;
			//TODO: also shift child ModelNodes
		}
	}	

	
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

	/**
	 * Sets the global position of the bone. All child bones save their position
	 * on the parent bone and recall that position once the bone has shifted.
	 * This is done recursively, so that all descendant bones maintain their position
	 * on the shifted ancestor.
	 * @param newPosition the position the Bone_old is shifted to.
	 * @param i
	 */
	public void setRecursiveGlobalXY( Point2D.Float newPosition, int i ) {
		for (int j = 0; j < size(); j++)
			children.get(j).savePosition( i );
		keyMc[i].setGlobalXY( newPosition );
		for (int j = 0; j < size(); j++)
			children.get(j).recallPosition( i );
	}
	
	public void setRecursiveLocalXY( Point2D.Float newPosition, int i, CanvasNode local ) {
		for (int j = 0; j < size(); j++)
			children.get(j).savePosition( i );
		keyMc[i].setLocalXY( newPosition, local );
		for (int j = 0; j < size(); j++)
			children.get(j).recallPosition( i );
	}
	
	// TODO: make this a temporary method scope variable.
	private Point2D.Float position;
	
	public void savePosition( int i ) {
		position = keyMc[i].getLocalXY( parent != null ? parent.keyMc[i] : keyMc[i].getParent() );
		for (int j = 0; j < size(); j++) {
			children.get(j).savePosition( i );
		}
	}
	
	public void recallPosition( int i ) {
		keyMc[i].setLocalXY( position, parent != null ? parent.keyMc[i] : keyMc[i].getParent() );
		for (int j = 0; j < size(); j++) {
			children.get(j).recallPosition( i );
		}
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
