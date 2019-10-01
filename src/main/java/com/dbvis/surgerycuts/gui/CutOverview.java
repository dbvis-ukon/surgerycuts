/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.gui;

import com.dbvis.surgerycuts.cut.Shape;
import com.dbvis.surgerycuts.renderer.MouseEvents;
import javax.swing.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CutOverview {
    private JPanel overviewpanel;
    private JTable overviewtable;
    private CutTableModel model;
    private HashMap<Integer, Shape> shapes;
    private SimpleDateFormat sdf;
    private JFrame frame;
    private MouseEvents events;

    public CutOverview() {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        shapes = new HashMap<>();

        frame = new JFrame("Cut Overview");
        frame.setSize(450, 300);
        frame.setResizable(false);
        frame.setVisible(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        frame.setAlwaysOnTop(true);
        List l = new ArrayList();
        model = new CutTableModel(l);

        model.addTableModelListener(e -> {
                for (int i = 0; i < model.getRowCount(); i++) {
                    events.setShapeActive((int) model.getValueAt(i,1), (boolean) model.getValueAt(i,0));
                }
        });

        JPanel contentPane = (JPanel) frame.getContentPane();
        contentPane.removeAll();
        JScrollPane scroll = new JScrollPane();

        overviewpanel = new JPanel();
        overviewtable = new JTable(model);
        scroll.getViewport().add(overviewtable);
        overviewpanel.add(scroll);

        contentPane.add(overviewpanel);
        contentPane.revalidate();
        contentPane.repaint();

        frame.setContentPane(contentPane);
    }

    public void set(Shape shape){
        if (shapes.containsKey(shape.getID())){
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1).equals(shape.getID())) {
                    model.setValueAt(shape.getID(), i, 1);
                    model.setValueAt(sdf.format(new Timestamp(System.currentTimeMillis())), i, 2);
                    model.setValueAt(shape.getFocusArea(), i, 3);
                }
            }
        } else {
            model.addRow(new TableEntry(shape.getID(), sdf.format(new Timestamp(System.currentTimeMillis())), shape.getFocusArea()));
            shapes.put(shape.getID(), shape);
        }

        overviewtable.setModel(model);
        overviewtable.updateUI();
    }

    public void toggleVisibility(){
        frame.setVisible(!frame.isVisible());
    }

    public void setEventHandler(MouseEvents ev){
        events  = ev;
    }
}
