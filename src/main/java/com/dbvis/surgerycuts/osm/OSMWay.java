/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.NamedNodeMap;

public class OSMWay {
    private final List ids;
    private final List tags; 
    private final List extratags; 
    private final long id;
    private final StringBuilder path; 
    private final String ts; 
    private boolean isExtra; 
    
    OSMWay(long p_id, String p_ts){
        ids = new ArrayList<>();
        extratags = new ArrayList<>(); 
        path = new StringBuilder(""); 
        tags = new ArrayList<>(); 
        this.id = p_id; 
        this.ts = p_ts; 
        isExtra = false; 
    }
    
    void addNode(OSMNode node){
        ids.add(node.getID()); 
        path.append(node.getProjectedCoordString());
        path.append(","); 
    }
    
    public void replaceNodeList(List nodes){
        ids.clear();
        path.setLength(0);
        Iterator iter = nodes.iterator(); 
        while (iter.hasNext()){
            OSMNode next = (OSMNode) iter.next(); 
            addNode(next); 
        }
    }
    
    void addTag(NamedNodeMap attributes){
        tags.add(attributes);  
    }

    public List getNodes(){
        return ids; 
    }
    
    List getTags(){
        return tags; 
    }
    
    List getExtraTags(){
        return extratags; 
    }
    
    public long getID(){
        return this.id; 
    }
    
    String getTimestamp(){
        return this.ts; 
    }
    
    public boolean isExtra(){
        return this.isExtra; 
    }
    
    @Override
    public String toString(){
        String tmp = path.toString(); 
        return "LINESTRING(" + tmp.substring(0, tmp.length()-1) + ")"; 
    }
}
