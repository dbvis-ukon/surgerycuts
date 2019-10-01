/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

requirejs.config({
    "baseUrl": "js/app",
    "paths": {
        "app" : "../app",
        "d3": "//d3js.org/d3.v4.min",
        "jquery": "//cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min",
        "bootstrap": "//maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min",
        "jquery-ui": "//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui",
        "datatables.net": "//cdn.datatables.net/1.10.12/js/jquery.dataTables.min",
        "datatables.net.select":"//cdn.datatables.net/select/1.2.4/js/dataTables.select.min"
    },
    "shim": {
        "jquery-ui" : ["jquery", "bootstrap"],
        "bootstrap": ["jquery"],
        "listgroup" : ["bootstrap", "jquery"]
    }
});

requirejs(['d3'], function(d3) {
    window.d3 = d3;
});

requirejs(["app/main"]);
