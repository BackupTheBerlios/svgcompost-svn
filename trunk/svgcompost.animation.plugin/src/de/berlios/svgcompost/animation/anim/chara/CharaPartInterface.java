package de.berlios.svgcompost.animation.anim.chara;

import org.apache.batik.gvt.GraphicsNode;

import de.berlios.svgcompost.animation.anim.skeleton.Bone;
import de.berlios.svgcompost.animation.anim.skeleton.Skeleton;

public class CharaPartInterface extends Skeleton {

	public CharaPartInterface(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public Bone body;
	public Side left, right, front, rear, exposed, inner;
	protected Side[] sides;
	protected String[] sideNames;
	
	public Bone[] limbs;
	
	public Bone head, mouth, neck;

	public GraphicsNode mc;
	Skeleton charaParts;
	
	Bone leftArm;
	Bone rightArm;
	Bone leftLeg;
	Bone rightLeg;
	public Walk_old walk;
	public void snapToAnchors() {
		// TODO Auto-generated method stub
		
	}
	
	
}
