/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts;

import com.dbvis.surgerycuts.gui.GUIHandler;
import java.io.File;

public final  class SCInit {
    public static void run(){
        double freemegabytes = Runtime.getRuntime().freeMemory()/(1024*1024);
        double maxmegabytes = Runtime.getRuntime().maxMemory()/(1024*1024);
        double totalmegabytes = Runtime.getRuntime().totalMemory()/(1024*1024);
        System.out.println("Available heap size:".concat(freemegabytes+ ""));
        System.out.println("Max heap size:".concat(maxmegabytes+ ""));
        System.out.println("Total heap size:".concat(totalmegabytes+ ""));


        String[] old = new String[]{"out1.map", "out2.map", "out1.osm", "out2.osm"};
        String root = System.getProperty("user.dir").concat("/data/out/");
        File f;
        for (String o: old) {
            f = new File(root.concat(o));
            if (f.delete()){
                System.out.println("Deleted old ".concat(o));
            } else {
                System.out.println(o.concat(" was not deleted."));
            }
        }
        root = System.getProperty("user.dir").concat("/init/");
        f = new File(root.concat("init.map"));
        if (f.delete()){
            System.out.println("Deleted old ".concat("init.map"));
        } else {
            if (f.exists()){
                System.out.println("Could not delete old temp data.");
                return;
            }
        }
        new GUIHandler("ram", 0.01,5.0);
    }
}
