/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

import com.dbvis.surgerycuts.misc.GISHelper;
import com.dbvis.surgerycuts.misc.Vec;
import com.vividsolutions.jts.geom.Coordinate;
import org.mapsforge.core.model.LatLong;

public class OSMNode {
    private long id;
    private boolean hasTranslateInfo = false; 
    private boolean isCutpoint = false; 
    private int cutpointDir;
    private LatLong coords;
    private Vec proj_coords;
    private Vec transl_proj;
    private final String ts;

    public OSMNode(long p_id, LatLong coords, String p_ts){
        this.id = p_id; 
        this.coords = coords;
        this.ts = p_ts;
        proj_coords = GISHelper.Deg2UTM(coords);
    }

    public OSMNode(long p_id, Vec projected, LatLong ref, String p_ts){
        this.id = p_id;
        this.ts = p_ts;
        proj_coords = projected;
        coords = GISHelper.UTM2Deg(projected, ref);
    }

    public String toString(){
        return coords.toString() + "\n" + getProjectedCoordString();
    }
    
    public LatLong getCoords(){
        return coords;
    }

    public LatLong getTranslCoords(){
        return transl_proj.toLatLong(coords);
    }

    public String getProjectedCoordString(){
        return proj_coords.toString();
    }

    public Coordinate getProjectedCoordinates(){
        return new Coordinate(proj_coords.getX(), proj_coords.getY());
    }

    public Vec getVector(){
        return  proj_coords;
    }

    public void setLatLong(LatLong coords){
        this.coords = coords;
        proj_coords = GISHelper.Deg2UTM(coords);
    }
    
    public void setTranslateInfo(Vec translated){ //translated and projected
        this.transl_proj = translated;
        this.hasTranslateInfo = true; 
    }
    public boolean hasTranslateInfo(){
        return this.hasTranslateInfo; 
    }
    
    public void setCutPointDir(int dir){
        this.cutpointDir = dir; 
        this.isCutpoint = true; 
    }
    public int getCutPointDir(){
        return this.cutpointDir; 
    }
    
    public boolean isCutpoint(){
        return this.isCutpoint; 
    }
    
    public long getID(){
        return this.id; 
    }

    public String getTimestamp(){
        return this.ts; 
    }
}
