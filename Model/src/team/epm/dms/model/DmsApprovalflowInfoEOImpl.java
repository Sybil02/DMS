package team.epm.dms.model;

import oracle.jbo.AttributeList;
import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;
import oracle.jbo.server.TransactionEvent;

import org.apache.commons.lang.ObjectUtils;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Apr 13 20:33:58 CST 2016
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DmsApprovalflowInfoEOImpl extends EntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getId();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        ApprovalName {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getApprovalName();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setApprovalName((String)value);
            }
        }
        ,
        TemplateId {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getTemplateId();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setTemplateId((String)value);
            }
        }
        ,
        ValueSetId {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getValueSetId();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setValueSetId((String)value);
            }
        }
        ,
        Seq {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getSeq();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setSeq((Number)value);
            }
        }
        ,
        CreatedAt {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        ComRecordId {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getComRecordId();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setComRecordId((String)value);
            }
        }
        ,
        Idx {
            public Object get(DmsApprovalflowInfoEOImpl obj) {
                return obj.getIdx();
            }

            public void put(DmsApprovalflowInfoEOImpl obj, Object value) {
                obj.setIdx((Number)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DmsApprovalflowInfoEOImpl object);

        public abstract void put(DmsApprovalflowInfoEOImpl object,
                                 Object value);

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

    @Override
    protected void prepareForDML(int operation, TransactionEvent transactionEvent) {
        super.prepareForDML(operation, transactionEvent);
        if (operation==DML_UPDATE){
            this.setUpdatedAt(new Date(new java.sql.Timestamp(System.currentTimeMillis())));
            this.setUpdatedBy(ObjectUtils.toString(this.getDBTransaction().getSession().getUserData().get("userId")));
        }
    }

    public static final int ID = AttributesEnum.Id.index();
    public static final int APPROVALNAME = AttributesEnum.ApprovalName.index();
    public static final int TEMPLATEID = AttributesEnum.TemplateId.index();
    public static final int VALUESETID = AttributesEnum.ValueSetId.index();
    public static final int SEQ = AttributesEnum.Seq.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int COMRECORDID = AttributesEnum.ComRecordId.index();
    public static final int IDX = AttributesEnum.Idx.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DmsApprovalflowInfoEOImpl() {
    }

    /**
     * @return the definition object for this instance class.
     */
    public static synchronized EntityDefImpl getDefinitionObject() {
        if (mDefinitionObject == null) {
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.dms.model.DmsApprovalflowInfoEO");
        }
        return mDefinitionObject;
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
     * Gets the attribute value for ApprovalName, using the alias name ApprovalName.
     * @return the ApprovalName
     */
    public String getApprovalName() {
        return (String)getAttributeInternal(APPROVALNAME);
    }

    /**
     * Sets <code>value</code> as the attribute value for ApprovalName.
     * @param value value to set the ApprovalName
     */
    public void setApprovalName(String value) {
        setAttributeInternal(APPROVALNAME, value);
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
     * Gets the attribute value for ValueSetId, using the alias name ValueSetId.
     * @return the ValueSetId
     */
    public String getValueSetId() {
        return (String)getAttributeInternal(VALUESETID);
    }

    /**
     * Sets <code>value</code> as the attribute value for ValueSetId.
     * @param value value to set the ValueSetId
     */
    public void setValueSetId(String value) {
        setAttributeInternal(VALUESETID, value);
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
     * Gets the attribute value for ComRecordId, using the alias name ComRecordId.
     * @return the ComRecordId
     */
    public String getComRecordId() {
        return (String)getAttributeInternal(COMRECORDID);
    }

    /**
     * Sets <code>value</code> as the attribute value for ComRecordId.
     * @param value value to set the ComRecordId
     */
    public void setComRecordId(String value) {
        setAttributeInternal(COMRECORDID, value);
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

     * @return a Key object based on given key constituents.
     */
    public static Key createPrimaryKey(String id) {
        return new Key(new Object[]{id});
    }

    /**
     * Add attribute defaulting logic in this method.
     * @param attributeList list of attribute names/values to initialize the row
     */
    protected void create(AttributeList attributeList) {
        super.create(attributeList);
    }

    /**
     * Add entity remove logic in this method.
     */
    public void remove() {
        super.remove();
    }

    /**
     * Add locking logic here.
     */
    public void lock() {
        super.lock();
    }

    /**
     * Custom DML update/insert/delete logic here.
     * @param operation the operation type
     * @param e the transaction event
     */
    protected void doDML(int operation, TransactionEvent e) {
        super.doDML(operation, e);
    }
}