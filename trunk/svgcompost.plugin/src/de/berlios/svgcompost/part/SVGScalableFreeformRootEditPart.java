package de.berlios.svgcompost.part;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;

public class SVGScalableFreeformRootEditPart extends
		ScalableFreeformRootEditPart implements RootEditPart {
	
	public static String BACKGROUND_LAYER = "Background";

	@Override
	protected ScalableFreeformLayeredPane createScaledLayers() {
		ScalableFreeformLayeredPane pane = super.createScaledLayers();
		pane.add(new FreeformLayer(),BACKGROUND_LAYER,0);
		return pane;
	}

}
