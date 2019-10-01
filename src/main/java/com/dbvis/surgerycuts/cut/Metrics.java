/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.cut;

public class Metrics {
    public static double getRoom(Shape s){ //get ACA
        double radius = s.getRadius();
        double alpha = Math.toRadians(s.getAlpha());
        return  2 * Math.pow(radius,2) * (alpha - Math.sin(alpha)*Math.cos(alpha));
    }

    public static double getDistortedArea(Shape s){ //whole distortion-absorbing area without ACA
        return getWholeArea(s) - getRoom(s);
    }

    public static double getWholeArea(Shape s){ //whole distortion-absorbing area
        //double alpha = Math.toRadians(s.getAlpha());
        //double h = s.getRadius() * Math.sin(alpha) *2;
        double h = s.getCutlineLength();
        return s.getAA() * h;
    }

    public static double getDistortedAreaRoomRatio(Shape s){
        return getDistortedArea(s)/getRoom(s);
    }
}
