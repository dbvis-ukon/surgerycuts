/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

var Datalayer = function(container){
var p = this;
var _cache = {};

    function _constructor(){
        $(container).data('public', p); //bind p(ublic) method to the DOM element
    }

    p.setJSON = function(ts,cb){
        if (_cache.hasOwnProperty(ts)){
            cb(_cache[ts]);
            return;
        }
        $.getJSON('d/'+ts+'.json', function(d) {
            _cache[ts] = d;
            cb(_cache[ts]);
        })
    };

    p.getJSON = function(ts){
        return _cache[ts];
    };



_constructor();
};