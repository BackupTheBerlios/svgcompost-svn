package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.HashMap;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.w3c.dom.Element;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.util.xml.Attributes;
import de.berlios.svgcompost.animation.util.xml.Labels;
import de.berlios.svgcompost.animation.util.xml.SVGCompostElements;
import de.berlios.svgcompost.util.ElementTraversalHelper;

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

	public Skeleton createSkeleton(Element skeletonElement, String prefix, boolean makeCamelCase) {
		if( ! skeletonElement.hasAttributeNS(null, SVGCompostElements.NAME) )
			return null;
		Skeleton skeleton = new Skeleton( skeletonElement.getAttributeNS(null, SVGCompostElements.NAME) );
		skeleton.registerBone(skeleton.getName(), skeleton);
		processSkeleton( skeletonElement, skeleton, prefix, makeCamelCase );
		return skeleton;
	}

	public void processSkeleton(Element parentElement, Bone parentBone, String prefix, boolean makeCamelCase) {
		List<Element> children = ElementTraversalHelper.getChildElements(parentElement);
		for (Element element : children) {
			String elementname = element.getLocalName();
			if( elementname.equals(SVGCompostElements.BONE) ) {
				String name = element.getAttributeNS(null, SVGCompostElements.NAME);
				Bone bone = new Bone(name);
				parentBone.add(bone);
				Skeleton skeleton = parentBone.getSkeleton();
				skeleton.registerBone(prefix+(makeCamelCase?name.substring(0,1).toUpperCase()+name.substring(1):name), bone);
				processSkeleton(element, bone, prefix, makeCamelCase);
			}
			else if( elementname.equals(SVGCompostElements.JOINTLIMB) ) {
				String upperLimb = element.getAttributeNS(null, SVGCompostElements.UPPERLIMB);
				String lowerLimb = element.getAttributeNS(null, SVGCompostElements.LOWERLIMB);
				String appendage = element.getAttributeNS(null, SVGCompostElements.APPENDAGE);
				Skeleton skeleton = parentBone.getSkeleton();
				Limb limb = new JointedLimb(skeleton.getBone(upperLimb), skeleton.getBone(lowerLimb), skeleton.getBone(appendage), skeleton);
				skeleton.addConnector(limb);
			}
			else if( elementname.equals(SVGCompostElements.SQUASHYLIMB) ) {
				String limbBone = element.getAttributeNS(null, SVGCompostElements.LIMB);
				String appendage = element.getAttributeNS(null, SVGCompostElements.APPENDAGE);
				Skeleton skeleton = parentBone.getSkeleton();
				Limb limb = new SquashyLimb(skeleton.getBone(limbBone), skeleton.getBone(appendage));
				skeleton.addConnector(limb);
			}
		}
	}

}
