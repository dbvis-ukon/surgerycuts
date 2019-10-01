/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.renderer;

import com.dbvis.surgerycuts.cut.Metrics;
import com.dbvis.surgerycuts.cut.Shape;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.FixedTileSizeDisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Phil on 18.01.2018.
 */
public final class Exporter {
    private FileSystemTileCache tileCache;
    private TileBasedLabelStore tileBasedLabelStore;
    private RenderThemeFuture rtf;
    private DisplayModel dm;
    private MapDataStore map;
    private int tilesize;
    private int height;
    private int width;

    public Exporter(){
        GraphicFactory gf = AwtGraphicFactory.INSTANCE;
        tilesize = 2048;
        dm = new FixedTileSizeDisplayModel(tilesize);
        dm.setUserScaleFactor(5.0f);
        File external = new File(System.getProperty("user.dir")  + "/renderer/theme.xml");
        if (external.exists()){
            try {
                rtf = new RenderThemeFuture(gf, new ExternalRenderTheme(external), dm);
            } catch (FileNotFoundException e1) {
                rtf = new RenderThemeFuture(gf, InternalRenderTheme.OSMARENDER, dm);
            }
        } else {
            rtf = new RenderThemeFuture(gf, InternalRenderTheme.OSMARENDER, dm);
        }
        File cacheDir = new File(System.getProperty("user.dir").concat("/exporter/"));
        tileCache = new FileSystemTileCache(10, cacheDir, gf, false);
        tileBasedLabelStore = new TileBasedLabelStore(tileCache.getCapacityFirstLevel());
        tileBasedLabelStore.clear();
        Thread t = new Thread(rtf);
        t.start();
    }

    public void setFile(File f){
        map = new MapFile(f);
    }

    public  void run(BoundingBox view_bb, byte zoom, java.util.List<Shape> shapes, File out, boolean withCoordinates) {
        GraphicFactory gf = AwtGraphicFactory.INSTANCE;
        // Create renderer.
        DatabaseRenderer renderer = new DatabaseRenderer(map, gf, tileCache, tileBasedLabelStore, false, true);
        int tx0 = MercatorProjection.longitudeToTileX(view_bb.minLongitude, zoom);
        int ty0 = MercatorProjection.latitudeToTileY(view_bb.maxLatitude, zoom);
        int tx1 = MercatorProjection.longitudeToTileX(view_bb.maxLongitude, zoom);
        int ty1 = MercatorProjection.latitudeToTileY(view_bb.minLatitude, zoom);

        BufferedImage merged = new BufferedImage((Math.abs(tx0-tx1)+1)*tilesize, (Math.abs(ty0-ty1)+1)*tilesize, BufferedImage.TYPE_INT_RGB);
        Graphics g = merged.getGraphics();
        for (int x= tx0; x <= tx1; x++ ){
            for (int y= ty0; y<=ty1; y++){
                Tile tile = new Tile(x, y, zoom, tilesize);
                RendererJob job = new RendererJob(tile, map, rtf, dm, 1.0f, false, false);
                TileBitmap tb = renderer.executeJob(job);
                BufferedImage bi = AwtGraphicFactory.getBitmap(tb);
                g.drawImage(bi, (x-tx0)*tilesize, (y-ty0)*tilesize, tilesize, tilesize, null);
            }
        }
        g.dispose();

        BoundingBox tilebb_UL = (new Tile(tx0, ty0, zoom, tilesize)).getBoundingBox();

        double view_leftx = MercatorProjection.longitudeToPixelX(view_bb.minLongitude, zoom, tilesize);
        double view_rightx = MercatorProjection.longitudeToPixelX(view_bb.maxLongitude, zoom, tilesize);
        double tile_rightx = MercatorProjection.longitudeToPixelX(tilebb_UL.maxLongitude, zoom, tilesize);
        int padding_left = (int) (tile_rightx - view_leftx);
        padding_left = tilesize - padding_left;

        double view_topy = MercatorProjection.latitudeToPixelY(view_bb.maxLatitude, zoom, tilesize);
        double view_bottomy = MercatorProjection.latitudeToPixelY(view_bb.minLatitude, zoom, tilesize);
        double tile_bottomy = MercatorProjection.latitudeToPixelY(tilebb_UL.minLatitude, zoom, tilesize);
        int padding_top = (int) (tile_bottomy - view_topy);
        padding_top = tilesize - padding_top;

        width = (int) (view_rightx - view_leftx);
        height = (int) (view_bottomy - view_topy);

        BufferedImage cropped = merged.getSubimage(padding_left, padding_top, width, height);

        try {
            ImageIO.write(cropped, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.close();

        if (withCoordinates) exportShapes(shapes, view_bb,zoom, new File(out.getAbsolutePath().replace(".png", ".json")));
    }

    private void exportShapes(java.util.List<Shape> shapes,BoundingBox view_bb, byte zoom, File json){
        JSONObject root = new JSONObject();
        root.put("ts", Long.parseLong(json.getName().replace(".json", "")));
        root.put("width", width);
        root.put("height", height);
        root.put("minlat", view_bb.minLatitude);
        root.put("maxlat", view_bb.maxLatitude);
        root.put("minlon", view_bb.minLongitude);
        root.put("maxlon", view_bb.maxLongitude);

        JSONArray shapesarray = new JSONArray();

        double view_leftx = MercatorProjection.longitudeToPixelX(view_bb.minLongitude, zoom, tilesize);
        double view_topy = MercatorProjection.latitudeToPixelY(view_bb.maxLatitude, zoom, tilesize);
        for (Shape shape: shapes){
            if (shape.isActive()){
                JSONObject shapeobj = new JSONObject();
                shapeobj.put("id", shape.getID());
                shapeobj.put("isrect", shape.isRect());

                    shapeobj.put("m", geoToJson(shape.getM(), zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("m2", geoToJson(shape.getM2(), zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("a",  geoToJson(shape.getA(), zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("b", geoToJson(shape.getB(), zoom, tilesize, view_leftx, view_topy));

                    shapeobj.put("shapesize", Metrics.getRoom(shape));
                    shapeobj.put("distorted", Metrics.getDistortedArea(shape));
                    shapeobj.put("ratio", Metrics.getDistortedAreaRoomRatio(shape));
                    shapeobj.put("whole", Metrics.getWholeArea(shape));

                JSONArray upperrect = new JSONArray();
                    for (LatLong coords : shape.getUpperPolygonLatLng()) upperrect.add(geoToJson(coords, zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("upperrect", upperrect);

                    JSONArray lowerrect = new JSONArray();
                    for (LatLong coords : shape.getLowerPolygonLatLng()) lowerrect.add(geoToJson(coords, zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("lowerrect", lowerrect);

                JSONArray overlay = new JSONArray();
                    for (LatLong coords : shape.getOverlay()) overlay.add(geoToJson(coords, zoom, tilesize, view_leftx, view_topy));
                    shapeobj.put("overlay", overlay);

                JSONArray focus = new JSONArray();
                for (LatLong coords : shape.getOverlay()){
                    focus.add(geoToJson(coords, zoom, tilesize, view_leftx, view_topy));
                }
                shapeobj.put("focus", overlay);

                    shapesarray.add(shapeobj);
            }
        }
        root.put("shapes", shapesarray);
        try (FileWriter file = new FileWriter(json)) {
            file.write(root.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray geoToJson(LatLong coords,  byte zoom, int tilesize,   double left, double top ){
        double x = MercatorProjection.longitudeToPixelX(coords.longitude, zoom, tilesize) - left;
        double y = MercatorProjection.latitudeToPixelY(coords.latitude, zoom, tilesize) - top;
        JSONArray a = new JSONArray();
        a.add( x );
        a.add(y);
        return a;
    }
}
