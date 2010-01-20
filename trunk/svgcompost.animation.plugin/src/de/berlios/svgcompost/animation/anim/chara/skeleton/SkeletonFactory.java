package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.HashMap;

import org.apache.batik.bridge.BridgeContext;
import org.w3c.dom.Element;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.util.xml.Attributes;
import de.berlios.svgcompost.animation.util.xml.Labels;

public class SkeletonFactory {

	private HashMap<Bone,CanvasNode> bones2nodes = new HashMap<Bone,CanvasNode>();
	
	/**
	 * Creates child bones for a newly created parent jointedLimb.
	 * @param node
	 * @param parent
	 */
	protected void createChildBones( CanvasNode node, Bone parent ) {
		bones2nodes.put(parent, node);
		String mcName = node.getName();
		for (int i = 0; i < node.getSize(); i++) {
			CanvasNode mcChild = node.get( i );
			String childName = mcChild.getName();
			if( childName != null
					&& ! childName.equals( "" )
					&& ! childName.equals( mcName )
					&& ! childName.equals(Labels.ANCHOR1)
					&& ! childName.equals(Labels.ANCHOR2)
					&& ! childName.equals(Labels.ANCHOR3)
					&& ! childName.equals(Labels.ANCHOR4)
					&& ! childName.equals(Labels.ANCHOR5)
					) {
				Bone child = new Bone( childName );
				parent.add( child );
				createChildBones( mcChild, child );
			}
		}
	}

	/**
	 * Processes a jointedLimb a second time, after all bones of a skeleton have been created.
	 * @param node
	 * @param jointedLimb
	 */
	public void processBone(CanvasNode node, Bone bone) {
		// TODO: Use svgcompost namespace for attributes.
		BridgeContext ctx = node.getCanvas().getSourceCtx();
		Element el = ctx.getElement( node.getGraphicsNode() );
		if( el.hasAttribute(Attributes.CONNECT_WITH) && el.hasAttribute(Attributes.CONNECT_TO) ) {
			String lowerLimb = el.getAttribute(Attributes.CONNECT_WITH);
			String limbTarget = el.getAttribute(Attributes.CONNECT_TO);
			Skeleton skeleton = bone.skeleton;
			skeleton.addConnector( new JointedLimb(bone, skeleton.getBone(lowerLimb), skeleton.getBone(limbTarget), skeleton) );
//			bone.skeleton.addConnector( node.getName(), lowerLimb, limbTarget );
		}
		else if( el.hasAttribute(Attributes.SQUASH_TO) ) {
			String limbTarget = el.getAttribute(Attributes.SQUASH_TO);
			Skeleton skeleton = bone.skeleton;
			skeleton.addConnector( new SquashyLimb(bone, skeleton.getBone(limbTarget)) );
//			bone.skeleton.addConnector( node.getName(), lowerLimb, limbTarget );
		}
	}

	/**
	 * Constructs a ClusterStructure tree with a ClusterRoot as root node from the model mc.
	 * The new tree reflects the parent-child structures of the mc's descendants. 
	 * @param node The root node of the skeleton tree.
	 * @return The newly created model root.
	 */
	public Skeleton createSkeleton( CanvasNode node ) {
		if( node == null ) {
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
