
package odi11g.webservice.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RestartLoadPlanRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RestartLoadPlanRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LoadPlanInstanceId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="LogLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RestartLoadPlanRequestType", propOrder = {
    "loadPlanInstanceId",
    "logLevel"
})
public class RestartLoadPlanRequestType {

    @XmlElement(name = "LoadPlanInstanceId")
    protected long loadPlanInstanceId;
    @XmlElement(name = "LogLevel")
    protected Integer logLevel;

    /**
     * Gets the value of the loadPlanInstanceId property.
     * 
     */
    public long getLoadPlanInstanceId() {
        return loadPlanInstanceId;
    }

    /**
     * Sets the value of the loadPlanInstanceId property.
     * 
     */
    public void setLoadPlanInstanceId(long value) {
        this.loadPlanInstanceId = value;
    }

    /**
     * Gets the value of the logLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the value of the logLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLogLevel(Integer value) {
        this.logLevel = value;
    }

}
