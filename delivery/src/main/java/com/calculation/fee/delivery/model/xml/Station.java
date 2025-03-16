package com.calculation.fee.delivery.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@JacksonXmlRootElement(localName = "station")
@Data
public class Station {

    @JacksonXmlProperty(isAttribute = true)
    private String timestamp;

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlProperty(localName = "wmocode")
    private String wmoCode;

    @JacksonXmlProperty(localName = "longitude")
    private String longitude;

    @JacksonXmlProperty(localName = "latitude")
    private String latitude;

    @JacksonXmlProperty(localName = "phenomenon")
    private String phenomenon;

    @JacksonXmlProperty(localName = "visibility")
    private String visibility;

    @JacksonXmlProperty(localName = "precipitations")
    private String precipitations;

    @JacksonXmlProperty(localName = "airpressure")
    private String airpressure;

    @JacksonXmlProperty(localName = "relativehumidity")
    private String relativehumidity;

    @JacksonXmlProperty(localName = "airtemperature")
    private String airtemperature;

    @JacksonXmlProperty(localName = "winddirection")
    private String winddirection;

    @JacksonXmlProperty(localName = "windspeed")
    private String windspeed;

    @JacksonXmlProperty(localName = "windspeedmax")
    private String windspeedmax;

    @JacksonXmlProperty(localName = "waterlevel")
    private String waterlevel;

    @JacksonXmlProperty(localName = "waterlevel_eh2000")
    private String waterlevel_eh2000;

    @JacksonXmlProperty(localName = "watertemperature")
    private String watertemperature;

    @JacksonXmlProperty(localName = "uvindex")
    private String uvindex;

    @JacksonXmlProperty(localName = "sunshineduration")
    private String sunshineduration;

    @JacksonXmlProperty(localName = "globalradiation")
    private String globalradiation;
}