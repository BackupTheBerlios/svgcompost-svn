package de.berlios.svgcompost.animation.plugin.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewMovieWizard extends Wizard implements INewWizard {
	
	private IStructuredSelection selection;
    private NewMovieFileCreationPage newFileWizardPage;
    private IWorkbench workbench;
    
    public NewMovieWizard() {

        setWindowTitle("New SVGCompost Animation Movie");

    } 

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
	
    @Override
    public void addPages() {

        newFileWizardPage = new NewMovieFileCreationPage(selection);
        addPage(newFileWizardPage);
    }
    
	@Override
    public boolean performFinish() {
       
        IFile file = newFileWizardPage.createNewFile();
        if (file != null)
            return true;
        else
            return false;
    }
	
}
