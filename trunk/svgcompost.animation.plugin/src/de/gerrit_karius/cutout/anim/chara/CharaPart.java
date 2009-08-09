package de.gerrit_karius.cutout.anim.chara;

import de.gerrit_karius.cutout.anim.skeleton.Bone;
import de.gerrit_karius.cutout.anim.skeleton.Skeleton;

public class CharaPart {

	protected String rootName;	
	protected String topName;
	protected Bone root;	
	protected Bone top;
	
	public CharaPart top( String topName ) {
		this.topName = topName;
		return this;
	}
	public CharaPart root( String rootName ) {
		this.rootName = rootName;
		return this;
	}
	public void getFromStruct( Skeleton structRoot ) {
		root = structRoot.getBone( rootName );
		top = structRoot.getBone( topName );
	}
	public Bone getRoot() {
		return root;
	}
	public Bone getTop() {
		return top;
	}
}
