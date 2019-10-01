/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.cut;

import java.util.HashMap;
import java.util.Map;

public class ProcessorSettings {
    private Map settings;

    public ProcessorSettings(){
        settings = new HashMap<>(); 
    }

    public void set(String key, Double value){
        settings.put(key, value); 
    }

    public Double get(String key){
        return (Double) settings.getOrDefault(key, 0.0); 
    }
}
