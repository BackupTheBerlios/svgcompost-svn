package de.berlios.svgcompost.wizard;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import de.berlios.svgcompost.plugin.SVGCompostPlugin;

public class NewSVGFileCreationPage extends WizardNewFileCreationPage {

	public NewSVGFileCreationPage(IStructuredSelection selection) {
		super("NewSVGFileCreationPage", selection);
        setTitle("SVG File");
        setDescription("Creates a new SVG File");
        setFileExtension("svg");
	}
	
	@Override
    protected InputStream getInitialContents() {
        try {
            return SVGCompostPlugin.getDefault().getBundle().getEntry("res/newfile.svg")
            .openStream();
        } catch (IOException e) {
            return null;
        }
    }

}
