package com.ftn.dm.dom.model;

/**
 * @author - a.pejakovic
 */
public class DomPathValueModel {

    private String pPath = "";
    private double value = 1;

    public DomPathValueModel() {
        super();
        this.pPath = "";
        this.value = 1;
    }

    public DomPathValueModel(String pPath, double value) {
        super();
        this.pPath = pPath;
        this.value = value;
    }

    public String getpPath() {
        return pPath;
    }

    public void setpPath(String pPath) {
        this.pPath = pPath;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
