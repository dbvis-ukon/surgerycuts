/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.renderer;

import com.dbvis.surgerycuts.cut.Processor;
import com.dbvis.surgerycuts.cut.*;
import com.dbvis.surgerycuts.gui.CutOverview;
import com.dbvis.surgerycuts.misc.CollisionLookup;
import com.dbvis.surgerycuts.misc.GISHelper;
import com.dbvis.surgerycuts.osm.OSMMap;
import com.dbvis.surgerycuts.osm.OSMWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.*;

public class MouseEvents implements MouseListener, MouseMotionListener{
    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private String  in;
    private static final String[] out = new String[]{System.getProperty("user.dir") + "/data/out/out1.osm",
                                                     System.getProperty("user.dir") + "/data/out/out2.osm"};
    private static int out_candidate = 1;
    private Renderer r;
    private Processor proc;
    private MapView map;
    private Paint paint_default;
    private Paint paint_highlight;
    private Paint paint_overlay;
    private Paint paint_stroke;
    private Paint paint_stroke_red;

    private GeometryFactory geom_factory;
    private List<Circle> circles;
    private List<Geometry> circle_geoms;
    private int highlighted;
    private List<Shape> lastoverlay;
    
    private List<LatLong> p;
    private List<Shape> shapes;
    private List<Polygon> helper_upper;
    private List<Polygon> helper_lower;
    private List<Polygon> overlay;
    private List<Polyline> cutline;
    private CollisionLookup collisions;

    private boolean working;

    private JButton standardbutton;
    private JButton transformedbutton;
    private JMenuItem export;
    private JMenuItem resetmarkers;
    private CutOverview overview;
    private boolean roundedshapes= true;


   public MouseEvents(Renderer r, Processor proc, JButton standardbutton, JButton transformedbutton, JMenuItem export, JMenuItem resetmarkers, CutOverview overview){
       this.proc = proc;
       this.overview = overview;

       this.standardbutton = standardbutton;
       this.transformedbutton = transformedbutton;
       this.export = export;
       this.resetmarkers = resetmarkers;
       geom_factory  = JTSFactoryFinder.getGeometryFactory();

       resetMap(r);
       p = new ArrayList<>();
       paint_default = GRAPHIC_FACTORY.createPaint(); //for filling circle_geoms
       paint_default.setColor(0xff000000);
       paint_default.setStyle(Style.FILL);

       paint_stroke = GRAPHIC_FACTORY.createPaint(); //for filling circle_geoms
       paint_stroke.setColor(0xff000000);
       paint_stroke.setStyle(Style.STROKE);

       paint_stroke_red = GRAPHIC_FACTORY.createPaint(); //for filling circle_geoms
       paint_stroke_red.setColor(0xffff0000);
       paint_stroke_red.setStyle(Style.STROKE);

       paint_highlight = GRAPHIC_FACTORY.createPaint(); //for filling circle_geoms
       paint_highlight.setColor(0xff00ff00);
       paint_highlight.setStyle(Style.FILL);

       paint_overlay = GRAPHIC_FACTORY.createPaint(); //for filling circle_geoms
       paint_overlay.setColor(0x40000000);
       paint_overlay.setStyle(Style.FILL);
   }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && circles.size()> 2) {
            if (collisions.hasCollision()){
                JOptionPane.showMessageDialog(null, "Transformation not possible. Cut shapes are colliding.");
            } else {
                boolean alldeactivated = true;
                for (Shape c : shapes){
                    if (c.isActive()) alldeactivated = false;
                }
                if (alldeactivated){
                    JOptionPane.showMessageDialog(null, "Transformation not possible. All cut shapes are inactive.");
                } else {
                    callproc();
                }
            }
            return;
        }
        int j= 0;
        for (Circle c: circles){
            Point a = map.getMapViewProjection().toPixels(c.getPosition());
            circle_geoms.set(j, GISHelper.createCircle(a.x, a.y, 5.0));
            j++;
        }

        int clicked = -1;
        if (circles.size() > 0) { //circle was clicked
            LatLong rel = new LatLong(0, 0);
            int i = 0;
            for (Geometry c : circle_geoms) {
                if (c.contains(geom_factory.createPoint(new Coordinate(e.getX(), e.getY())))) {
                    clicked = i;
                    rel = map.getMapViewProjection().fromPixels(e.getX(), e.getY());
                    break;
                }
                i++;
            }
            if (clicked > -1) {
                p.set(clicked, rel);
                toggleHighlight(clicked);
                return;
            }
        }

        if (circles.size() <=2 ||  clicked+1 % 4 <2){
            circles.add(drawMarker(map.getMapViewProjection().fromPixels(e.getX(),e.getY())));
            circle_geoms.add(GISHelper.createCircle(e.getX(), e.getY(), 5.0));
            p.add(map.getMapViewProjection().fromPixels(e.getX(),e.getY()) );
            if ((circles.size()-1) % 4 == 0){
                cutline.add(new Polyline(paint_stroke,GRAPHIC_FACTORY, true));
                map.getLayerManager().getLayers().add(cutline.get(cutline.size()-1));
                if (roundedshapes){
                    shapes.add(new RoundedShape(shapes.size(), p.get(p.size()-1), proc.getSettings()));
                } else {
                    shapes.add(new RectShape(shapes.size(), p.get(p.size()-1), proc.getSettings()));
                }
            } else {
                shapes.get(shapes.size()-1).setB(p.get(p.size()-1));
                updateShape(clicked);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e){
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (circles.size() % 4 == 1){ //new shape
            cutline.get(cutline.size()-1).getLatLongs().clear();
            cutline.get(cutline.size()-1).getLatLongs().add(circles.get(circles.size()-1).getPosition());
            cutline.get(cutline.size()-1).getLatLongs().add(map.getMapViewProjection().fromPixels(e.getX(),e.getY()));
            cutline.get(cutline.size()-1).requestRedraw();
        } else {
            if (highlighted > -1) {
                int ref = (int) Math.floor((highlighted ) / 4);
                Shape currentshape = shapes.get(ref);

                if (highlighted % 4 >= 2){
                    if (highlighted % 4 == 2) { //change cut area
                        LatLong l = map.getMapViewProjection().fromPixels(e.getX(), e.getY());
                        com.vividsolutions.jts.geom.Point tmp = geom_factory.createPoint(GISHelper.Deg2UTM(l).toCoordinate());
                        if (currentshape.getUpperFocusGeom().contains(tmp)) {
                            double h = Math.min(Math.max(0.00000000001, currentshape.getCutlineGeom().distance(tmp)), currentshape.getCutlineGeom().getLength() / 2);
                            double t = Math.min(Math.max(0.00000000001, currentshape.getOrthoGeom().distance(tmp)), currentshape.getCutlineGeom().getLength() / 2);
                            currentshape.setShapeExtents(h, t);
                            drawOverlay(currentshape.getOverlay(), ref);
                            drawHelper(currentshape,ref);

                            //handle upper control point
                            p.set(4*ref+3, currentshape.getM2());
                            Circle uppercircle = circles.get(ref*4+3);
                            uppercircle.setLatLong(p.get(ref*4+3));
                            circles.set(ref*4+3, uppercircle);
                            Point g= map.getMapViewProjection().toPixels(p.get(ref*4+3));
                            circle_geoms.set(ref*4+3, GISHelper.createCircle(g.x, g.y, 5.0));
                            circles.get(ref*4+3).requestRedraw();
                        }
                    } else if (highlighted % 4 == 3) { //change glue area
                        LatLong l = map.getMapViewProjection().fromPixels(e.getX(), e.getY());
                        com.vividsolutions.jts.geom.Point tmp = geom_factory.createPoint(GISHelper.Deg2UTM(l).toCoordinate());
                        if (currentshape.getUpperFocusDefaultGeom().contains(tmp)) {
                            currentshape.setFocusRatio(currentshape.getCutlineGeom().distance(tmp) / currentshape.getFocusheight());
                            drawHelper(currentshape, ref);
                        }
                    }
                } else {
                    cutline.get(ref).getLatLongs().clear();
                    int addendum = (highlighted % 2 == 0) ?  1 : 0;
                    cutline.get(ref).getLatLongs().add(circles.get(4*ref+addendum).getPosition());
                    cutline.get(ref).getLatLongs().add(map.getMapViewProjection().fromPixels(e.getX(),e.getY()));
                }
                cutline.get(ref).requestRedraw();
                circles.get(highlighted).setLatLong(map.getMapViewProjection().fromPixels(e.getX(), e.getY()));
                circle_geoms.set(highlighted, GISHelper.createCircle(e.getX(), e.getY(), 5.0));
                circles.get(highlighted).requestRedraw();
            }
        }
    }

    private Circle drawMarker(LatLong pos){
        Circle c = new FixedPixelCircle(pos,5,  paint_default, paint_default, false);
        map.getLayerManager().getLayers().add(c);
        return c;
    }

    private void drawOverlay(List<LatLong> l, int ref){
        if (ref == -1){
            ref = overlay.size();
            overlay.add(new Polygon(paint_overlay,null, GRAPHIC_FACTORY));
            map.getLayerManager().getLayers().add(overlay.get(overlay.size()-1));
        } else {
            overlay.get(ref).getLatLongs().clear();
        }
        overlay.get(ref).getLatLongs().addAll(l);
        overlay.get(ref).requestRedraw();
    }

    private void drawHelper(Shape c, Integer i){
        if (i == -1){ //add helper
            helper_lower.add(new Polygon(paint_stroke,null, GRAPHIC_FACTORY));
            helper_upper.add(new Polygon(paint_stroke,null, GRAPHIC_FACTORY));
            map.getLayerManager().getLayers().add(helper_lower.get( helper_lower.size()-1));
            map.getLayerManager().getLayers().add(helper_upper.get( helper_upper.size()-1));
        }

        //collision detection
        LinkedList<Shape> nocollisions = new LinkedList<>();
        for (Shape cutcandidate: shapes){
            if (c != cutcandidate && c.getFocusGeom().intersects(cutcandidate.getFocusGeom())){
                collisions.addCollision(c, cutcandidate);
            } else {
                if (c!= cutcandidate){
                    boolean[] isempty = collisions.removeCollision(c, cutcandidate);
                    if (isempty[0]) nocollisions.add(c);
                    if (isempty[1]) nocollisions.add(cutcandidate);
                }
            }
        }
            for (Shape tmp : nocollisions){
                helper_upper.get(tmp.getID()).setPaintFill(paint_stroke);
                helper_lower.get(tmp.getID()).setPaintFill(paint_stroke);
                helper_upper.get(tmp.getID()).requestRedraw();
                helper_lower.get(tmp.getID()).requestRedraw();
            }
            for (Shape tmp: collisions.getCollisions()){
                helper_upper.get(tmp.getID()).setPaintFill(paint_stroke_red);
                helper_lower.get(tmp.getID()).setPaintFill(paint_stroke_red);
                helper_upper.get(tmp.getID()).requestRedraw();
                helper_lower.get(tmp.getID()  ).requestRedraw();
            }

            //update current shape
            helper_lower.get(c.getID()).getLatLongs().clear();
            helper_upper.get(c.getID()).getLatLongs().clear();
            helper_lower.get(c.getID()).getLatLongs().addAll(c.getLowerPolygonLatLng());
            helper_upper.get(c.getID()).getLatLongs().addAll(c.getUpperPolygonLatLng());
            helper_lower.get(c.getID()).requestRedraw();
            helper_upper.get(c.getID()).requestRedraw();
            overview.set(c);
    }


    private void updateShape(int clicked){
        Shape currentshape;
        int ref = (int) Math.floor((clicked)/4);

        if (clicked == -1){
            currentshape = shapes.get(shapes.size()-1);
        } else {
            currentshape = shapes.get(ref);
        }
        if (clicked % 4 < 2 && clicked > -1){
            currentshape.setAB(p.get(clicked- (clicked % 4)), p.get(clicked- (clicked % 4)+1));
        }

        if (circles.size() % 4 == 2) {
            p.add(currentshape.getM());
            circles.add(drawMarker(p.get(p.size() - 1)));
            Point e = map.getMapViewProjection().toPixels(p.get(p.size() - 1));
            circle_geoms.add(GISHelper.createCircle(e.x, e.y, 5.0));

            p.add(currentshape.getM2());
            circles.add(drawMarker(p.get(p.size() - 1)));
            Point e2 = map.getMapViewProjection().toPixels(p.get(p.size() - 1));
            circle_geoms.add(GISHelper.createCircle(e2.x, e2.y, 5.0));


            drawOverlay(currentshape.getOverlay(), -1);
            drawHelper(currentshape, -1);
        } else {
            p.set(4*ref+2, currentshape.getM());
            p.set(4*ref+3, currentshape.getM2());

            for (int i= 2; i<=3; i++){
                Circle tmp = circles.get(ref*4+i);
                tmp.setLatLong(p.get(ref*4+i));
                circles.set(ref*4+i,tmp);
                Point e = map.getMapViewProjection().toPixels(p.get(ref*4+i));
                circle_geoms.set(ref*4+i, GISHelper.createCircle(e.x, e.y, 5.0));
                circles.get(ref*4+i).requestRedraw();
            }


            drawOverlay(currentshape.getOverlay(), ref);
            drawHelper(currentshape,ref);
        }

        System.out.println("Room:" + Metrics.getRoom(currentshape));
        System.out.println("whole area:" +Metrics.getWholeArea(currentshape));
        System.out.println("Distorted area:"  + Metrics.getDistortedArea(currentshape));
        System.out.println("Ratio:" +  Metrics.getDistortedAreaRoomRatio(currentshape));
    }

    public void showMarkers(){
        Layers l = map.getLayerManager().getLayers();
        for (Polygon o: overlay){
            l.add(o);
        }
        for (Polygon h_u: helper_upper){
            l.add(h_u);
        }
        for (Polygon h_l: helper_lower){
            l.add(h_l);
        }
        for (Polyline c: cutline){
            l.add(c);
        }
        for (Circle c: circles){
            l.add(c);
        }
        drawTmp();
    }

    public List<Shape> getLastOverlays(){
        return lastoverlay;
    }

    public int getLastOutput(){
        return out_candidate;
    }

    public void callproc(){
        Thread t = new Thread(() -> {
            working = true;
            applyproc();
        });
        if (!working) t.start();
    }

    private void applyproc(){
        if (in == null){
            System.out.println("Input map does not exist");
            return;
        }

        transformedbutton.setEnabled(false);
        int count = 0;
        OSMMap o;
        for (Shape c: shapes){
            if (c.isActive()){
                System.out.println("Apply cut for shape with ID=" + c.getID());
                o = (count == 0) ? new OSMMap(in) : proc.getResult();
                c.updateOverlayGeometry();
                proc.setCutshape(c);
                proc.getConverter().getProgressbar().setString("Applying Cut(s)...");
                proc.getConverter().getProgressbar().setValue(0);
                lastoverlay.add(c);
                proc.apply(o);
                count++;
            }
        }

        //delete old temp data:
        try {

            File tmp = new File(out[out_candidate]);
            File tmp2 = new File(out[out_candidate].replace(".osm", ".map"));
            tmp.delete();
            tmp2.delete();

        } catch (Exception e){
            System.out.println(e.toString());
        }
        out_candidate = 1 - out_candidate;
        proc.getConverter().getProgressbar().setString("Write .osm");
        OSMWriter.write(proc.getResult(), out[out_candidate]); //save .osm

        /*
            Make custom rendering possible.
            For this, convert .osm to .map, using Osmosis Cmd.
            Rendering is done with Mapsforge.
        */

        proc.getConverter().OsmToMap(out[out_candidate], true); //to .map for custom rendering
        working = false;
        AwtGraphicFactory.clearResourceMemoryCache();
        r.setLayer(new File(out[out_candidate].replace(".osm", ".map")), false, false);
        for (Shape c: shapes){
            if (c.isActive()) r.drawOverlay(c.getOverlay());
        }

        r.setEventListenersActive(false);
        standardbutton.setEnabled(true);
        resetmarkers.setEnabled(false);
        export.setEnabled(true);
    }

    private void toggleHighlight(int clicked){
        Paint oldpaint = circles.get(clicked).getPaintFill();

        //reset all other highlighting:
        for (Circle c : circles){
            c.setPaintFill(paint_default);
            c.requestRedraw();
        }

        int ref = (int) Math.floor((clicked)/4);
        if (oldpaint == paint_default) {
            circles.get(clicked).setPaintFill(paint_highlight);
            highlighted = clicked;
            circles.get(clicked).requestRedraw();
            if (clicked % 4 < 2 && circles.size() >2){
                map.getLayerManager().getLayers().remove(overlay.get(ref));
                map.getLayerManager().getLayers().remove(helper_upper.get(ref));
                map.getLayerManager().getLayers().remove(helper_lower.get(ref));
                int addendum = 0;
                if (clicked % 4 == 0) addendum = 1;
                map.getLayerManager().getLayers().remove(circles.get(clicked+1+ addendum));
                map.getLayerManager().getLayers().remove(circles.get(clicked+2+ addendum));
                r.removeExtralayers();
            }
        } else { //unhighlight
            highlighted = -1;
            if (clicked % 4 < 2 && circles.size() >2){
                map.getLayerManager().getLayers().add(overlay.get(ref));
                map.getLayerManager().getLayers().add(helper_upper.get(ref));
                map.getLayerManager().getLayers().add(helper_lower.get(ref));
                int addendum = 0; //to-do: use ref
                if (clicked % 4 == 0) addendum = 1;
                map.getLayerManager().getLayers().add(circles.get(clicked+1+ addendum));
                map.getLayerManager().getLayers().add(circles.get(clicked+2+ addendum));
            }
            updateShape(clicked);
        }
    }

    public void resetMap(Renderer r){
        if (map != null && map.getLayerManager() != null) {
            Layers l =  map.getLayerManager().getLayers();
            for (Polygon o: overlay){
                l.remove(o);
            }
            for (Polygon h_u: helper_upper){
                l.remove(h_u);
            }
            for (Polygon h_l: helper_lower){
                l.remove(h_l);
            }
            for (Polygon h_l: helper_lower){
                l.remove(h_l);
            }
            for (Polyline c: cutline){
                l.remove(c);
            }
            for (Circle c: circles){
                l.remove(c);
            }
        }
        shapes = new LinkedList<>();
        helper_lower = new LinkedList<>();
        helper_upper = new LinkedList<>();
        cutline = new LinkedList<>();
        overlay = new LinkedList<>();
        circles = new LinkedList<>();
        circle_geoms = new LinkedList<>();
        collisions = new CollisionLookup();
        lastoverlay = new LinkedList<>();

        highlighted = -1;
        p = new ArrayList<>();
        working = false;
        this.r = r;
        map = r.getMapView();

        drawTmp();
    }

    public void setShapeActive(int id, boolean active){
        shapes.get(id).setActive(active);
        Paint circle_tmp = active ? paint_default : null;
        Paint stroke_tmp = active ? paint_stroke : null;
        if (active && collisions.hasCollisionSingle(shapes.get(id))) stroke_tmp = paint_stroke_red; //colliding color
        Paint overlay_tmp = active ? paint_overlay : null;
        for (int i= 0; i<4; i++){
            circles.get(id*4+i).setPaintFill(circle_tmp);
            circles.get(id*4+i).setPaintStroke(circle_tmp);
            circles.get(id*4+i).requestRedraw();
        }
        helper_upper.get(id).setPaintFill(stroke_tmp);
        helper_lower.get(id).setPaintFill(stroke_tmp);
        cutline.get(id).setPaintStroke(stroke_tmp);
        overlay.get(id).setPaintFill(overlay_tmp);
        overlay.get(id).requestRedraw();
        cutline.get(id).requestRedraw();
        helper_upper.get(id).requestRedraw();
        helper_lower.get(id).requestRedraw();
    }

    public List<Shape> getShapes(){
        return shapes;
    }

    public void setInputPath(String path){
        in = path;
    }

    public File getRecentTMap(){
        return new File(out[out_candidate].replace(".osm", ".map"));
    }

    public void setRoundedShapes(boolean b){
        roundedshapes = b;
    }

    private void drawTmp(){
        LatLong[] tmp = new LatLong[]{new LatLong(41.94099985473493, 12.471000035858), //big one
                new LatLong(41.921, 12.673),
                new LatLong(41.925, 12.529),
                new LatLong(41.825, 12.479),
                new LatLong(41.834, 12.442),
                new LatLong(41.963, 12.544),
                new LatLong(41.925,12.52900000000) //new cut (slide 3)
        };
        for (LatLong c : tmp){
            drawMarker(c);
        }
    }
}
