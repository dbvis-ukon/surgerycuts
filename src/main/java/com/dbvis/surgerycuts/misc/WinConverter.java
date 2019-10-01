/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class WinConverter implements Converter {

    private JProgressBar progress;
    private MessageFormat cmd;

    public WinConverter(MessageFormat cmd, JProgressBar progress){
        this.cmd = cmd;
        this.progress = progress;
    }

    public int OsmToMap(String in, String out, boolean withprogress){
        Object[] arguments = {in, out};
        String command = cmd.format(arguments);

        try {
            Process p = Runtime.getRuntime().exec("cmd");
            if (withprogress) {
                new Thread(new SyncPipe(p.getErrorStream(), System.err, progress)).start();
                new Thread(new SyncPipe(p.getInputStream(), System.out, progress)).start();
            } else {
                new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
                new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
            }
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println(command);
            stdin.close();
            int returnCode = p.waitFor();
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