
package org.yes.cart.bulkimport.xml.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for product-typeTypeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="product-typeTypeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="display-name" type="{}i18nsType" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templates" type="{}product-typeTemplatesType" minOccurs="0"/>
 *         &lt;element name="configuration" type="{}product-type-configurationType" minOccurs="0"/>
 *         &lt;element name="groups" type="{}product-type-groupsType" minOccurs="0"/>
 *         &lt;element name="attributes" type="{}product-type-attributesType" minOccurs="0"/>
 *         &lt;element name="created-timestamp" type="{}dateTimeType" minOccurs="0"/>
 *         &lt;element name="created-by" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="updated-timestamp" type="{}dateTimeType" minOccurs="0"/>
 *         &lt;element name="updated-by" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="guid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="import-mode" type="{}entityImportModeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "product-typeTypeType", propOrder = {
    "name",
    "displayName",
    "description",
    "templates",
    "configuration",
    "groups",
    "attributes",
    "createdTimestamp",
    "createdBy",
    "updatedTimestamp",
    "updatedBy"
})
public class ProductTypeTypeType {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "display-name")
    protected I18NsType displayName;
    protected String description;
    protected ProductTypeTemplatesType templates;
    protected ProductTypeConfigurationType configuration;
    protected ProductTypeGroupsType groups;
    protected ProductTypeAttributesType attributes;
    @XmlElement(name = "created-timestamp")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String createdTimestamp;
    @XmlElement(name = "created-by")
    protected String createdBy;
    @XmlElement(name = "updated-timestamp")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String updatedTimestamp;
    @XmlElement(name = "updated-by")
    protected String updatedBy;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "guid", required = true)
    protected String guid;
    @XmlAttribute(name = "import-mode")
    protected EntityImportModeType importMode;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link I18NsType }
     *     
     */
    public I18NsType getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NsType }
     *     
     */
    public void setDisplayName(I18NsType value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the templates property.
     * 
     * @return
     *     possible object is
     *     {@link ProductTypeTemplatesType }
     *     
     */
    public ProductTypeTemplatesType getTemplates() {
        return templates;
    }

    /**
     * Sets the value of the templates property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductTypeTemplatesType }
     *     
     */
    public void setTemplates(ProductTypeTemplatesType value) {
        this.templates = value;
    }

    /**
     * Gets the value of the configuration property.
     * 
     * @return
     *     possible object is
     *     {@link ProductTypeConfigurationType }
     *     
     */
    public ProductTypeConfigurationType getConfiguration() {
        return configuration;
    }

    /**
     * Sets the value of the configuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductTypeConfigurationType }
     *     
     */
    public void setConfiguration(ProductTypeConfigurationType value) {
        this.configuration = value;
    }

    /**
     * Gets the value of the groups property.
     * 
     * @return
     *     possible object is
     *     {@link ProductTypeGroupsType }
     *     
     */
    public ProductTypeGroupsType getGroups() {
        return groups;
    }

    /**
     * Sets the value of the groups property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductTypeGroupsType }
     *     
     */
    public void setGroups(ProductTypeGroupsType value) {
        this.groups = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ProductTypeAttributesType }
     *     
     */
    public ProductTypeAttributesType getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductTypeAttributesType }
     *     
     */
    public void setAttributes(ProductTypeAttributesType value) {
        this.attributes = value;
    }

    /**
     * Gets the value of the createdTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Sets the value of the createdTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedTimestamp(String value) {
        this.createdTimestamp = value;
    }

    /**
     * Gets the value of the createdBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the value of the createdBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /**
     * Gets the value of the updatedTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    /**
     * Sets the value of the updatedTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdatedTimestamp(String value) {
        this.updatedTimestamp = value;
    }

    /**
     * Gets the value of the updatedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the value of the updatedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdatedBy(String value) {
        this.updatedBy = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the importMode property.
     * 
     * @return
     *     possible object is
     *     {@link EntityImportModeType }
     *     
     */
    public EntityImportModeType getImportMode() {
        return importMode;
    }

    /**
     * Sets the value of the importMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityImportModeType }
     *     
     */
    public void setImportMode(EntityImportModeType value) {
        this.importMode = value;
    }

}
