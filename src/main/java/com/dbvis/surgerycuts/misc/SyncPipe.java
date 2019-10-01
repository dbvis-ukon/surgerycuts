/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import org.apache.commons.lang3.StringUtils;
import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SyncPipe implements Runnable
{
    private JProgressBar progress;
    private InputStream istrm;
    private OutputStream ostrm;

    SyncPipe(InputStream istrm, OutputStream ostrm, JProgressBar progress) {
      this.istrm = istrm;
      this.ostrm = ostrm;
      this.progress = progress;
    }

    SyncPipe(InputStream istrm, OutputStream ostrm) {
        this.istrm = istrm;
        this.ostrm = ostrm;

    }

      public void run() {
            if (progress != null){
                runwithprogress();
            } else {
                runwithoutprogress();
            }
      }

      private void runwithprogress(){
          try
          {
              progress.setString("Convert .osm...");
              progress.setValue(0);
               byte[] buffer = new byte[1024];
              for (int length = 0; (length = istrm.read(buffer)) != -1; )
              {
                  String s = new String(buffer, StandardCharsets.UTF_8);

                  String i = StringUtils.substringBetween(s, "written ", "% of sub file");
                  if (i != null){
                      progress.setValue((int) Double.parseDouble(i));
                      progress.setString(i+"%");
                  } else {
                      String finished = StringUtils.substringBetween(s,"Total execution time: ", " milliseconds");
                      if (finished != null){
                          progress.setString("100%, " +  finished + " ms");
                          progress.setValue(100);
                      }
                  }
                  ostrm.write(buffer, 0, length);
              }
          }
          catch (Exception e)
          {
              e.printStackTrace();
          }
      }

    private void runwithoutprogress(){
        try
        {
            final byte[] buffer = new byte[1024];
            for (int length; (length = istrm.read(buffer)) != -1; )
            {
                ostrm.write(buffer, 0, length);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}