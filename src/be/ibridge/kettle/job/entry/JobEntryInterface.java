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
 
package be.ibridge.kettle.job.entry;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.createfile.JobEntryCreateFile;
import be.ibridge.kettle.job.entry.deletefile.JobEntryDeleteFile;
import be.ibridge.kettle.job.entry.eval.JobEntryEval;
import be.ibridge.kettle.job.entry.filecompare.JobEntryFileCompare;
import be.ibridge.kettle.job.entry.fileexists.JobEntryFileExists;
import be.ibridge.kettle.job.entry.ftp.JobEntryFTP;
import be.ibridge.kettle.job.entry.http.JobEntryHTTP;
import be.ibridge.kettle.job.entry.job.JobEntryJob;
import be.ibridge.kettle.job.entry.mail.JobEntryMail;
import be.ibridge.kettle.job.entry.mysqlbulkload.JobEntryMysqlBulkLoad;
import be.ibridge.kettle.job.entry.mysqlbulkfile.JobEntryMysqlBulkFile;
import be.ibridge.kettle.job.entry.msgboxinfo.JobEntryMsgBoxInfo;
import be.ibridge.kettle.job.entry.delay.JobEntryDelay;
import be.ibridge.kettle.job.entry.zipfile.JobEntryZipFile;
import be.ibridge.kettle.job.entry.xslt.JobEntryXSLT;
import be.ibridge.kettle.job.entry.getpop.JobEntryGetPOP;
import be.ibridge.kettle.job.entry.sftp.JobEntrySFTP;
import be.ibridge.kettle.job.entry.sftpput.JobEntrySFTPPUT;
import be.ibridge.kettle.job.entry.shell.JobEntryShell;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.job.entry.sql.JobEntrySQL;
import be.ibridge.kettle.job.entry.tableexists.JobEntryTableExists;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.job.entry.waitforfile.JobEntryWaitForFile;
import be.ibridge.kettle.job.entry.abort.JobEntryAbort;
import be.ibridge.kettle.repository.Repository;


/**
 * Interface for the different JobEntry classes.
 * 
 * @author Matt
 * @since 18-06-04
 * 
 */

public interface JobEntryInterface
{
	public final static int TYPE_JOBENTRY_NONE           =  0;
	public final static int TYPE_JOBENTRY_TRANSFORMATION =  1;
	public final static int TYPE_JOBENTRY_JOB            =  2;
	public final static int TYPE_JOBENTRY_SHELL          =  3;
	public final static int TYPE_JOBENTRY_MAIL           =  4;
	public final static int TYPE_JOBENTRY_SQL            =  5;
	public final static int TYPE_JOBENTRY_FTP            =  6;
	public final static int TYPE_JOBENTRY_TABLE_EXISTS   =  7;
	public final static int TYPE_JOBENTRY_FILE_EXISTS    =  8;
	public final static int TYPE_JOBENTRY_EVALUATION     =  9;
	public final static int TYPE_JOBENTRY_SPECIAL        = 10;
    public static final int TYPE_JOBENTRY_SFTP           = 11;
    public static final int TYPE_JOBENTRY_HTTP           = 12;
    public static final int TYPE_JOBENTRY_CREATE_FILE    = 13;
    public static final int TYPE_JOBENTRY_DELETE_FILE    = 14;
    public static final int TYPE_JOBENTRY_WAIT_FOR_FILE  = 15;
    public static final int TYPE_JOBENTRY_SFTPPUT        = 16;
    public static final int TYPE_JOBENTRY_FILE_COMPARE   = 17;
    public static final int TYPE_JOBENTRY_MYSQL_BULK_LOAD= 18;
	public static final int TYPE_JOBENTRY_MSGBOX_INFO= 19;
	public static final int TYPE_JOBENTRY_DELAY= 20;
	public static final int TYPE_JOBENTRY_ZIP_FILE= 21;
	public static final int TYPE_JOBENTRY_XSLT= 22;
	public static final int TYPE_JOBENTRY_MYSQL_BULK_FILE= 23;
    public static final int TYPE_JOBENTRY_ABORT= 24;
	public static final int TYPE_JOBENTRY_GET_POP= 25;

	public final static String typeCode[] =
		{
			"-",
            "TRANS",
            "JOB",
            "SHELL",
            "MAIL",
            "SQL",
            "FTP",
            "TABLE_EXISTS",
            "FILE_EXISTS",
            "EVAL",
            "SPECIAL",
            "SFTP",
            "HTTP",
            "CREATE_FILE",
            "DELETE_FILE",
            "WAIT_FOR_FILE",
            "SFTPPUT",
            "FILE_COMPARE",
            "MYSQL_BULK_LOAD",
			"MSGBOX_INFO",
			"DELAY",
			"ZIP_FILE",
			"XSLT",
			"MYSQL_BULK_FILE",
			"ABORT",
			"GET_POP",
		};

	public final static String typeDesc[] =
		{
			"-",
            Messages.getString("JobEntry.Trans.TypeDesc"),
            Messages.getString("JobEntry.Job.TypeDesc"),
            Messages.getString("JobEntry.Shell.TypeDesc"),
            Messages.getString("JobEntry.Mail.TypeDesc"),
            Messages.getString("JobEntry.SQL.TypeDesc"),
            Messages.getString("JobEntry.FTP.TypeDesc"),
            Messages.getString("JobEntry.TableExists.TypeDesc"),
            Messages.getString("JobEntry.FileExists.TypeDesc"),
            Messages.getString("JobEntry.Evaluation.TypeDesc"),
            Messages.getString("JobEntry.Special.TypeDesc"),
            Messages.getString("JobEntry.SFTP.TypeDesc"),
            Messages.getString("JobEntry.HTTP.TypeDesc"),
            Messages.getString("JobEntry.CreateFile.TypeDesc"),
            Messages.getString("JobEntry.DeleteFile.TypeDesc"),
            Messages.getString("JobEntry.WaitForFile.TypeDesc"),
            Messages.getString("JobEntry.SFTPPut.TypeDesc"),            
            Messages.getString("JobEntry.FileCompare.TypeDesc"),
            Messages.getString("JobEntry.MysqlBulkLoad.TypeDesc"),
			Messages.getString("JobEntry.MsgBoxInfo.TypeDesc"),
			Messages.getString("JobEntry.Delay.TypeDesc"),
			Messages.getString("JobEntry.ZipFile.TypeDesc"),
			Messages.getString("JobEntry.XSLT.TypeDesc"),
			Messages.getString("JobEntry.MysqlBulkFile.TypeDesc"),
			Messages.getString("JobEntry.Abort.TypeDesc"),
			Messages.getString("JobEntry.GetPOP.TypeDesc"),
		};

	public final static String icon_filename[] = 
		{
		 	"",
			"TRN.png",
			"JOB.png",
		    "SHL.png",
			"MAIL.png",
			"SQL.png",
			"FTP.png",
			"TEX.png",
			"FEX.png",
			"RES.png",
            "",
            "SFT.png",
            "WEB.png",
            "CFJ.png",
            "DFJ.png",
            "WFF.png",           
            "SFP.png",                        
            "BFC.png",
            "MBL.png",
			"INF.png",
			"DLT.png",
			"ZIP.png",
			"XSLT.png",
			"MBF.png",
			"ABR.png",
			"GETPOP.png",
		};
	
	public final static String type_tooltip_desc[] = 
		{
			"", 
            Messages.getString("JobEntry.Trans.Tooltip"),
            Messages.getString("JobEntry.Job.Tooltip"),
            Messages.getString("JobEntry.Shell.Tooltip"),
            Messages.getString("JobEntry.Mail.Tooltip"),
            Messages.getString("JobEntry.SQL.Tooltip"),
            Messages.getString("JobEntry.FTP.Tooltip"),
            Messages.getString("JobEntry.TableExists.Tooltip"),
            Messages.getString("JobEntry.FileExists.Tooltip"),
            Messages.getString("JobEntry.Evaluation.Tooltip"),
            Messages.getString("JobEntry.Special.Tooltip"),
            Messages.getString("JobEntry.SFTP.Tooltip"),
            Messages.getString("JobEntry.HTTP.Tooltip"),
            Messages.getString("JobEntry.CreateFile.Tooltip"),
            Messages.getString("JobEntry.DeleteFile.Tooltip"),
            Messages.getString("JobEntry.WaitForFile.Tooltip"),
            Messages.getString("JobEntry.SFTPPut.Tooltip"),
            Messages.getString("JobEntry.FileCompare.Tooltip"),
            Messages.getString("JobEntry.MysqlBulkLoad.Tooltip"),
		    Messages.getString("JobEntry.MsgBoxInfo.Tooltip"),
			Messages.getString("JobEntry.Delay.Tooltip"),
			Messages.getString("JobEntry.ZipFile.Tooltip"),
			Messages.getString("JobEntry.XSLT.Tooltip"),
			Messages.getString("JobEntry.MysqlBulkFile.Tooltip"),
			Messages.getString("JobEntry.Abort.Tooltip"),
			Messages.getString("JobEntry.GetPOP.Tooltip"),
 		};
	
	public final static Class type_classname[] = 
		{
	        null,
	        JobEntryTrans.class,
	        JobEntryJob.class,
	        JobEntryShell.class,
	        JobEntryMail.class,
	        JobEntrySQL.class,
	        JobEntryFTP.class,
	        JobEntryTableExists.class,
	        JobEntryFileExists.class,
	        JobEntryEval.class,
	        JobEntrySpecial.class,
            JobEntrySFTP.class,
            JobEntryHTTP.class,
            JobEntryCreateFile.class,
            JobEntryDeleteFile.class,
            JobEntryWaitForFile.class,
            JobEntrySFTPPUT.class,
            JobEntryFileCompare.class,
            JobEntryMysqlBulkLoad.class,
			JobEntryMsgBoxInfo.class,
			JobEntryDelay.class,
			JobEntryZipFile.class,
			JobEntryXSLT.class,
			JobEntryMysqlBulkFile.class,
			JobEntryAbort.class,
			JobEntryGetPOP.class,
		};

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleException;
	
	public void    clear();
	public long    getID();
	public void    setID(long id);
	public String  getName();
	public void    setName(String name);
	public String  getDescription();
	public void    setDescription(String description);
	public void    setChanged();
	public void    setChanged(boolean ch);
	public boolean hasChanged();

	public void    loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException;
	public String  getXML();
	public void    loadRep(Repository rep, long id_jobentry, ArrayList databases) throws KettleException;
	public void    saveRep(Repository rep, long id_job) throws KettleException;
	
	public int     getType();
	public String  getTypeCode();
    public String  getPluginID();


	public boolean isStart();
	public boolean isDummy();
	public Object  clone();
	
	public boolean resetErrorsBeforeExecution();
	public boolean evaluates();
	public boolean isUnconditional();
	
	public boolean isEvaluation();
	public boolean isTransformation();
	public boolean isJob();
	public boolean isShell();
	public boolean isMail();
	public boolean isSpecial();
    
    public ArrayList getSQLStatements(Repository repository) throws KettleException;
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep);
    
    public String getFilename();
    public String getRealFilename();
    
    /**
     * This method returns all the database connections that are used by the job entry.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections();

    public void setPluginID(String id);
}
