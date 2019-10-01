/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

public class OSMMember {
    private String type; 
    private long ref; 
    private String role;
    
    OSMMember(String type, long ref, String role){
        this.type = type; 
        this.ref = ref; 
        this.role = role; 
    }
    
    public String getType(){
        return this.type; 
    }
    long getRef(){
        return this.ref; 
    }
    String getRole(){
        return this.role; 
    }
}
