/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class Main {

    public static void main(String[] args) throws Exception {
        (new SCInit()).run();
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/", new PublicProvider());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class PublicProvider implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String root = "web_public/";
            URI uri = t.getRequestURI();
            if (uri.getPath().equals("/getcuts")){
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();
                File folder  = new File(System.getProperty("user.dir").concat("/web_public/d/"));
                String res = "";
                for (File f : folder.listFiles()){
                    if (f.getName().contains(".png")) res += f.getName().replace(".png", "") + ";";
                }
                os.write(res.substring(0, res.length()-1).getBytes());
                os.close();
                return;
            }
            File file = new File(root + uri.getPath()).getCanonicalFile();
            if (!file.isFile()) {
                // Object does not exist or is not a file: reject with 404 error.
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer,0,count);
                }
                fs.close();
                os.close();
            }
        }
    }
}
