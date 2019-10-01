/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */
package com.dbvis.surgerycuts.misc;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class MacConverter implements Converter {

    private JProgressBar progress;
    private MessageFormat cmd;

    public MacConverter(MessageFormat cmd, JProgressBar progress){
        this.cmd = cmd;
        this.progress = progress;
    }

    public int OsmToMap(String in, String out, boolean withprogress){
        Object[] arguments = {in, out};
        progress.setString("Convert osm.");
        String command = cmd.format(arguments);

        try {
            System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String l = reader.readLine();
            while (l != null){
                System.out.println(l);
                l = reader.readLine();
            }
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            l = reader2.readLine();
            while (l != null){
                System.out.println(l);
                l = reader2.readLine();
            }

            int returnCode = p.waitFor();
            progress.setString("Ready.");
            return returnCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int OsmToMap(String in, boolean withprogress){ //for sample files
        return OsmToMap(in, in.replace(".osm", ".map"), withprogress);
    }

    public JProgressBar getProgressbar(){
        return progress;
    }
}
