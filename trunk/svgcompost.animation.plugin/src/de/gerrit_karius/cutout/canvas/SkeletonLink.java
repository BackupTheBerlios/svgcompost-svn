package de.gerrit_karius.cutout.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.gerrit_karius.cutout.anim.skeleton.Bone;
import de.gerrit_karius.cutout.anim.skeleton.Skeleton;

/**
 * Links a frame CanvasNode to several Skeletons.
 * @author gerrit
 *
 */
public class SkeletonLink {

	private static Logger log = Logger.getLogger(SkeletonLink.class);


	private CanvasNode frameNode;
	
	protected HashMap<Bone,CanvasNode> nodesForBones = new HashMap<Bone,CanvasNode>();
	protected List<Skeleton> skeletons = new ArrayList<Skeleton>();
	
	public SkeletonLink( CanvasNode canvasNode ) {
		this.frameNode = canvasNode;
	}
	
	public void applySkeleton( Skeleton skeleton ) {
		searchForBones( skeleton, frameNode, nodesForBones );
		log.debug( "Found "+nodesForBones.size()+" bones of skeleton "+skeleton.getName()+" in keyframe "+frameNode.getName()+"." );
		skeletons.add( skeleton );
	}
	
	public CanvasNode getNodeForBone( Bone bone ) {
		return nodesForBones.get(bone);
	}
	
	public BoneLink getLinkForBone( Bone bone ) {
		CanvasNode node = getNodeForBone( bone );
		if( node == null )
			return null;
		return node.getBoneLink();
	}
	
	protected void searchForBones( Skeleton skeleton, CanvasNode node, HashMap<Bone,CanvasNode> nodesForBones ) {
		String nodeName = node.getName();

		if( skeleton.containsBone( nodeName ) ) {
			Bone bone = skeleton.getBone( nodeName );
			node.getBoneLink().setBone( bone );
			node.getBoneLink().setFrame( frameNode );
			nodesForBones.put(bone, node);
		}
		else
			log.debug( "couldn't find a bone named "+node.getName() );
		for( int i = 0; i < node.getSize(); i++ )
			searchForBones( skeleton, node.get(i), nodesForBones );
	}
	
	public static void setupTweening( List<CanvasNode> frames ) {
		for(CanvasNode frame : frames)
			for( Skeleton skeleton : frame.getSkeletonLink().skeletons ) {
				skeleton.calcKeyMatrices(frame.getSkeletonLink());			
				skeleton.calcLimbMatrices(frame.getSkeletonLink());			
			}
		for(int i=0; i<frames.size(); i++)
			for( Skeleton skeleton : frames.get(i).getSkeletonLink().skeletons ) {
				skeleton.setupTweening(frames, i);
				skeleton.setupLimbTweening(frames, i);
			}
	}
	
	public static void tween( CanvasNode tweeningKey, CanvasNode activeKey, double percentage ) {
		for( Skeleton skeleton : tweeningKey.getSkeletonLink().skeletons ) {
			skeleton.tween(tweeningKey.getSkeletonLink(), activeKey.getSkeletonLink(), percentage);
			skeleton.tweenLimbs(tweeningKey.getSkeletonLink(), activeKey.getSkeletonLink(), percentage);
		}
	}

}
