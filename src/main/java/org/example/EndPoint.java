package org.example;

public class EndPoint {
    public EndPoint(String url, String name, String verb) {
        this.name = name;
        this.URL = url;
        this.verb = verb;
    }

    public String URL;
    public String name;
    public String verb;
}
