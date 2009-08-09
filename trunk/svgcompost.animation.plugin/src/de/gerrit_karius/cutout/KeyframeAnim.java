package de.gerrit_karius.cutout;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.gerrit_karius.cutout.anim.cluster.Skeleton;
import de.gerrit_karius.cutout.anim.composite.Sequence;
import de.gerrit_karius.cutout.anim.easing.Easing;
import de.gerrit_karius.cutout.canvas.CanvasNode;

public class KeyframeAnim extends Sequence {
	
	
	private static Logger log = Logger.getLogger(KeyframeAnim.class);
	
	protected String symbolId;
	int visibleKey;
	int prevVisibleKey;

	public HashMap<String,Skeleton> modelsByName;
	public ArrayList<String> modelNames;
	public ArrayList<Skeleton> models;
	public ArrayList<String> keyframeIds;
	public ArrayList<CanvasNode> keyframes;
	
	//@Override
	protected void _animate(double percentage) {
		
		if( log.isDebugEnabled() ) {
			log.debug( "innerGo: "+percentage );
		}
		
		calcCurrentIndex( percentage );
		boolean keyAlign2nd = (Easing.getAlign(easing) == Easing.EASE_OUT || Easing.getAlign(easing) == Easing.EASE_IN_OUT && percentage > 0.5 || percentage >= 1);
		
		// FIXME: Find out why this must happen one frame after the switch! Looks like a bug.
		if( prevVisibleKey != visibleKey ) {
			keyframes.get(prevVisibleKey).setVisible( false );
			keyframes.get(visibleKey).setVisible( true );
		}
		
		prevVisibleKey = visibleKey;
		visibleKey = keyAlign2nd ? currentIndex + 1 : currentIndex;
		log.debug( "Set visible key:      "+visibleKey );
		for( Skeleton model : models ) {
			model.setCurrentTweeningPair( currentIndex );
			model.setActiveKey( visibleKey );
		}
//		log.debug( "currentIndex: "+currentIndex );
//		log.debug( "keyAlign2nd: "+keyAlign2nd );
//		log.debug( "visibleKey: "+visibleKey );
//		if( visibleKey != currentIndex ) {
//			keyframes.get(currentIndex).setVisible( true );
//			visibleKey = currentIndex;
//			log.debug( "visibleKey: "+visibleKey );
//		}
		
	}
	
}
