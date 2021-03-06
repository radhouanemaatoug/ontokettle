package be.ibridge.kettle.trans.step.abort;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * Step that will abort after having seen 'x' number of rows on its input.
 * 
 * @author Sven Boden
 */
public class Abort extends BaseStep implements StepInterface {

    private AbortMeta meta;
    private AbortData data;
    private int nrInputRows;
    private int nrThresholdRows;
    
    public Abort(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			nrInputRows = 0;
			String threshold = StringUtil.environmentSubstitute(meta.getRowThreshold());
			nrThresholdRows = Const.toInt(threshold, -1);
			if ( nrThresholdRows < 0 )
			{
			    logError(Messages.getString("Abort.Log.ThresholdInvalid", threshold));
			}
			
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta=(AbortMeta)smi;
		data=(AbortData)sdi;
		
        Row r=getRow();       // Get row from input rowset & set row busy!
        if (r==null)          // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        else
        {
        	putRow(r);
        	nrInputRows++;
        	if ( nrInputRows > nrThresholdRows)
        	{
        	   //
        	   // Here we abort!!
        	   //
        	   logMinimal(Messages.getString("Abort.Log.Wrote.AbortRow", Long.toString(nrInputRows), r.toString()) );
        		
        	   String message = StringUtil.environmentSubstitute(meta.getMessage());
        	   if ( message == null || message.length() == 0 )
        	   {
        		   logMinimal(Messages.getString("Abort.Log.DefaultAbortMessage", "" + nrInputRows));
        	   }
        	   else
        	   {
        		   logMinimal(message);
        	   }
               setErrors(1);
               stopAll();        	   
        	}
        	else 
        	{
        		// seen a row but not yet reached the threshold
        		if ( meta.isAlwaysLogRows() )
        		{
        			logMinimal(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), r.toString()) );
        		}
        		else
        		{
        	        if (log.isRowLevel())
        	        {        	        	
        	            logRowlevel(Messages.getString("Abort.Log.Wrote.Row", Long.toString(nrInputRows), r.toString()) );
        	        }
        		}
        	}
        }
        
        return true;
    }

    //
    // Run is were the action happens!
    public void run()
    {
        try
        {
        	logBasic(Messages.getString("Abort.Log.StartingToRun")); //$NON-NLS-1$ 
            while (processRow(meta, data) && !isStopped());
        }
        catch(Exception e)
        {
        	logError(Messages.getString("Abort.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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