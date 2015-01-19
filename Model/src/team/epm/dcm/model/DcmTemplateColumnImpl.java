package team.epm.dcm.model;

import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

import team.epm.dms.common.DmsEntityImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Fri Jan 09 15:45:31 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DcmTemplateColumnImpl extends DmsEntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getId();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        Locale {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getLocale();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        ColumnLabel {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getColumnLabel();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setColumnLabel((String)value);
            }
        }
        ,
        DbTableCol {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getDbTableCol();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setDbTableCol((String)value);
            }
        }
        ,
        CreatedAt {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        IsPk {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getIsPk();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setIsPk((String)value);
            }
        }
        ,
        Readonly {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getReadonly();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setReadonly((String)value);
            }
        }
        ,
        DataType {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getDataType();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setDataType((String)value);
            }
        }
        ,
        Visible {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getVisible();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setVisible((String)value);
            }
        }
        ,
        Seq {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getSeq();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setSeq((Number)value);
            }
        }
        ,
        TemplateId {
            public Object get(DcmTemplateColumnImpl obj) {
                return obj.getTemplateId();
            }

            public void put(DcmTemplateColumnImpl obj, Object value) {
                obj.setTemplateId((String)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DcmTemplateColumnImpl object);

        public abstract void put(DcmTemplateColumnImpl object, Object value);

        public int index() {
            return AttributesEnum.firstIndex() + ordinal();
        }

        public static final int firstIndex() {
            return firstIndex;
        }

        public static int count() {
            return AttributesEnum.firstIndex() + AttributesEnum.staticValues().length;
        }

        public static final AttributesEnum[] staticValues() {
            if (vals == null) {
                vals = AttributesEnum.values();
            }
            return vals;
        }
    }
    public static final int ID = AttributesEnum.Id.index();
    public static final int LOCALE = AttributesEnum.Locale.index();
    public static final int COLUMNLABEL = AttributesEnum.ColumnLabel.index();
    public static final int DBTABLECOL = AttributesEnum.DbTableCol.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();
    public static final int ISPK = AttributesEnum.IsPk.index();
    public static final int READONLY = AttributesEnum.Readonly.index();
    public static final int DATATYPE = AttributesEnum.DataType.index();
    public static final int VISIBLE = AttributesEnum.Visible.index();
    public static final int SEQ = AttributesEnum.Seq.index();
    public static final int TEMPLATEID = AttributesEnum.TemplateId.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DcmTemplateColumnImpl() {
    }

    /**
     * Gets the attribute value for Id, using the alias name Id.
     * @return the Id
     */
    public String getId() {
        return (String)getAttributeInternal(ID);
    }

    /**
     * Sets <code>value</code> as the attribute value for Id.
     * @param value value to set the Id
     */
    public void setId(String value) {
        setAttributeInternal(ID, value);
    }

    /**
     * Gets the attribute value for Locale, using the alias name Locale.
     * @return the Locale
     */
    public String getLocale() {
        return (String)getAttributeInternal(LOCALE);
    }

    /**
     * Sets <code>value</code> as the attribute value for Locale.
     * @param value value to set the Locale
     */
    public void setLocale(String value) {
        setAttributeInternal(LOCALE, value);
    }

    /**
     * Gets the attribute value for ColumnLabel, using the alias name ColumnLabel.
     * @return the ColumnLabel
     */
    public String getColumnLabel() {
        return (String)getAttributeInternal(COLUMNLABEL);
    }

    /**
     * Sets <code>value</code> as the attribute value for ColumnLabel.
     * @param value value to set the ColumnLabel
     */
    public void setColumnLabel(String value) {
        setAttributeInternal(COLUMNLABEL, value);
    }

    /**
     * Gets the attribute value for DbTableCol, using the alias name DbTableCol.
     * @return the DbTableCol
     */
    public String getDbTableCol() {
        return (String)getAttributeInternal(DBTABLECOL);
    }

    /**
     * Sets <code>value</code> as the attribute value for DbTableCol.
     * @param value value to set the DbTableCol
     */
    public void setDbTableCol(String value) {
        setAttributeInternal(DBTABLECOL, value);
    }

    /**
     * Gets the attribute value for CreatedAt, using the alias name CreatedAt.
     * @return the CreatedAt
     */
    public Date getCreatedAt() {
        return (Date)getAttributeInternal(CREATEDAT);
    }

    /**
     * Sets <code>value</code> as the attribute value for CreatedAt.
     * @param value value to set the CreatedAt
     */
    public void setCreatedAt(Date value) {
        setAttributeInternal(CREATEDAT, value);
    }

    /**
     * Gets the attribute value for UpdatedAt, using the alias name UpdatedAt.
     * @return the UpdatedAt
     */
    public Date getUpdatedAt() {
        return (Date)getAttributeInternal(UPDATEDAT);
    }

    /**
     * Sets <code>value</code> as the attribute value for UpdatedAt.
     * @param value value to set the UpdatedAt
     */
    public void setUpdatedAt(Date value) {
        setAttributeInternal(UPDATEDAT, value);
    }

    /**
     * Gets the attribute value for UpdatedBy, using the alias name UpdatedBy.
     * @return the UpdatedBy
     */
    public String getUpdatedBy() {
        return (String)getAttributeInternal(UPDATEDBY);
    }

    /**
     * Sets <code>value</code> as the attribute value for UpdatedBy.
     * @param value value to set the UpdatedBy
     */
    public void setUpdatedBy(String value) {
        setAttributeInternal(UPDATEDBY, value);
    }

    /**
     * Gets the attribute value for CreatedBy, using the alias name CreatedBy.
     * @return the CreatedBy
     */
    public String getCreatedBy() {
        return (String)getAttributeInternal(CREATEDBY);
    }

    /**
     * Sets <code>value</code> as the attribute value for CreatedBy.
     * @param value value to set the CreatedBy
     */
    public void setCreatedBy(String value) {
        setAttributeInternal(CREATEDBY, value);
    }

    /**
     * Gets the attribute value for IsPk, using the alias name IsPk.
     * @return the IsPk
     */
    public String getIsPk() {
        return (String)getAttributeInternal(ISPK);
    }

    /**
     * Sets <code>value</code> as the attribute value for IsPk.
     * @param value value to set the IsPk
     */
    public void setIsPk(String value) {
        setAttributeInternal(ISPK, value);
    }

    /**
     * Gets the attribute value for Readonly, using the alias name Readonly.
     * @return the Readonly
     */
    public String getReadonly() {
        return (String)getAttributeInternal(READONLY);
    }

    /**
     * Sets <code>value</code> as the attribute value for Readonly.
     * @param value value to set the Readonly
     */
    public void setReadonly(String value) {
        setAttributeInternal(READONLY, value);
    }

    /**
     * Gets the attribute value for DataType, using the alias name DataType.
     * @return the DataType
     */
    public String getDataType() {
        return (String)getAttributeInternal(DATATYPE);
    }

    /**
     * Sets <code>value</code> as the attribute value for DataType.
     * @param value value to set the DataType
     */
    public void setDataType(String value) {
        setAttributeInternal(DATATYPE, value);
    }

    /**
     * Gets the attribute value for Visible, using the alias name Visible.
     * @return the Visible
     */
    public String getVisible() {
        return (String)getAttributeInternal(VISIBLE);
    }

    /**
     * Sets <code>value</code> as the attribute value for Visible.
     * @param value value to set the Visible
     */
    public void setVisible(String value) {
        setAttributeInternal(VISIBLE, value);
    }

    /**
     * Gets the attribute value for Seq, using the alias name Seq.
     * @return the Seq
     */
    public Number getSeq() {
        return (Number)getAttributeInternal(SEQ);
    }

    /**
     * Sets <code>value</code> as the attribute value for Seq.
     * @param value value to set the Seq
     */
    public void setSeq(Number value) {
        setAttributeInternal(SEQ, value);
    }

    /**
     * Gets the attribute value for TemplateId, using the alias name TemplateId.
     * @return the TemplateId
     */
    public String getTemplateId() {
        return (String)getAttributeInternal(TEMPLATEID);
    }

    /**
     * Sets <code>value</code> as the attribute value for TemplateId.
     * @param value value to set the TemplateId
     */
    public void setTemplateId(String value) {
        setAttributeInternal(TEMPLATEID, value);
    }

    /**
     * getAttrInvokeAccessor: generated method. Do not modify.
     * @param index the index identifying the attribute
     * @param attrDef the attribute

     * @return the attribute value
     * @throws Exception
     */
    protected Object getAttrInvokeAccessor(int index,
                                           AttributeDefImpl attrDef) throws Exception {
        if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
            return AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].get(this);
        }
        return super.getAttrInvokeAccessor(index, attrDef);
    }

    /**
     * setAttrInvokeAccessor: generated method. Do not modify.
     * @param index the index identifying the attribute
     * @param value the value to assign to the attribute
     * @param attrDef the attribute

     * @throws Exception
     */
    protected void setAttrInvokeAccessor(int index, Object value,
                                         AttributeDefImpl attrDef) throws Exception {
        if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
            AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
            return;
        }
        super.setAttrInvokeAccessor(index, value, attrDef);
    }

    /**
     * @param id key constituent
     * @param locale key constituent

     * @return a Key object based on given key constituents.
     */
    public static Key createPrimaryKey(String id, String locale) {
        return new Key(new Object[]{id, locale});
    }

    /**
     * @return the definition object for this instance class.
     */
    public static synchronized EntityDefImpl getDefinitionObject() {
        if (mDefinitionObject == null) {
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.dcm.model.DcmTemplateColumn");
        }
        return mDefinitionObject;
    }
}
