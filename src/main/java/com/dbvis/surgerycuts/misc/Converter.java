/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import javax.swing.*;

public  interface Converter {
    int OsmToMap(String in, String out, boolean withprogress);
    int OsmToMap(String in, boolean withprogress);
    JProgressBar getProgressbar();
}
