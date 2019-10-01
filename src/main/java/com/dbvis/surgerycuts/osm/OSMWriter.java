/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.osm;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.NamedNodeMap;

public class OSMWriter {
    
    public static void write(OSMMap m, String out){
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbfac.newDocumentBuilder();
            
            Document doc = db.newDocument();
            Element osm = doc.createElement("osm");
            osm.setAttribute("version", "0.6");
            doc.appendChild(osm); 
            
            Element b = doc.createElement("bounds");
            double[] bounds = m.getBounds(); 
            b.setAttribute("minlat", bounds[0]+"");
            b.setAttribute("minlon", bounds[1]+"");
            b.setAttribute("maxlat", bounds[2]+"");
            b.setAttribute("maxlon", bounds[3]+"");
            osm.appendChild(b); 
            
            Iterator iternodes =  m.getNodes().entrySet().iterator();
            Long id;
            OSMNode o;
            Element n;
            while (iternodes.hasNext()){
                Map.Entry<Long,OSMNode> next = (Map.Entry<Long,OSMNode>) iternodes.next();
                id = next.getKey();
                 o = next.getValue();
                 n = doc.createElement("node");
                n.setAttribute("id", id+"");
                n.setAttribute("lat", o.getCoords().getLatitude()+"");
                n.setAttribute("lon", o.getCoords().getLongitude()+"");
                n.setAttribute("timestamp", o.getTimestamp());
                n.setAttribute("visible", "true");
                n.setAttribute("version", "1");
                osm.appendChild(n); 
            }
            
            Iterator iterways = m.getWays().entrySet().iterator();
            Map.Entry pair;
            OSMWay next;
            Element w;
            Long nd;
            Element n2;
            NamedNodeMap attributes;
            Element t;
            Element t2;
            String[] attributes2;
            while (iterways.hasNext()){
                pair = (Map.Entry) iterways.next();
                next = (OSMWay) pair.getValue();
                w = doc.createElement("way");
                w.setAttribute("id", next.getID()+"");
                w.setAttribute("timestamp", next.getTimestamp());
                w.setAttribute("version", "1");
                w.setAttribute("visible", "true"); 
                osm.appendChild(w); 
            
                Iterator iternd = next.getNodes().iterator();

                while (iternd.hasNext()){
                    nd = (Long) iternd.next();
                    n2 = doc.createElement("nd");
                    n2.setAttribute("ref", nd+"");
                    w.appendChild(n2); 
                }

                
                Iterator itertags = next.getTags().iterator(); 
                while (itertags.hasNext()){
                    attributes = (NamedNodeMap) itertags.next();
                    t = doc.createElement("tag");
                    for (int i = 0; i<attributes.getLength(); i++){
                        t.setAttribute(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
                    }
                    
                    w.appendChild(t); 
                }


                Iterator iterextratags = next.getExtraTags().iterator(); 
                while (iterextratags.hasNext()){
                     attributes2 = (String[]) iterextratags.next();
                     t2 = doc.createElement("tag");
                    t2.setAttribute("k", attributes2[0]);
                    t2.setAttribute("v", attributes2[1]);
                    w.appendChild(t2);
                }
            }

            OSMRelation nextrel;
            Element r;
            Iterator iterrel = m.getRelations().iterator();
            Iterator itermembers;
            OSMMember member;
            Element m_e;
            Element tag;
            NamedNodeMap attributes3;
            while (iterrel.hasNext()){
                 nextrel = (OSMRelation) iterrel.next();
                r = doc.createElement("relation");
                r.setAttribute("id", nextrel.getID()+"");
                r.setAttribute("timestamp", nextrel.getTimestamp());
                r.setAttribute("version", "1");
                r.setAttribute("visible", "true"); 
                osm.appendChild(r); 
                
                itermembers = nextrel.getMembers().iterator();
                while (itermembers.hasNext()){
                    member = (OSMMember) itermembers.next();
                    m_e = doc.createElement("member");
                    m_e.setAttribute("type", member.getType());
                    m_e.setAttribute("ref", member.getRef()+"");
                    m_e.setAttribute("role", member.getRole());
                    r.appendChild(m_e);
                }

                Iterator itertags = nextrel.getTags().iterator();
                while (itertags.hasNext()){
                     attributes3 = (NamedNodeMap) itertags.next();
                    tag = doc.createElement("tag");
                    for (int i = 0; i<attributes3.getLength(); i++){
                        tag.setAttribute(attributes3.item(i).getNodeName(), attributes3.item(i).getNodeValue());
                    }
                    r.appendChild(tag);
                }
            }

            TransformerFactory transfact = TransformerFactory.newInstance();
            Transformer trans = transfact.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(doc);
            StreamResult res = new StreamResult(new File(out));
            trans.transform(src, res);
        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(OSMWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
