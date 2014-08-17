package br.com.riselabs.fclcheck.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class FCLCheckNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "br.com.riselabs.fclcheck.fclcheckNature";

	private IProject project;

	private static final String[] FILENAME_OPTIONS = { "constraints.fcl",
			"fclproject.constraints" };
	// Default option
	public static String CONSTRAINTS_FILENAME = FILENAME_OPTIONS[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		if (!isThereConstraintFile()) {
			String chosenfile = askByFileName(FILENAME_OPTIONS);
			createConstraintFile(chosenfile);
		}

		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(FCLCheckBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(FCLCheckBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	private void createConstraintFile(String filename) throws CoreException {
		final IFile fclFile = project.getFile(filename);

		String contents = "% Automatic Created File\n";
		switch (filename) {
		case "constraints.fcl":
			CONSTRAINTS_FILENAME = FILENAME_OPTIONS[0];
			contents += "% this is a comment\n"
					+ "% 'groups:' marks the begin of the groups (XOR, OR) declarations\n"
					+ "groups:\n"
					+ "%  'alternative' group example and (its alias)\n"
					+ "%  alternative: AA, BB, CC, DD % (at-most-one: AA, BB, CC, DD)\n\n"
					+ "%  'or' groups and (its alias)  [this declarations are optional because "
					+ "			there is no dependency at all]\n"
					+ "%  or: H,I,J % (at-least-one: H,I,J)\n\n"
					+ "% 'cross-tree:' marks the begin of 'cross-tree constraints' declarations\n"
					+ "cross-tree:\n "
					+ "% declaration of 'cross-tree constraints' and (its alias)\n"
					+ "%  XXXX requires-exclusion PPPP % (XXXX excludes PPPP)\n"
					+ "%  YYYY requires-inclusion UUUU % (YYYY includes UUUU)\n"
					+ "%  VVVV mutually-exclusive PPPP % (VVVV excludes PPPP && PPPP excludes VVVV)\n"
					+ project.getName().toUpperCase()+" includes SYSTEMOUT %" ;
			break;

		case "fclproject.constraints":
			CONSTRAINTS_FILENAME = FILENAME_OPTIONS[1];
			contents += "% this file follows the LVAT constraints file format."
					+ project.getName().toUpperCase()+" => SYSTEMOUT %";
			break;
		default:
			showMessage("Error generating constraint file",
					"Please create the file yourself in the 'root' of your project.");
			break;
		}
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		fclFile.create(source, false, null);

	}

	private boolean isThereConstraintFile() {
		for (String s : FILENAME_OPTIONS) {
			final IFile fclFile = project.getFile(s);
			if (fclFile.exists()) {
				CONSTRAINTS_FILENAME = s;
				return true;
			}
		}
		return false;
	}

	/**
	 * Pergunta ao usuário qual o concern deve ser considerado para a
	 * intersecção. lista de opções preparada a partir o vetor passado como
	 * parâmetro.
	 * 
	 * @param concerns
	 * @return
	 */
	private String askByFileName(Object[] concerns) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				Display.getCurrent().getActiveShell(), new LabelProvider());

		dialog.setElements(concerns);
		dialog.setTitle("What constraint file you want to create?");
		// enquanto o usuário não disser qual é o concern
		while (dialog.open() != Window.OK) {
			String title = "Enabling FCLCheck nature";
			String message = "You must create constraint file.";
			showMessage(title, message);
		}
		Object[] result = dialog.getResult();
		return (String) result[0];
	}

	private void showMessage(String title, String message) {
		MessageDialog.openInformation(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(FCLCheckBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
	 * .resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
