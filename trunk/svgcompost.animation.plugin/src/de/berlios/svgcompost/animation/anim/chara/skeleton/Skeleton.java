package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * A Skeleton is the root node for a composite Bone tree.
 * It serves as an entry point for the load functions for models and matrices.
 * The root also stores all parameters that are needed only once for the entire structure tree.
 * The mc of the root is not intended to align with other mcs. 
 * @author User
 *
 */
public class Skeleton extends Bone {

//	public int activeKey;
//	public int currentTweening;
//	public double currentPercentage;
	private HashMap<String,Bone> bones = new HashMap<String,Bone>();
	protected ArrayList<Limb> connectors;
//	protected ArrayList<CanvasNode> rootMc;
	
	public Skeleton( String name ) {
		super( name );
		skeleton = this;
//		rootMc = new ArrayList<CanvasNode>();
	}
	
	public void addConnector( String parent, String child, String target ) {
		addConnector( new Limb( getBone(parent), getBone(child), getBone(target), this ) );
	}
	
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

	public void setupLimbTweening( List<CanvasNode> frames ) {
		for(int i=0; i<frames.size(); i++) {
			for(Limb limb : connectors)
				limb.readRotationPoint(frames.get(i).getSkeletonKey(this));
		}
	}
	
	public void tweenLimbs( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {
		for(Limb limb : connectors)
			limb.tween(tweeningKeyLink, activeKeyLink, percentage);
	}
	
}
