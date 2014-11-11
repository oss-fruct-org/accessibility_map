package org.fruct.oss.accessibilitymap.Model;

import java.util.List;

/**
 * Created by alexander on 11.11.14.
 */
public class Passport {
    public String name;
    public String objectName;
    public String address;

    public double latitude;
    public double longitude;

    public List<Scope> scopes;
    public String route;
    public String adaptiveRoute;
    public List<Accessibility> accessibilities;
    public String site;

    public String contacts;
    public long id;

    public Passport(String name, String objectName, String address, double latitude, double longitude, List<Scope> scopes, String route, String adaptiveRoute, List<Accessibility> accessibilities, String site, String contacts, long id) {
        this.name = name;
        this.objectName = objectName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scopes = scopes;
        this.route = route;
        this.adaptiveRoute = adaptiveRoute;
        this.accessibilities = accessibilities;
        this.site = site;
        this.contacts = contacts;
        this.id = id;
    }
}
