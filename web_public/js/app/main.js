/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

define(['jquery', 'bootstrap', 'jquery-ui', 'd3', 'gui/guiinit',  'datatables.net', "datatables.net.select", "layer/Datalayer", "layer/Vislayer"], function($) {
    $(function() {
        new GuiHandler();
        new Datalayer('#overview');
        new Vislayer('#canvas');
    });
});