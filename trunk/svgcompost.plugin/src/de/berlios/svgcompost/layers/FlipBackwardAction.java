package de.berlios.svgcompost.layers;

import org.eclipse.ui.IWorkbenchPart;

public class FlipBackwardAction extends FlipForwardAction {
	
	public static String FLIP_BACKWARD = "Action.FlipBackward"; 

	public FlipBackwardAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Flip Backward");
		setId(FLIP_BACKWARD);
		setEnabled(false);
	}
	
	protected int getDirection() {
		return +1;
	}

}
