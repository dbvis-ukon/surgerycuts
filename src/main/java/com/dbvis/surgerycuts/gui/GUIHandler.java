/*
 * Publication: 
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685. 
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019). 
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.gui;

import com.dbvis.surgerycuts.cut.Processor;
import com.dbvis.surgerycuts.cut.ProcessorSettings;
import com.dbvis.surgerycuts.misc.Converter;
import com.dbvis.surgerycuts.misc.MacConverter;
import com.dbvis.surgerycuts.misc.WinConverter;
import com.dbvis.surgerycuts.renderer.MouseEvents;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.model.Model;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;

public class GUIHandler {
    private static final String in_default = "rome.map";
    private static String map_root = System.getProperty("user.dir") + "/data/in/";
    private static final String[] out = new String[]{System.getProperty("user.dir") + "/data/out/out1.map",
                                                     System.getProperty("user.dir") + "/data/out/out2.map"};
    private Processor fisheye;
    private MouseEvents events;
    private Converter converter;
    private JButton original, transformed;
    private JCheckBox rounded;
    private com.dbvis.surgerycuts.renderer.Renderer r;
    private JFrame frame;
    private CutOverview CutOverview;

    private JFileChooser fileChooser;
    private JFileChooser exportDialog;
    private JMenuItem exportitem;
    private JMenuItem fisheyepanel;
    private JMenuItem resetmarkers;
    private JMenu recentmenu;
    private String writemode;
    private File selectedfile;
    
    
    public GUIHandler(String writemode, double segmentratio, double minlength){
        ProcessorSettings settings = new ProcessorSettings();
        settings.set("SegmentRatio", segmentratio);
        settings.set("MinSegmentLength", minlength);
        this.writemode = writemode;
        
        fileChooser = new JFileChooser();
        selectedfile= new File(map_root.concat(in_default.replace(".map", ".osm")));
        //run an osmosis test, convert tmp/init.osm to tmp/init.map.
        //first, try to call osmosis via system path variable
        //if this does not work, try osmosis installation in the osmosis subfolder.

        if (osmosistest("osmosis")){
            fisheye = new Processor(converter, settings);
            init();
        } else {
            System.out.println("Could not call osmosis via path variable.");
            if (osmosistest(System.getProperty("user.dir").concat("/osmosis/bin/osmosis"))){
                fisheye = new Processor(converter, settings);
                init();
            } else {
                System.out.println("Could not call osmosis via local installation.");
            }
        }
    }

    private boolean osmosistest(String osmosispath){
        String tmp = System.getProperty("user.dir").concat("/init/init.osm");
        File newfile = new File(tmp.replace(".osm", ".map"));
        String os = System.getProperty("os.name").toLowerCase();
        MessageFormat mf;
        
        if (os.indexOf("mac")>=0){
            mf = new  MessageFormat(osmosispath.concat(" --rx file={0} --mapfile-writer file={1} type="+writemode+""));
            converter = new MacConverter(mf, new JProgressBar(0, 100));
        } else { //for now windows
            mf = new  MessageFormat(osmosispath.concat(" --rx file=\"{0}\" --mapfile-writer file=\"{1}\" type=\""+writemode+"\""));
            converter = new WinConverter(mf, new JProgressBar(0, 100));
        }
            if (converter.OsmToMap(tmp,false) == 0 && newfile.exists()){
            return true;
            } else {
                System.out.println("Osmosis conversion via local installation did not work.");
                return false;
            }
    }

    private void init(){
        frame = new JFrame();
        frame.setTitle("Surgery Cut ("+selectedfile.getAbsolutePath()+")");
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        
        CutOverview = new CutOverview(); // events initialised by initProcessor();

        JMenuBar menuBar = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        JMenu viewmenu = new JMenu("View");
        JMenu aboutmenu = new JMenu("About");

        JMenuItem setinput = new JMenuItem("Import Input Map");
        recentmenu = new JMenu("Recent Input Maps");
        recentmenu.setEnabled(false);

        filemenu.add(setinput);
        filemenu.add(recentmenu);
        menuBar.add(filemenu);

        fisheyepanel = new JMenuItem("Cut Overview");
        viewmenu.add(fisheyepanel);

        exportitem = new JMenuItem("...to Server");
        //exportitem.setEnabled(false);
        viewmenu.add(exportitem);

        resetmarkers = new JMenuItem("Reset Markers");
        viewmenu.add(resetmarkers);

        menuBar.add(viewmenu);
        menuBar.add(aboutmenu);
        frame.setJMenuBar(menuBar);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        rounded = new JCheckBox("Rounded Shape", true);

        original = new JButton("Original Map");
        original.setVerticalTextPosition(AbstractButton.CENTER);
        original.setHorizontalTextPosition(AbstractButton.LEADING);

        transformed = new JButton("Transformed Map");
        transformed.setVerticalTextPosition(AbstractButton.CENTER);
        transformed.setHorizontalTextPosition(AbstractButton.LEADING);

        JProgressBar progress = converter.getProgressbar();
        progress.setValue(0);
        progress.setStringPainted(true);

        controlPanel.add(rounded);
        controlPanel.add(original);
        controlPanel.add(transformed);
        controlPanel.add(progress);
        
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileNameExtensionFilter("OpenStreetMap XML (.osm)", "osm"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        exportDialog = new JFileChooser();
        exportDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        exportDialog.addChoosableFileFilter(new FileNameExtensionFilter(".jpeg", "jpeg"));
        exportDialog.addChoosableFileFilter(new FileNameExtensionFilter(".png", "png"));
        exportDialog.setAcceptAllFileFilterUsed(false);

        initProcessor();
        CutOverview.setEventHandler(events);
        frame.add(controlPanel, BorderLayout.PAGE_START);
        frame.setVisible(true);
        frame.add(r.getMapView());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        transformed.setEnabled(false);
        original.setEnabled(false);

        rounded.addActionListener(e -> {
            events.setRoundedShapes(rounded.isSelected());
        });

        original.addActionListener(e -> {
            String curr = map_root.concat(selectedfile.getName().replace(".osm", ".map"));
            r.setLayer(new File(curr), true, false);
            events.showMarkers();
            r.setEventListenersActive(true);
            resetmarkers.setEnabled(true);
            original.setEnabled(false);
            transformed.setEnabled(true);
            exportitem.setEnabled(false);
        });

        transformed.addActionListener(e -> {
            r.setLayer(new File(out[events.getLastOutput()]), false, false);
            for (com.dbvis.surgerycuts.cut.Shape shape : events.getLastOverlays()){
                r.drawOverlay(shape.getOverlay());
            }
            r.setEventListenersActive(false);
            transformed.setEnabled(false);
            resetmarkers.setEnabled(false);
            original.setEnabled(true);
            exportitem.setEnabled(true);
        });

        setinput.addActionListener(e -> {
            File old = selectedfile;
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File curr = fileChooser.getSelectedFile();
                if (old.getAbsolutePath().equals(curr.getAbsolutePath())) return;
                selectedfile = curr;
                importMap(curr, old);
                resetmarkers.setEnabled(true);
                transformed.setEnabled(false);
                r.setEventListenersActive(true);
                events.resetMap(r);
            }
        });

        exportitem.addActionListener(e -> {
            r.export();
        });

        fisheyepanel.addActionListener(e -> {
            CutOverview.toggleVisibility();
        });

        resetmarkers.addActionListener(e -> events.resetMap(r));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result;
                result = JOptionPane.showConfirmDialog(frame, "Want to close?", "Close", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION && r!= null) {
                        r.getMapView().destroyAll();
                        AwtGraphicFactory.clearResourceMemoryCache();
                        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        File init = new File(System.getProperty("user.dir").concat("/init/init.map"));
                        if (init.delete()){
                            System.out.println("Temp data was deleted.");
                        } else {
                            System.out.println("Temp data was not deleted");
                        }
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
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
            }
        });
    }

    private void initProcessor(){
        r = new com.dbvis.surgerycuts.renderer.Renderer(map_root.concat(in_default));
        Model model= r.getMapView().getModel();
        model.mapViewPosition.setZoomLevel((byte) 14);
        events = new MouseEvents(r, fisheye, original, transformed, exportitem, resetmarkers, CutOverview);
        System.out.println("events initiated.");
        events.setInputPath(map_root.concat(in_default).replace(".map", ".osm"));
        System.out.println("set input map.");
        r.setEventListener(events);
    }

    private void importMap(File selected, File last){
        if (selected.isFile()){

           // cut.setInput(selected);
                String newmap = map_root.concat(selected.getName().replace(".osm", ".map"));
                if (!new File(newmap).exists()){
                    if (converter.OsmToMap(selected.getAbsolutePath(), newmap, true)  != 0) return;
                }
                events.setInputPath(selected.getAbsolutePath());
                JMenuItem sub= new JMenuItem("");
                sub.setToolTipText(last.getAbsolutePath());

                sub.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JMenuItem clicked = (JMenuItem) e.getSource();
                        File old = selectedfile;
                        File newfile = new File(clicked.getToolTipText());
                        selectedfile = newfile;
                        importMap(newfile,old);
                        resetmarkers.setEnabled(true);
                        transformed.setEnabled(false);
                        r.setEventListenersActive(true);
                        events.resetMap(r);
                    }
                });

                recentmenu.setEnabled(true);
                recentmenu.add(sub,0);
                makeMenuUnique(selected.getAbsolutePath());
                if (recentmenu.getItemCount()>7){ //dont save more than 7 .map files in cache + recentmenu
                    File f = new File(recentmenu.getItem(7).getToolTipText());
                    File todelete = new File(map_root.concat(f.getName().replace(".osm", ".map")));
                    todelete.delete();
                    recentmenu.remove(7); //save only the last 7 maps
                }
                setSubitemText();

                frame.setTitle("Surgery Cut ("+selected.getAbsolutePath()+")");
                r.setInitMap(newmap);
                r.setLayer(new File(newmap), true, true);
                original.setEnabled(false);
                resetmarkers.setEnabled(true);
                //exportitem.setEnabled(false);
        }
    }

    private void makeMenuUnique(String newpath){
        for (int i= 0; i< recentmenu.getItemCount();i++){
            JMenuItem curr = recentmenu.getItem(i);
            if (curr.getToolTipText().equals(newpath)){
                recentmenu.remove(i);
                break;
            }
        }
    }

    private void setSubitemText(){
        for (int i= 0; i< recentmenu.getItemCount();i++){
            JMenuItem curr = recentmenu.getItem(i);
            File f = new File(curr.getToolTipText());
            File p = f.getParentFile();
            String name = f.getName();
            if (p.exists()) name = p.getName().concat("/").concat(name);
            name = (i+1) + "-" + name;
            curr.setText(name);
        }
    }
}
