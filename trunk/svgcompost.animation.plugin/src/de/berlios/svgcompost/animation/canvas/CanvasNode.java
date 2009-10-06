package de.berlios.svgcompost.animation.canvas;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.chara.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.chara.skeleton.BoneKey;
import de.berlios.svgcompost.animation.anim.chara.skeleton.Skeleton;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;


public class CanvasNode {
	
	private static Logger log = Logger.getLogger(CanvasNode.class);

	protected static final double[] matrixDummy = new double[6];
	protected static final Point2D.Float pointDummy = new Point2D.Float();
	protected static final List<GraphicsNode> emptyList = new ArrayList<GraphicsNode>();
	
	public static final LabelKey KEY_WRAPPER = new LabelKey(1026);
	
	public static final String inkscapeNs = "http://www.inkscape.org/namespaces/inkscape";
	public static final String xlinkNs = "http://www.w3.org/1999/xlink";
	public static final String inkscapePrefix = "inkscape";
	
	private GraphicsNode gNode;
	private BoneKey boneKey = new BoneKey( this, null );
	
	private Canvas canvas;
	
	private CanvasNode( GraphicsNode gNode, Canvas canvas ) {
		this.gNode = gNode;
		this.canvas = canvas;
	}
	
	public static CanvasNode getCanvasNode( GraphicsNode gNode, Canvas canvas ) {
		if( gNode == null )
			return null;
		RenderingHints hints =  gNode.getRenderingHints();
		CanvasNode cNode = null;
		if( hints != null )
			cNode = (CanvasNode) hints.get( KEY_WRAPPER );
		if( cNode == null ) {
			cNode = new CanvasNode( gNode, canvas );
			gNode.setRenderingHint( KEY_WRAPPER, cNode );
		}
		return cNode;
	}
	
	protected List<GraphicsNode> getGraphicsNodeList() {
		if( gNode == null || ! (gNode instanceof CompositeGraphicsNode) )
			return emptyList;
		CompositeGraphicsNode group = (CompositeGraphicsNode) gNode;
		List<GraphicsNode> children = group.getChildren();
		return children;
	}
		
	public CanvasNode getChild( String name ) {
		List<GraphicsNode> children = getGraphicsNodeList();
		for (GraphicsNode child : children) {
			RenderingHints hints = child.getRenderingHints();
			if( hints == null )
				continue;
			if( name.equals( hints.get( Canvas.KEY_LABEL ) ) )
				return getCanvasNode( child, canvas );
		}
		return null;
	}
	
	public CanvasNode get( int index ) {
		return getCanvasNode( getGraphicsNodeList().get( index ), canvas );
	}
	
	public int getSize() {
		return getGraphicsNodeList().size();
//		if( ! (gNode instanceof CompositeGraphicsNode) ) {
//			log.trace( "getSize() invoked on non-group node "+getName() );
//			log.trace( "class of "+getName()+" is "+gNode.getClass().getName() );
//			if( gNode instanceof TextNode ) {
//				TextNode textNode = (TextNode) gNode;
//				log.trace( "TextNode text: "+textNode.getText() );
//			}
//			return 0;
//		}
//		return ((CompositeGraphicsNode)gNode).size();
	}
	
//	public CanvasNode get( int i ) {
//		if( ! (gNode instanceof CompositeGraphicsNode) ) {
//			if( getName().equals( "elfyPose1" ) )
//				log.debug("id "+getSymbolId()+" is not a CompositeGraphicsNode: "+gNode.getClass());
//			return null;
//		}
//		return getCanvasNode( (GraphicsNode) ((CompositeGraphicsNode)gNode).get( i ), canvas );
//	}
	
	public ArrayList<CanvasNode> getChildren() {
		List<GraphicsNode> children = getGraphicsNodeList();
		ArrayList<CanvasNode> childList = new ArrayList<CanvasNode>();
		for( int i=0; i<children.size(); i++ )
			childList.add( getCanvasNode( children.get(i), canvas ) );
		return childList;
	}
	
	public CanvasNode getParent() {
		return getCanvasNode( gNode.getParent(), canvas );
	}
	
	public CanvasNode addSymbolInstance( String symbolId, String name ) {
		return canvas.insertSymbolNode(this, symbolId, name);
	}
	
	public CanvasNode addEmptyChild( String name ) {
		return Canvas.insertGroupNode(this, name);
	}
	
	public void removeNode() {
		if( gNode.getParent() == null )
			return;
		CompositeGraphicsNode group = (CompositeGraphicsNode) gNode.getParent();
		group.remove( gNode );
	}
		
	public boolean isVisible() {
		return gNode.isVisible();
	}
	
	public void setVisible( boolean visible ) {
		gNode.setVisible( visible );
	}
	
	public float getX() {
		return (float) getTransform().getTranslateX();
	}
	
	public float getY() {
		return (float) getTransform().getTranslateY();
	}
	
	public Point2D.Float getXY() {
		return new Point2D.Float( getX(), getY() );
	}
	
	public Point2D.Float getGlobalXY() {
		Point2D.Float xy = new Point2D.Float( 0, 0 );
		getGlobalTransform().transform( xy, xy );
		return xy;
	}
	
	public void setGlobalXY( Point2D.Float global ) {
		try {
			getParent().getGlobalTransform().inverseTransform( global, pointDummy );
		} catch (NoninvertibleTransformException e) {
			log.error( e.getMessage(), e );
		}
		setXY( pointDummy.x, pointDummy.y );
	}
	
	public Point2D.Float getLocalXY( CanvasNode localNode ) {
//		Point2D.Float xy = new Point2D.Float( 0, 0 );
//		getGlobalTransform().transform( xy, xy );
//		try {
//			localNode.getGlobalTransform().inverseTransform( xy, xy );
//		} catch (NoninvertibleTransformException e) {
//			log.error( e.getMessage(), e );
//		}
//		return xy;
		return projectCenterToLocal( localNode );
	}
	
	public void setLocalXY( Point2D.Float global, CanvasNode localNode ) {
		localNode.getGlobalTransform().transform( global, pointDummy );
		try {
			getParent().getGlobalTransform().inverseTransform( pointDummy, pointDummy );
		} catch (NoninvertibleTransformException e) {
			log.error( e.getMessage(), e );
		}
		setXY( pointDummy.x, pointDummy.y );
	}
	
	public void setScale( float scale ) {
		if( gNode.getTransform() != null )
			gNode.getTransform().translate( scale / gNode.getTransform().getScaleX(),
					scale / gNode.getTransform().getScaleY() );
		else
			gNode.setTransform( AffineTransform.getScaleInstance( scale, scale ) );
	}
	
	public void setX( float x ) {
		if( gNode.getTransform() != null )
			gNode.getTransform().translate( x-gNode.getTransform().getTranslateX(), 0 );
		else
			gNode.setTransform( AffineTransform.getTranslateInstance( x, 0 ) );
	}
	
	public void setY( float y ) {
		if( gNode.getTransform() != null )
			gNode.getTransform().translate( 0, y-gNode.getTransform().getTranslateY() );
		else
			gNode.setTransform( AffineTransform.getTranslateInstance( 0, y ) );
	}

	public void setXY( Point2D.Float xy ) {
		setXY( xy.x, xy.y );
	}
	
	public void setXY( float x, float y ) {
		if( gNode.getTransform() != null ) {
			gNode.getTransform().getMatrix( matrixDummy );
			gNode.getTransform().setTransform( matrixDummy[0], matrixDummy[1], matrixDummy[2], matrixDummy[3], x, y );
		}
		else
			gNode.setTransform( AffineTransform.getTranslateInstance( x, y ) );
	}
	
	public String getName() {
		RenderingHints hints = gNode.getRenderingHints();
		if( hints == null )
			return null;
		return (String) hints.get( Canvas.KEY_LABEL );
	}
	
	public String getSymbolId() {
		RenderingHints hints = gNode.getRenderingHints();
		if( hints == null )
			return null;
		return (String) hints.get( Canvas.KEY_SYMBOL_ID );
	}
	
	public String getPath() {
		if( gNode.getParent() == null )
			return ""+getName();
		return getParent().getPath()+"."+getName();
	}
	
	public void setTransform( AffineTransform transform ) {
		gNode.setTransform( (AffineTransform) transform.clone() );
		if( ! getTransform().equals( transform ) ) {
			log.warn( "transforms NOT equal (NaN values?): "+transform );
//			log.warn( transform );
//			log.warn( getTransform() );
		}
	}
	
	public AffineTransform getTransform() {
		if( gNode.getTransform() == null ) {
			return new AffineTransform();
		}
//		else if( ! node.getTransform().isIdentity() )
//			System.out.println( "has transform: "+getName( node ) );
		return (AffineTransform) gNode.getTransform().clone();
	}
	
	public AffineTransform getGlobalTransform() {
		if( gNode.getGlobalTransform() == null ) {
			log.warn( "GlobalTransform was NULL for "+getPath() );
//			System.out.println( "GlobalTransform was NULL." );
			return new AffineTransform();
		}
		return (AffineTransform) gNode.getGlobalTransform().clone();
	}

	public void setGlobalTransform( AffineTransform transform ) {
		if( gNode.getParent() != null ) {
			transform = (AffineTransform) transform.clone();
			try {
				transform.preConcatenate( gNode.getParent().getGlobalTransform().createInverse() );
			} catch (NoninvertibleTransformException e) {
				log.error( e.getMessage(), e );
			}
		}
		setTransform( transform );
	}
	
	public AffineTransform getLocalToLocal( CanvasNode toLocal ) {
//		if( toLocal == null )
//			return null;
		AffineTransform trafo = getGlobalTransform();
		try {
			trafo.preConcatenate( toLocal.getGlobalTransform().createInverse() );
		} catch (NoninvertibleTransformException e) {
			log.error( e.getMessage(), e );
			return null;
		}
		return trafo;
	}

	public Point2D.Float projectCenterToLocal( CanvasNode toLocal ) {
//		AffineTransform trafoToLocal = getLocalToLocal( toLocal );
//		Point2D.Float center = new Point2D.Float( 0, 0 );
//		trafoToLocal.transform( center, center );
		return projectPointToLocal( new Point2D.Float( 0, 0 ), toLocal );
	}

	public Point2D.Float projectPointToLocal( Point2D.Float point, CanvasNode toLocal ) {
		AffineTransform trafoToLocal = getLocalToLocal( toLocal );
		Point2D.Float projectedPoint = new Point2D.Float( 0, 0 );
		trafoToLocal.transform( point, projectedPoint );
		return projectedPoint;
	}

	public GraphicsNode getGraphicsNode() {
		return gNode;
	}
	
	public BoneKey getBoneKey() {
		return boneKey;
	}

	protected Map<Skeleton,SkeletonKey> skeletonKeys = new HashMap<Skeleton,SkeletonKey>();
	
	public SkeletonKey applySkeleton( Skeleton skeleton, Map<Skeleton, SkeletonKey> previousKeys ) {
		SkeletonKey skeletonKey = new SkeletonKey( skeleton, this );
		skeletonKeys.put( skeleton, skeletonKey );
		if( previousKeys != null && previousKeys.containsKey(skeleton) )
			skeletonKey.setPreviousKey( previousKeys.get(skeleton) );
		return skeletonKey;
	}
	
	protected void searchForBones( Skeleton skeleton, CanvasNode node, HashMap<Bone,CanvasNode> nodesForBones ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			node.getBoneKey().setBone( bone );
			nodesForBones.put(bone, node);
		}
		else
			log.debug( "couldn't find a bone named "+node.getName() );
		
		for( int i = 0; i < node.getSize(); i++ )
			searchForBones( skeleton, node.get(i), nodesForBones );
	}

	public SkeletonKey getSkeletonKey( Skeleton forSkeleton ) {
		return skeletonKeys.get(forSkeleton);
	}
	
	public Map<Skeleton,SkeletonKey> getSkeletonKeys() {
		return skeletonKeys;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	@Override
	public boolean equals( Object object ) {
		if( ! (object instanceof CanvasNode) )
			return false;
		return gNode == ((CanvasNode)object).getGraphicsNode();
	}
}
