package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.ArrayList;
import java.util.HashMap;

import de.berlios.svgcompost.animation.timeline.Keyframe;

/**
 * A Skeleton is the root node for a composite Bone tree.
 * It serves as an entry point for the load functions for models and matrices.
 * The root also stores all parameters that are needed only once for the entire structure tree.
 * The mc of the root is not intended to align with other mcs. 
 * @author User
 *
 */
public class Skeleton extends Bone {

	private HashMap<String,Bone> bones = new HashMap<String,Bone>();
	protected ArrayList<Limb> connectors = new ArrayList<Limb>();
	
	public Skeleton( String name ) {
		super( name );
		skeleton = this;
	}
	
//	public void addConnector( String parent, String child, String target ) {
//		addConnector( new JointedLimb( getBone(parent), getBone(child), getBone(target), this ) );
//	}
	
	public void addConnector( Limb connector ) {
		if( connectors == null )
			connectors = new ArrayList<Limb>();
		connector.setSkeleton(this);
		connectors.add( connector );
	}
	
	public Limb getConnector( int i ) {
		if( connectors == null )
			return null;
		return connectors.get( i );
	}
	
	public int connectorSize() {
		if( connectors == null )
			return 0;
		return connectors.size();
	}
	
	public boolean containsBone( String name ) {
		return bones.keySet().contains( name );
	}
	
	public Bone getBone( String forName ) {
		return bones.get( forName );
	}
	
	public void registerBone( String forName, Bone bone ) {
		bones.put( forName, bone );
	}

	public void setupLimbTweening( Keyframe keyframe ) {
		for(Limb jointedLimb : connectors)
			jointedLimb.readRotationPoint(keyframe.getSkeletonKey(this));
	}
	
	public void tweenLimbs( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {
		for(Limb jointedLimb : connectors)
			jointedLimb.tween(tweeningKeyLink, activeKeyLink, percentage);
	}

	public void setupTweening( Keyframe keyframe ) {
		SkeletonKey skeletonKey = keyframe.getSkeletonKey(skeleton);
		setupTweening(skeletonKey);
	}

	
}
