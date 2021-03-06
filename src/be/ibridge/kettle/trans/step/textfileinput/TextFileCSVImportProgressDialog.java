/*
 *
 *
 */

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out what tables, views etc we can
 * reach in the database.
 * 
 * @author Matt
 * @since 07-apr-2005
 */
public class TextFileCSVImportProgressDialog
{
    private Shell             shell;

    private TextFileInputMeta meta;

    private int               samples;
    
    private int               clearFields;
    
    private String            message;

    private String            debug;
    
    private long              rownumber;

    private InputStreamReader reader;
    
    private Thread parentThread;  

    /**
     * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
     * database.
     * @deprecated please use the constructor version without log or props
     */
    public TextFileCSVImportProgressDialog( LogWriter log, 
                                            Props props, 
                                            Shell shell, 
                                            TextFileInputMeta meta, 
                                            InputStreamReader reader, 
                                            int samples, 
                                            int clearFields
                                          )
    {
        this(shell, meta, reader, samples, clearFields);
    }
    
    /**
     * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
     * database.
     */
    public TextFileCSVImportProgressDialog( Shell shell, TextFileInputMeta meta, InputStreamReader reader, int samples, int clearFields )
    {
        this.shell = shell;
        this.meta = meta;
        this.reader = reader;
        this.samples       = samples;
        this.clearFields   = clearFields;

        message = null;
        debug = "init";
        rownumber = 1L;
        
        this.parentThread = Thread.currentThread();
    }

    public String open()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

                try
                {
                    message = doScan(monitor);
                }
                catch (Exception e)
                {
                    throw new InvocationTargetException(e, Messages.getString("TextFileCSVImportProgressDialog.Exception.ErrorScanningFile", ""+rownumber, debug, e.toString()));
                }
            }
        };

        try
        {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, Messages.getString("TextFileCSVImportProgressDialog.ErrorScanningFile.Title"), Messages.getString("TextFileCSVImportProgressDialog.ErrorScanningFile.Message"), e);
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, Messages.getString("TextFileCSVImportProgressDialog.ErrorScanningFile.Title"), Messages.getString("TextFileCSVImportProgressDialog.ErrorScanningFile.Message"), e);
        }
  
        return message;
    }

    private String doScan(IProgressMonitor monitor) throws KettleException
    {
        if (samples>0) monitor.beginTask(Messages.getString("TextFileCSVImportProgressDialog.Task.ScanningFile"), samples+1);
        else           monitor.beginTask(Messages.getString("TextFileCSVImportProgressDialog.Task.ScanningFile"), 2);
        LogWriter log = LogWriter.getInstance();

        String line = "";
        long fileLineNumber = 0;
        
        NumberFormat nf = NumberFormat.getInstance();
        DecimalFormat df = (DecimalFormat)nf;
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        SimpleDateFormat daf  = new SimpleDateFormat();
        daf.setLenient(false);
        DateFormatSymbols dafs = new DateFormatSymbols();

        int nrfields = meta.getInputFields().length;

        // How many null values?
        int nrnull[] = new int[nrfields]; // How many times null value?

        // String info
        String minstr[] = new String[nrfields]; // min string
        String maxstr[] = new String[nrfields]; // max string
        boolean firststr[] = new boolean[nrfields]; // first occ. of string?

        // Date info
        boolean isDate[] = new boolean[nrfields]; // is the field perhaps a Date?
        int dateFormatCount[] = new int[nrfields]; // How many date formats work?
        boolean dateFormat[][] = new boolean[nrfields][Const.getDateFormats().length]; // What are the date formats that
        // work?
        Date minDate[][] = new Date[nrfields][Const.getDateFormats().length]; // min date value
        Date maxDate[][] = new Date[nrfields][Const.getDateFormats().length]; // max date value

        // Number info
        boolean isNumber[] = new boolean[nrfields]; // is the field perhaps a Number?
        int numberFormatCount[] = new int[nrfields]; // How many number formats work?
        boolean numberFormat[][] = new boolean[nrfields][Const.getNumberFormats().length]; // What are the number format that work?
        double minValue[][] = new double[nrfields][Const.getDateFormats().length]; // min number value
        double maxValue[][] = new double[nrfields][Const.getDateFormats().length]; // max number value
        int numberPrecision[][] = new int[nrfields][Const.getNumberFormats().length]; // remember the precision?
        int numberLength[][] = new int[nrfields][Const.getNumberFormats().length]; // remember the length?

        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            if (log.isDebug()) debug = "init field #" + i;
          
            if (clearFields == SWT.YES) // Clear previous info...
            {
                field.setName(meta.getInputFields()[i].getName());
                field.setType(meta.getInputFields()[i].getType());
                field.setFormat("");
                field.setLength(-1);
                field.setPrecision(-1);
                field.setCurrencySymbol(dfs.getCurrencySymbol());
                field.setDecimalSymbol("" + dfs.getDecimalSeparator());
                field.setGroupSymbol("" + dfs.getGroupingSeparator());
                field.setNullString("-");
                field.setTrimType(TextFileInputMeta.TYPE_TRIM_NONE);
            }

            nrnull[i] = 0;
            minstr[i] = "";
            maxstr[i] = "";
            firststr[i] = true;

            // Init data guess
            isDate[i] = true;
            for (int j = 0; j < Const.getDateFormats().length; j++)
            {
                dateFormat[i][j] = true;
                minDate[i][j] = Const.MAX_DATE;
                maxDate[i][j] = Const.MIN_DATE;
            }
            dateFormatCount[i] = Const.getDateFormats().length;

            // Init number guess
            isNumber[i] = true;
            for (int j = 0; j < Const.getNumberFormats().length; j++)
            {
                numberFormat[i][j] = true;
                minValue[i][j] = Double.MAX_VALUE;
                maxValue[i][j] = -Double.MAX_VALUE;
                numberPrecision[i][j] = -1;
                numberLength[i][j] = -1;
            }
            numberFormatCount[i] = Const.getNumberFormats().length;
        }

        TextFileInputMeta strinfo = (TextFileInputMeta) meta.clone();
        for (int i = 0; i < nrfields; i++)
            strinfo.getInputFields()[i].setType(Value.VALUE_TYPE_STRING);

        // Sample <samples> rows...
        debug = "get first line";

        // If the file has a header we overwrite the first line
        // However, if it doesn't have a header, take a new line
        if (meta.hasHeader()) 
        {
            line = TextFileInput.getLine(log, reader, meta.getFileFormat());
            fileLineNumber++;
            int skipped=1;
            while (line!=null && skipped<meta.getNrHeaderLines())
            {
                line = TextFileInput.getLine(log, reader, meta.getFileFormat());
                skipped++;
                fileLineNumber++;
            }
        }
        int linenr = 1;

        // Allocate number and date parsers
        DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();
        SimpleDateFormat daf2 = new SimpleDateFormat();

        boolean errorFound = false;
        while (!errorFound && line != null && (linenr <= samples || samples == 0) && !monitor.isCanceled())
        {
            monitor.subTask(Messages.getString("TextFileCSVImportProgressDialog.Task.ScanningLine", ""+linenr));
            if (samples>0) monitor.worked(1);
            
            if (log.isDebug()) debug = "convert line #" + linenr + " to row";
            Row r = TextFileInput.convertLineToRow(log, new TextFileLine(line, fileLineNumber, null), strinfo, df, dfs, daf, dafs, meta.getFilePaths()[0], rownumber);

            rownumber++;
            for (int i = 0; i < nrfields && i < r.size(); i++)
            {
                TextFileInputField field = meta.getInputFields()[i];

                if (log.isDebug()) debug = "Start of for loop, get new value " + i;
                Value v = r.getValue(i);
                if (log.isDebug()) debug = "Start of for loop over " + r.size() + " elements in Row r, now at #" + i + " containing value : [" + v.toString() + "]";
                if (!v.isNull() && v.getString() != null)
                {
                    String fieldValue = v.getString();

                    int trimthis = TextFileInputMeta.TYPE_TRIM_NONE;

                    boolean spacesBefore = Const.nrSpacesBefore(fieldValue) > 0;
                    boolean spacesAfter = Const.nrSpacesAfter(fieldValue) > 0;

                    fieldValue = Const.trim(fieldValue);

                    if (spacesBefore) trimthis |= TextFileInputMeta.TYPE_TRIM_LEFT;
                    if (spacesAfter) trimthis |= TextFileInputMeta.TYPE_TRIM_RIGHT;

                    if (log.isDebug()) debug = "change trim type[" + i + "]";
                    field.setTrimType(field.getTrimType() | trimthis);

                    if (log.isDebug()) debug = "Field #" + i + " has type : " + Value.getTypeDesc(field.getType());

                    // See if the field has only numeric fields
                    if (isNumber[i])
                    {
                        if (log.isDebug()) debug = "Number checking of [" + fieldValue + "] on line #" + linenr;

                        boolean containsDot = false;
                        boolean containsComma = false;

                        for (int x = 0; x < fieldValue.length() && field.getType() == Value.VALUE_TYPE_NUMBER; x++)
                        {
                            char ch = fieldValue.charAt(x);
                            if (!Character.isDigit(ch) && ch != '.' && ch != ',' && (ch != '-' || x > 0) && ch != 'E' && ch != 'e' // exponential
                            )
                            {
                                isNumber[i] = false;
                            } else
                            {
                                if (ch == '.') containsDot = true;
                                if (ch == ',') containsComma = true;
                            }
                        }
                        // If it's still a number, try to parse it as a double
                        if (isNumber[i])
                        {
                            if (containsDot && !containsComma) // american 174.5
                            {
                                dfs2.setDecimalSeparator('.');
                                field.setDecimalSymbol(".");
                                dfs2.setGroupingSeparator(',');
                                field.setGroupSymbol(",");
                            } else
                                if (!containsDot && containsComma) // Belgian 174,5
                                {
                                    dfs2.setDecimalSeparator(',');
                                    field.setDecimalSymbol(",");
                                    dfs2.setGroupingSeparator('.');
                                    field.setGroupSymbol(".");
                                } else
                                    if (containsDot && containsComma) // Both appear!
                                    {
                                        // What's the last occurance: decimal point!
                                        int indexDot = fieldValue.indexOf(".");
                                        int indexComma = fieldValue.indexOf(",");
                                        if (indexDot > indexComma)
                                        {
                                            dfs2.setDecimalSeparator('.');
                                            field.setDecimalSymbol(".");
                                            dfs2.setGroupingSeparator(',');
                                            field.setGroupSymbol(",");
                                        } else
                                        {
                                            dfs2.setDecimalSeparator(',');
                                            field.setDecimalSymbol(",");
                                            dfs2.setGroupingSeparator('.');
                                            field.setGroupSymbol(".");
                                        }
                                    }

                            // Try the remaining possible number formats!
                            for (int x = 0; x < Const.getNumberFormats().length; x++)
                            {
                                if (numberFormat[i][x])
                                {
                                    try
                                    {
                                        df2.setDecimalFormatSymbols(dfs2);
                                        df2.applyPattern(Const.getNumberFormats()[x]);
                                        double d = df2.parse(fieldValue).doubleValue();

                                        // System.out.println("("+i+","+x+") : Converted ["+field.toString()+"]
                                        // to ["+d+"] with format ["+numberFormats[x]+"] and dfs2
                                        // ["+dfs2.getDecimalSeparator()+dfs2.getGroupingSeparator()+"]");

                                        // After everything, still a number?
                                        // Then guess the precision
                                        int prec = TextFileInputDialog.guessPrecision(d);
                                        if (prec > numberPrecision[i][x]) numberPrecision[i][x] = prec;

                                        int leng = TextFileInputDialog.guessLength(d) + prec; // add precision!
                                        if (leng > numberLength[i][x]) numberLength[i][x] = leng;

                                        if (d < minValue[i][x]) minValue[i][x] = d;
                                        if (d > maxValue[i][x]) maxValue[i][x] = d;
                                    }
                                    catch (Exception e)
                                    {
                                        numberFormat[i][x] = false; // Don't try it again in the future.
                                        numberFormatCount[i]--; // One less that works..
                                    }
                                }
                            }

                            // Still not found: just a string
                            if (numberFormatCount[i] == 0)
                            {
                                isNumber[i] = false;
                            }
                        }
                    }

                    if (log.isDebug()) debug = "Check max length on field #" + i + " called " + field.getName() + " : [" + fieldValue + "]";
                    // Capture the maximum length of the field (trimmed)
                    if (fieldValue.length() > field.getLength()) field.setLength(fieldValue.length());

                    // So is it really a string or a date field?
                    // Check it as long as we found a format that works...
                    if (isDate[i])
                    {
                        for (int x = 0; x < Const.getDateFormats().length; x++)
                        {
                            if (dateFormat[i][x])
                            {
                                try
                                {
                                    daf2.applyPattern(Const.getDateFormats()[x]);
                                    Date date = daf2.parse(fieldValue);

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int year = cal.get(Calendar.YEAR);

                                    if (year < 1800 || year > 2200)
                                    {
                                        dateFormat[i][x] = false; // Don't try it again in the future.
                                        dateFormatCount[i]--; // One less that works..
                                        // System.out.println("Field #"+i+", pattern ["+dateFormats[x]+"],
                                        // year="+year+", field=["+field+"] : year<1800 or year>2200!! not a
                                        // date!");
                                    }

                                    if (minDate[i][x].compareTo(date) > 0) minDate[i][x] = date;
                                    if (maxDate[i][x].compareTo(date) < 0) maxDate[i][x] = date;
                                }
                                catch (Exception e)
                                {
                                    dateFormat[i][x] = false; // Don't try it again in the future.
                                    dateFormatCount[i]--; // One less that works..
                                    // System.out.println("field ["+field+"] is not a date,
                                    // format=["+dateFormats[x]+", x="+x+", error: ("+e.toString()+")");
                                }
                            }
                        }

                        // Still not found: just a string
                        if (dateFormatCount[i] == 0)
                        {
                            isDate[i] = false;
                            // System.out.println("Field #"+i+" is not a date!");
                        }
                    }

                    // Determine maximum & minimum string values...
                    if (firststr[i])
                    {
                        firststr[i] = false;
                        minstr[i] = fieldValue;
                        maxstr[i] = fieldValue;
                    }
                    if (minstr[i].compareTo(fieldValue) > 0) minstr[i] = fieldValue;
                    if (maxstr[i].compareTo(fieldValue) < 0) maxstr[i] = fieldValue;

                    debug = "End of for loop";
                } else
                {
                    nrnull[i]++;
                }
            }

            fileLineNumber++;
            if (!r.isIgnored())
                linenr++;
            else
                rownumber--;

            // Grab another line...
            debug = "Grab another line";
            line = TextFileInput.getLine(log, reader, meta.getFileFormat());
            debug = "End of while loop";
        }

        monitor.worked(1);
        monitor.setTaskName(Messages.getString("TextFileCSVImportProgressDialog.Task.AnalyzingResults"));
        
        // Include the results from the number, date & string search!
        // some cleanup of format fields for strings...
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            if (field.getType() == Value.VALUE_TYPE_STRING)
            {
                if (isDate[i])
                {
                    field.setType(Value.VALUE_TYPE_DATE);
                    for (int x = Const.getDateFormats().length - 1; x >= 0; x--)
                    {
                        if (dateFormat[i][x])
                        {
                            field.setFormat(Const.getDateFormats()[x]);
                            field.setLength(TextFileInputDialog.dateLengths[x]);
                            field.setPrecision(-1);
                        }
                    }
                } else
                    if (isNumber[i])
                    {
                        field.setType(Value.VALUE_TYPE_NUMBER);
                        for (int x = Const.getNumberFormats().length - 1; x >= 0; x--)
                        {
                            if (numberFormat[i][x])
                            {
                                field.setFormat(Const.getNumberFormats()[x]);
                                field.setLength(numberLength[i][x]);
                                field.setPrecision(numberPrecision[i][x]);

                                if (field.getPrecision() == 0 && field.getLength() < 18)
                                {
                                    field.setType(Value.VALUE_TYPE_INTEGER);
                                    field.setFormat("");
                                }
                            }
                        }
                    } else
                    {
                        field.setDecimalSymbol("");
                        field.setGroupSymbol("");
                        field.setCurrencySymbol("");
                    }
            }
        }
        
        
        // Show information on items using dialog box
        String message = "";
        message += Messages.getString("TextFileCSVImportProgressDialog.Info.ResultAfterScanning", ""+(linenr-1));
        message += Messages.getString("TextFileCSVImportProgressDialog.Info.HorizontalLine");
        for (int i = 0; i < nrfields; i++)
        {
            TextFileInputField field = meta.getInputFields()[i];

            message += Messages.getString("TextFileCSVImportProgressDialog.Info.FieldNumber", ""+(i + 1));

            message += Messages.getString("TextFileCSVImportProgressDialog.Info.FieldName", field.getName());
            message += Messages.getString("TextFileCSVImportProgressDialog.Info.FieldType", field.getTypeDesc());

            switch (field.getType())
            {
            case Value.VALUE_TYPE_NUMBER:
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.EstimatedLength", (field.getLength() < 0 ? "-" : "" + field.getLength()));
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.EstimatedPrecision", field.getPrecision() < 0 ? "-" : "" + field.getPrecision());
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberFormat", field.getFormat());
                if (numberFormatCount[i] > 1)
                {
                    message += Messages.getString("TextFileCSVImportProgressDialog.Info.WarnNumberFormat");
                }
                for (int x = 0; x < Const.getNumberFormats().length; x++)
                {
                    if (numberFormat[i][x])
                    {
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberFormat2", Const.getNumberFormats()[x]);
                        Value minnum = new Value("minnum", minValue[i][x]);
                        Value maxnum = new Value("maxnum", maxValue[i][x]);
                        minnum.setLength(numberLength[i][x], numberPrecision[i][x]);
                        maxnum.setLength(numberLength[i][x], numberPrecision[i][x]);
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberMinValue", minnum.toString());
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberMaxValue", maxnum.toString());

                        try
                        {
                            df2.applyPattern(Const.getNumberFormats()[x]);
                            df2.setDecimalFormatSymbols(dfs2);
                            double mn = df2.parse(minstr[i]).doubleValue();
                            Value val = new Value("min", mn);
                            val.setLength(numberLength[i][x], numberPrecision[i][x]);
                            message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberExample", Const.getNumberFormats()[x], minstr[i], val.toString());
                        }
                        catch (Exception e)
                        {
                            log.logBasic(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.getNumberFormats()[x]
                                    + "] did not work.");
                        }
                    }
                }
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.NumberNrNullValues", ""+nrnull[i]);
                break;
            case Value.VALUE_TYPE_STRING:
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.StringMaxLength", ""+field.getLength());
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.StringMinValue", minstr[i]);
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.StringMaxValue", maxstr[i]);
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.StringNrNullValues", ""+nrnull[i]);
                break;
            case Value.VALUE_TYPE_DATE:
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateMaxLength", field.getLength() < 0 ? "-" : "" + field.getLength());
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateFormat", field.getFormat());
                if (dateFormatCount[i] > 1)
                {
                    message += Messages.getString("TextFileCSVImportProgressDialog.Info.WarnDateFormat");
                }
                for (int x = 0; x < Const.getDateFormats().length; x++)
                {
                    if (dateFormat[i][x])
                    {
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateFormat2", Const.getDateFormats()[x]);
                        Value mindate = new Value("mindate", minDate[i][x]);
                        Value maxdate = new Value("maxdate", maxDate[i][x]);
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateMinValue", mindate.toString());
                        message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateMaxValue", maxdate.toString());

                        daf2.applyPattern(Const.getDateFormats()[x]);
                        try
                        {
                            Date md = daf2.parse(minstr[i]);
                            Value val = new Value("min", md);
                            val.setLength(field.getLength());
                            message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateExample", Const.getDateFormats()[x], minstr[i], val.toString());
                        }
                        catch (Exception e)
                        {
                            log.logError(toString(), "This is unexpected: parsing [" + minstr[i] + "] with format [" + Const.getDateFormats()[x]
                                    + "] did not work.");
                        }
                    }
                }
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.DateNrNullValues", ""+nrnull[i]);
                break;
            default:
                break;
            }
            if (nrnull[i] == linenr - 1)
            {
                message += Messages.getString("TextFileCSVImportProgressDialog.Info.AllNullValues");
            }
            message += Const.CR;
        }
        
        monitor.worked(1);
        monitor.done();
        
        return message;

    }
}
