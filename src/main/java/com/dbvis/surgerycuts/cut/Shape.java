/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.cut;

import com.dbvis.surgerycuts.misc.Vec;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.mapsforge.core.model.LatLong;
import java.util.List;

public interface Shape {

    Vec getTranslate(Point p);
    LineString getCutlineGeom();
    LineString getOrthoGeom();
    boolean isActive();
    void setB(LatLong b);
    Polygon getUpperFocusGeom();
    Polygon getUpperFocusDefaultGeom();
    void setFocusRatio(double ratio);
    double getFocusheight();
    void setShapeExtents(double h, double t);
    List<LatLong> getOverlay();
    LatLong getM2();
    LatLong getM();
    LatLong getA();
    LatLong getB();
    Polygon getFocusGeom();

    void setAB(LatLong a, LatLong b);
    void updateOverlayGeometry();
    Integer getID();
    void setActive(boolean b);
    List<LatLong> getLowerPolygonLatLng();
    List<LatLong> getUpperPolygonLatLng();
    double getFocusArea();
    boolean isRect();
    double getAlpha();
    double getRadius();
    double getAA();
    double getCutlineLength();
}
