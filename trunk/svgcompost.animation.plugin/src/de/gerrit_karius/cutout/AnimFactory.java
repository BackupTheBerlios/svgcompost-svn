package de.gerrit_karius.cutout;

import de.gerrit_karius.cutout.anim.chara.WalkDef;
import de.gerrit_karius.cutout.anim.chara.WalkSetup;
import de.gerrit_karius.cutout.anim.cluster.ClusterAnim;
import de.gerrit_karius.cutout.anim.cluster.ModelLoader;
import de.gerrit_karius.cutout.anim.composite.Parallel;
import de.gerrit_karius.cutout.anim.composite.Scene;
import de.gerrit_karius.cutout.anim.composite.Sequence;

public class AnimFactory {

	public static Parallel par() {
		return new Parallel();
	}
	public static Sequence seq() {
		return new Sequence();
	}
	public static Scene scene() {
		return new Scene();
	}
//	public static Anim test() {
//		return new TestAnim();
//	}
//	public static Setter set() {
//		return new Setter();
//	}
//	public static WalkAnim walk() {
//		return new WalkAnim();
//	}
//	public static PoseToPose pose() {
//		return new PoseToPose();
//	}
	public static ModelLoader model() {
		return new ModelLoader();
	}
	public static WalkDef loadWalk() {
		return new WalkDef();
	}
	public static WalkSetup walk() {
		return new WalkSetup();
	}
	public static ModelLoader loadModel() {
		return new ModelLoader();
	}
	public static ClusterAnim cluster() {
		return new ClusterAnim();
	}
//	public static CharaModel model() {
//		return new CharaModel();
//	}
//	public static CleanUp clean() {
//		return new CleanUp();
//	}
	
	
	public Scene buildData( Scene sc ) {
		
//		Scene sc = scene();
		
		sc.run( loadModel().model( "elfy" ) );
//		sc.run( loadWalk().model( "elfy" ).poses( "elfyWalkPoses" ) );
//		sc.run( walk().poses( "elfyWalkPoses" ).start( "100,-100" ).end( "120,100" ).dur( "3000" ) );
//		sc.run( cluster().model( "elfy" ).from( "elfyPose1" ).to( "elfyPose2" ).dur( "3000" ).ease( "cube.inout" ) );
		
		sc.run( cluster().model( "elfy" ).poses( "elfyWalkPoses" ).dur( "5000" ).ease( "cube.inout" ) );
		
//		sc.run( cluster().from( "group1" ).to( "group2" ).dur( "2000" ) );
		
		
		return sc;
	}
	
}
