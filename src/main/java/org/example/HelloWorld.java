package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

public class HelloWorld extends AbstractHandler
{
    private List<Connector> connectors = new ArrayList<>();
    private Map<String, EndPoint> endPoints = new HashMap<>();

    public HelloWorld() throws Exception {
        JSONParser parser = new JSONParser();
        Reader epFile = getFile("conf/endpoints.json");
        JSONObject obj = (JSONObject) parser.parse(epFile);


        for(Object epNameObj: obj.keySet()) {
            String epName = (String)epNameObj;
            JSONObject ep = (JSONObject)obj.get(epName);
            
            String verb = (String) ep.get("verb");
            String url = (String) ep.get("URL");

            System.out.format("%s -> %s, %s\r\n", epName, verb, url);
        }

        endPoints.put("test", new EndPoint("/test", "test", "POST"));
        endPoints.put("postmane_headers", new EndPoint("https://postman-echo.com/headers", "postman_headers", "GET"));

        connectors.add(new Connector("postmane_headers", "test"));
    }


    private Reader getFile(String fileName) throws Exception {
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        return new InputStreamReader(classLoader.getResource(fileName).openStream());
    }

    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException
        {

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            String uri = request.getRequestURI();


            for(Connector c: this.connectors) {
                String internal = c.InternalEndPointName;
                String external = c.ExternalEndPointName;
                EndPoint ep = endPoints.get(internal);
                EndPoint ext = endPoints.get(external);

                if ( ep.URL.equals(uri) && request.getMethod().equals(ep.verb)) {
                    render(response, ext);
                    return;
                }
            }

            response.getWriter().println("invalid request");
            //Else: render 404

            // if ( uri.equals("/raw") ) {
            //     response.getWriter().println("raw - <h1>Hello World</h1>" + uri + " " + request.getQueryString() + " method = "+request.getMethod());
            // } else {
            //     if ( this.gateway.containsKey(uri) ) {
            //         String targetUrl = gateway.get(uri);
            //         String tempResponse = executePost(targetUrl, request.getQueryString());


            //         response.getWriter().println("invoke done! <br />" + tempResponse);
            //     }
            // }
        }


    private static void render(HttpServletResponse response, EndPoint ep) throws IOException
    {
        String responseStr = executeRequest(ep.verb, ep.URL);

        response.getWriter().println("new version render finished! <br /> API called: " + ep.URL + " Response: " + responseStr);
    }


    public static String executeRequest(String verb, String targetURL) {
        HttpURLConnection connection = null;
        System.out.println(targetURL);

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(verb);
            connection.setRequestProperty("Content-Type", 
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "0");
            connection.setRequestProperty("Content-Language", "en-US");  

            connection.setUseCaches(false);
            xconnection.setDoOutput(true);

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new HelloWorld());

        server.start();
        server.join();
    }
}

