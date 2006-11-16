package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

public class AddTransServlet extends HttpServlet
{
    private static final long serialVersionUID = -6850701762586992604L;
    private static LogWriter log = LogWriter.getInstance();
    
    public static final String CONTEXT_PATH = "/kettle/addTrans";
    
    private TransformationMap transformationMap;
    
    public AddTransServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }
    
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getServletPath().equals(CONTEXT_PATH)) return;

        if (log.isDebug()) log.logDebug(toString(), "Addition of transformation requested");

        response.setContentType("text/html");

        OutputStream os = response.getOutputStream(); // to write to the browser/client
        InputStream is = request.getInputStream(); // read from the client
        
        PrintStream out = new PrintStream(os);

        out.println("<HTML>");
        out.println("<HEAD><TITLE>Add transformation</TITLE></HEAD>");
        out.println("<BODY>");

        try
        {
            // First read the complete transformation in memory from the inputStream
            int c;
            StringBuffer xml = new StringBuffer();
            while ( (c=is.read())!=-1)
            {
                xml.append((char)c);
            }
            
            // Parse the XML, create a transformation
            //
            Document doc = XMLHandler.loadXMLString(xml.toString());
            Node transNode = XMLHandler.getSubNode(doc, "transformation");
            TransMeta transMeta = new TransMeta(transNode);
            
            // Create the transformation and store in the list...
            //
            Trans trans = new Trans(log, transMeta);
            transformationMap.addTransformation(transMeta.getName(), trans);
                
            out.println("<H1>Transformation '"+trans.getName()+"' was added to the list.</H1>");
            out.println("<a href=\"/kettle/transStatus?name="+trans.getName()+"\">Go to the transformation status page</a><p>");
        }
        catch (Exception ex)
        {
            out.println("<p>");
            out.println("<pre>");
            ex.printStackTrace(out);
            out.println("</pre>");
        }

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");

        out.flush();

        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
    }
    
    public String toString()
    {
        return "Add Transformation";
    }

}