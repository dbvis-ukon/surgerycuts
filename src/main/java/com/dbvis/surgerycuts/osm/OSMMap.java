/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.dbvis.surgerycuts.misc.Vec;
import org.mapsforge.core.model.LatLong;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OSMMap {
    private File f;
    private long maxNodeID;
    private long maxWayID;
    private Map nodemap;
    private Map waymap; //will become a map
    private List relationlist;

    private double[] bounds;
    private  SimpleDateFormat sdf ;

    public OSMMap(String s){
        nodemap = new HashMap<>();
        waymap = new HashMap<>();
        relationlist = new ArrayList<>();
        maxNodeID = 0;
        maxWayID = 0;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            f = new File(s);
            DocumentBuilderFactory db_factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = db_factory.newDocumentBuilder();
            Document doc = db.parse(f);
            doc.getDocumentElement().normalize();

            NodeList b_tmp =  doc.getElementsByTagName("bounds");
            Element b = (Element) b_tmp.item(0);

            bounds = new double[4];
            bounds[0] =  Double.parseDouble(b.getAttribute("minlat"));
            bounds[1] = Double.parseDouble(b.getAttribute("minlon"));
            bounds[2] = Double.parseDouble(b.getAttribute("maxlat"));
            bounds[3] = Double.parseDouble(b.getAttribute("maxlon"));

            NodeList n = doc.getElementsByTagName("node");
            int bound = n.getLength();
            Node tmp;
            Element e;
            long id;
            for (int i=0; i<bound; i++){
                tmp = n.item(i);
                if (tmp.getNodeType() == Node.ELEMENT_NODE) {
                    e = (Element) tmp;
                    id =  Long.parseLong(e.getAttribute("id"));
                    if (id > maxNodeID) maxNodeID = id;
                    LatLong coords = new LatLong(Double.parseDouble(e.getAttribute("lat")), Double.parseDouble(e.getAttribute("lon")));
                    OSMNode o = new OSMNode(id, coords,  e.getAttribute("timestamp"));
                    nodemap.put(id, o);
                }
            }

            NodeList w = doc.getElementsByTagName("way");
            bound = w.getLength();
            for (int i=0; i<bound; i++){
                tmp = w.item(i);
                if (tmp.getNodeType() == Node.ELEMENT_NODE) {
                    e = (Element) tmp;
                    long wayID = Long.parseLong(e.getAttribute("id"));
                    OSMWay way = new OSMWay(wayID, e.getAttribute("timestamp"));
                    if (wayID > maxWayID) maxWayID = wayID;
                    NodeList children = e.getChildNodes();
                    for (int j= 0; j < children.getLength(); j++){
                        if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element singlenode  = (Element) children.item(j);
                            if (singlenode.getTagName().equals("nd")){
                                way.addNode((OSMNode) nodemap.get(Long.parseLong(singlenode.getAttribute("ref"))));
                            } else {
                                way.addTag(singlenode.getAttributes());
                            }
                        }
                    }
                    waymap.put(wayID,way);
                }
            }

            NodeList r = doc.getElementsByTagName("relation");
            for (int i= 0; i<r.getLength(); i++){
                if (r.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    e= (Element) r.item(i);
                    OSMRelation rel = new OSMRelation(Long.parseLong(e.getAttribute("id")), e.getAttribute("timestamp"));
                    NodeList children = e.getChildNodes();
                    Element child;
                    for (int j= 0; j<children.getLength(); j++){
                        if (children.item(j).getNodeType() == Node.ELEMENT_NODE ){
                            child = (Element) children.item(j);
                            if (child.getTagName().equals("member")){
                                rel.addMember(new OSMMember(child.getAttribute("type"), Long.parseLong(child.getAttribute("ref")), child.getAttribute("role")));
                            } else {
                                rel.addTag(child.getAttributes());
                            }
                        }
                    }
                    relationlist.add(rel);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(OSMMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public OSMMap(OSMMap copy){
        this.f = copy.getFile();
        this.maxNodeID = copy.getMaxNodeID();
        this.maxWayID = copy.getMaxWayID();
        this.nodemap = new HashMap(copy.getNodeMap());
        this.waymap = new HashMap(copy.getWayMap());
        System.out.println(copy.getRelationList().size());
        this.relationlist = new ArrayList(copy.getRelationList());
        this.bounds = copy.getBounds();
        this.sdf = copy.getSdf();
    }

    public File getFile(){ return f;}

    private long getMaxNodeID(){ return maxNodeID;}

    private long getMaxWayID(){ return maxWayID;}

    private Map getNodeMap(){ return nodemap;}

    private Map getWayMap(){ return waymap;}

    private List getRelationList(){ return relationlist;}

    private SimpleDateFormat getSdf(){ return sdf; }

    public Map getNodes(){
        return nodemap;
    }

    public Map getWays(){
        return waymap;
    }

    public List getRelations(){
        return relationlist;
    }

    public OSMNode addExtraNode(Vec projected, LatLong ref){
            maxNodeID++;
            Timestamp ts_node = new Timestamp(System.currentTimeMillis());
            OSMNode n = new OSMNode(maxNodeID, projected, ref , sdf.format(ts_node));
            nodemap.put(maxNodeID, n);
            return n;
        }

    public double[] getBounds(){
        return bounds;
    }
}
