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
 
package be.ibridge.kettle.trans.step.filesfromresult;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Reads results from a previous transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class FilesFromResult extends BaseStep implements StepInterface
{
	private FilesFromResultMeta meta;
	private FilesFromResultData data;
	
	public FilesFromResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(FilesFromResultMeta)getStepMeta().getStepMetaInterface();
		data=(FilesFromResultData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        debug = "Check if there are result files and if we're done processing";
		if (data.result==null ||
		    data.result.getResultFiles()==null ||
			linesRead>=data.result.getResultFiles().size())
		{
			setOutputDone();
			return false;
		}
		
		
        debug = "Get a row from the result files ("+(linesRead+1)+"/"+data.result.getResultFiles().size()+")";
        ResultFile resultFile = (ResultFile) data.result.getResultFiles().get((int)linesRead);
        Row r = resultFile.getRow();
        
        linesRead++;
		
        debug = "Put the row";
		putRow(r);     // copy row to possible alternate rowset(s).

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("RowsFromResult.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FilesFromResultMeta)smi;
		data=(FilesFromResultData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.result = getTransMeta().getPreviousResult();
			
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("RowsFromResult.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("RowsFromResult.Log.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}