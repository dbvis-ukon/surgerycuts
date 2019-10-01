SurgeryCuts V1.2 
=============================================================================================================================================

Setup Instructions 
-------------------------------------
1. Clone/Download and extract the repository. 
2. During runtime, we convert .osm xml to .map files, which are readable by the custom rendering engine Mapsforge. 
   For this, you need to setup the osmosis library including Mapsforge map-writer plugin. 

    2.1 Install osmosis: https://wiki.openstreetmap.org/wiki/Osmosis#How_to_install
    
    2.2 Add map-writer plugin. See https://github.com/mapsforge/mapsforge/blob/master/docs/Getting-Started-Map-Writer.md. 
    
    2.3 Add the "osmosis/bin" folder to your PATH variable.  
    
3. Perform a double-click on the surgerycuts-1.2.jar or run via cmd line "java -jar surgerycuts-1.2.jar". 
    If you want to distort really large maps, make sure to have enough java heap space assigned. 

3. OR: Import the repository as a maven project into your favorite IDE. Specify the JDK, eventually increase heap space, and set
    the run configuration (-> com.dbvis.surgerycuts.Main.java). 

Interaction 
-------------------------------------
Shape Selection: 
1. Perform a left-click at two different map positions. Based on the resulting cutline, we propose initial shape and distortion properties.
2. Edit a shape by clicking on a black circle: it gets colored in green and you can apply any new position by another left-click.
3. If you want to apply the surgery cut: Simply perform a right-click on the map.
4. All points and polygons used for sketching the surgery shape, can be reset via menu: "View/Reset Markers"

Import your own .osm file: 
1. Via File/Import Input Map
2. OR: via File/Recent Input Maps/...

Exporting graphics: 
1. While watching the transformed map, just click on "View/...to Server"
2. You can open the exported map, e.g., in your web browser at localhost:3000/index.html. According files are saved in the folder "web_public/d". 

