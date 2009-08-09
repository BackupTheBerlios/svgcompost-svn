package de.gerrit_karius.cutout.export;

public interface Export {

	public abstract void writeOutput(String fileName);

	public abstract void captureFrame();

	public abstract void end();

}