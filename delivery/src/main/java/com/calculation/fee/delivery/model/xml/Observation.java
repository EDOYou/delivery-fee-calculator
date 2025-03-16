package com.calculation.fee.delivery.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "observations")
public class Observation {

    @JacksonXmlProperty(isAttribute = true)
    private String timestamp;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "station")
    private List<Station> stations;
}