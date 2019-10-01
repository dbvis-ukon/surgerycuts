/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.cut;

import com.dbvis.surgerycuts.misc.GISHelper;
import com.dbvis.surgerycuts.misc.Vec;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import org.mapsforge.core.model.LatLong;
import java.util.ArrayList;
import java.util.List;

public class RoundedShape implements Shape {
    private boolean active = true;
    private Integer id;
    private GeometryFactory factory;

    //lat/long
    private LatLong a;
    private LatLong b;
    private LatLong center; //between a and b


    //UTM-projected:
    private Vec a_proj;
    private Vec b_proj;
    private Vec center_proj;
    private Vec circle_center_upper_proj;
    private Vec circle_center_lower_proj;

    private Vec direction; // normalised vector between a and b
    private Vec normal; //normalised normal vector with respect to a and b

    private Polygon fullfocus_proj;
    private Polygon upperfocus_proj;
    private Polygon upperfocus_default;
    private double focusratio = 1.0;
    private Polygon lowerfocus_proj;
    private Geometry uppercircle;
    private Geometry lowercircle;
    private LineString cutline_geom;
    private LineString ortho_geom;
    private List<LatLong> overlay;
    private Polygon overlaygeom;


    private double semiheight; //in meters
    private double heightratio;
    private double radius;
    private double focusheight; //single focus height, i.e. totalfocusheight/2

    public RoundedShape(Integer id, LatLong a, ProcessorSettings settings){
        this.id = id;
        factory = new GeometryFactory();
        heightratio = 0.25;
        this.a= a;
    }

    public void setB(LatLong b){
        this.b = b;
        setAB(a,b);
        System.out.println(b.toString());
    }
    private void update(boolean resetratio){
            a_proj = GISHelper.Deg2UTM(a);
            b_proj = GISHelper.Deg2UTM(b);
            cutline_geom = factory.createLineString(new Coordinate[]{new Coordinate(a_proj.toCoordinate()),new Coordinate(b_proj.toCoordinate())});

            double distance = a_proj.getDistance(b_proj);
            semiheight = distance * heightratio;
            this.semiheight = Math.min(distance/2,Math.max(0.1,semiheight));

            center_proj =  a_proj.plus(b_proj).times(0.5);
            center = center_proj.toLatLong(a);

            radius = 4 * Math.pow(semiheight,2) + Math.pow(distance,2);
            radius /= 8 * semiheight;
            focusheight = semiheight*8;

            //compute circle radii
            direction = b_proj.minus(a_proj).times(1/distance); //direction vector between a  and b
            normal = new Vec(-direction.getY(), direction.getX()); // normal between a and b;
            //Vec up = normal.times(Math.min(radius,Math.max(0,radius-semiheight))); //how much to shift?
            Vec up = normal.times(Math.abs(radius-semiheight));

            circle_center_upper_proj  = center_proj.plus(up);
            circle_center_lower_proj  = center_proj.minus(up);
            //left = factory.createLineString(new Coordinate[]{new Coordinate(a_proj.plus(normal.times(focusheight)).toCoordinate()),new Coordinate(a_proj.minus(normal.times(focusheight)).toCoordinate())});
            //define full focus geometry:
            //this focus geometry will have the following area: (4*r) * (2*r)
            if (resetratio) focusratio = 1;
            fullfocus_proj = generateFocusGeom();
            Coordinate[] tmp = fullfocus_proj.getCoordinates();
            ortho_geom = factory.createLineString(new Coordinate[]{tmp[0],tmp[tmp.length-1]});


            //define "lower"/"upper" focus geometry.
            // Those will decide into which (sign) direction we'll shift the point.
            upperfocus_proj = generateUpperFocusGeom();
            upperfocus_default  = upperfocus_proj;
            lowerfocus_proj = generateLowerFocusGeom();
            uppercircle = GISHelper.createCircle(circle_center_upper_proj.getX(), circle_center_upper_proj.getY(), radius);
            lowercircle = GISHelper.createCircle(circle_center_lower_proj.getX(), circle_center_lower_proj.getY(), radius);

           overlay=  constructOverlay();
    }

    public void setFocusRatio(double ratio){
        focusratio = Math.min(1,Math.max((semiheight * 2)/focusheight, ratio));
        upperfocus_proj = generateUpperFocusGeom();
        lowerfocus_proj = generateLowerFocusGeom();
        fullfocus_proj = generateFocusGeom();
    }

    public double getFocusheight(){
        return focusheight;
}


    public Polygon getFocusGeom(){
        return fullfocus_proj;
    }
    private Polygon generateFocusGeom(){
        Vec[] tmp = new Vec[5];
        tmp[0] =  a_proj.plus(normal.times(focusheight*focusratio));
        tmp[1] =  b_proj.plus(normal.times(focusheight*focusratio));
        tmp[2] =  b_proj.minus(normal.times(focusheight*focusratio));
        tmp[3] =  a_proj.minus(normal.times(focusheight*focusratio));
        tmp[4] =  a_proj.plus(normal.times(focusheight*focusratio));
        return constructPolygon(tmp);
    }

    public Polygon getUpperFocusGeom(){
        return upperfocus_proj;
    }
    public Polygon getUpperFocusDefaultGeom(){
        return upperfocus_default;
    }
    private Polygon generateUpperFocusGeom(){ //actual lower
        Vec[] tmp = new Vec[5];
        tmp[0] =  a_proj;
        tmp[1] =  a_proj.plus(normal.times(focusheight*focusratio));
        tmp[2] =  b_proj.plus(normal.times(focusheight*focusratio));
        tmp[3] =  b_proj;
        tmp[4] =  a_proj;
        return constructPolygon(tmp);
    }


    private Polygon generateLowerFocusGeom(){
        Vec[] tmp = new Vec[5];
        tmp[0] =  a_proj;
        tmp[1] =  b_proj;
        tmp[2] =  b_proj.minus(normal.times(focusheight*focusratio));
        tmp[3] =  a_proj.minus(normal.times(focusheight*focusratio));
        tmp[4] =  a_proj;
        return constructPolygon(tmp);
    }

    public List<LatLong> getUpperPolygonLatLng(){
        List<LatLong> tmp = new ArrayList<>();
        tmp.add(a);
        tmp.add(a_proj.plus(normal.times(focusheight*focusratio)).toLatLong(a));
        tmp.add(b_proj.plus(normal.times(focusheight*focusratio)).toLatLong(b));
        tmp.add(b);
        tmp.add(a);
        return tmp;
    }
    public List<LatLong> getLowerPolygonLatLng(){
        List<LatLong> tmp = new ArrayList<>();
        tmp.add(a);
        tmp.add(b_proj.toLatLong(b));
        tmp.add(b_proj.minus(normal.times(focusheight*focusratio)).toLatLong(b));
        tmp.add(a_proj.minus(normal.times(focusheight*focusratio)).toLatLong(a));
        tmp.add(a);
        return tmp;
    }

    public LatLong getM2(){
        return center_proj.plus(normal.times(focusheight*focusratio)).toLatLong(center);
    }

    public LineString getCutlineGeom() {
        return cutline_geom;
    }

    public LineString getOrthoGeom() {
        return ortho_geom;
    }

    private List<LatLong> constructOverlay(){
        List<LatLong> l = new ArrayList<>();
        double x,y;
        //to-do: limit range of degrees:
        for (double d= 0; d<= 2* Math.PI; d+= 0.01){
            x=  circle_center_lower_proj.getX() + radius * Math.cos(d);
            y = circle_center_lower_proj.getY() + radius * Math.sin(d);
            if (upperfocus_proj.contains(factory.createPoint(new Coordinate(x, y)))){
                l.add(new Vec(x,y).toLatLong(a));
            }
        }

        for (double d= 0; d<= 2* Math.PI; d+=0.01){
            x=  circle_center_upper_proj.getX() + radius * Math.cos(d);
            y = circle_center_upper_proj.getY() + radius * Math.sin(d);
            if (lowerfocus_proj.contains(factory.createPoint(new Coordinate(x, y)))){
                l.add(new Vec(x,y).toLatLong(a));
            }
        }
        return l;
    }
    public LatLong getM(){
        return center_proj.plus(normal.times(semiheight)).toLatLong(center);
    }

    public LatLong getA() { return a; }
    public LatLong getB() { return b; }

    public void updateOverlayGeometry(){
        Coordinate[] c = new Coordinate[overlay.size()+1];
        int i= 0;
        for (LatLong latLong : overlay) {
            c[i] = GISHelper.Deg2UTM(latLong).toCoordinate();
            i++;
        }
        c[i] = c[0];//closed linear ring
        overlaygeom = factory.createPolygon(c);
    }
    public List<LatLong> getOverlay(){
        return overlay;
    }

    public Vec getTranslate(Point p){
        int d;
        int i;
        Geometry c;
        if (upperfocus_proj.contains(p)){
            d = 1;
            i = 1;
            c = lowercircle;
        } else if (lowerfocus_proj.contains(p)){
            c = uppercircle;
            d = -1;
            i = 0;
        }  else{
            //d= 0 won't happen as we know that fullfocus_geom.contains(p)
            return new Vec(0,0);
        }

        Vec pv = new Vec(p.getX(), p.getY());
        Vec pv_up = pv.plus(normal.times((focusheight*focusratio)));
        Vec pv_down = pv.minus(normal.times(focusheight*focusratio));
        LineString l = factory.createLineString(new Coordinate[]{pv_down.toCoordinate(), pv_up.toCoordinate()});
        if (c.intersects(l)){
            Coordinate circlecut = c.intersection(l).getCoordinates()[i];
            Point circlecut2 =  factory.createPoint(circlecut);

            double h = cutline_geom.distance(circlecut2);
            if (overlaygeom.contains(p)) return normal.times(h*d);
            double y = cutline_geom.distance(p);
            double dy =  h * (focusheight*focusratio-y)/(focusheight*focusratio-h);
            return normal.times(dy*d);
        } else {
            //this wont happen, I promise!
            return new Vec(0,0);
        }
    }

    public void setShapeExtents(double h, double t){
        this.semiheight = h;
        heightratio = h/a_proj.getDistance(b_proj);
        focusheight = 1.0;
        focusratio = 1.0;
        update(false);
    }

    public void setAB(LatLong p1, LatLong p2){
        Vec tmp1 =  GISHelper.Deg2UTM(p1);
        Vec tmp2 = GISHelper.Deg2UTM(p2);
        if (tmp1.getX() <= tmp2.getX()){
            a = p1;
            b = p2;
            a_proj = tmp1;
            b_proj = tmp2;
        } else {
            a = p2;
            b = p1;
            a_proj = tmp2;
            b_proj = tmp1;
        }
        this.semiheight = a_proj.getDistance(b_proj)*heightratio;
        update(true);
    }

    private Polygon constructPolygon(Vec[] v){
        Coordinate[] c = new Coordinate[v.length];
        for (int i= 0; i< c.length; i++) c[i] = new Coordinate(v[i].getX(), v[i].getY());
        return factory.createPolygon(c);
    }

    public Integer getID(){
        return id;
    }

    public double getFocusArea(){
        return fullfocus_proj.getArea();
    }

    public void setActive(boolean val){
        active = val;
    }

    public boolean isActive(){
        return active;
    }
    public boolean isRect(){return false;}

    public double getAlpha(){
        double h = a_proj.getDistance(b_proj)/2;
        double alpha = (Math.sqrt(Math.pow(radius,2) - Math.pow(h,2)))/radius;
        return Math.toDegrees(Math.acos(alpha));
    }

    public double getRadius(){
        return radius;
    }

    public double getAA(){
        return focusheight*focusratio*2;
    }

    public double getCutlineLength(){
        return a_proj.getDistance(b_proj);
    }
}
