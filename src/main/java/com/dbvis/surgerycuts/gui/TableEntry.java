/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.gui;

public class TableEntry {
    Boolean checked;
    int id;
    String ts;
    double area;

    TableEntry(int id, String ts, double area){
        this.checked = true;
        this.id = id;
        this.ts = ts;
        this.area = area;
    }

    public int getID(){
        return id;
    }
    String getTS(){
        return ts;
    }
    public double getArea(){
        return area;
    }
    Boolean getChecked(){ return checked;}
    void setID(int id){
        this.id = id;
    }
    void setTS(String ts){
        this.ts = ts;
    }
    public void setArea(double area){
        this.area = area;
    }
    void setChecked(Boolean val){ checked = val; }
}
