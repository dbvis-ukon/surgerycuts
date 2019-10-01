/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import com.vividsolutions.jts.geom.Coordinate;
import org.mapsforge.core.model.LatLong;

public class Vec {
    private double x;
    private double y;

    public Vec(double x, double y){
        this.x = x;
        this.y = y;
    }

    private double getLength(){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); 
    }

    public Vec plus(Vec v){
        return new Vec(this.x+v.getX(), this.y+v.getY());
    }

    public Vec minus(Vec v){
        return new Vec(this.x-v.getX(), this.y-v.getY());
    }

    public Vec times(double scalar){
        return new Vec(this.x * scalar, this.y * scalar);
    }

    public double getDistance(Vec v){
        Vec d = new Vec(this.x - v.getX(), this.y-v.getY());
        return d.getLength();
    }

    public LatLong toLatLong(LatLong ref){
        return  GISHelper.UTM2Deg(new Vec(this.x, this.y),ref);
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public Coordinate toCoordinate(){
        return new Coordinate(x,y);
    }

    public String toString(){
        return x + " " + y;
    }
}
