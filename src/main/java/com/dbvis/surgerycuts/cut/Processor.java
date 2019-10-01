/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.cut;

import com.dbvis.surgerycuts.misc.Vec;
import com.dbvis.surgerycuts.osm.OSMMap;
import com.dbvis.surgerycuts.osm.OSMNode;
import com.dbvis.surgerycuts.osm.OSMWay;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import com.dbvis.surgerycuts.misc.Converter;

public class Processor {
     //Instantiate parser for WKT -> Geometry Object
    private static  WKTReader parser;
    private static Converter converter;
    private static GeometryFactory factory_geom;
    private static ProcessorSettings settings;
    private static Shape cutshape;
    private static Polygon focus_geom;
    private static Polygon upperfocus_geom;
    private static OSMMap o;  //result map
    
    public Processor(Converter converter, ProcessorSettings settings){
        this.settings = settings;
        this.converter = converter;

        parser = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326));
        factory_geom  = JTSFactoryFinder.getGeometryFactory();
    }

    public Converter getConverter(){
        return converter;
    }

    public void setCutshape(Shape cutshape){
        this.cutshape = cutshape;
        focus_geom = cutshape.getFocusGeom();
        upperfocus_geom = cutshape.getUpperFocusGeom();
    }

    public static void apply(OSMMap map){
        if (cutshape == null) return;
        o = map;
        //Make Clipping Shape visible on our OSM map
        Map<Long,OSMWay> ways = o.getWays(); 
        
        List intersecting = new ArrayList<>(); //ways, intersecting our focus
        try { 
            for (Map.Entry<Long, OSMWay> entry : ways.entrySet()){ 
                OSMWay way_curr =  entry.getValue(); 
                LineString way_geom = (LineString) parser.read(way_curr.toString());  
                if (way_geom.intersects(focus_geom) && !way_curr.isExtra()){
                    //(//avoids clipping shape as intersecting line)
                    intersecting.add(way_curr);
                }
            }
            segment(intersecting);
            Map<Long, OSMNode> n = o.getNodes(); //to-do: get from result set
            n.entrySet().stream().map((entry) -> entry.getValue())
                    .filter((candidate) -> (candidate.hasTranslateInfo()))
                    .forEach((candidate) -> candidate.setLatLong(candidate.getTranslCoords()));
            } catch (ParseException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private static void segment(List intersecting){
        Iterator iter_ways = intersecting.iterator();
        LineString cutline = cutshape.getCutlineGeom();
        while (iter_ways.hasNext()){
                OSMWay way_curr = (OSMWay) iter_ways.next();
                List withcutpoints = addCutpoints(way_curr, cutline);
                List subsegmented = subsegment(withcutpoints);
                setTranslate(subsegmented);
                way_curr.replaceNodeList(subsegmented);
        }
    }

    private static List addCutpoints(OSMWay w, LineString cutline){
        List result = new ArrayList();
        Iterator iter_nodes = w.getNodes().iterator();
        OSMNode node_prev = (OSMNode) o.getNodes().get(iter_nodes.next()); //ignore first iteration
        while (iter_nodes.hasNext()){
            OSMNode node_next = (OSMNode) o.getNodes().get(iter_nodes.next());
            LineString segment = factory_geom.createLineString(new Coordinate[]{node_prev.getProjectedCoordinates(),node_next.getProjectedCoordinates()});
            result.add(node_prev); 
            if (segment.intersects(focus_geom)){
                if (segment.intersects(cutline)){
                    Geometry cutpoint = segment.intersection(cutline);
                    Vec cutvector = new Vec(cutpoint.getCoordinate().x, cutpoint.getCoordinate().y);

                    //add new cutpoints to the cutline.
                    //use nodeprev's coordinates as UTM reference point (to get the UTM zone)

                    OSMNode cutnode1 = o.addExtraNode(cutvector, node_prev.getCoords());
                    OSMNode cutnode2 = o.addExtraNode(cutvector, node_prev.getCoords());

                    //define the line between node_prev and the cutpoint. From where does the line come?
                    LineString firstcutsegment = factory_geom.createLineString(new Coordinate[]{node_prev.getProjectedCoordinates(),cutnode1.getProjectedCoordinates()});

                    ///decide into which direction cutnodes (being now at the same position) shall be shifted!
                    if (upperfocus_geom.intersects(firstcutsegment)){
                        //the last iterated node was "above" the cutline
                        cutnode1.setCutPointDir(1);
                        cutnode2.setCutPointDir(-1);
                    } else {
                        //the last iterated node was "below" the cutline
                        cutnode1.setCutPointDir(-1);
                        cutnode2.setCutPointDir(1);
                    }
                    result.add(cutnode1); 
                    result.add(cutnode2);
                }
            }
            node_prev = node_next;
        }
        result.add(node_prev);
        return result;
    }
    
    private static List subsegment(List nodes){
        List result = new ArrayList<>(); 
        Iterator iter_nodes = nodes.iterator(); 
        OSMNode node_prev = (OSMNode) iter_nodes.next(); 
        while (iter_nodes.hasNext()){
            result.add(node_prev); 
            OSMNode node_next = (OSMNode) iter_nodes.next(); 
            if (!(node_prev.isCutpoint() && node_next.isCutpoint())){ //don't segment the cutline's normal
                LineString segment = factory_geom.createLineString(new Coordinate[]{node_prev.getProjectedCoordinates(),node_next.getProjectedCoordinates()});
                if (segment.intersects(focus_geom)){
                   result.addAll( subsegment_inner(node_prev, node_next));
                }
            }
            node_prev = node_next; 
        }
        result.add(node_prev); 
        return result; 
    }

    private static List subsegment_inner(OSMNode node1, OSMNode node2){
        List newnodes = new ArrayList<>(); //new, resp. translated nodes
        Vec start = node1.getVector();
        Vec end = node2.getVector();

        //get distance between both nodes
        double vector_length = start.getDistance(end);
        //normalize direction vector:
        Vec direction = end.minus(start).times(1/vector_length);

        int count = 1;
        double segmentlength = Math.max(settings.get("MinSegmentLength"),settings.get("SegmentRatio") * vector_length);
        Vec tmp = start.plus(direction.times(count * segmentlength));

        while (tmp.getDistance(start) < vector_length){
            if (focus_geom.isWithinDistance(factory_geom.createPoint(tmp.toCoordinate()),segmentlength)){
                //new vectors being closer to the start vector will use their UTM zone for re-projection
                if (tmp.getDistance(start)/vector_length <= 0.5){
                    newnodes.add(o.addExtraNode(tmp, node1.getCoords()));
                } else {
                    newnodes.add(o.addExtraNode(tmp, node2.getCoords()));
                }
            }
            //next iteration:
            count++;
            tmp = start.plus(direction.times(count * segmentlength));

        }
        return newnodes; 
    }

    private static void setTranslate(List nodes){
        Iterator iter = nodes.iterator();
        OSMNode curr; 
        while (iter.hasNext()){
            curr = (OSMNode) iter.next();
            Point p = factory_geom.createPoint(curr.getProjectedCoordinates());
            if (focus_geom.contains(p) && !curr.hasTranslateInfo()){
                Vec translate = cutshape.getTranslate(p);
                if (curr.isCutpoint()) translate = translate.times(curr.getCutPointDir());
                curr.setTranslateInfo(curr.getVector().plus(translate));
            } 
        }        
    }

    public OSMMap getResult(){
        return o;
    }

    public ProcessorSettings getSettings(){
        return settings;
    }
}
