/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.renderer;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.ReadBuffer;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Renderer {
    private static final  GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;

    private static String in;
    private static MapView mv;
    private static BoundingBox bb;
    private static Exporter exporter;
    private MouseEvents e;
    private static ArrayList<Layer> extralayer;
    private static Paint paintfill;
    private static Paint paintstroke;
    private static Paint paintoverlay;
    
    public Renderer(String in){
        this.in = in;

        extralayer = new ArrayList<>();
        //instantiate paint object
        paintfill = GRAPHIC_FACTORY.createPaint(); //for filling circles
        paintfill.setColor(0x110000ff);
        paintfill.setStyle(Style.FILL);

        paintstroke = GRAPHIC_FACTORY.createPaint(); // for polylines and circle boundaries
        paintstroke.setStrokeWidth(1f);
        paintstroke.setStyle(Style.STROKE);

        paintoverlay = GRAPHIC_FACTORY.createPaint(); //for filling circles
        paintoverlay.setColor(0xff0000ff);//Color.BLUE
        paintoverlay.setStyle(Style.FILL);

        initMapsforge(); 
    }
    
    private static void initMapsforge(){
        AwtGraphicFactory.clearResourceMemoryCache();
        ReadBuffer.setMaximumBufferSize(650000000);
        MapWorkerPool.NUMBER_OF_THREADS = 1;

        MapView mv = createMapView();

        bb = setLayer(getInitMap(), true, true);
        mv.setZoomLevel((byte) 14);
        mv.setCenter(bb.getCenterPoint());

        exporter = new Exporter();
    }

    public MapView getMapView(){
        return mv;
    }
    
    private static File getInitMap(){
        return new File(in);
    }
    
    private static MapView createMapView(){
        mv = new MapView();
        mv.getMapScaleBar().setVisible(true);
        return mv; 
    }
    public void setInitMap(String i){
        in = i;
    }

    public static BoundingBox setLayer( File f, boolean debug, boolean recenter){
        mv.getModel().displayModel.setFixedTileSize(512);
        MultiMapDataStore mapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
        mapDataStore.addMapDataStore(new MapFile(f), true, true);
        TileRendererLayer tilerenderer = createTileRendererLayer(createTileCache(128), mapDataStore, mv.getModel().mapViewPosition);
        tilerenderer.setTextScale(0.00000001f);
        Layers l = mv.getLayerManager().getLayers();

        l.clear();
        l.add(tilerenderer);

        /*
        if (debug){
            l.add(new TileGridLayer(GRAPHIC_FACTORY, mv.getModel().displayModel));
            l.add(new TileCoordinatesLayer(GRAPHIC_FACTORY, mv.getModel().displayModel));
        }
        */

        bb = mapDataStore.boundingBox();
        //mv.getModel().mapViewPosition.setMapLimit(bb);
        if (recenter) mv.setCenter(bb.getCenterPoint());
        return bb;
    }
    
    private static TileCache createTileCache(int cap){
        TileCache memcache = new InMemoryTileCache(cap);
        File cachedir = new File(System.getProperty("user.dir") + "/renderer/cache");
        TileCache fscache = new FileSystemTileCache(1024,cachedir, GRAPHIC_FACTORY );
        return new TwoLevelTileCache(memcache,fscache);
    }
    
    private static TileRendererLayer createTileRendererLayer(TileCache cache, MapDataStore ds, MapViewPosition pos){
        TileRendererLayer renderer = new TileRendererLayer(cache,ds,pos, GRAPHIC_FACTORY){};
        File external = new File(System.getProperty("user.dir")  + "/renderer/theme.xml");
        if (external.exists()){
            try {
                renderer.setXmlRenderTheme(new ExternalRenderTheme(external));
            } catch (FileNotFoundException e1) {
                renderer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
            }
        } else {
            renderer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        }
        return renderer; 
    }

    public static void drawOverlay(List<LatLong> l){
        Polygon p = new Polygon(paintoverlay, null, GRAPHIC_FACTORY);
        p.getLatLongs().addAll(l);
        mv.getLayerManager().getLayers().add(p);
    }

    public static void removeExtralayers(){
        Iterator<Layer> iter = extralayer.iterator();
        Layers all = mv.getLayerManager().getLayers();
        while (iter.hasNext()){
            Layer l = iter.next();
            all.remove(l);
        }
    }

    public void export(){
        Long ts = System.currentTimeMillis()/1000;
        exporter.setFile(e.getRecentTMap());
        File outtransformed = new File(System.getProperty("user.dir").concat("/web_public/d/") +ts + ".png");
        exporter.run(mv.getBoundingBox(),mv.getModel().mapViewPosition.getZoomLevel(), e.getShapes(), outtransformed, true);
    }

    public void setEventListener(MouseEvents e){
        if (this.e != null) setEventListenersActive(false);
        this.e = e;
        setEventListenersActive(true);
    }

    public void setEventListenersActive(boolean activate){
        MouseListener[] listener = mv.getMouseListeners();
        if (listener.length > 1) mv.removeMouseListener(listener[1]);
        MouseMotionListener[] listener2 = mv.getMouseMotionListeners();
        if (listener2.length > 1) mv.removeMouseMotionListener(listener2[1]);
        if (activate){
            mv.addMouseMotionListener(this.e);
            mv.addMouseListener(this.e);
        }

    }
}
