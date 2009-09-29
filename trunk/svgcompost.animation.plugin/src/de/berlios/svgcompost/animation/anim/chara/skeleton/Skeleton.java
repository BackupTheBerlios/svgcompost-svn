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

	public void calcLimbMatrices( SkeletonKey keyframeLink ) {
		for(Limb limb : connectors)
			limb.calcKeyMatrices(keyframeLink);
	}
	
	public void setupLimbTweening( List<CanvasNode> frames, int key ) {
		for(Limb limb : connectors)
			limb.setupTweening(frames.get(key).getSkeletonKey(this));
	}
	
	public void tweenLimbs( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {
		for(Limb limb : connectors)
			limb.tween(tweeningKeyLink, activeKeyLink, percentage);
	}
	
	/*
	public void addRootKey( CanvasNode rootKey ) {
		log.debug("Adding root key '"+rootKey.getName()+"' to model '"+name+"'.");
		rootMc.add( rootKey );
	}
	*/
	
	/*
	public void discardKeys() {
		log.debug("delete keys for: "+name);
		rootMc = new ArrayList<CanvasNode>();
		numberOfKeys = 0;
		setArrayLengths();
	}
	*/
	
	/**
	 * Loads the data for 2 animation keys from 2 mcs into the structure tree.
	 * The keys can then be tweened with the tween() function.
	 * Each mc on the key structure must correspond to a structure in the tree,
	 * otherwise it will not be tweened.
	 */
	/*
	public void setup() {
		
		setupWithoutConnectors();
		setupConnectors();
	}
	*/
	
	/*
	public void setupWithoutConnectors() {
		
		numberOfKeys = rootMc.size();
		log.debug( "numberOfKeys: "+numberOfKeys );
		setArrayLengths();
		
		for (int i = 0; i < numberOfKeys; i++) {
			keyMc[i] = rootMc.get(i);
			if( keyMc[i] == null ) {
				log.warn("Key "+i+" is null for model "+name);
				return;
			}
		}
		
		log.debug("Search for bones...");

		for (int i = 0; i < numberOfKeys; i++)
			searchForBones( rootMc.get(i), i );
		
		log.debug("Searched for bones.");

		setupTweening();
	}
	*/
	
	/*
	protected void setupConnectors() {
		for (int i = 0; i < connectorSize(); i++) {
			getConnector(i).setupTweening();
		}
	}
	*/

	/*
	protected void searchForBones( CanvasNode cNode, int key ) {
		String nodeName = cNode.getName();
		if( bones.keySet().contains( nodeName ) ) {
			Bone bone = bones.get( nodeName );
			bone.keyMc[key] = cNode;
		}
		else
			log.debug( "couldn't find bone "+cNode.getName() );
		for( int i = 0; i < cNode.getSize(); i++ )
			searchForBones( cNode.get(i), key );
	}
	*/
	
	/*
	public void setActiveKey( int key ) {
		activeKey = key;
	}
	*/
	
	/*
	public void setCurrentTweeningPair( int tweening ) {
		currentTweening = tweening;
	}
	*/
	
	/*
	public void displayTween( double percentage ) {
		currentPercentage = percentage;
		tweenStructure();
		for (int i = 0; i < connectorSize(); i++) {
			Limb connector = getConnector(i);
			connector.connectTo( currentTweening, currentPercentage, activeKey );
		}
	}
	*/

	/*
	public void registerBone_old(String name, Bone_old child) {
		// TODO Auto-generated method stub
	}
	*/
	
}
