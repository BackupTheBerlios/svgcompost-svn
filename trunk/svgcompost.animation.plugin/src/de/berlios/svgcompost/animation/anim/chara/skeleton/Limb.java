package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.util.CatmullRomSpline;
import de.berlios.svgcompost.animation.util.Polar;

/**
 * Represents a body part which may not influence its children,
 * e.g. a leg where the foot is fixed firmly on the ground.
 * The original position of the leg is therefore discarded during tweenings.
 * Instead, the position is calculated so that it fits to the connection points
 * on the body and the foot set in the keyframes. 
 * @author gerrit
 *
 */
public class Limb {
	
	private static Logger log = Logger.getLogger(Limb.class);

	protected Bone mParent;
	protected Bone mChild;
	protected Bone mTarget;
	protected Bone mSystem;
	
	protected Skeleton skeleton;
	
	/**
	 * The rotation point on the target. With an arm, this would be
	 * the wrist, where the lower arm is jointed to the hand.
	 * This is not usually not the center of the target, and unless specified,
	 * it must be calculated dynamically.
	 */
	/*
	Point2D.Float[] rotPointOnTarget;
	Point2D.Float[] rotPointOnChild;
	Point2D.Float[] rotPointOnParent;
	
	int numberOfKeys;
	int numberOfTweenings;
	*/

	public void calcKeyMatrices( SkeletonKey keyframeLink ) {
		CanvasNode parent = keyframeLink.getNodeForBone( mParent );
		CanvasNode child = keyframeLink.getNodeForBone( mChild );
		CanvasNode target = keyframeLink.getNodeForBone( mTarget );
			
		AffineTransform targetToChild = target.getLocalToLocal( child );
		
//		child.getBoneKey().setLimbKeyMatrix(targetToChild);
		keyframeLink.getLimbKey(this).setLimbKeyMatrix(targetToChild);
	}
			
	public void setupTweening( List<CanvasNode> frames, int key ) {
		
		// TODO: move loop over frames from SkeletonLink to this method.
		// Then use rotPointOnXXX for first and last key,
		// for all others, use a 50% linear tween between rotPointOnXXX-1 and rotPointOnXXX+1.
		if( key < 0 || key >= frames.size() || frames.size() == 1 )
			return;
		SkeletonKey keyframeLink = frames.get(key).getSkeletonKey(skeleton);
		LimbKey limbKey = keyframeLink.getLimbKey(this);
		
		CanvasNode child = keyframeLink.getNodeForBone(mChild);
		CanvasNode parent = keyframeLink.getNodeForBone(mParent);
		
		AffineTransform targetToChild = limbKey.getLimbKeyMatrix();
		
		Point2D.Float rotPointOnParent = child.projectCenterToLocal( parent );
		
		SkeletonKey nextKeyframeLink = frames.get(key==frames.size()-1?key-1:key+1).getSkeletonKey(skeleton);
		AffineTransform nextTargetToChild = nextKeyframeLink.getLimbKey(this).getLimbKeyMatrix();

		Point2D.Float rotPointOnTarget = findRotationPoint( targetToChild, nextTargetToChild );
		Point2D.Float rotPointOnChild = new Point2D.Float();
		targetToChild.transform( rotPointOnTarget, rotPointOnChild );
		if( rotPointOnTarget == null ) {
			log.warn( "rotPointOnTarget is null: "+key );
		}
		if( rotPointOnChild == null ) {
			log.warn( "rotPointOnChild is null: "+key );
		}
		
		limbKey.setLimbPoint(new Point2D.Float[]{rotPointOnTarget, rotPointOnChild, rotPointOnParent});
	}
	
	/**
	 * 
	 * @param tweeningKeyLink The first of the two keys that are tweened.
	 * @param activeKeyLink The key (first or second) that is changed to create the tweens.
	 * @param percentage The percentage of tweening.
	 */
	public void tween( SkeletonKey tweeningKeyLink, SkeletonKey activeKeyLink, double percentage ) {
		log.debug("tween: "+percentage);
//		if(0==0)
//			return;
		
		// TODO: optimize, once it works
		
		// Keys with wrong positions. Only parent position is correct.
		CanvasNode parent = activeKeyLink.getNodeForBone(mParent);
		CanvasNode child = activeKeyLink.getNodeForBone(mChild);
		CanvasNode target = activeKeyLink.getNodeForBone(mTarget);
		CanvasNode system = activeKeyLink.getNodeForBone(mSystem);
		
		// TODO: test: target should also be mapped onto system coord space
		Point2D.Float shoulderPoint = parent.projectCenterToLocal( system );
		Point2D.Float elbowPoint = child.projectCenterToLocal( system );
		
		// should be tweeningKey?
		BoneKey tweeningChild = tweeningKeyLink.getBoneKey(mChild);
//		Point2D.Float rotPointOnTarget = tweeningChild.getLimbPoint()[0];
//		Point2D.Float rotPointOnChild = tweeningChild.getLimbPoint()[1];
//		Point2D.Float rotPointOnParent = tweeningChild.getLimbPoint()[2];
		Point2D.Float rotPointOnTarget = cmrTweenTargetPoint(tweeningChild, 0, percentage);
		Point2D.Float rotPointOnChild = cmrTweenTargetPoint(tweeningChild, 1, percentage);
		Point2D.Float rotPointOnParent = cmrTweenTargetPoint(tweeningChild, 2, percentage);
		
		Point2D.Float targetPoint = target.projectPointToLocal( rotPointOnTarget, system );
		if( rotPointOnChild == null ) {
			log.warn( "rotPointOnChild is null: "+child.getName() );
		}
		Point2D.Float handPoint = child.projectPointToLocal( rotPointOnChild, system );
		
		Polar shoulderElbow = Polar.fromCartesianDiff( shoulderPoint, elbowPoint );
		Polar elbowHand = Polar.fromCartesianDiff( elbowPoint, handPoint );
		Polar shoulderTarget = Polar.fromCartesianDiff( shoulderPoint, targetPoint );
		
		boolean bendsClockwise = true;
		
		// upper arm (a)
		float a = shoulderElbow.r;
		// lower arm (b)
		float b = elbowHand.r;
		// straight line from start to target (c)
		float c = shoulderTarget.r;
		// squares
		float shoulderElbow2 = a*a;
		float elbowHand2 = b*b;
		float shoulderTarget2 = c*c;
		// shoulder angle (beta)
		log.info("a = "+a); 
		log.info("b = "+b); 
		log.info("c = "+c); 
		log.info("elbowHand2 = "+elbowHand2); 
		log.info("shoulderElbow2 = "+shoulderElbow2); 
		log.info("shoulderTarget2 = "+shoulderTarget2);
		float acos = (elbowHand2 - shoulderElbow2 - shoulderTarget2) / (-2 * a * c);
		if( acos < -1 )
			acos = -1;
		if( acos > 1 )
			acos = 1;
		float shoulderAngle = (float) Math.acos( acos );
		// elbow angle (gamma)
//		float elbowAngle = (float) Math.acos( (shoulderTarget2 - shoulderElbow2 - elbowHand2) / (-2 * a * b) );
//		// hand angle (alpha)
//		float handAngle = (float) Math.acos( (shoulderElbow2 - elbowHand2 - shoulderTarget2) / (-2 * b * c) );
		if( bendsClockwise ) {
			shoulderAngle *= -1;
//			elbowAngle *= -1;
		}
		
		log.debug("shoulderElbow = "+shoulderElbow);
		log.debug("shoulderTarget = "+shoulderTarget);
		log.debug("shoulderAngle = "+shoulderAngle);
		Point2D.Float newElbow = Polar.toCartesian( shoulderElbow.r, shoulderTarget.a + shoulderAngle );
		log.debug("newElbow = "+newElbow);
		newElbow.x += shoulderPoint.x;
		newElbow.y += shoulderPoint.y;
		Point2D.Float newElbowForShoulder = system.projectPointToLocal( newElbow, parent.getParent() );
		Point2D.Float newElbowForChild = system.projectPointToLocal( newElbow, child.getParent() );
		Point2D.Float targetForChild = system.projectPointToLocal( targetPoint, child.getParent() );
		
		log.debug("newElbowForShoulder = "+newElbowForShoulder);
		log.debug("targetForChild = "+targetForChild);
		
		Point2D.Float childOnParent = child.getLocalXY( parent );
		alignWithPoint( parent, rotPointOnParent, newElbowForShoulder );
		child.setLocalXY( childOnParent, parent );
		
//		child.setXY( newElbowForChild );
		alignWithPoint( child, rotPointOnChild, targetForChild );
				
	}

	protected Point2D.Float linearTweenTargetPoint( BoneKey currentKey, int targetIndex, double percentage ) {
		return null;
	}
	
	/**
	 * Catmull-Rom tweens the target point with the given index.
	 * @param currentKey
	 * @param targetIndex
	 * @param percentage
	 * @return
	 */
	protected Point2D.Float cmrTweenTargetPoint( BoneKey currentKey, int targetIndex, double percentage ) {
		BoneKey prevKey = currentKey.previousKey();// getRelativeKey(-1);
		BoneKey nextKey = currentKey.nextKey();//getRelativeKey(+1);
		BoneKey afterKey = nextKey==null?null:nextKey.nextKey();//currentKey.getRelativeKey(+2);
//		log.debug("afterKey = "+afterKey);
//		log.debug("afterKey.getLimbPoint() = "+afterKey==null?null:afterKey.getLimbPoint());
		Point2D.Float rotPointTween = CatmullRomSpline.tween( percentage,
				prevKey==null?currentKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex]:prevKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex],
				currentKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex],
//				afterKey==null?currentKey.getLimbPoint()[targetIndex]:
					nextKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex],
//				afterKey==null||
//				afterKey.getRelativeKey(+1)==null?
//						currentKey.getLimbPoint()[targetIndex]:
//							afterKey.getLimbPoint()[targetIndex]
					afterKey==null?nextKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex]:afterKey.getSkeletonKey().getLimbKey(this).getLimbPoint()[targetIndex]
		);
		return rotPointTween;
	}

	
	/**
	 * @param parent The parent part of the connector, e.g. the upper leg.
	 * @param child The child part of the connector, e.g. the lower leg.
	 * @param target The part that the connector connects to, e.g. a foot.
	 * @param system The part whose coordinate system is used for the calculations, usually the skeleton root. 
	 */
	public Limb(Bone parent, Bone child, Bone target, Bone system) {
		mChild = child;
		mParent = parent;
		mTarget = target;
		mSystem = system;
	}

	/**
	 * For 2 different transforms, finds the source point that is transformed to
	 * the same target point in both transforms.
	 * @param a
	 * @param b
	 * @return The rotation point.
	 */
	protected static Point2D.Float findRotationPoint( AffineTransform a, AffineTransform b ) {
		double sx = a.getScaleX() - b.getScaleX();
		double rx = a.getShearX() - b.getShearX(); // skew x by y
		double tx = a.getTranslateX() - b.getTranslateX();
		
		double sy = a.getScaleY() - b.getScaleY();
		double ry = a.getShearY() - b.getShearY(); // skew y by x
		double ty = a.getTranslateY() - b.getTranslateY();
		
		double denom = (rx*ry - sx*sy);
		
		if( denom == 0 )
			return null;
		
		double x = (sy*tx - rx*ty) / denom;
		double y = (sx*ty - ry*tx) / denom;
		
		return new Point2D.Float( (float) x, (float) y );
	}
	

	/**
	 * Applies a transformation to the node so that the align point in the node's
	 * own coordinate space aligns with the align-with point on the node's parent's
	 * coordinate space.
	 * @param node
	 * @param alignPoint
	 * @param alignWith
	 */
	public static void alignWithPoint(CanvasNode node, Point2D.Float alignPoint, Point2D.Float alignWith) {
		log.debug("alignWith = "+alignWith);
		Point2D.Float center = node.projectCenterToLocal( node.getParent() );
		Point2D.Float alignPointOnParent = node.projectPointToLocal( alignPoint, node.getParent() );
		Polar alignPointPolar = Polar.fromCartesianDiff( center, alignPointOnParent );
		log.debug("alignPointPolar = "+alignPointPolar);
		Polar alignWithPolar = Polar.fromCartesianDiff( center, alignWith );
		log.debug("alignWithPolar = "+alignWithPolar);
		float angle = alignWithPolar.a - alignPointPolar.a;
		log.debug("angle = "+angle);
		float x = node.getX();
		float y = node.getY();
		AffineTransform trafo = AffineTransform.getTranslateInstance( -x, -y );
		trafo.preConcatenate( AffineTransform.getRotateInstance( angle ) );
		trafo.preConcatenate( AffineTransform.getTranslateInstance( x, y ) );
		trafo.concatenate( node.getTransform() );
		node.setTransform( trafo );
	}

	public Bone getTarget() {
		return mTarget;
	}

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}
	
	/**
	 * Adds a rotation at system level, so that the rotation of the target will
	 * equal the target rotation.
	 * @param target
	 * @param targetRot
	 * @param baseRot
	 * @param system
	 */
	/*
	protected static void addRotation( CanvasNode target, float targetRot, float baseRot, CanvasNode system ) {
		
		AffineTransform toSystem = target.getLocalToLocal( system );
		double tx = toSystem.getTranslateX();
		double ty = toSystem.getTranslateY();
		toSystem.preConcatenate( AffineTransform.getRotateInstance( targetRot - baseRot ) );
		double matrix[] = new double[6];
		toSystem.getMatrix( matrix );
		matrix[4] = tx;
		matrix[5] = ty;
		AffineTransform global = new AffineTransform( matrix );
		global.preConcatenate( system.getGlobalTransform() );
		target.setGlobalTransform( global );
	}
	*/

	/*
	public void setupTweening() {
		numberOfKeys = mTarget.getNumberOfKeys();
		numberOfTweenings = numberOfKeys -1;
		
		rotPointOnChild = new Point2D.Float[numberOfTweenings]; 
		rotPointOnTarget = new Point2D.Float[numberOfTweenings]; 
		
		rotPointOnParent = new Point2D.Float[numberOfKeys]; 
		
		AffineTransform[] targetToChild = new AffineTransform[numberOfKeys];
		AffineTransform[] childToTarget = new AffineTransform[numberOfKeys];
		
		for( int i=0; i<numberOfKeys; i++ ) {
			CanvasNode parent = mParent.getKey( i );
			CanvasNode child = mChild.getKey( i );
			CanvasNode target = mTarget.getKey( i );
			
			targetToChild[i] = target.getLocalToLocal( child );
			childToTarget[i] = child.getLocalToLocal( target );
			
			// TODO: calc the dynamic target joint point
			// and set up tweeners (linear?) for the target point
			
			rotPointOnParent[i] = child.projectCenterToLocal( parent );
		}
		
		for( int i=0; i<numberOfTweenings; i++ ) {
			// would it make more sense to use childToTarget?
			rotPointOnTarget[i] = findRotationPoint( targetToChild[i], targetToChild[i+1] );
			rotPointOnChild[i] = new Point2D.Float();
			targetToChild[i].transform( rotPointOnTarget[i], rotPointOnChild[i] );
			if( rotPointOnTarget[i] == null ) {
				System.out.println( "rotPointOnTarget is null: "+i );
			}
			if( rotPointOnChild[i] == null ) {
				System.out.println( "rotPointOnChild is null: "+i );
			}
		}
	}
	*/
	
	/**
	 * Connects the specified limb composed of 2 jointed links to the specified target.
	 * The upper limb is only rotated. The lower limb is moved to retain its place on the
	 * upper limb and is also rotated. The previously calculated point on the lower limb comes
	 * to sit on the previously calculated point on the target.
	 * @param tweening
	 * @param percentage
	 * @param key
	 */
	/*
	public void connectTo( int tweening, double percentage, int key ) {
//		if(0==0)
//			return;
		
		// TODO: optimize, once it works
		
		// Keys with wrong positions. Only parent position is correct.
		CanvasNode parent = mParent.getKey(key);
		CanvasNode child = mChild.getKey(key);
		CanvasNode target = mTarget.getKey(key);
		CanvasNode system = mSystem.getKey(key);
		
		// TODO: test: target should also be mapped onto system coord space
		Point2D.Float shoulderPoint = parent.projectCenterToLocal( system );
		Point2D.Float elbowPoint = child.projectCenterToLocal( system );
		
		Point2D.Float targetPoint = target.projectPointToLocal( rotPointOnTarget[tweening], system );
		if( rotPointOnChild[tweening] == null ) {
			System.out.println( "rotPointOnChild is null: "+tweening );
		}
		Point2D.Float handPoint = child.projectPointToLocal( rotPointOnChild[tweening], system );
		
		Polar shoulderElbow = Polar.fromCartesianDiff( shoulderPoint, elbowPoint );
		Polar elbowHand = Polar.fromCartesianDiff( elbowPoint, handPoint );
		Polar shoulderTarget = Polar.fromCartesianDiff( shoulderPoint, targetPoint );
		
		boolean bendsClockwise = true;
		
		// upper arm (a)
		float a = shoulderElbow.r;
		// lower arm (b)
		float b = elbowHand.r;
		// straight line from start to target (c)
		float c = shoulderTarget.r;
		// squares
		float shoulderElbow2 = a*a;
		float elbowHand2 = b*b;
		float shoulderTarget2 = c*c;
		// shoulder angle (beta)
		float shoulderAngle = (float) Math.acos( (elbowHand2 - shoulderElbow2 - shoulderTarget2) / (-2 * a * c) );
		// elbow angle (gamma)
//		float elbowAngle = (float) Math.acos( (shoulderTarget2 - shoulderElbow2 - elbowHand2) / (-2 * a * b) );
//		// hand angle (alpha)
//		float handAngle = (float) Math.acos( (shoulderElbow2 - elbowHand2 - shoulderTarget2) / (-2 * b * c) );
		if( bendsClockwise ) {
			shoulderAngle *= -1;
//			elbowAngle *= -1;
		}
		
		Point2D.Float newElbow = Polar.toCartesian( shoulderElbow.r, shoulderTarget.a + shoulderAngle );
		newElbow.x += shoulderPoint.x;
		newElbow.y += shoulderPoint.y;
		Point2D.Float newElbowForShoulder = system.projectPointToLocal( newElbow, parent.getParent() );
		Point2D.Float newElbowForChild = system.projectPointToLocal( newElbow, child.getParent() );
		Point2D.Float targetForChild = system.projectPointToLocal( targetPoint, child.getParent() );
		
		Point2D.Float childOnParent = child.getLocalXY( parent );
		alignWithPoint( parent, rotPointOnParent[key], newElbowForShoulder );
		child.setLocalXY( childOnParent, parent );
		
//		child.setXY( newElbowForChild );
		alignWithPoint( child, rotPointOnChild[key], targetForChild );
		
		
//		float pi = (float)Math.PI;
//		float upperRotation = pi + shoulderAngle;
//		float lowerRotation = pi - elbowAngle;
//
//		float _0_5_pi = pi * 0.5f;
//		float _1_5_pi = pi * 1.5f;
//		
//		parent.setTransform( AffineTransform.getRotateInstance( upperRotation ) );
//		child.setTransform( AffineTransform.getRotateInstance( lowerRotation ) );
//		
//		Main.circle.setXY( joint );
		
//		parent.setTransform( new AffineTransform() );
//		child.setTransform( new AffineTransform() );
		
//		addRotation( parent, _0_5_pi, startToJoint.a, system );
//		addRotation( child, _0_5_pi, 0, system );
		
//		addRotation( parent, upperRotation, startToJoint.a, system );
//		addRotation( child, lowerRotation, jointToTarget.a, parent );
		
	}
	*/
}
