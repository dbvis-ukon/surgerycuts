/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.NamedNodeMap;

public class OSMRelation {
    private final String ts; 
    private final long id; 
    private final List members; 
    private final List tags; 
    
    OSMRelation(long p_id, String p_ts){
        this.id = p_id; 
        this.ts = p_ts; 
        members = new ArrayList<>(); 
        tags = new ArrayList<>();
    }
    
    void addTag(NamedNodeMap attributes){
        tags.add(attributes); 
    }
    
    void addMember(OSMMember member){
        members.add(member);
    }
    
    public long getID(){
        return id; 
    }
    
    String getTimestamp(){
        return ts; 
    }
    
    List getTags(){
        return tags; 
    }
    
    List getMembers(){
        return members; 
    }
}
