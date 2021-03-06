/*
 *
 *
 */

package be.ibridge.kettle.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a transformation...
 * 
 * @author Matt
 * @since  15-mrt-2005
 */
public class GetSQLProgressDialog
{
	private Shell shell;
	private TransMeta transMeta;
	private ArrayList stats;

    /**
     * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
     * @deprecated please use the constructor version without log or props
     */
    public GetSQLProgressDialog(LogWriter log, Props props, Shell shell, TransMeta transMeta)
    {
        this(shell, transMeta);
    }
    
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
	 */
	public GetSQLProgressDialog(Shell shell, TransMeta transMeta)
	{
		this.shell = shell;
		this.transMeta = transMeta;
	}
	
	public ArrayList open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), parentThread, true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true, op);

				try
				{
					stats = transMeta.getSQLStatements(monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, Messages.getString("GetSQLProgressDialog.RuntimeError.UnableToGenerateSQL.Exception", e.getMessage())); //Error generating SQL for transformation: \n{0}
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(false, false, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, Messages.getString("GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for transformation","An error occured generating the SQL for this transformation\!"
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Messages.getString("GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for transformation","An error occured generating the SQL for this transformation\!"
			stats = null;
		}

		return stats;
	}
}
