/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.15 $
 */

package br.com.riselabs.fclcheck.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.


/**
 * Saves the concern model to a file.
 */
public class SaveAsAction extends Action
{
    
	/**
	 * @param pView The view containing the action
	 */
	public SaveAsAction( )
	{
		setText( "Save as..." );
//		setImageDescriptor( Tabuleta.imageDescriptorFromPlugin( Tabuleta.ID_PLUGIN, "icons/saveas.gif")); 
		setToolTipText("Specify the path to save your file."); 
	}
	
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{	
		IFile lFile = null;
		
		SaveAsDialog lDialog = new SaveAsDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
				
		IFile lCurrentFile = Tabuleta.getDefault().getDefaultResource();
		if( lCurrentFile != null )
		{
			lDialog.setOriginalFile( lCurrentFile );
		}
		lDialog.open();
		
		if( lDialog.getReturnCode() == Window.CANCEL )
		{
			return;
		}
		
		IPath lPath = lDialog.getResult();
		
		lPath = addCMFileExtension( lPath );
		
		IWorkspace lWorkspace = ResourcesPlugin.getWorkspace();
		lFile = lWorkspace.getRoot().getFile( lPath );
		
		if( !lFile.exists() )
		{
			try
			{
				lFile.create( null, true, null );
			}
			catch( CoreException lException )
			{
				MessageDialog.openError( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					Tabuleta.getResourceString( "actions.SaveAsAction.ErrorLabel"),
					Tabuleta.getResourceString( "actions.SaveAsAction.ErrorMessage") + " " + lException.getMessage());
				return;
			}
		}
		
		try
		{
			Tabuleta.getDefault().setDefaultResource( lFile );
			ModelWriter lWriter = new ModelWriter( Tabuleta.getDefault().getConcernModel() );
			lWriter.write( lFile );
		}
		catch( ModelIOException lException )
		{
		    MessageDialog.openError( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					Tabuleta.getResourceString( "actions.SaveAsAction.ErrorLabel"),
					Tabuleta.getResourceString( "actions.SaveAsAction.ErrorMessage") + " " + lException.getMessage());
			return;
		}
		Tabuleta.getDefault().resetDirty();
		if( aView != null )
		{
			aView.updateActionState();
		}
	}
	
	/**
	 * We have to define this class simply to be able to change the title
	 * of the dialog.  This is necessary because when the save as functionality
	 * kicks in as the result of an autosave the default title is too generic and 
	 * users may not directly see that they are saving their concern model.
	 */
	/**
	 * @author martin
	 *
	 */
	class ConcernModelSaveAsDialog extends SaveAsDialog
	{
		/**
		 * Creates a new SaveAsDialog for saving concern models.
		 * @param pShell The parent shell.
		 */
		public ConcernModelSaveAsDialog( Shell pShell )
		{
			super( pShell );
		}
		
		
	    /**
	     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	     * @param pParent the parent composite for the controls in this window. The type
	     * 					of layout used is determined by getLayout()
	 	 * @return the control that will be returned by subsequent calls to getControl()
	     */
	    protected Control createContents( Composite pParent )
	    {
	        Control lContents = super.createContents( pParent );

	        setTitle( Tabuleta.getResourceString( "actions.SaveAsAction.DialogTitle") );
	        setMessage( Tabuleta.getResourceString( "actions.SaveAsAction.DialogMessage") );

	        return lContents;
	    }
	}
	
	private static IPath addCMFileExtension( IPath pPath )
	{
		IPath lReturn = pPath;
		if(Tabuleta.getDefault().getPreferenceStore().getBoolean( ConcernMapperPreferencePage.P_CM_FILE_EXT ))
		{
			if ( lReturn.getFileExtension() == null )
			{
				lReturn = lReturn.addFileExtension( "cm" );
			}
			else if ( !lReturn.getFileExtension().equals( "cm" ) )
			{
				lReturn = lReturn.removeFileExtension();
				lReturn = lReturn.addFileExtension( "cm" );
			}
		}
		return lReturn;
	}
}
