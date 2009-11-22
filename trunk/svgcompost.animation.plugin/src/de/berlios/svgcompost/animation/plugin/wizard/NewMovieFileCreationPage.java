package de.berlios.svgcompost.animation.plugin.wizard;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import de.berlios.svgcompost.animation.plugin.AnimPlugin;

public class NewMovieFileCreationPage extends WizardNewFileCreationPage {

	public NewMovieFileCreationPage(IStructuredSelection selection) {
		super("NewMovieFileCreationPage", selection);
        setTitle("SVGCompost animation movie");
        setDescription("Creates a new SVGCompost animation movie");
        setFileExtension("svg");
	}
	
	@Override
    protected InputStream getInitialContents() {
        try {
            return AnimPlugin.getDefault().getBundle().getEntry("res/newmovie.svg")
            .openStream();
        } catch (IOException e) {
            return null;
        }
    }

}
