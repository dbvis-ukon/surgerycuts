/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

var GuiHandler = function(){
   var p = this;

   function _constructor(){
        _init();
       p.fillDatatable();
   }

   function _init(){
       $('#settings').on('mouseenter', function(){
           $(this).css('opacity', 1.0)
       });
       $('#settings').on('mouseleave', function(){
           $(this).css('opacity', 0.1)
       });
       $('#overview').DataTable({
           paging: false,
           bFilter: false,
           info: false,
           scrollY: "100px",
           order: [[ 2, "desc" ]], //order by unix ts
           language: {
               emptyTable: "No cuts available."
           },
           select: {
               style:    'multi',
               selector: 'td:first-child'
           },
           columnDefs: [
               {
                   visible: false,
                   targets:   2
               }]
       });
       $('#overview tbody').on('click', 'tr', function(){
           if (!$(this).hasClass('selected')) {
               var vis = $('#canvas').data('public');
               var that = this;
               var tbl = $('#overview').DataTable();
               var cutid = tbl.row($(this)).data()[2];
               $('#overview').data('public').setJSON(cutid, function (d) {
                   vis.updateBackground(d);
                   vis.updateGeom(d);
                   $('tr.selected').removeClass('selected');
                   $(that).addClass('selected');
               }); //if required, request json from server
           } else {
               $(this).removeClass('selected');
           }
       });
   }
   p.fillDatatable = function(){
       var format = d3.timeFormat("%d.%m.%y %H:%M:%S")
       $.get({
           url: "/getcuts",
           success: function (d) {
               var tbl = $('#overview').DataTable();
               tbl.clear();
               d.split(";").forEach(function(f,i){
                   tbl.row.add([i, format(new Date(f*1000)), f]);
               });
               tbl.draw();
           }
       });
   };

    _constructor();
};