package de.gerrit_karius.cutout.anim.skeleton;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import de.gerrit_karius.cutout.canvas.CanvasNode;
import de.gerrit_karius.cutout.canvas.Library;

public class WalkAnimFactory {

	
	Library library;
	CanvasNode stage;
	
	public void createWalkAnim( String posesId, String modelName, Point2D.Float start, Point2D.Float end ) {
		Skeleton model = library.getModel(modelName);
		ArrayList<Bone> feet = new ArrayList<Bone>();
		for( int i=0; i<model.connectorSize(); i++ ) {
			feet.add( model.getConnector(i).getTarget() );
		}
		CanvasNode poses = stage.addSymbolInstance(posesId, posesId);
		int noOfPoses = poses.getSize();
		int lastPose = noOfPoses-1;
		Point2D.Float originalStart = calcCenterPoint( feet, 0 );
		Point2D.Float originalEnd = calcCenterPoint( feet, lastPose );
		Point2D.Float dStart = new Point2D.Float( start.x-originalStart.x, start.y-originalStart.y );
		Point2D.Float dEnd = new Point2D.Float( end.x-originalEnd.x, end.y-originalEnd.y );
	}
	
	public static Point2D.Float calcCenterPoint( CanvasNode node1, CanvasNode node2 ) {
		return new Point2D.Float( (node1.getX()+node2.getX())/2, (node1.getY()+node2.getY())/2 );
	}
	
	public static Point2D.Float calcCenterPoint( ArrayList<Bone> bones, int key ) {
		float x = 0;
		float y = 0;
		for (Bone bone : bones) {
			x += bone.getKey(key).getX();
			y += bone.getKey(key).getY();
		}
		x /= bones.size();
		y /= bones.size();
		return new Point2D.Float( x, y );
	}
}
