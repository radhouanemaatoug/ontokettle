 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.job.entry.delay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.LabelTextVar;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the File compare job entry settings.
 *
 * @author samatar Hassan
 * @since  21-02-2007
 */
public class JobEntryDelayDialog extends Dialog implements JobEntryDialogInterface
{


	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label wlScaleTime;
	private  CCombo wScaleTime;
	private FormData fdlScaleTime, fdScaleTime;

	private LabelTextVar      wMaximumTimeout;
	private FormData     fdMaximumTimeout;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryDelay jobEntry;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;

	public JobEntryDelayDialog(Shell parent, JobEntryDelay jobEntry, JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.jobEntry=jobEntry;

		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobEval.Title"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, props.getJobsDialogStyle());
		props.setLook(shell);
		JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobEval.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobEval.Name.Label"));
		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, 0);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// MaximumTimeout line
		wMaximumTimeout = new LabelTextVar(shell, Messages.getString("JobEval.MaximumTimeout.Label"), Messages
			.getString("JobEval.MaximumTimeout.Tooltip"));
		props.setLook(wMaximumTimeout);
		wMaximumTimeout.addModifyListener(lsMod);
		fdMaximumTimeout = new FormData();
		fdMaximumTimeout.left = new FormAttachment(0, -margin);
		fdMaximumTimeout.top = new FormAttachment(wName, margin);
		fdMaximumTimeout.right = new FormAttachment(100, 0);
		wMaximumTimeout.setLayoutData(fdMaximumTimeout);

		// Whenever something changes, set the tooltip to the expanded version:
		wMaximumTimeout.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wMaximumTimeout.setToolTipText(StringUtil.environmentSubstitute( wMaximumTimeout.getText() ) );
			}
		}
			);




		//Scale time
		wlScaleTime = new Label(shell, SWT.RIGHT);
		wlScaleTime.setText("");
		props.setLook(wlScaleTime);
		fdlScaleTime = new FormData();
		fdlScaleTime.left = new FormAttachment(0, 0);
		fdlScaleTime.right = new FormAttachment(middle, 0);
		fdlScaleTime.top = new FormAttachment(wMaximumTimeout, margin);
		wlScaleTime.setLayoutData(fdlScaleTime);
		wScaleTime = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wScaleTime.add(Messages.getString("JobEval.SScaleTime.Label"));
		wScaleTime.add(Messages.getString("JobEval.MnScaleTime.Label"));
		wScaleTime.add(Messages.getString("JobEval.HrScaleTime.Label"));
		wScaleTime.select(0); // +1: starts at -1

		props.setLook(wScaleTime);
		fdScaleTime= new FormData();
		fdScaleTime.left = new FormAttachment(middle, 0);
		fdScaleTime.top = new FormAttachment(wMaximumTimeout, margin);
		fdScaleTime.right = new FormAttachment(100, 0);
		wScaleTime.setLayoutData(fdScaleTime);

		fdScaleTime = new FormData();
		fdScaleTime.left = new FormAttachment(middle, 0);
		fdScaleTime.top = new FormAttachment(wMaximumTimeout, margin);
		fdScaleTime.right = new FormAttachment(100, 0);
		wScaleTime.setLayoutData(fdScaleTime);



	
		
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wScaleTime);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wMaximumTimeout.addSelectionListener( lsDef );


		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		if (jobEntry.getMaximumTimeout()!= null) wMaximumTimeout.setText( jobEntry.getMaximumTimeout() );

		wScaleTime.select(jobEntry.scaletime );

	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setMaximumTimeout(wMaximumTimeout.getText());
		jobEntry.scaletime = wScaleTime.getSelectionIndex();
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
}