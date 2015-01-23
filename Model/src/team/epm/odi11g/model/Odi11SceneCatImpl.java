package team.epm.odi11g.model;

import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

import team.epm.dms.common.DmsEntityImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Fri Jan 23 09:32:40 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class Odi11SceneCatImpl extends DmsEntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getId();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        PId {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getPId();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setPId((String)value);
            }
        }
        ,
        CatName {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getCatName();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setCatName((String)value);
            }
        }
        ,
        Locale {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getLocale();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        Idx {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getIdx();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setIdx((Number)value);
            }
        }
        ,
        CreatedAt {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(Odi11SceneCatImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(Odi11SceneCatImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(Odi11SceneCatImpl object);

        public abstract void put(Odi11SceneCatImpl object, Object value);

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
    public static final int PID = AttributesEnum.PId.index();
    public static final int CATNAME = AttributesEnum.CatName.index();
    public static final int LOCALE = AttributesEnum.Locale.index();
    public static final int IDX = AttributesEnum.Idx.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();

    /**
     * This is the default constructor (do not remove).
     */
    public Odi11SceneCatImpl() {
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
     * Gets the attribute value for PId, using the alias name PId.
     * @return the PId
     */
    public String getPId() {
        return (String)getAttributeInternal(PID);
    }

    /**
     * Sets <code>value</code> as the attribute value for PId.
     * @param value value to set the PId
     */
    public void setPId(String value) {
        setAttributeInternal(PID, value);
    }

    /**
     * Gets the attribute value for CatName, using the alias name CatName.
     * @return the CatName
     */
    public String getCatName() {
        return (String)getAttributeInternal(CATNAME);
    }

    /**
     * Sets <code>value</code> as the attribute value for CatName.
     * @param value value to set the CatName
     */
    public void setCatName(String value) {
        setAttributeInternal(CATNAME, value);
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
     * Gets the attribute value for Idx, using the alias name Idx.
     * @return the Idx
     */
    public Number getIdx() {
        return (Number)getAttributeInternal(IDX);
    }

    /**
     * Sets <code>value</code> as the attribute value for Idx.
     * @param value value to set the Idx
     */
    public void setIdx(Number value) {
        setAttributeInternal(IDX, value);
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
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.odi11g.model.Odi11SceneCat");
        }
        return mDefinitionObject;
    }
}
