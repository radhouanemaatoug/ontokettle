package be.ibridge.kettle.cluster.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * 
 * Dialog that allows you to edit the settings of the cluster schema
 * 
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class ClusterSchemaDialog extends Dialog 
{
	private ClusterSchema clusterSchema;
	
	private Shell     shell;

    // Name
	private Text     wName;

    // Servers
    private TableView     wServers;

	private Button    wOK, wCancel;
	
    private ModifyListener lsMod;

	private Props     props;

    private int middle;
    private int margin;

    private ClusterSchema originalSchema;
    private boolean ok;

    private Button wSelect;

    private TextVar wPort;

    private TextVar wBufferSize;

    private TextVar wFlushInterval;

    private Button wCompressed;

    private List slaveServers;
    
	public ClusterSchemaDialog(Shell par, ClusterSchema clusterSchema, List slaveServers)
	{
		super(par, SWT.NONE);
		this.clusterSchema=(ClusterSchema) clusterSchema.clone();
        this.originalSchema=clusterSchema;
        this.slaveServers = slaveServers;
                
		props=Props.getInstance();
        ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				clusterSchema.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText("Clustering schema dialog");
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons, so we added those first...
        
        // What's the schema name??
        Label wlName = new Label(shell, SWT.RIGHT); 
        props.setLook(wlName);
        wlName.setText("Schema name  ");
        FormData fdlName = new FormData();
        fdlName.top   = new FormAttachment(0, 0);
        fdlName.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlName.right = new FormAttachment(middle, 0);
        wlName.setLayoutData(fdlName);

        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        FormData fdName = new FormData();
        fdName.top  = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(middle, margin); // To the right of the label
        fdName.right= new FormAttachment(95, 0);
        wName.setLayoutData(fdName);
        
        // What's the base port??
        Label wlPort = new Label(shell, SWT.RIGHT); 
        props.setLook(wlPort);
        wlPort.setText("Base socket port  ");
        FormData fdlPort = new FormData();
        fdlPort.top   = new FormAttachment(wName, margin);
        fdlPort.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlPort.right = new FormAttachment(middle, 0);
        wlPort.setLayoutData(fdlPort);

        wPort = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        FormData fdPort = new FormData();
        fdPort.top  = new FormAttachment(wName, margin);
        fdPort.left = new FormAttachment(middle, margin); // To the right of the label
        fdPort.right= new FormAttachment(95, 0);
        wPort.setLayoutData(fdPort);

        
        // What are the sockets buffer sizes??
        Label wlBufferSize = new Label(shell, SWT.RIGHT); 
        props.setLook(wlBufferSize);
        wlBufferSize.setText("Sockets buffer size  ");
        FormData fdlBufferSize = new FormData();
        fdlBufferSize.top   = new FormAttachment(wPort, margin);
        fdlBufferSize.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlBufferSize.right = new FormAttachment(middle, 0);
        wlBufferSize.setLayoutData(fdlBufferSize);

        wBufferSize = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wBufferSize);
        wBufferSize.addModifyListener(lsMod);
        FormData fdBufferSize = new FormData();
        fdBufferSize.top  = new FormAttachment(wPort, margin);
        fdBufferSize.left = new FormAttachment(middle, margin); // To the right of the label
        fdBufferSize.right= new FormAttachment(95, 0);
        wBufferSize.setLayoutData(fdBufferSize);

        // What are the sockets buffer sizes??
        Label wlFlushInterval = new Label(shell, SWT.RIGHT); 
        props.setLook(wlFlushInterval);
        wlFlushInterval.setText("Sockets flush interval (rows)  ");
        FormData fdlFlushInterval = new FormData();
        fdlFlushInterval.top   = new FormAttachment(wBufferSize, margin);
        fdlFlushInterval.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlFlushInterval.right = new FormAttachment(middle, 0);
        wlFlushInterval.setLayoutData(fdlFlushInterval);

        wFlushInterval = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wFlushInterval);
        wFlushInterval.addModifyListener(lsMod);
        FormData fdFlushInterval = new FormData();
        fdFlushInterval.top  = new FormAttachment(wBufferSize, margin);
        fdFlushInterval.left = new FormAttachment(middle, margin); // To the right of the label
        fdFlushInterval.right= new FormAttachment(95, 0);
        wFlushInterval.setLayoutData(fdFlushInterval);

        // What are the sockets buffer sizes??
        Label wlCompressed = new Label(shell, SWT.RIGHT); 
        props.setLook(wlCompressed);
        wlCompressed.setText("Sockets data compressed?  ");
        FormData fdlCompressed = new FormData();
        fdlCompressed.top   = new FormAttachment(wFlushInterval, margin);
        fdlCompressed.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlCompressed.right = new FormAttachment(middle, 0);
        wlCompressed.setLayoutData(fdlCompressed);

        wCompressed = new Button(shell, SWT.CHECK );
        props.setLook(wCompressed);
        FormData fdCompressed = new FormData();
        fdCompressed.top  = new FormAttachment(wFlushInterval, margin);
        fdCompressed.left = new FormAttachment(middle, margin); // To the right of the label
        fdCompressed.right= new FormAttachment(95, 0);
        wCompressed.setLayoutData(fdCompressed);

        
        // Schema servers:
        Label wlServers = new Label(shell, SWT.RIGHT);
        wlServers.setText("Slave servers  ");
        props.setLook(wlServers);
        FormData fdlServers=new FormData();
        fdlServers.left = new FormAttachment(0, 0);
        fdlServers.right = new FormAttachment(middle, 0);
        fdlServers.top  = new FormAttachment(wCompressed, margin);
        wlServers.setLayoutData(fdlServers);
        
        // Some buttons to manage...
        wSelect = new Button(shell, SWT.PUSH);
        wSelect.setText("Select slave servers");
        props.setLook(wSelect);
        FormData fdSelect=new FormData();
        fdSelect.right= new FormAttachment(100, 0);
        fdSelect.top  = new FormAttachment(wlServers, 5*margin);
        wSelect.setLayoutData(fdSelect);
        wSelect.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectSlaveServers(); }});

        ColumnInfo[] partitionColumns = new ColumnInfo[] { 
                new ColumnInfo( "Name", ColumnInfo.COLUMN_TYPE_TEXT, true, false), //$NON-NLS-1$
                new ColumnInfo( "Service URL", ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
                new ColumnInfo( "Master?", ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        };
        wServers = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, partitionColumns, 1, lsMod, props);
        wServers.setReadonly(false);
        props.setLook(wServers);
        FormData fdServers = new FormData();
        fdServers.left = new FormAttachment(middle, margin );
        fdServers.right = new FormAttachment(wSelect, -2*margin);
        fdServers.top = new FormAttachment(wCompressed, margin);
        fdServers.bottom = new FormAttachment(wOK, -margin * 2);
        wServers.setLayoutData(fdServers);
        wServers.table.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { editSlaveServer(); }});
		
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
        wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
        SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(selAdapter);
        wPort.addSelectionListener(selAdapter);
        wBufferSize.addSelectionListener(selAdapter);
        wFlushInterval.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return ok;
	}
	
    private void editSlaveServer()
    {
        int idx = wServers.getSelectionIndex();
        if (idx>=0)
        {
            SlaveServer slaveServer = clusterSchema.findSlaveServer(wServers.getItems(0)[idx]);
            if (slaveServer!=null)
            {
                SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
                if (dialog.open())
                {
                    refreshSlaveServers();
                }
            }
        }
    }

    private void selectSlaveServers()
    {
        String[] names = SlaveServer.getSlaveServerNames(slaveServers);
        int idx[] = Const.indexsOfFoundStrings(wServers.getItems(0), names);
        
        EnterSelectionDialog dialog = new EnterSelectionDialog(shell, names, "Select the servers", "Select the servers for this cluster");
        dialog.setSelectedNrs(idx);
        dialog.setMulti(true);
        if (dialog.open()!=null)
        {
            clusterSchema.getSlaveServers().clear();
            int[] indeces = dialog.getSelectionIndeces();
            for (int i=0;i<indeces.length;i++)
            {
                SlaveServer slaveServer = SlaveServer.findSlaveServer(slaveServers, names[indeces[i]]);
                clusterSchema.getSlaveServers().add(slaveServer);
            }
            
            refreshSlaveServers();
        }
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void getData()
	{
		wName.setText( Const.NVL(clusterSchema.getName(), "") );
		wPort.setText( Const.NVL(clusterSchema.getBasePort(), ""));
        wBufferSize.setText( Const.NVL(clusterSchema.getSocketsBufferSize(), ""));
        wFlushInterval.setText( Const.NVL(clusterSchema.getSocketsFlushInterval(), ""));
        wCompressed.setSelection( clusterSchema.isSocketsCompressed());
        
        refreshSlaveServers();
        
		wName.setFocus();
	}
    
	private void refreshSlaveServers()
    {
        wServers.clearAll(false);
        List slaveServers = clusterSchema.getSlaveServers();
        for (int i=0;i<slaveServers.size();i++)
        {
            TableItem item = new TableItem(wServers.table, SWT.NONE);
            SlaveServer slaveServer = (SlaveServer)slaveServers.get(i);
            item.setText(1, Const.NVL(slaveServer.getName(), ""));
            item.setText(2, Const.NVL(slaveServer.toString(), ""));
            item.setText(3, slaveServer.isMaster()?"Y":"N");
        }
        wServers.removeEmptyRows();
        wServers.setRowNums();
        wServers.optWidth(true);
    }

    private void cancel()
	{
		originalSchema = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalSchema.setName(clusterSchema.getName());
        originalSchema.setBasePort(clusterSchema.getBasePort());
        originalSchema.setSocketsBufferSize(clusterSchema.getSocketsBufferSize());
        originalSchema.setSocketsFlushInterval(clusterSchema.getSocketsFlushInterval());
        originalSchema.setSocketsCompressed(clusterSchema.isSocketsCompressed());

        originalSchema.setSlaveServers(clusterSchema.getSlaveServers());
        originalSchema.setChanged();

        ok=true;
        
        dispose();
	}
    
	private void getInfo()
    {
        clusterSchema.setName(wName.getText());
        clusterSchema.setBasePort(wPort.getText());
        clusterSchema.setSocketsBufferSize(wBufferSize.getText());
        clusterSchema.setSocketsFlushInterval(wFlushInterval.getText());
        clusterSchema.setSocketsCompressed(wCompressed.getSelection());

        String[] names = SlaveServer.getSlaveServerNames(slaveServers);
        int idx[] = Const.indexsOfFoundStrings(wServers.getItems(0), names);
        
        clusterSchema.getSlaveServers().clear();
        for (int i=0;i<idx.length;i++)
        {
            SlaveServer slaveServer = SlaveServer.findSlaveServer(slaveServers, names[idx[i]]);
            clusterSchema.getSlaveServers().add(slaveServer);
        }
            
    }
}