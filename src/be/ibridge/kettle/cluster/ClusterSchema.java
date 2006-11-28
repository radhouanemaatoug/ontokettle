package be.ibridge.kettle.cluster;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.repository.Repository;

/**
 * A cluster schema combines a list of slave servers so that they can be set altogether. 
 * It (can) also contain a number of performance tuning options regarding this cluster.
 * For example options regarding communications with the master node of the nodes themselves come to mind.
 * 
 * @author Matt
 * @since 17-nov-2006
 */
public class ClusterSchema extends ChangedFlag implements Cloneable, SharedObjectInterface
{
    public static final String XML_TAG = "clusterschema";
    
    /** the name of the cluster schema */
    private String name;
    
    /** The list of slave servers we can address */
    private List slaveServers;

    private boolean shared;
    
    public ClusterSchema()
    {
        slaveServers = new ArrayList();
    }
    
    /**
     * @param name
     * @param slaveServers
     */
    public ClusterSchema(String name, List slaveServers)
    {
        this.name = name;
        this.slaveServers = slaveServers;
    }

    public Object clone() 
    {
        ClusterSchema clusterSchema = new ClusterSchema();
        clusterSchema.setName(name);
        
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            clusterSchema.getSlaveServers().add(slaveServer.clone());
        }
        
        return clusterSchema;
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj==null) return false;
        return name.equals(((ClusterSchema)obj).name);
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("        <"+XML_TAG+">"+Const.CR);
        xml.append("          "+XMLHandler.addTagValue("name", name));
        xml.append("          <slaveservers>"+Const.CR);
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            xml.append("            "+slaveServer.getXML()).append(Const.CR);
        }
        xml.append("          </slaveservers>"+Const.CR);
        xml.append("        </"+XML_TAG+">"+Const.CR);
        return xml.toString();
    }
    
    public ClusterSchema(Node clusterSchemaNode)
    {
        this();
        
        name = XMLHandler.getTagValue(clusterSchemaNode, "name");
        
        Node slavesNode = XMLHandler.getSubNode(clusterSchemaNode, "slaveservers");
        int nrSlaves = XMLHandler.countNodes(slavesNode, "slaveserver");
        for (int i=0;i<nrSlaves;i++)
        {
            Node slaveNode = XMLHandler.getSubNodeByNr(slavesNode, "slaveserver", i);
            SlaveServer slaveServer = new SlaveServer(slaveNode);
            slaveServers.add(slaveServer);
        }
    }
    
    public void saveRep(Repository rep, long id_transformation) throws KettleDatabaseException
    {
        long id_cluster_schema = rep.insertClusterSchema(id_transformation, name);
        
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            slaveServer.saveRep(rep, id_transformation, id_cluster_schema);
        }
    }
    
    public ClusterSchema(Repository rep, long id_cluster_schema) throws KettleDatabaseException
    {
        this();
        
        Row row = rep.getClusterSchema(id_cluster_schema);
        
        name = row.getString("SCHEMA_NAME", null);
        
        long[] pids = rep.getSlaveServerIDs(id_cluster_schema);
        for (int i=0;i<pids.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(rep, pids[i]);
            slaveServers.add(slaveServer);
        }
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the slaveServers
     */
    public List getSlaveServers()
    {
        return slaveServers;
    }

    /**
     * @param slaveServers the slaveServers to set
     */
    public void setSlaveServers(List slaveServers)
    {
        this.slaveServers = slaveServers;
    }    
    
    /**
     * @return The slave server strings from this cluster schema
     */
    public String[] getSlaveServerStrings()
    {
        String[] strings = new String[slaveServers.size()];
        for (int i=0;i<strings.length;i++)
        {
            strings[i] = ((SlaveServer)slaveServers.get(i)).toString();
        }
        return strings;
    }

    /**
     * @return the shared
     */
    public boolean isShared()
    {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(boolean shared)
    {
        this.shared = shared;
    }
}