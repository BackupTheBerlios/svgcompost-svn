package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.HashMap;

import org.apache.batik.bridge.BridgeContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class SkeletonFactory {

	private static Logger log = Logger.getLogger(SkeletonFactory.class);
	
	private static HashMap<Bone,CanvasNode> bones2nodes = new HashMap<Bone,CanvasNode>();
	
	/**
	 * Creates child bones for a newly created parent bone.
	 * @param node
	 * @param parent
	 */
	protected static void createChildBones( CanvasNode node, Bone parent ) {
		bones2nodes.put(parent, node);
		String mcName = node.getName();
		if( log.isTraceEnabled() ) {
			log.trace( "create bone with name: "+mcName );
			log.trace( "# of children: "+node.getSize() );
		}
		for (int i = 0; i < node.getSize(); i++) {
			CanvasNode mcChild = node.get( i );
			String childName = mcChild.getName();
			if( log.isTraceEnabled() )
				log.trace( "create child bone with name: "+mcName );
			if( childName != null && ! childName.equals( "" ) && ! childName.equals( mcName ) ) {
				Bone child = new Bone( childName );
				parent.add( child );
				createChildBones( mcChild, child );
			}
		}
	}

	/**
	 * Processes a bone a second time, after all bones of a skeleton have been created.
	 * @param node
	 * @param bone
	 */
	public static void processBone(CanvasNode node, Bone bone) {
		BridgeContext ctx = node.getCanvas().getSourceCtx();
		Element el = ctx.getElement( node.getGraphicsNode() );
		if( el.hasAttribute("connectWith") && el.hasAttribute("connectTo") ) {
			String lowerLimb = el.getAttribute("connectWith");
			String limbTarget = el.getAttribute("connectTo");
			log.debug("add connector to "+node.getName());
			bone.skeleton.addConnector( node.getName(), lowerLimb, limbTarget );
		}
	}

	/**
	 * Constructs a ClusterStructure tree with a ClusterRoot as root node from the model mc.
	 * The new tree reflects the parent-child structures of the mc's descendants. 
	 * @param node The root node of the skeleton tree.
	 * @return The newly created model root.
	 */
	public static Skeleton createSkeleton( CanvasNode node ) {
		if( node == null ) {
			log.error( "Root for new skeleton is null." );
			return null;
		}
		
		Skeleton root = new Skeleton( node.getName() );
		root.registerBone(node.getName(), root);
	
		createChildBones( node, root );
		
		for(Bone bone : bones2nodes.keySet()) {
			processBone( bones2nodes.get(bone), bone );
		}
		
		return root;
	}

}
