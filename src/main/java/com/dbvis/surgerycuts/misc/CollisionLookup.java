/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.misc;

import com.dbvis.surgerycuts.cut.RoundedShape;
import com.dbvis.surgerycuts.cut.Shape;
import java.util.HashMap;
import java.util.Set;

public class CollisionLookup {

    private HashMap m;
    public CollisionLookup(){
        m = new HashMap();
    }

    public void addCollision(Shape a, Shape b){
        HashMap localdefault1 = new HashMap<RoundedShape, Integer>();
        HashMap localdefault2 = new HashMap<RoundedShape, Integer>();
        m.putIfAbsent(a, localdefault1);
        m.putIfAbsent(b, localdefault2);
        ((HashMap) m.get(a)).put(b,1.0);
        ((HashMap) m.get(b)).put(a,1.0);

    }

    public boolean[] removeCollision(Shape a, Shape b){
        boolean[] isempty = {false, false};
        if (m.containsKey(a)){
            ((HashMap) m.get(a)).remove(b);
            if (((HashMap) m.get(a)).size()<1){
                m.remove(a);
                isempty[0] = true;
            }
        }
        if (m.containsKey(b)){
            ((HashMap) m.get(b)).remove(a);
            if (((HashMap) m.get(b)).size()<1){
                m.remove(b);
                isempty[1] = true;
            }
        }
        return isempty;
    }

    public Set<Shape> getCollisions(){
       return m.keySet();
    }

    public boolean hasCollision(){ return m.keySet().size()>0;}

    public boolean hasCollisionSingle(Shape c){
        return m.containsKey(c);
    }
}
