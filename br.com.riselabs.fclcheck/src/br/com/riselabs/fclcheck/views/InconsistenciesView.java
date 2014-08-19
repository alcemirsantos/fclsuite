package br.com.riselabs.fclcheck.views;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import br.com.riselabs.fclcheck.FCLCheck;
import br.com.riselabs.fclcheck.jobs.InconsistenciesViewUpdateJob;

/**
 * Shows the inconsistencies found in the building process.
 */

public class InconsistenciesView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "br.com.riselabs.fclcheck.views.InconsistenciesView";

	private TableViewer viewer;
	private Action action1;
	private Action doubleClickAction;

	private static InconsistenciesViewUpdateJob updateJob;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class InconsistenciesViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object parent) {
			return ((List<IMarker>) parent).toArray();
		}
	}

	class InconsistenciesViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			IMarker marker = (IMarker) obj;
			switch(index){
			case 0: //	"Description", 
				return marker.getAttribute(IMarker.MESSAGE, "Undefined inconsistency.");
			case 1: //	"Type",
				try {
					return marker.getType();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			case 2: //	"Line", 
				return marker.getAttribute(IMarker.LINE_NUMBER, "1");
			case 3: //	"Location"
				return marker.getAttribute(IMarker.LOCATION, marker.getResource().getName());
			default:
				break;
			}
			return "";
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public  InconsistenciesView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		createViewer(parent);
		updateJob = new InconsistenciesViewUpdateJob("Updating Inconsistencies View...");
		updateJob.setInconsistenciesView(this);
		// Create the help context id for the viewer's control
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(viewer.getControl(), "br.com.riselabs.fclcheck.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		updateJob.schedule();
//		viewer.setInput(getViewSite());
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		createColumns();
		viewer.setContentProvider(new InconsistenciesViewContentProvider());
		viewer.setLabelProvider(new InconsistenciesViewLabelProvider());
		viewer.setSorter(new NameSorter());
	}

	// This will create the columns for the table
	private void createColumns() {

		String[] titles = {"Description", "Type", "Line", "Location"};
		int[] bounds = { 100, 50, 100, 50 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	public static Display getDisplay() {
		Display display = Display.getCurrent();
		// may be null if outside the UI thread
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	public void clearMarkers() {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				while (viewer.getElementAt(0) != null) {
					viewer.remove(viewer.getElementAt(0));
				}
			}
		});
	}

	public void addMarkers(final List<IMarker> markers) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				viewer.add(markers.toArray());
			}
		});
	}
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				InconsistenciesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				try {
					IDE.openEditor(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(),
							(IMarker) obj);
				} catch (PartInitException e) {
					MessageDialog.openError(
							new Shell(),
							FCLCheck.PLUGIN_ID,
							"Problem opening the marker. Cause: "
									+ e.getMessage());
					e.printStackTrace();
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Inconsistencies View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public static void sync() {
		updateJob.schedule();
		
	}
}