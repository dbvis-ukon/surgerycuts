/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.mapsforge.core.model.LatLong;

public class GISHelper {
    
    public static Vec Deg2UTM(LatLong c)
    {   char letter = getZoneLetter(c);
        //int zone= (int) Math.floor(c.getLongitude()/6+31);
        int zone = getZoneNumber(c);
        double easting, northing;
        easting=0.5*Math.log((1+Math.cos(c.getLatitude()*Math.PI/180)*Math.sin(c.getLongitude()*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(c.getLatitude()*Math.PI/180)*Math.sin(c.getLongitude()*Math.PI/180-(6*zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(c.getLatitude()*Math.PI/180)*Math.sin(c.getLongitude()*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(c.getLatitude()*Math.PI/180)*Math.sin(c.getLongitude()*Math.PI/180-(6*zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2)/3)+500000;
        easting=Math.round(easting*100)*0.01;
        northing = (Math.atan(Math.tan(c.getLatitude()*Math.PI/180)/Math.cos((c.getLongitude()*Math.PI/180-(6*zone -183)*Math.PI/180)))-c.getLatitude()*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(c.getLatitude()*Math.PI/180)*Math.sin((c.getLongitude()*Math.PI/180-(6*zone -183)*Math.PI/180)))/(1-Math.cos(c.getLatitude()*Math.PI/180)*Math.sin((c.getLongitude()*Math.PI/180-(6*zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2))+0.9996*6399593.625*(c.getLatitude()*Math.PI/180-0.005054622556*(c.getLatitude()*Math.PI/180+Math.sin(2*c.getLatitude()*Math.PI/180)/2)+4.258201531e-05*(3*(c.getLatitude()*Math.PI/180+Math.sin(2*c.getLatitude()*Math.PI/180)/2)+Math.sin(2*c.getLatitude()*Math.PI/180)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(c.getLatitude()*Math.PI/180+Math.sin(2*c.getLatitude()*Math.PI/180)/2)+Math.sin(2*c.getLatitude()*Math.PI/180)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2))/4+Math.sin(2*c.getLatitude()*Math.PI/180)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2)*Math.pow(Math.cos(c.getLatitude()*Math.PI/180),2))/3);
        if (letter<'M')
            northing = northing + 10000000;
        northing=Math.round(northing*100)*0.01;
        return new Vec(easting, northing);
    }

    public static LatLong UTM2Deg(Vec v, LatLong ref)
    {
        //From: https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
        char letter= getZoneLetter(ref);
        int zone = getZoneNumber(ref);
        double Easting=v.getX();
        double Northing=v.getY();
        double Hem;
        double longitude, latitude;
        if (letter>'M')
            Hem='N';
        else
            Hem='S';
        double north;
        if (Hem == 'S')
            north = Northing - 10000000;
        else
            north = Northing;
        latitude = (north/6366197.724/0.9996+(1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)-0.006739496742*Math.sin(north/6366197.724/0.9996)*Math.cos(north/6366197.724/0.9996)*(Math.atan(Math.cos(Math.atan(( Math.exp((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*( 1 -  0.006739496742*Math.pow((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996 )/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996 - 0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996 )*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996)*3/2)*(Math.atan(Math.cos(Math.atan((Math.exp((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996))*180/Math.PI;
        latitude=Math.round(latitude*10000000);
        latitude=latitude/10000000;
        longitude =Math.atan((Math.exp((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*( north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2* north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3)) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))*180/Math.PI+zone*6-183;
        longitude=Math.round(longitude*10000000);
        longitude=longitude/10000000;
        return new LatLong(latitude,longitude);
    }

    private static int getZoneNumber(LatLong c) {
        int zonenumber = (int) Math.floor((c.getLongitude() + 180) / 6) + 1;
        if (c.getLatitude() >= 56.0 && c.getLatitude() < 64.0 && c.getLongitude() >= 3.0 && c.getLongitude() < 12.0)
            zonenumber = 32;

        if (c.getLatitude() >= 72.0 && c.getLatitude() < 84.0) {
            if (c.getLongitude() >= 0.0 && c.getLongitude() < 9.0) {
                zonenumber = 31;
            } else if (c.getLongitude() >= 9.0 && c.getLongitude() < 21.0) {
                zonenumber = 33;
            } else if (c.getLongitude() >= 21.0 && c.getLongitude() < 33.0) {
                zonenumber = 35;
            } else if (c.getLongitude() >= 33.0 && c.getLongitude() < 42.0) {
                zonenumber = 37;
            }
        }
        return zonenumber;
    }

    private static char getZoneLetter(LatLong c){
        //From: https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
        char letter;
        if (c.getLatitude()<-72)
            letter='C';
        else if (c.getLatitude()<-64)
            letter='D';
        else if (c.getLatitude()<-56)
            letter='E';
        else if (c.getLatitude()<-48)
            letter='F';
        else if (c.getLatitude()<-40)
            letter='G';
        else if (c.getLatitude()<-32)
            letter='H';
        else if (c.getLatitude()<-24)
            letter='J';
        else if (c.getLatitude()<-16)
            letter='K';
        else if (c.getLatitude()<-8)
            letter='L';
        else if (c.getLatitude()<0)
            letter='M';
        else if (c.getLatitude()<8)
            letter='N';
        else if (c.getLatitude()<16)
            letter='P';
        else if (c.getLatitude()<24)
            letter='Q';
        else if (c.getLatitude()<32)
            letter='R';
        else if (c.getLatitude()<40)
            letter='S';
        else if (c.getLatitude()<48)
            letter='T';
        else if (c.getLatitude()<56)
            letter='U';
        else if (c.getLatitude()<64)
            letter='V';
        else if (c.getLatitude()<72)
            letter='W';
        else
            letter='X';
        return letter;
    }

    public static Geometry createCircle(double x, double y, final double RADIUS) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(new Coordinate(x, y));
        shapeFactory.setSize(RADIUS * 2);
        return shapeFactory.createCircle();
    }
}
