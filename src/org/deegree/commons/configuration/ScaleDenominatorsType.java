//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.12 at 11:12:53 AM MESZ 
//


package org.deegree.commons.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         The scale denominators define the map resolution to which the datasets are valid. TODO define exact value calculation here!
 *       
 * 
 * <p>Java class for ScaleDenominatorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScaleDenominatorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="min" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="max" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="native" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScaleDenominatorsType")
public class ScaleDenominatorsType {

    @XmlAttribute(required = true)
    protected double min;
    @XmlAttribute(required = true)
    protected double max;
    @XmlAttribute(name = "native")
    protected Double _native;

    /**
     * Gets the value of the min property.
     * 
     */
    public double getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     */
    public void setMin(double value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(double value) {
        this.max = value;
    }

    /**
     * Gets the value of the native property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNative() {
        return _native;
    }

    /**
     * Sets the value of the native property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNative(Double value) {
        this._native = value;
    }

}
