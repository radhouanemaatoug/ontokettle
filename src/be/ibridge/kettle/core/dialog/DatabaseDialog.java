/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.core.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.BaseDatabaseMeta;
import be.ibridge.kettle.core.database.ConnectionPoolUtil;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseConnectionPoolParameter;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.GenericDatabaseMeta;
import be.ibridge.kettle.core.database.PartitionDatabaseMeta;
import be.ibridge.kettle.core.database.SAPR3DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.spoon.Spoon;
import be.ibridge.kettle.trans.step.BaseStepDialog;

/**
 * 
 * Dialog that allows you to edit the settings of a database connection.
 * 
 * @see <code>DatabaseInfo</code>
 * @author Matt
 * @since 18-05-2003
 * 
 */

public class DatabaseDialog extends Dialog
{
    private DatabaseMeta   databaseMeta;

    private CTabFolder     wTabFolder;

    private CTabItem       wDbTab, wPoolTab, wOracleTab, wIfxTab, wMySQLTab, wSAPTab, wGenericTab, wOptionsTab, wSQLTab, wClusterTab;

    private Composite      wDbComp, wPoolComp, wOracleComp, wIfxComp, wMySQLComp, wSAPComp, wGenericComp, wOptionsComp, wSQLComp, wClusterComp;

    private Shell          shell;

    // DB
    private Label          wlConn, wlConnType, wlConnAcc, wlHostName, wlDBName, wlPort, wlUsername, wlPassword, wlData, wlIndex;

    private Text           wConn;

    private TextVar        wHostName, wDBName, wPort, wUsername, wPassword, wData, wIndex;

    private List           wConnType, wConnAcc;

    // Informix
    private Label          wlServername;

    private Text           wServername;

    // MySQL
    private Label          wlStreamResult;

    private Button         wStreamResult;

    // Pooling
    private Label          wlUsePool, wlInitPool, wlMaxPool;

    private Button         wUsePool;

    private TextVar        wInitPool, wMaxPool;

    private TableView      wPoolParameters;

    private Label          wlPoolParameters;

    // SAP
    private Label          wlSAPLanguage, wlSAPSystemNumber, wlSAPClient;

    private Text           wSAPLanguage, wSAPSystemNumber, wSAPClient;

    // Generic
    private Label          wlURL, wlDriverClass;

    private Text           wDriverClass;
    
    private TextVar        wURL;

    // Options
    private TableView      wOptions;

    // SQL
    private Label          wlSQL;

    private Text           wSQL;

    // Cluster
    private Label          wlUseCluster;

    private Button         wUseCluster;

    private TableView      wCluster;

    private Button         wOK, wTest, wExp, wList, wCancel, wOptionsHelp;

    private String         databaseName;

    private ModifyListener lsMod;

    private boolean        changed;

    private Props          props;

    private String         previousDatabaseType;

    private ArrayList      databases;

    private Map            extraOptions;

    private int            middle;

    private int            margin;

    private long           database_id;

    /**
     * @deprecated Use the simple version without <i>style</i>, <i>log</i> and <i>props</i> parameters
     */
    public DatabaseDialog(Shell parent, int style, LogWriter log, DatabaseMeta databaseMeta, Props props)
    {
        this(parent, databaseMeta);
    }

    public DatabaseDialog(Shell parent, DatabaseMeta databaseMeta)
    {
        super(parent, SWT.NONE);
        this.databaseMeta = databaseMeta;
        this.databaseName = databaseMeta.getName();
        this.props = Props.getInstance();
        this.databases = null;
        this.extraOptions = databaseMeta.getExtraOptions();
        this.database_id = databaseMeta.getID();

        String path = ""; //$NON-NLS-1$
        try
        {
            File file = new File("simple-jndi"); //$NON-NLS-1$
            path = file.getCanonicalPath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("org.osjava.sj.root", path); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public String open()
    {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);

        lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                databaseMeta.setChanged();
            }
        };
        changed = databaseMeta.hasChanged();

        middle = props.getMiddlePct();
        margin = Const.MARGIN;

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setText(Messages.getString("DatabaseDialog.Shell.title")); //$NON-NLS-1$
        shell.setLayout(formLayout);

        // First, add the buttons...

        // Buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$

        wTest = new Button(shell, SWT.PUSH);
        wTest.setText(Messages.getString("DatabaseDialog.button.Test")); //$NON-NLS-1$

        wExp = new Button(shell, SWT.PUSH);
        wExp.setText(Messages.getString("DatabaseDialog.button.Explore")); //$NON-NLS-1$

        wList = new Button(shell, SWT.PUSH);
        wList.setText(Messages.getString("DatabaseDialog.button.FeatureList")); //$NON-NLS-1$

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

        Button[] buttons = new Button[] { wOK, wTest, wExp, wList, wCancel };
        BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);

        // The rest stays above the buttons...

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        addGeneralTab();
        addPoolTab();
        addMySQLTab();
        addOracleTab();
        addInformixTab();
        addSAPTab();
        addGenericTab();
        addOptionsTab();
        addSQLTab();
        addClusterTab();

        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(0, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(wOK, -margin);
        wTabFolder.setLayoutData(fdTabFolder);

        // Add listeners
        wOK.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        });

        wCancel.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        });
        wTest.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                test();
            }
        });
        wExp.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                explore();
            }
        });
        wList.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                showFeatureList();
            }
        });
        SelectionAdapter selAdapter = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wHostName.addSelectionListener(selAdapter);
        wDBName.addSelectionListener(selAdapter);
        wPort.addSelectionListener(selAdapter);
        wUsername.addSelectionListener(selAdapter);
        wPassword.addSelectionListener(selAdapter);
        wConn.addSelectionListener(selAdapter);
        wData.addSelectionListener(selAdapter);
        wIndex.addSelectionListener(selAdapter);

        // OK, if the password contains a variable, we don't want to have the password hidden...
        wPassword.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                checkPasswordVisible(wPassword.getTextWidget());
            }
        });

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        wTabFolder.setSelection(0);

        getData();
        enableFields();

        SelectionAdapter lsTypeAcc = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                enableFields();
                setPortNumber();
            }
        };

        wConnType.addSelectionListener(lsTypeAcc);
        wConnAcc.addSelectionListener(lsTypeAcc);

        BaseStepDialog.setSize(shell);

        databaseMeta.setChanged(changed);
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return databaseName;
    }

    public static final void checkPasswordVisible(Text wPassword)
    {
        String password = wPassword.getText();
        java.util.List list = new ArrayList();
        StringUtil.getUsedVariables(password, list, true);
        // ONLY show the variable in clear text if there is ONE variable used
        // Also, it has to be the only string in the field.
        //

        if (list.size() != 1)
        {
            wPassword.setEchoChar('*');
        }
        else
        {
            if ((password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE))
                    || (password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE)))
            {
                wPassword.setEchoChar('\0'); // Show it all...
            }
            else
            {
                wPassword.setEchoChar('*');
            }
        }
    }

    private void addGeneralTab()
    {
        // ////////////////////////
        // START OF DB TAB ///
        // ////////////////////////
        wDbTab = new CTabItem(wTabFolder, SWT.NONE);
        wDbTab.setText(Messages.getString("DatabaseDialog.DbTab.title")); //$NON-NLS-1$

        wDbComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wDbComp);

        FormLayout GenLayout = new FormLayout();
        GenLayout.marginWidth = Const.FORM_MARGIN;
        GenLayout.marginHeight = Const.FORM_MARGIN;
        wDbComp.setLayout(GenLayout);

        // What's the connection name?
        wlConn = new Label(wDbComp, SWT.RIGHT);
        props.setLook(wlConn);
        wlConn.setText(Messages.getString("DatabaseDialog.label.ConnectionName")); //$NON-NLS-1$
        FormData fdlConn = new FormData();
        fdlConn.top = new FormAttachment(0, 0);
        fdlConn.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlConn.right = new FormAttachment(middle, -margin);
        wlConn.setLayoutData(fdlConn);

        wConn = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wConn);
        wConn.addModifyListener(lsMod);
        FormData fdConn = new FormData();
        fdConn.top = new FormAttachment(0, 0);
        fdConn.left = new FormAttachment(middle, 0); // To the right of the label
        fdConn.right = new FormAttachment(95, 0);
        wConn.setLayoutData(fdConn);

        // What types are there?
        wlConnType = new Label(wDbComp, SWT.RIGHT);
        wlConnType.setText(Messages.getString("DatabaseDialog.label.ConnectionType")); //$NON-NLS-1$
        props.setLook(wlConnType);
        FormData fdlConnType = new FormData();
        fdlConnType.top = new FormAttachment(wConn, margin); // below the line above
        fdlConnType.left = new FormAttachment(0, 0);
        fdlConnType.right = new FormAttachment(middle, -margin);
        wlConnType.setLayoutData(fdlConnType);

        wConnType = new List(wDbComp, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.V_SCROLL);
        props.setLook(wConnType);
        String[] dbtypes = DatabaseMeta.getDBTypeDescLongList();
        for (int i = 0; i < dbtypes.length; i++)
        {
            wConnType.add(dbtypes[i]);
        }
        props.setLook(wConnType);
        FormData fdConnType = new FormData();
        fdConnType.top = new FormAttachment(wConn, margin);
        fdConnType.left = new FormAttachment(middle, 0); // right of the label
        fdConnType.right = new FormAttachment(95, 0);
        fdConnType.bottom = new FormAttachment(wConn, 150);
        wConnType.setLayoutData(fdConnType);

        // What access types are there?
        wlConnAcc = new Label(wDbComp, SWT.RIGHT);
        wlConnAcc.setText(Messages.getString("DatabaseDialog.label.AccessMethod")); //$NON-NLS-1$
        props.setLook(wlConnAcc);
        FormData fdlConnAcc = new FormData();
        fdlConnAcc.top = new FormAttachment(wConnType, margin); // below the line above
        fdlConnAcc.left = new FormAttachment(0, 0);
        fdlConnAcc.right = new FormAttachment(middle, -margin);
        wlConnAcc.setLayoutData(fdlConnAcc);

        wConnAcc = new List(wDbComp, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.V_SCROLL);
        props.setLook(wConnAcc);
        props.setLook(wConnAcc);
        FormData fdConnAcc = new FormData();
        fdConnAcc.top = new FormAttachment(wConnType, margin);
        fdConnAcc.left = new FormAttachment(middle, 0); // right of the label
        fdConnAcc.right = new FormAttachment(95, 0);
        // fdConnAcc.bottom = new FormAttachment(wConnType, 50);
        wConnAcc.setLayoutData(fdConnAcc);

        // Hostname
        wlHostName = new Label(wDbComp, SWT.RIGHT);
        wlHostName.setText(Messages.getString("DatabaseDialog.label.ServerHostname")); //$NON-NLS-1$
        props.setLook(wlHostName);
        FormData fdlHostName = new FormData();
        fdlHostName.top = new FormAttachment(wConnAcc, margin);
        fdlHostName.left = new FormAttachment(0, 0);
        fdlHostName.right = new FormAttachment(middle, -margin);
        wlHostName.setLayoutData(fdlHostName);

        wHostName = new TextVar(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wHostName);
        wHostName.addModifyListener(lsMod);
        FormData fdHostName = new FormData();
        fdHostName.top = new FormAttachment(wConnAcc, margin);
        fdHostName.left = new FormAttachment(middle, 0);
        fdHostName.right = new FormAttachment(95, 0);
        wHostName.setLayoutData(fdHostName);

        // DBName
        wlDBName = new Label(wDbComp, SWT.RIGHT);
        wlDBName.setText(Messages.getString("DatabaseDialog.label.DatabaseName")); //$NON-NLS-1$
        props.setLook(wlDBName);
        FormData fdlDBName = new FormData();
        fdlDBName.top = new FormAttachment(wHostName, margin);
        fdlDBName.left = new FormAttachment(0, 0);
        fdlDBName.right = new FormAttachment(middle, -margin);
        wlDBName.setLayoutData(fdlDBName);

        wDBName = new TextVar(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wDBName);
        wDBName.addModifyListener(lsMod);
        FormData fdDBName = new FormData();
        fdDBName.top = new FormAttachment(wHostName, margin);
        fdDBName.left = new FormAttachment(middle, 0);
        fdDBName.right = new FormAttachment(95, 0);
        wDBName.setLayoutData(fdDBName);

        // Port
        wlPort = new Label(wDbComp, SWT.RIGHT);
        wlPort.setText(Messages.getString("DatabaseDialog.label.PortNumber")); //$NON-NLS-1$
        props.setLook(wlPort);
        FormData fdlPort = new FormData();
        fdlPort.top = new FormAttachment(wDBName, margin);
        fdlPort.left = new FormAttachment(0, 0);
        fdlPort.right = new FormAttachment(middle, -margin);
        wlPort.setLayoutData(fdlPort);

        wPort = new TextVar(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        FormData fdPort = new FormData();
        fdPort.top = new FormAttachment(wDBName, margin);
        fdPort.left = new FormAttachment(middle, 0);
        fdPort.right = new FormAttachment(95, 0);
        wPort.setLayoutData(fdPort);

        // Username
        wlUsername = new Label(wDbComp, SWT.RIGHT);
        wlUsername.setText(Messages.getString("DatabaseDialog.label.Username")); //$NON-NLS-1$
        props.setLook(wlUsername);
        FormData fdlUsername = new FormData();
        fdlUsername.top = new FormAttachment(wPort, margin);
        fdlUsername.left = new FormAttachment(0, 0);
        fdlUsername.right = new FormAttachment(middle, -margin);
        wlUsername.setLayoutData(fdlUsername);

        wUsername = new TextVar(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUsername);
        wUsername.addModifyListener(lsMod);
        FormData fdUsername = new FormData();
        fdUsername.top = new FormAttachment(wPort, margin);
        fdUsername.left = new FormAttachment(middle, 0);
        fdUsername.right = new FormAttachment(95, 0);
        wUsername.setLayoutData(fdUsername);

        // Password
        wlPassword = new Label(wDbComp, SWT.RIGHT);
        wlPassword.setText(Messages.getString("DatabaseDialog.label.Password")); //$NON-NLS-1$
        props.setLook(wlPassword);
        FormData fdlPassword = new FormData();
        fdlPassword.top = new FormAttachment(wUsername, margin);
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);

        wPassword = new TextVar(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        FormData fdPassword = new FormData();
        fdPassword.top = new FormAttachment(wUsername, margin);
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.right = new FormAttachment(95, 0);
        wPassword.setLayoutData(fdPassword);

        FormData fdDbComp = new FormData();
        fdDbComp.left = new FormAttachment(0, 0);
        fdDbComp.top = new FormAttachment(0, 0);
        fdDbComp.right = new FormAttachment(100, 0);
        fdDbComp.bottom = new FormAttachment(100, 0);
        wDbComp.setLayoutData(fdDbComp);

        wDbComp.layout();
        wDbTab.setControl(wDbComp);

        // ///////////////////////////////////////////////////////////
        // / END OF GEN TAB
        // ///////////////////////////////////////////////////////////
    }

    private void addPoolTab()
    {
        // ////////////////////////
        // START OF POOL TAB///
        // /
        wPoolTab = new CTabItem(wTabFolder, SWT.NONE);
        wPoolTab.setText(Messages.getString("DatabaseDialog.PoolTab.title")); //$NON-NLS-1$

        FormLayout poolLayout = new FormLayout();
        poolLayout.marginWidth = Const.FORM_MARGIN;
        poolLayout.marginHeight = Const.FORM_MARGIN;

        wPoolComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wPoolComp);
        wPoolComp.setLayout(poolLayout);

        // What's the data tablespace name?
        wlUsePool = new Label(wPoolComp, SWT.RIGHT);
        props.setLook(wlUsePool);
        wlUsePool.setText(Messages.getString("DatabaseDialog.label.UseConnectionPool")); //$NON-NLS-1$
        FormData fdlUsePool = new FormData();
        fdlUsePool.top = new FormAttachment(0, 0);
        fdlUsePool.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlUsePool.right = new FormAttachment(middle, -margin);
        wlUsePool.setLayoutData(fdlUsePool);

        wUsePool = new Button(wPoolComp, SWT.CHECK);
        wUsePool.setSelection(databaseMeta.isUsingConnectionPool());
        props.setLook(wUsePool);
        wUsePool.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                databaseMeta.setChanged();
                enableFields();
            }
        });
        FormData fdUsePool = new FormData();
        fdUsePool.top = new FormAttachment(0, 0);
        fdUsePool.left = new FormAttachment(middle, 0); // To the right of the label
        wUsePool.setLayoutData(fdUsePool);

        // What's the initial pool size
        wlInitPool = new Label(wPoolComp, SWT.RIGHT);
        props.setLook(wlInitPool);
        wlInitPool.setText(Messages.getString("DatabaseDialog.label.InitialPoolSize")); //$NON-NLS-1$
        FormData fdlInitPool = new FormData();
        fdlInitPool.top = new FormAttachment(wUsePool, margin);
        fdlInitPool.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlInitPool.right = new FormAttachment(middle, -margin);
        wlInitPool.setLayoutData(fdlInitPool);

        wInitPool = new TextVar(wPoolComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wInitPool.setText(Integer.toString(databaseMeta.getInitialPoolSize()));
        props.setLook(wInitPool);
        wInitPool.addModifyListener(lsMod);
        FormData fdInitPool = new FormData();
        fdInitPool.top = new FormAttachment(wUsePool, margin);
        fdInitPool.left = new FormAttachment(middle, 0); // To the right of the label
        fdInitPool.right = new FormAttachment(95, 0);
        wInitPool.setLayoutData(fdInitPool);

        // What's the maximum pool size
        wlMaxPool = new Label(wPoolComp, SWT.RIGHT);
        props.setLook(wlMaxPool);
        wlMaxPool.setText(Messages.getString("DatabaseDialog.label.MaximumPoolSize")); //$NON-NLS-1$
        FormData fdlMaxPool = new FormData();
        fdlMaxPool.top = new FormAttachment(wInitPool, margin);
        fdlMaxPool.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlMaxPool.right = new FormAttachment(middle, -margin);
        wlMaxPool.setLayoutData(fdlMaxPool);

        wMaxPool = new TextVar(wPoolComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wMaxPool.setText(Integer.toString(databaseMeta.getMaximumPoolSize()));
        props.setLook(wMaxPool);
        wMaxPool.addModifyListener(lsMod);
        FormData fdMaxPool = new FormData();
        fdMaxPool.top = new FormAttachment(wInitPool, margin);
        fdMaxPool.left = new FormAttachment(middle, 0); // To the right of the label
        fdMaxPool.right = new FormAttachment(95, 0);
        wMaxPool.setLayoutData(fdMaxPool);

        // What's the maximum pool size
        wlPoolParameters = new Label(wPoolComp, SWT.RIGHT);
        props.setLook(wlPoolParameters);
        wlPoolParameters.setText(Messages.getString("DatabaseDialog.label.PoolParameters")); //$NON-NLS-1$
        FormData fdlPoolParameters = new FormData();
        fdlPoolParameters.top = new FormAttachment(wInitPool, margin);
        fdlPoolParameters.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlPoolParameters.right = new FormAttachment(middle, -margin);
        wlPoolParameters.setLayoutData(fdlPoolParameters);

        // options list
        ColumnInfo[] colinfo = new ColumnInfo[] {
                new ColumnInfo(Messages.getString("DatabaseDialog.column.PoolParameter"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.PoolDefault"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.PoolValue"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
        };
        colinfo[2].setUsingVariables(true);
        final ArrayList parameters = DatabaseConnectionPoolParameter.getRowList(BaseDatabaseMeta.poolingParameters, Messages
                .getString("DatabaseDialog.column.PoolParameter"), Messages.getString("DatabaseDialog.column.PoolDefault"), Messages
                .getString("DatabaseDialog.column.PoolDescription"));
        colinfo[0].setSelectionAdapter(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                SelectRowDialog dialog = new SelectRowDialog(shell, SWT.NONE, parameters);
                Row row = dialog.open();
                if (row != null)
                {
                    // the parameter is the first value
                    String parameterName = row.getValue(0).getString();
                    String defaultValue = DatabaseConnectionPoolParameter.findParameter(parameterName, BaseDatabaseMeta.poolingParameters)
                            .getDefaultValue();
                    wPoolParameters.setText(Const.NVL(parameterName, ""), e.x, e.y);
                    wPoolParameters.setText(Const.NVL(defaultValue, ""), e.x + 1, e.y);
                }
            }
        });

        wPoolParameters = new TableView(wPoolComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, lsMod, props);
        props.setLook(wPoolParameters);
        FormData fdOptions = new FormData();
        fdOptions.left = new FormAttachment(0, 0);
        fdOptions.right = new FormAttachment(100, 0);
        fdOptions.top = new FormAttachment(wMaxPool, margin * 2);
        fdOptions.bottom = new FormAttachment(100, -20);
        wPoolParameters.setLayoutData(fdOptions);

        FormData fdPoolComp = new FormData();
        fdPoolComp.left = new FormAttachment(0, 0);
        fdPoolComp.top = new FormAttachment(0, 0);
        fdPoolComp.right = new FormAttachment(100, 0);
        fdPoolComp.bottom = new FormAttachment(100, 0);
        wPoolComp.setLayoutData(fdPoolComp);

        wPoolComp.layout();
        wPoolTab.setControl(wPoolComp);
    }

    private void addOracleTab()
    {
        // ////////////////////////
        // START OF ORACLE TAB///
        // /
        wOracleTab = new CTabItem(wTabFolder, SWT.NONE);
        wOracleTab.setText(Messages.getString("DatabaseDialog.OracleTab.title")); //$NON-NLS-1$

        FormLayout oracleLayout = new FormLayout();
        oracleLayout.marginWidth = Const.FORM_MARGIN;
        oracleLayout.marginHeight = Const.FORM_MARGIN;

        wOracleComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wOracleComp);
        wOracleComp.setLayout(oracleLayout);

        // What's the data tablespace name?
        wlData = new Label(wOracleComp, SWT.RIGHT);
        props.setLook(wlData);
        wlData.setText(Messages.getString("DatabaseDialog.label.TablespaceForData")); //$NON-NLS-1$
        FormData fdlData = new FormData();
        fdlData.top = new FormAttachment(0, 0);
        fdlData.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlData.right = new FormAttachment(middle, -margin);
        wlData.setLayoutData(fdlData);

        wData = new TextVar(wOracleComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wData.setText(NVL(databaseMeta.getDataTablespace() == null ? "" : databaseMeta.getDataTablespace(), "")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wData);
        wData.addModifyListener(lsMod);
        FormData fdData = new FormData();
        fdData.top = new FormAttachment(0, 0);
        fdData.left = new FormAttachment(middle, 0); // To the right of the label
        fdData.right = new FormAttachment(95, 0);
        wData.setLayoutData(fdData);

        // What's the index tablespace name?
        wlIndex = new Label(wOracleComp, SWT.RIGHT);
        props.setLook(wlIndex);
        wlIndex.setText(Messages.getString("DatabaseDialog.label.TablespaceForIndexes")); //$NON-NLS-1$
        FormData fdlIndex = new FormData();
        fdlIndex.top = new FormAttachment(wData, margin);
        fdlIndex.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlIndex.right = new FormAttachment(middle, -margin);
        wlIndex.setLayoutData(fdlIndex);

        wIndex = new TextVar(wOracleComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wIndex.setText(NVL(databaseMeta.getIndexTablespace() == null ? "" : databaseMeta.getIndexTablespace(), "")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wIndex);
        wIndex.addModifyListener(lsMod);
        FormData fdIndex = new FormData();
        fdIndex.top = new FormAttachment(wData, margin);
        fdIndex.left = new FormAttachment(middle, 0); // To the right of the label
        fdIndex.right = new FormAttachment(95, 0);
        wIndex.setLayoutData(fdIndex);

        FormData fdOracleComp = new FormData();
        fdOracleComp.left = new FormAttachment(0, 0);
        fdOracleComp.top = new FormAttachment(0, 0);
        fdOracleComp.right = new FormAttachment(100, 0);
        fdOracleComp.bottom = new FormAttachment(100, 0);
        wOracleComp.setLayoutData(fdOracleComp);

        wOracleComp.layout();
        wOracleTab.setControl(wOracleComp);
    }

    private void addInformixTab()
    {
        // ////////////////////////
        // START OF INFORMIX TAB///
        // /
        wIfxTab = new CTabItem(wTabFolder, SWT.NONE);
        wIfxTab.setText(Messages.getString("DatabaseDialog.IfxTab.title")); //$NON-NLS-1$

        FormLayout ifxLayout = new FormLayout();
        ifxLayout.marginWidth = Const.FORM_MARGIN;
        ifxLayout.marginHeight = Const.FORM_MARGIN;

        wIfxComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wIfxComp);
        wIfxComp.setLayout(ifxLayout);

        // Servername
        wlServername = new Label(wIfxComp, SWT.RIGHT);
        wlServername.setText(Messages.getString("DatabaseDialog.label.InformixServername")); //$NON-NLS-1$
        props.setLook(wlServername);
        FormData fdlServername = new FormData();
        fdlServername.top = new FormAttachment(0, margin);
        fdlServername.left = new FormAttachment(0, 0);
        fdlServername.right = new FormAttachment(middle, -margin);
        wlServername.setLayoutData(fdlServername);

        wServername = new Text(wIfxComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServername);
        wServername.addModifyListener(lsMod);
        FormData fdServername = new FormData();
        fdServername.top = new FormAttachment(0, margin);
        fdServername.left = new FormAttachment(middle, 0);
        fdServername.right = new FormAttachment(95, 0);
        wServername.setLayoutData(fdServername);

        FormData fdIfxComp = new FormData();
        fdIfxComp.left = new FormAttachment(0, 0);
        fdIfxComp.top = new FormAttachment(0, 0);
        fdIfxComp.right = new FormAttachment(100, 0);
        fdIfxComp.bottom = new FormAttachment(100, 0);
        wIfxComp.setLayoutData(fdIfxComp);

        wIfxComp.layout();
        wIfxTab.setControl(wIfxComp);
    }

    private void addMySQLTab()
    {
        // ////////////////////////
        // START OF MySQL TAB///
        // /
        wMySQLTab = new CTabItem(wTabFolder, SWT.NONE);
        wMySQLTab.setText(Messages.getString("DatabaseDialog.MySQLTab.title")); //$NON-NLS-1$

        FormLayout MySQLLayout = new FormLayout();
        MySQLLayout.marginWidth = Const.FORM_MARGIN;
        MySQLLayout.marginHeight = Const.FORM_MARGIN;

        wMySQLComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wMySQLComp);
        wMySQLComp.setLayout(MySQLLayout);

        // StreamResult
        wlStreamResult = new Label(wMySQLComp, SWT.RIGHT);
        wlStreamResult.setText(Messages.getString("DatabaseDialog.label.MySQLStreamResults")); //$NON-NLS-1$
        props.setLook(wlStreamResult);
        FormData fdlStreamResult = new FormData();
        fdlStreamResult.top = new FormAttachment(0, margin);
        fdlStreamResult.left = new FormAttachment(0, 0);
        fdlStreamResult.right = new FormAttachment(middle, -margin);
        wlStreamResult.setLayoutData(fdlStreamResult);

        wStreamResult = new Button(wMySQLComp, SWT.CHECK);
        props.setLook(wStreamResult);
        FormData fdStreamResult = new FormData();
        fdStreamResult.top = new FormAttachment(0, margin);
        fdStreamResult.left = new FormAttachment(middle, 0);
        fdStreamResult.right = new FormAttachment(95, 0);
        wStreamResult.setLayoutData(fdStreamResult);

        FormData fdMySQLComp = new FormData();
        fdMySQLComp.left = new FormAttachment(0, 0);
        fdMySQLComp.top = new FormAttachment(0, 0);
        fdMySQLComp.right = new FormAttachment(100, 0);
        fdMySQLComp.bottom = new FormAttachment(100, 0);
        wMySQLComp.setLayoutData(fdMySQLComp);

        wMySQLComp.layout();
        wMySQLTab.setControl(wMySQLComp);
    }

    private void addSAPTab()
    {
        // ////////////////////////
        // START OF SAP TAB///
        // /
        wSAPTab = new CTabItem(wTabFolder, SWT.NONE);
        wSAPTab.setText(Messages.getString("DatabaseDialog.label.Sap")); //$NON-NLS-1$

        FormLayout sapLayout = new FormLayout();
        sapLayout.marginWidth = Const.FORM_MARGIN;
        sapLayout.marginHeight = Const.FORM_MARGIN;

        wSAPComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wSAPComp);
        wSAPComp.setLayout(sapLayout);

        // wSAPLanguage, wSSAPystemNumber, wSAPSystemID

        // Language
        wlSAPLanguage = new Label(wSAPComp, SWT.RIGHT);
        wlSAPLanguage.setText(Messages.getString("DatabaseDialog.label.Language")); //$NON-NLS-1$
        props.setLook(wlSAPLanguage);
        FormData fdlSAPLanguage = new FormData();
        fdlSAPLanguage.top = new FormAttachment(0, margin);
        fdlSAPLanguage.left = new FormAttachment(0, 0);
        fdlSAPLanguage.right = new FormAttachment(middle, -margin);
        wlSAPLanguage.setLayoutData(fdlSAPLanguage);

        wSAPLanguage = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSAPLanguage);
        wSAPLanguage.addModifyListener(lsMod);
        FormData fdSAPLanguage = new FormData();
        fdSAPLanguage.top = new FormAttachment(0, margin);
        fdSAPLanguage.left = new FormAttachment(middle, 0);
        fdSAPLanguage.right = new FormAttachment(95, 0);
        wSAPLanguage.setLayoutData(fdSAPLanguage);

        // SystemNumber
        wlSAPSystemNumber = new Label(wSAPComp, SWT.RIGHT);
        wlSAPSystemNumber.setText(Messages.getString("DatabaseDialog.label.SystemNumber")); //$NON-NLS-1$
        props.setLook(wlSAPSystemNumber);
        FormData fdlSAPSystemNumber = new FormData();
        fdlSAPSystemNumber.top = new FormAttachment(wSAPLanguage, margin);
        fdlSAPSystemNumber.left = new FormAttachment(0, 0);
        fdlSAPSystemNumber.right = new FormAttachment(middle, -margin);
        wlSAPSystemNumber.setLayoutData(fdlSAPSystemNumber);

        wSAPSystemNumber = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSAPSystemNumber);
        wSAPSystemNumber.addModifyListener(lsMod);
        FormData fdSAPSystemNumber = new FormData();
        fdSAPSystemNumber.top = new FormAttachment(wSAPLanguage, margin);
        fdSAPSystemNumber.left = new FormAttachment(middle, 0);
        fdSAPSystemNumber.right = new FormAttachment(95, 0);
        wSAPSystemNumber.setLayoutData(fdSAPSystemNumber);

        // SystemID
        wlSAPClient = new Label(wSAPComp, SWT.RIGHT);
        wlSAPClient.setText(Messages.getString("DatabaseDialog.label.SapClient")); //$NON-NLS-1$
        props.setLook(wlSAPClient);
        FormData fdlSAPClient = new FormData();
        fdlSAPClient.top = new FormAttachment(wSAPSystemNumber, margin);
        fdlSAPClient.left = new FormAttachment(0, 0);
        fdlSAPClient.right = new FormAttachment(middle, -margin);
        wlSAPClient.setLayoutData(fdlSAPClient);

        wSAPClient = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSAPClient);
        wSAPClient.addModifyListener(lsMod);
        FormData fdSAPClient = new FormData();
        fdSAPClient.top = new FormAttachment(wSAPSystemNumber, margin);
        fdSAPClient.left = new FormAttachment(middle, 0);
        fdSAPClient.right = new FormAttachment(95, 0);
        wSAPClient.setLayoutData(fdSAPClient);

        FormData fdSAPComp = new FormData();
        fdSAPComp.left = new FormAttachment(0, 0);
        fdSAPComp.top = new FormAttachment(0, 0);
        fdSAPComp.right = new FormAttachment(100, 0);
        fdSAPComp.bottom = new FormAttachment(100, 0);
        wSAPComp.setLayoutData(fdSAPComp);

        wSAPComp.layout();
        wSAPTab.setControl(wSAPComp);
    }

    private void addGenericTab()
    {
        // ////////////////////////
        // START OF DB TAB///
        // /
        wGenericTab = new CTabItem(wTabFolder, SWT.NONE);
        wGenericTab.setText(Messages.getString("DatabaseDialog.GenericTab.title")); //$NON-NLS-1$
        wGenericTab.setToolTipText(Messages.getString("DatabaseDialog.GenericTab.tooltip")); //$NON-NLS-1$

        FormLayout genericLayout = new FormLayout();
        genericLayout.marginWidth = Const.FORM_MARGIN;
        genericLayout.marginHeight = Const.FORM_MARGIN;

        wGenericComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wGenericComp);
        wGenericComp.setLayout(genericLayout);

        // URL
        wlURL = new Label(wGenericComp, SWT.RIGHT);
        wlURL.setText(Messages.getString("DatabaseDialog.label.Url")); //$NON-NLS-1$
        props.setLook(wlURL);
        FormData fdlURL = new FormData();
        fdlURL.top = new FormAttachment(0, margin);
        fdlURL.left = new FormAttachment(0, 0);
        fdlURL.right = new FormAttachment(middle, -margin);
        wlURL.setLayoutData(fdlURL);

        wURL = new TextVar(wGenericComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wURL);
        wURL.addModifyListener(lsMod);
        FormData fdURL = new FormData();
        fdURL.top = new FormAttachment(0, margin);
        fdURL.left = new FormAttachment(middle, 0);
        fdURL.right = new FormAttachment(95, 0);
        wURL.setLayoutData(fdURL);

        // Driver class
        wlDriverClass = new Label(wGenericComp, SWT.RIGHT);
        wlDriverClass.setText(Messages.getString("DatabaseDialog.label.DriverClass")); //$NON-NLS-1$
        props.setLook(wlDriverClass);
        FormData fdlDriverClass = new FormData();
        fdlDriverClass.top = new FormAttachment(wURL, margin);
        fdlDriverClass.left = new FormAttachment(0, 0);
        fdlDriverClass.right = new FormAttachment(middle, -margin);
        wlDriverClass.setLayoutData(fdlDriverClass);

        wDriverClass = new Text(wGenericComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wDriverClass);
        wDriverClass.addModifyListener(lsMod);
        FormData fdDriverClass = new FormData();
        fdDriverClass.top = new FormAttachment(wURL, margin);
        fdDriverClass.left = new FormAttachment(middle, 0);
        fdDriverClass.right = new FormAttachment(95, 0);
        wDriverClass.setLayoutData(fdDriverClass);

        FormData fdGenericComp = new FormData();
        fdGenericComp.left = new FormAttachment(0, 0);
        fdGenericComp.top = new FormAttachment(0, 0);
        fdGenericComp.right = new FormAttachment(100, 0);
        fdGenericComp.bottom = new FormAttachment(100, 0);
        wGenericComp.setLayoutData(fdGenericComp);

        wGenericComp.layout();
        wGenericTab.setControl(wGenericComp);
    }

    private void addOptionsTab()
    {
        // ////////////////////////
        // START OF OPTIONS TAB///
        // /
        wOptionsTab = new CTabItem(wTabFolder, SWT.NONE);
        wOptionsTab.setText(Messages.getString("DatabaseDialog.label.Options")); //$NON-NLS-1$
        wOptionsTab.setToolTipText(Messages.getString("DatabaseDialog.tooltip.Options")); //$NON-NLS-1$

        FormLayout optionsLayout = new FormLayout();
        optionsLayout.marginWidth = margin;
        optionsLayout.marginHeight = margin;

        wOptionsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wOptionsComp);
        wOptionsComp.setLayout(optionsLayout);

        wOptionsHelp = new Button(wOptionsComp, SWT.PUSH);
        wOptionsHelp.setText(Messages.getString("DatabaseDialog.button.ShowHelp")); //$NON-NLS-1$
        wOptionsHelp.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                showOptionsHelpText();
            }
        });

        BaseStepDialog.positionBottomButtons(wOptionsComp, new Button[] { wOptionsHelp }, margin, null);

        // options list
        ColumnInfo[] colinfo = new ColumnInfo[] {
                new ColumnInfo(
                        Messages.getString("DatabaseDialog.column.DbType"), ColumnInfo.COLUMN_TYPE_CCOMBO, DatabaseMeta.getDBTypeDescLongList(), true), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Parameter"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
        };

        colinfo[0].setToolTip(Messages.getString("DatabaseDialog.tooltip.DbType")); //$NON-NLS-1$
        colinfo[1].setToolTip(Messages.getString("DatabaseDialog.tooltip.Parameter")); //$NON-NLS-1$
        colinfo[2].setUsingVariables(true);

        wOptions = new TableView(wOptionsComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, lsMod, props);
        props.setLook(wOptions);
        FormData fdOptions = new FormData();
        fdOptions.left = new FormAttachment(0, 0);
        fdOptions.right = new FormAttachment(100, 0);
        fdOptions.top = new FormAttachment(0, 0);
        fdOptions.bottom = new FormAttachment(wOptionsHelp, -margin);
        wOptions.setLayoutData(fdOptions);

        FormData fdOptionsComp = new FormData();
        fdOptionsComp.left = new FormAttachment(0, 0);
        fdOptionsComp.top = new FormAttachment(0, 0);
        fdOptionsComp.right = new FormAttachment(100, 0);
        fdOptionsComp.bottom = new FormAttachment(100, 0);
        wOptionsComp.setLayoutData(fdOptionsComp);

        wOptionsComp.layout();
        wOptionsTab.setControl(wOptionsComp);
    }

    private void addSQLTab()
    {
        // ////////////////////////
        // START OF SQL TAB///
        // /
        wSQLTab = new CTabItem(wTabFolder, SWT.NONE);
        wSQLTab.setText(Messages.getString("DatabaseDialog.SQLTab.title")); //$NON-NLS-1$
        wSQLTab.setToolTipText(Messages.getString("DatabaseDialog.SQLTab.tooltip")); //$NON-NLS-1$

        FormLayout sqlLayout = new FormLayout();
        sqlLayout.marginWidth = margin;
        sqlLayout.marginHeight = margin;

        wSQLComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wSQLComp);
        wSQLComp.setLayout(sqlLayout);

        wlSQL = new Label(wSQLComp, SWT.LEFT);
        props.setLook(wlSQL);
        wlSQL.setText(Messages.getString("DatabaseDialog.label.Statements")); //$NON-NLS-1$

        FormData fdlSQL = new FormData();
        fdlSQL.left = new FormAttachment(0, 0);
        fdlSQL.top = new FormAttachment(0, 0);
        wlSQL.setLayoutData(fdlSQL);

        wSQL = new Text(wSQLComp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER);
        props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);

        FormData fdSQL = new FormData();
        fdSQL.left = new FormAttachment(0, 0);
        fdSQL.right = new FormAttachment(100, 0);
        fdSQL.top = new FormAttachment(wlSQL, margin);
        fdSQL.bottom = new FormAttachment(100, 0);
        wSQL.setLayoutData(fdSQL);

        FormData fdSQLComp = new FormData();
        fdSQLComp.left = new FormAttachment(0, 0);
        fdSQLComp.top = new FormAttachment(0, 0);
        fdSQLComp.right = new FormAttachment(100, 0);
        fdSQLComp.bottom = new FormAttachment(100, 0);
        wSQLComp.setLayoutData(fdSQLComp);

        wSQLComp.layout();
        wSQLTab.setControl(wSQLComp);
    }

    private void addClusterTab()
    {
        // ////////////////////////
        // START OF CLUSTER TAB///
        // /

        // The tab
        wClusterTab = new CTabItem(wTabFolder, SWT.NONE);
        wClusterTab.setText(Messages.getString("DatabaseDialog.ClusterTab.title")); //$NON-NLS-1$
        wClusterTab.setToolTipText(Messages.getString("DatabaseDialog.ClusterTab.tooltip")); //$NON-NLS-1$

        FormLayout clusterLayout = new FormLayout();
        clusterLayout.marginWidth = margin;
        clusterLayout.marginHeight = margin;

        // The composite
        wClusterComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wClusterComp);
        wClusterComp.setLayout(clusterLayout);

        // The check box
        wlUseCluster = new Label(wClusterComp, SWT.RIGHT);
        props.setLook(wlUseCluster);
        wlUseCluster.setText(Messages.getString("DatabaseDialog.label.UseClustering")); //$NON-NLS-1$
        wlUseCluster.setToolTipText(Messages.getString("DatabaseDialog.tooltip.UseClustering")); //$NON-NLS-1$
        FormData fdlUseCluster = new FormData();
        fdlUseCluster.left = new FormAttachment(0, 0);
        fdlUseCluster.right = new FormAttachment(middle, 0);
        fdlUseCluster.top = new FormAttachment(0, 0);
        wlUseCluster.setLayoutData(fdlUseCluster);

        wUseCluster = new Button(wClusterComp, SWT.CHECK);
        props.setLook(wUseCluster);
        FormData fdUseCluster = new FormData();
        fdUseCluster.left = new FormAttachment(middle, margin);
        fdUseCluster.right = new FormAttachment(100, 0);
        fdUseCluster.top = new FormAttachment(0, 0);
        wUseCluster.setLayoutData(fdUseCluster);
        wUseCluster.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                enableFields();
            }
        });

        // Cluster list
        ColumnInfo[] colinfo = new ColumnInfo[] {
                new ColumnInfo(Messages.getString("DatabaseDialog.column.PartitionId"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Hostname"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Port"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.DatabaseName"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Username"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("DatabaseDialog.column.Password"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //$NON-NLS-1$
        };

        colinfo[0].setToolTip(Messages.getString("DatabaseDialog.tooltip.PartitionId")); //$NON-NLS-1$
        colinfo[1].setToolTip(Messages.getString("DatabaseDialog.tooltip.Hostname")); //$NON-NLS-1$

        colinfo[5].setPasswordField(true);
        colinfo[5].setUsingVariables(true);

        wCluster = new TableView(wClusterComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, lsMod, props);
        props.setLook(wCluster);
        FormData fdCluster = new FormData();
        fdCluster.left = new FormAttachment(0, 0);
        fdCluster.right = new FormAttachment(100, 0);
        fdCluster.top = new FormAttachment(wUseCluster, margin);
        fdCluster.bottom = new FormAttachment(100, 0);
        wCluster.setLayoutData(fdCluster);

        FormData fdClusterComp = new FormData();
        fdClusterComp.left = new FormAttachment(0, 0);
        fdClusterComp.top = new FormAttachment(0, 0);
        fdClusterComp.right = new FormAttachment(100, 0);
        fdClusterComp.bottom = new FormAttachment(100, 0);
        wClusterComp.setLayoutData(fdClusterComp);

        wClusterComp.layout();
        wClusterTab.setControl(wClusterComp);
    }

    private void showOptionsHelpText()
    {
        DatabaseMeta meta = new DatabaseMeta();
        try
        {
            getInfo(meta);
            String helpText = meta.getExtraOptionsHelpText();
            if (Const.isEmpty(helpText)) return;

            // Try to open a new tab in the Spoon editor.
            // If spoon is not available, not in the classpath or can't open the tab, we show the URL in a dialog
            // 
            boolean openedTab = false;
            try
            {
                Spoon spoon = Spoon.getInstance();
                if (spoon != null)
                {
                    openedTab = spoon.addSpoonBrowser(Messages.getString("DatabaseDialog.JDBCOptions.Tab", meta.getDatabaseTypeDesc()), helpText);
                }
            }
            catch (Throwable t)
            {
            }
            if (!openedTab)
            {

                EnterTextDialog dialog = new EnterTextDialog(shell,
                        Messages.getString("DatabaseDialog.HelpText.title"), Messages.getString("DatabaseDialog.HelpText.description", meta.getDatabaseTypeDesc()), helpText, true); //$NON-NLS-1$ //$NON-NLS-2$
                dialog.setReadOnly();
                dialog.open();
            }
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell,
                    Messages.getString("DatabaseDialog.ErrorHelpText.title"), Messages.getString("DatabaseDialog.ErrorHelpText.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    public void setDatabases(ArrayList databases)
    {
        this.databases = databases;
    }

    public void getData()
    {
        wConn.setText(NVL(databaseMeta == null ? "" : databaseMeta.getName(), "")); //$NON-NLS-1$ //$NON-NLS-2$
        wConnType.select(databaseMeta.getDatabaseType() - 1);
        wConnType.showSelection();
        previousDatabaseType = DatabaseMeta.getDatabaseTypeCode(wConnType.getSelectionIndex() + 1);

        setAccessList();

        String accessList[] = wConnAcc.getItems();
        int accessIndex = Const.indexOfString(databaseMeta.getAccessTypeDesc(), accessList);
        wConnAcc.select(accessIndex);
        wConnAcc.showSelection();

        wHostName.setText(NVL(databaseMeta.getHostname(), "")); //$NON-NLS-1$
        wDBName.setText(NVL(databaseMeta.getDatabaseName(), "")); //$NON-NLS-1$
        wPort.setText(NVL(databaseMeta.getDatabasePortNumberString(), "")); //$NON-NLS-1$
        wServername.setText(NVL(databaseMeta.getServername(), "")); //$NON-NLS-1$
        wUsername.setText(NVL(databaseMeta.getUsername(), "")); //$NON-NLS-1$
        wPassword.setText(NVL(databaseMeta.getPassword(), "")); //$NON-NLS-1$
        wData.setText(NVL(databaseMeta.getDataTablespace(), "")); //$NON-NLS-1$
        wIndex.setText(NVL(databaseMeta.getIndexTablespace(), "")); //$NON-NLS-1$

        wSAPLanguage.setText(databaseMeta.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, "")); //$NON-NLS-1$
        wSAPSystemNumber.setText(databaseMeta.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, "")); //$NON-NLS-1$
        wSAPClient.setText(databaseMeta.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, "")); //$NON-NLS-1$

        wURL.setText(databaseMeta.getAttributes().getProperty(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, "")); //$NON-NLS-1$
        wDriverClass.setText(databaseMeta.getAttributes().getProperty(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, "")); //$NON-NLS-1$

        wStreamResult.setSelection( databaseMeta.isStreamingResults() );
        
        getOptionsData();
        checkPasswordVisible(wPassword.getTextWidget());

        wSQL.setText(NVL(databaseMeta.getConnectSQL(), "")); //$NON-NLS-1$

        getPoolingData();

        wConn.setFocus();
        wConn.selectAll();

        getClusterData();
    }

    private void getClusterData()
    {
        // The clustering information
        wUseCluster.setSelection(databaseMeta.isPartitioned());
        PartitionDatabaseMeta[] clusterInformation = databaseMeta.getPartitioningInformation();
        for (int i = 0; i < clusterInformation.length; i++)
        {
            PartitionDatabaseMeta meta = clusterInformation[i];
            TableItem tableItem = new TableItem(wCluster.table, SWT.NONE);
            tableItem.setText(1, Const.NVL(meta.getPartitionId(), "")); //$NON-NLS-1$
            tableItem.setText(2, Const.NVL(meta.getHostname(), "")); //$NON-NLS-1$
            tableItem.setText(3, Const.NVL(meta.getPort(), "")); //$NON-NLS-1$
            tableItem.setText(4, Const.NVL(meta.getDatabaseName(), "")); //$NON-NLS-1$
            tableItem.setText(5, Const.NVL(meta.getUsername(), "")); //$NON-NLS-1$
            tableItem.setText(5, Const.NVL(meta.getPassword(), "")); //$NON-NLS-1$
        }
        wCluster.removeEmptyRows();
        wCluster.setRowNums();
        wCluster.optWidth(true);
    }

    private void getOptionsData()
    {
        // The extra options as well...
        Iterator keys = extraOptions.keySet().iterator();
        while (keys.hasNext())
        {
            String parameter = (String) keys.next();
            String value = (String) extraOptions.get(parameter);
            if (!Const.isEmpty(value) && value.equals(DatabaseMeta.EMPTY_OPTIONS_STRING)) value = ""; //$NON-NLS-1$

            // If the paremeter starts with a database type code we add it...
            // 
            // For example MySQL.defaultFetchSize

            int dotIndex = parameter.indexOf("."); //$NON-NLS-1$
            if (dotIndex >= 0 && wConnType.getSelectionCount() == 1)
            {
                String databaseTypeString = parameter.substring(0, dotIndex);
                String parameterOption = parameter.substring(dotIndex + 1);
                int databaseType = DatabaseMeta.getDatabaseType(databaseTypeString);

                TableItem item = new TableItem(wOptions.table, SWT.NONE);
                item.setText(1, DatabaseMeta.getDatabaseTypeDesc(databaseType));
                item.setText(2, parameterOption);
                if (value != null) item.setText(3, value);
            }
        }
        wOptions.removeEmptyRows();
        wOptions.setRowNums();
        wOptions.optWidth(true);
    }

    private void getPoolingData()
    {
        // The extra options as well...
        Properties properties = databaseMeta.getConnectionPoolingProperties();
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext())
        {
            String parameter = (String) keys.next();
            String value = (String) properties.get(parameter);
            String defValue = DatabaseConnectionPoolParameter.findParameter(parameter, BaseDatabaseMeta.poolingParameters).getDefaultValue();
            TableItem item = new TableItem(wPoolParameters.table, SWT.NONE);

            item.setText(1, Const.NVL(parameter, ""));
            item.setText(2, Const.NVL(defValue, ""));
            item.setText(3, Const.NVL(value, ""));
        }
        wPoolParameters.removeEmptyRows();
        wPoolParameters.setRowNums();
        wPoolParameters.optWidth(true);
    }

    public void enableFields()
    {
        // See if we need to refresh the access list...
        String type = DatabaseMeta.getDatabaseTypeCode(wConnType.getSelectionIndex() + 1);
        if (!type.equalsIgnoreCase(previousDatabaseType)) setAccessList();
        previousDatabaseType = type;

        int idxAccType = wConnAcc.getSelectionIndex();
        int acctype = -1;
        if (idxAccType >= 0)
        {
            acctype = DatabaseMeta.getAccessType(wConnAcc.getItem(idxAccType));
        }

        // Hide the fields not relevent to JNDI data sources
        wlHostName.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wHostName.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wlPort.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wPort.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);

        wlURL.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wURL.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wlDriverClass.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wDriverClass.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);

        wlUsername.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wUsername.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wlPassword.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);
        wPassword.setEnabled(acctype != DatabaseMeta.TYPE_ACCESS_JNDI);

        if (acctype == DatabaseMeta.TYPE_ACCESS_JNDI) { return; }

        int idxDBType = wConnType.getSelectionIndex();
        if (idxDBType >= 0)
        {
            int dbtype = DatabaseMeta.getDatabaseType(wConnType.getItem(idxDBType));

            // If the type is not Informix: disable the servername field!
            wlServername.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_INFORMIX);
            wServername.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_INFORMIX);

            // If the type is not Mysql: disable the result streaming option.
            wlStreamResult.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_MYSQL);
            wStreamResult.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_MYSQL);

            // If this is an Oracle connection enable the Oracle tab
            wlData.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_ORACLE);
            wData.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_ORACLE);
            wlIndex.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_ORACLE);
            wIndex.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_ORACLE);

            wlSAPLanguage.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wSAPLanguage.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wlSAPSystemNumber.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wSAPSystemNumber.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wlSAPClient.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wSAPClient.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_SAPR3);
            wlDBName.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);
            wDBName.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);
            wlPort.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);
            wPort.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);
            wTest.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);
            wExp.setEnabled(dbtype != DatabaseMeta.TYPE_DATABASE_SAPR3);

            wlHostName.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE)
                    || (dbtype == DatabaseMeta.TYPE_ACCESS_JNDI));
            wHostName.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wlDBName.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wDBName.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wlPort.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wPort.setEnabled(!(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));

            wlURL.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wURL.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wlDriverClass.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wDriverClass.setEnabled(dbtype == DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
        }

        // The connection pooling options: do those as well...
        wlMaxPool.setEnabled(wUsePool.getSelection());
        wMaxPool.setEnabled(wUsePool.getSelection());
        wlInitPool.setEnabled(wUsePool.getSelection());
        wInitPool.setEnabled(wUsePool.getSelection());
        wlPoolParameters.setEnabled(wUsePool.getSelection());
        wPoolParameters.setEnabled(wUsePool.getSelection());
        wPoolParameters.table.setEnabled(wUsePool.getSelection());

        // How about the clustering stuff?
        wCluster.table.setEnabled(wUseCluster.getSelection());
    }

    public void setPortNumber()
    {
        String type = DatabaseMeta.getDatabaseTypeCode(wConnType.getSelectionIndex() + 1);

        // What port should we select?
        String acce = wConnAcc.getItem(wConnAcc.getSelectionIndex());
        int port = DatabaseMeta.getPortForDBType(type, acce);
        if (port < 0)
            wPort.setText(""); //$NON-NLS-1$
        else
            wPort.setText("" + port); //$NON-NLS-1$
    }

    public void setAccessList()
    {
        if (wConnType.getSelectionCount() < 1) return;

        int acc[] = DatabaseMeta.getAccessTypeList(wConnType.getSelection()[0]);
        wConnAcc.removeAll();
        for (int i = 0; i < acc.length; i++)
        {
            wConnAcc.add(DatabaseMeta.getAccessTypeDescLong(acc[i]));
        }
        // If nothing is selected: select the first item (mostly the native driver)
        if (wConnAcc.getSelectionIndex() < 0)
        {
            wConnAcc.select(0);
        }
    }

    private void cancel()
    {
        databaseName = null;
        databaseMeta.setChanged(changed);
        dispose();
    }

    public void getInfo(DatabaseMeta databaseMeta) throws KettleException
    {
        // Before we put all attributes back in, clear the old list to make sure...
        // Warning: the port is an attribute too now.
        // 
        databaseMeta.getAttributes().clear();

        // Name:
        databaseMeta.setName(wConn.getText());

        // Connection type:
        String contype[] = wConnType.getSelection();
        if (contype.length > 0)
        {
            databaseMeta.setDatabaseType(contype[0]);
        }

        // Access type:
        String acctype[] = wConnAcc.getSelection();
        if (acctype.length > 0)
        {
            databaseMeta.setAccessType(DatabaseMeta.getAccessType(acctype[0]));
        }

        // Hostname
        databaseMeta.setHostname(wHostName.getText());

        // Database name
        databaseMeta.setDBName(wDBName.getText());

        // Port number
        databaseMeta.setDBPort(wPort.getText());

        // Username
        databaseMeta.setUsername(wUsername.getText());

        // Password
        databaseMeta.setPassword(wPassword.getText());

        // Servername
        databaseMeta.setServername(wServername.getText());

        // MySQL
        databaseMeta.setStreamingResults(wStreamResult.getSelection());
        
        // Data tablespace
        databaseMeta.setDataTablespace(wData.getText());

        // Index tablespace
        databaseMeta.setIndexTablespace(wIndex.getText());

        // SAP Attributes...
        databaseMeta.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, wSAPLanguage.getText());
        databaseMeta.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, wSAPSystemNumber.getText());
        databaseMeta.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, wSAPClient.getText());

        // Generic settings...
        databaseMeta.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, wURL.getText());
        databaseMeta.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, wDriverClass.getText());

        String[] remarks = databaseMeta.checkParameters();
        if (remarks.length != 0)
        {
            String message = ""; //$NON-NLS-1$
            for (int i = 0; i < remarks.length; i++)
                message += "    * " + remarks[i] + Const.CR; //$NON-NLS-1$
            throw new KettleException(Messages.getString("DatabaseDialog.Exception.IncorrectParameter") + Const.CR + message); //$NON-NLS-1$
        }

        // Now put in the extra options...
        for (int i = 0; i < wOptions.nrNonEmpty(); i++)
        {
            TableItem item = wOptions.getNonEmpty(i);
            String dbTypeStr = item.getText(1);
            String parameter = item.getText(2);
            String value = item.getText(3);

            int dbType = DatabaseMeta.getDatabaseType(dbTypeStr);

            // Only if both parameters are supplied, we will add to the map...
            if (!Const.isEmpty(parameter) && dbType != DatabaseMeta.TYPE_DATABASE_NONE)
            {
                if (Const.isEmpty(value)) value = DatabaseMeta.EMPTY_OPTIONS_STRING;

                String typedParameter = BaseDatabaseMeta.ATTRIBUTE_PREFIX_EXTRA_OPTION + DatabaseMeta.getDatabaseTypeCode(dbType) + "." + parameter; //$NON-NLS-1$
                databaseMeta.getAttributes().put(typedParameter, value);
            }
        }

        // The SQL to execute...
        databaseMeta.setConnectSQL(wSQL.getText());

        // The connection pooling stuff...
        databaseMeta.setUsingConnectionPool(wUsePool.getSelection());
        databaseMeta.setInitialPoolSize(Const.toInt(wInitPool.getText(), ConnectionPoolUtil.defaultInitialNrOfConnections));
        databaseMeta.setMaximumPoolSize(Const.toInt(wMaxPool.getText(), ConnectionPoolUtil.defaultMaximumNrOfConnections));
        Properties poolProperties = new Properties();
        for (int i = 0; i < wPoolParameters.nrNonEmpty(); i++)
        {
            TableItem item = wPoolParameters.getNonEmpty(i);
            String parameterName = item.getText(1);
            String value = item.getText(3);
            if (!Const.isEmpty(parameterName) && !Const.isEmpty(value))
            {
                poolProperties.setProperty(parameterName, value);
            }
        }
        databaseMeta.setConnectionPoolingProperties(poolProperties);

        // Now grab the clustering information...
        databaseMeta.setPartitioned(wUseCluster.getSelection());
        PartitionDatabaseMeta[] clusterInfo = new PartitionDatabaseMeta[wCluster.nrNonEmpty()];
        for (int i = 0; i < clusterInfo.length; i++)
        {
            TableItem tableItem = wCluster.getNonEmpty(i);
            String partitionId = tableItem.getText(1);
            String hostname = tableItem.getText(2);
            String port = tableItem.getText(3);
            String dbName = tableItem.getText(4);
            String username = tableItem.getText(5);
            String password = tableItem.getText(6);
            clusterInfo[i] = new PartitionDatabaseMeta(partitionId, hostname, port, dbName);
            clusterInfo[i].setUsername(username);
            clusterInfo[i].setPassword(password);
        }
        databaseMeta.setPartitioningInformation(clusterInfo);
    }

    /**
     * @deprecated use ok() in stead, like in most dialogs
     */
    public void handleOK()
    {
        ok();
    }

    public void ok()
    {
        try
        {
            getInfo(databaseMeta);
            databaseName = databaseMeta.getName();
            databaseMeta.setID(database_id);
            dispose();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell,
                    Messages.getString("DatabaseDialog.ErrorParameters.title"), Messages.getString("DatabaseDialog.ErrorParameters.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public String NVL(String str, String rep)
    {
        if (str == null) return rep;
        return str;
    }

    public void test()
    {
        try
        {
            DatabaseMeta dbinfo = new DatabaseMeta();
            getInfo(dbinfo);
            test(shell, dbinfo);
        }
        catch (KettleException e)
        {
            new ErrorDialog(
                    shell,
                    Messages.getString("DatabaseDialog.ErrorParameters.title"), Messages.getString("DatabaseDialog.ErrorConnectionInfo.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Test the database connection
     */
    public static final void test(Shell shell, DatabaseMeta dbinfo)
    {
        String[] remarks = dbinfo.checkParameters();
        if (remarks.length == 0)
        {
            StringBuffer report = new StringBuffer();

            Database db = new Database(dbinfo);
            if (dbinfo.isPartitioned())
            {
                PartitionDatabaseMeta[] partitioningInformation = dbinfo.getPartitioningInformation();
                for (int i = 0; i < partitioningInformation.length; i++)
                {
                    try
                    {
                        db.connect(partitioningInformation[i].getPartitionId());
                        report
                                .append(Messages.getString(
                                        "DatabaseDialog.report.ConnectionWithPartOk", dbinfo.getName(), partitioningInformation[i].getPartitionId()) + Const.CR); //$NON-NLS-1$
                    }
                    catch (KettleException e)
                    {
                        report
                                .append(Messages
                                        .getString(
                                                "DatabaseDialog.report.ConnectionWithPartError", dbinfo.getName(), partitioningInformation[i].getPartitionId(), e.toString()) + Const.CR); //$NON-NLS-1$
                        report.append(Const.getStackTracker(e) + Const.CR);
                    }
                    finally
                    {
                        db.disconnect();
                    }
                    report.append(Messages.getString("DatabaseDialog.report.Hostname") + partitioningInformation[i].getHostname() + Const.CR); //$NON-NLS-1$
                    report.append(Messages.getString("DatabaseDialog.report.Port") + partitioningInformation[i].getPort() + Const.CR); //$NON-NLS-1$
                    report.append(Messages.getString("DatabaseDialog.report.DatabaseName") + partitioningInformation[i].getDatabaseName() + Const.CR); //$NON-NLS-1$
                    report.append(Const.CR);
                }
            }
            else
            {
                try
                {
                    db.connect();
                    report.append(Messages.getString("DatabaseDialog.report.ConnectionOk", dbinfo.getName()) + Const.CR); //$NON-NLS-1$
                }
                catch (KettleException e)
                {
                    report.append(Messages.getString("DatabaseDialog.report.ConnectionError", dbinfo.getName()) + e.toString() + Const.CR); //$NON-NLS-1$
                    report.append(Const.getStackTracker(e) + Const.CR);
                }
                finally
                {
                    db.disconnect();
                }
                report.append(Messages.getString("DatabaseDialog.report.Hostname") + dbinfo.getHostname() + Const.CR); //$NON-NLS-1$
                report.append(Messages.getString("DatabaseDialog.report.Port") + dbinfo.getDatabasePortNumberString() + Const.CR); //$NON-NLS-1$
                report.append(Messages.getString("DatabaseDialog.report.DatabaseName") + dbinfo.getDatabaseName() + Const.CR); //$NON-NLS-1$
                report.append(Const.CR);
            }

            EnterTextDialog dialog = new EnterTextDialog(
                    shell,
                    Messages.getString("DatabaseDialog.ConnectionReport.title"), Messages.getString("DatabaseDialog.ConnectionReport.description"), report.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.setReadOnly();
            dialog.setFixed(true);
            dialog.open();
        }
        else
        {
            String message = ""; //$NON-NLS-1$
            for (int i = 0; i < remarks.length; i++)
                message += "    * " + remarks[i] + Const.CR; //$NON-NLS-1$

            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(Messages.getString("DatabaseDialog.ErrorParameters2.title")); //$NON-NLS-1$
            mb.setMessage(Messages.getString("DatabaseDialog.ErrorParameters2.description", message)); //$NON-NLS-1$
            mb.open();
        }
    }

    public void explore()
    {
        DatabaseMeta dbinfo = new DatabaseMeta();
        try
        {
            getInfo(dbinfo);
            DatabaseExplorerDialog ded = new DatabaseExplorerDialog(shell, SWT.NONE, dbinfo, databases, true);
            ded.open();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell,
                    Messages.getString("DatabaseDialog.ErrorParameters,title"), Messages.getString("DatabaseDialog.ErrorParameters.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void showFeatureList()
    {
        DatabaseMeta dbinfo = new DatabaseMeta();
        try
        {
            getInfo(dbinfo);
            ArrayList buffer = (ArrayList) dbinfo.getFeatureSummary();
            PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, Messages.getString("DatabaseDialog.FeatureList.title"), buffer); //$NON-NLS-1$
            prd.setTitleMessage(Messages.getString("DatabaseDialog.FeatureList.title"), Messages.getString("DatabaseDialog.FeatureList.title2")); //$NON-NLS-1$ //$NON-NLS-2$
            prd.open();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell,
                    Messages.getString("DatabaseDialog.FeatureListError.title"), Messages.getString("DatabaseDialog.FeatureListError.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }
}
