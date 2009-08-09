package de.gerrit_karius.cutout.anim.skeleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.gerrit_karius.cutout.anim.chara.JointedConnection;
import de.gerrit_karius.cutout.canvas.CanvasNode;
import de.gerrit_karius.cutout.canvas.SkeletonLink;

/**
 * A Skeleton is the root node for a composite Bone tree.
 * It serves as an entry point for the load functions for models and matrices.
 * The root also stores all parameters that are needed only once for the entire structure tree.
 * The mc of the root is not intended to align with other mcs. 
 * @author User
 *
 */
public class Skeleton extends Bone {

	private static Logger log = Logger.getLogger(Skeleton.class);

//	public int activeKey;
//	public int currentTweening;
//	public double currentPercentage;
	private HashMap<String,Bone> bones = new HashMap<String,Bone>();
	protected ArrayList<JointedConnection> connectors;
//	protected ArrayList<CanvasNode> rootMc;
	
	public Skeleton( String name ) {
		super( name );
		root = this;
//		rootMc = new ArrayList<CanvasNode>();
	}
	
	public void addConnector( String parent, String child, String target ) {
		addConnector( new JointedConnection( getBone(parent), getBone(child), getBone(target), this ) );
	}
	
	public void addConnector( JointedConnection connector ) {
		if( connectors == null )
			connectors = new ArrayList<JointedConnection>();
		connectors.add( connector );
	}
	
	public JointedConnection getConnector( int i ) {
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

	/**
	 * Constructs a ClusterStructure tree with a ClusterRoot as root node from the model mc.
	 * The new tree reflects the parent-child structures of the mc's descendants. 
	 * @param mc
	 * @return The newly created model root.
	 */
	public static Skeleton createModelRoot( CanvasNode mc ) {
		if( mc == null ) {
			log.error( "Root for new skeleton is null." );
			return null;
		}
		Skeleton root = new Skeleton( mc.getName() );
		root.registerBone(mc.getName(), root);

		Bone.createBone( mc, root );
		
		return root;
	}
	
	public void calcLimbMatrices( SkeletonLink keyframeLink ) {
		for(JointedConnection limb : connectors)
			limb.calcKeyMatrices(keyframeLink);
	}
	
	public void setupLimbTweening( List<CanvasNode> frames, int key ) {
		for(JointedConnection limb : connectors)
			limb.setupTweening(frames, key);
	}
	
	public void tweenLimbs( SkeletonLink tweeningKeyLink, SkeletonLink activeKeyLink, double percentage ) {
		for(JointedConnection limb : connectors)
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
			JointedConnection connector = getConnector(i);
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
