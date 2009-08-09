package de.berlios.svgcompost.animation.export;

public interface Export {

	public abstract void writeOutput(String fileName);

	public abstract void captureFrame();

	public abstract void end();

}