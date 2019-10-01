/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

var Vislayer = function(container){
   var p = this;
   var _bg;
    var _overlays; //blue shapes
    var _svg;
    var _zoom;

   function _constructor(){
       $(container).data('public', p);
       _svg = d3.select(container).append('svg').attr('width', '100%').attr('height', '100%');
       _zoom = d3.zoom()
           .scaleExtent([0.2, 1])
           .on("zoom", function(){
               _bg.attr('transform',d3.zoomTransform(this));
               _svg.selectAll('polygon').attr('transform',d3.zoomTransform(this));
                _svg.selectAll('foreignObject').attr('transform',d3.zoomTransform(this));
            console.log("zoomed");
           });
       _svg.call(_zoom);
       _bg =_svg.append('svg:image').attr('width',0).attr('height',0).attr('transform', 'translate(0,0)scale(1)')//.call(zoom);

        _overlays = _svg.append('g');
   }

   p.updateBackground = function(data){
       _bg.attr("xlink:href", "/d/" + data.ts + ".png")
           .attr('width', data.width).attr('height', data.height);
   };

   p.updateGeom = function(data){
        _updatePoly(data, 'overlay');
        _updatePoly(data, 'upperrect');
         _updatePoly(data, 'lowerrect');
         _updateLabels();
       _svg.call(_zoom.transform, d3.zoomIdentity.scale(1));
    };

    function _updatePoly(data, geomtype){
        var accessor = function(d) {return d[geomtype].join(" ");};
        var polys = _overlays.selectAll('.'+geomtype).data(data.shapes);
        polys.exit().remove();
        polys.enter().append('polygon').classed(geomtype, true)
        _overlays.selectAll('.'+geomtype).attr("points", accessor);
    }

    function _updateLabels(){
    _svg.selectAll('foreignObject').remove();
      _overlays.selectAll('.lowerrect').each(function(d){
            console.log(d);
        var bb = d3.select(this).node().getBBox()

        var f = _svg.append('foreignObject').attr('x', bb.x+bb.width/2-500).attr('y', bb.y+bb.height/2-250).attr('width', '900px').attr('height', '430px');
        var div = f.append('xhtml:div').attr('style', 'width:900px;height:430px;')
        div.html(_format(d));
      })
    }

    function _format(d){
        var out = "Shape Area: " + Math.round(d.shapesize*1000)/1000 + "<br>";
        out += "Distorted Area: " + Math.round(d.distorted*1000)/1000 + "<br>";
        out += "Distorted Ratio: " + Math.round(d.ratio*1000)/1000 + "<br>";
        out += "Entire Area: " + Math.round(d.whole*1000)/1000 + "<br>";
        return out
    }

   _constructor();
};