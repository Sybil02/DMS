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
// ---    Fri Jan 09 15:45:53 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DcmTemplateImpl extends DmsEntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(DcmTemplateImpl obj) {
                return obj.getId();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        Locale {
            public Object get(DcmTemplateImpl obj) {
                return obj.getLocale();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        Name {
            public Object get(DcmTemplateImpl obj) {
                return obj.getName();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setName((String)value);
            }
        }
        ,
        CreatedAt {
            public Object get(DcmTemplateImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DcmTemplateImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DcmTemplateImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DcmTemplateImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        Readonly {
            public Object get(DcmTemplateImpl obj) {
                return obj.getReadonly();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setReadonly((String)value);
            }
        }
        ,
        Seq {
            public Object get(DcmTemplateImpl obj) {
                return obj.getSeq();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setSeq((Number)value);
            }
        }
        ,
        Description {
            public Object get(DcmTemplateImpl obj) {
                return obj.getDescription();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setDescription((String)value);
            }
        }
        ,
        DbTable {
            public Object get(DcmTemplateImpl obj) {
                return obj.getDbTable();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setDbTable((String)value);
            }
        }
        ,
        DbView {
            public Object get(DcmTemplateImpl obj) {
                return obj.getDbView();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setDbView((String)value);
            }
        }
        ,
        TmpTable {
            public Object get(DcmTemplateImpl obj) {
                return obj.getTmpTable();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setTmpTable((String)value);
            }
        }
        ,
        PreProgram {
            public Object get(DcmTemplateImpl obj) {
                return obj.getPreProgram();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setPreProgram((String)value);
            }
        }
        ,
        HandleProgram {
            public Object get(DcmTemplateImpl obj) {
                return obj.getHandleProgram();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setHandleProgram((String)value);
            }
        }
        ,
        AfterProgram {
            public Object get(DcmTemplateImpl obj) {
                return obj.getAfterProgram();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setAfterProgram((String)value);
            }
        }
        ,
        HandleMode {
            public Object get(DcmTemplateImpl obj) {
                return obj.getHandleMode();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setHandleMode((String)value);
            }
        }
        ,
        TemplateFile {
            public Object get(DcmTemplateImpl obj) {
                return obj.getTemplateFile();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setTemplateFile((String)value);
            }
        }
        ,
        DataStartLine {
            public Object get(DcmTemplateImpl obj) {
                return obj.getDataStartLine();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setDataStartLine((Number)value);
            }
        }
        ,
        CombinationId {
            public Object get(DcmTemplateImpl obj) {
                return obj.getCombinationId();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setCombinationId((String)value);
            }
        }
        ,
        CategoryId {
            public Object get(DcmTemplateImpl obj) {
                return obj.getCategoryId();
            }

            public void put(DcmTemplateImpl obj, Object value) {
                obj.setCategoryId((String)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DcmTemplateImpl object);

        public abstract void put(DcmTemplateImpl object, Object value);

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
    public static final int NAME = AttributesEnum.Name.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();
    public static final int READONLY = AttributesEnum.Readonly.index();
    public static final int SEQ = AttributesEnum.Seq.index();
    public static final int DESCRIPTION = AttributesEnum.Description.index();
    public static final int DBTABLE = AttributesEnum.DbTable.index();
    public static final int DBVIEW = AttributesEnum.DbView.index();
    public static final int TMPTABLE = AttributesEnum.TmpTable.index();
    public static final int PREPROGRAM = AttributesEnum.PreProgram.index();
    public static final int HANDLEPROGRAM = AttributesEnum.HandleProgram.index();
    public static final int AFTERPROGRAM = AttributesEnum.AfterProgram.index();
    public static final int HANDLEMODE = AttributesEnum.HandleMode.index();
    public static final int TEMPLATEFILE = AttributesEnum.TemplateFile.index();
    public static final int DATASTARTLINE = AttributesEnum.DataStartLine.index();
    public static final int COMBINATIONID = AttributesEnum.CombinationId.index();
    public static final int CATEGORYID = AttributesEnum.CategoryId.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DcmTemplateImpl() {
    }

    /**
     * @return the definition object for this instance class.
     */
    public static synchronized EntityDefImpl getDefinitionObject() {
        if (mDefinitionObject == null) {
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.dcm.model.DcmTemplate");
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
     * Gets the attribute value for Name, using the alias name Name.
     * @return the Name
     */
    public String getName() {
        return (String)getAttributeInternal(NAME);
    }

    /**
     * Sets <code>value</code> as the attribute value for Name.
     * @param value value to set the Name
     */
    public void setName(String value) {
        setAttributeInternal(NAME, value);
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
     * Gets the attribute value for Description, using the alias name Description.
     * @return the Description
     */
    public String getDescription() {
        return (String)getAttributeInternal(DESCRIPTION);
    }

    /**
     * Sets <code>value</code> as the attribute value for Description.
     * @param value value to set the Description
     */
    public void setDescription(String value) {
        setAttributeInternal(DESCRIPTION, value);
    }

    /**
     * Gets the attribute value for DbTable, using the alias name DbTable.
     * @return the DbTable
     */
    public String getDbTable() {
        return (String)getAttributeInternal(DBTABLE);
    }

    /**
     * Sets <code>value</code> as the attribute value for DbTable.
     * @param value value to set the DbTable
     */
    public void setDbTable(String value) {
        setAttributeInternal(DBTABLE, value);
    }

    /**
     * Gets the attribute value for DbView, using the alias name DbView.
     * @return the DbView
     */
    public String getDbView() {
        return (String)getAttributeInternal(DBVIEW);
    }

    /**
     * Sets <code>value</code> as the attribute value for DbView.
     * @param value value to set the DbView
     */
    public void setDbView(String value) {
        setAttributeInternal(DBVIEW, value);
    }

    /**
     * Gets the attribute value for TmpTable, using the alias name TmpTable.
     * @return the TmpTable
     */
    public String getTmpTable() {
        return (String)getAttributeInternal(TMPTABLE);
    }

    /**
     * Sets <code>value</code> as the attribute value for TmpTable.
     * @param value value to set the TmpTable
     */
    public void setTmpTable(String value) {
        setAttributeInternal(TMPTABLE, value);
    }

    /**
     * Gets the attribute value for PreProgram, using the alias name PreProgram.
     * @return the PreProgram
     */
    public String getPreProgram() {
        return (String)getAttributeInternal(PREPROGRAM);
    }

    /**
     * Sets <code>value</code> as the attribute value for PreProgram.
     * @param value value to set the PreProgram
     */
    public void setPreProgram(String value) {
        setAttributeInternal(PREPROGRAM, value);
    }

    /**
     * Gets the attribute value for HandleProgram, using the alias name HandleProgram.
     * @return the HandleProgram
     */
    public String getHandleProgram() {
        return (String)getAttributeInternal(HANDLEPROGRAM);
    }

    /**
     * Sets <code>value</code> as the attribute value for HandleProgram.
     * @param value value to set the HandleProgram
     */
    public void setHandleProgram(String value) {
        setAttributeInternal(HANDLEPROGRAM, value);
    }

    /**
     * Gets the attribute value for AfterProgram, using the alias name AfterProgram.
     * @return the AfterProgram
     */
    public String getAfterProgram() {
        return (String)getAttributeInternal(AFTERPROGRAM);
    }

    /**
     * Sets <code>value</code> as the attribute value for AfterProgram.
     * @param value value to set the AfterProgram
     */
    public void setAfterProgram(String value) {
        setAttributeInternal(AFTERPROGRAM, value);
    }

    /**
     * Gets the attribute value for HandleMode, using the alias name HandleMode.
     * @return the HandleMode
     */
    public String getHandleMode() {
        return (String)getAttributeInternal(HANDLEMODE);
    }

    /**
     * Sets <code>value</code> as the attribute value for HandleMode.
     * @param value value to set the HandleMode
     */
    public void setHandleMode(String value) {
        setAttributeInternal(HANDLEMODE, value);
    }

    /**
     * Gets the attribute value for TemplateFile, using the alias name TemplateFile.
     * @return the TemplateFile
     */
    public String getTemplateFile() {
        return (String)getAttributeInternal(TEMPLATEFILE);
    }

    /**
     * Sets <code>value</code> as the attribute value for TemplateFile.
     * @param value value to set the TemplateFile
     */
    public void setTemplateFile(String value) {
        setAttributeInternal(TEMPLATEFILE, value);
    }

    /**
     * Gets the attribute value for DataStartLine, using the alias name DataStartLine.
     * @return the DataStartLine
     */
    public Number getDataStartLine() {
        return (Number)getAttributeInternal(DATASTARTLINE);
    }

    /**
     * Sets <code>value</code> as the attribute value for DataStartLine.
     * @param value value to set the DataStartLine
     */
    public void setDataStartLine(Number value) {
        setAttributeInternal(DATASTARTLINE, value);
    }

    /**
     * Gets the attribute value for CombinationId, using the alias name CombinationId.
     * @return the CombinationId
     */
    public String getCombinationId() {
        return (String)getAttributeInternal(COMBINATIONID);
    }

    /**
     * Sets <code>value</code> as the attribute value for CombinationId.
     * @param value value to set the CombinationId
     */
    public void setCombinationId(String value) {
        setAttributeInternal(COMBINATIONID, value);
    }

    /**
     * Gets the attribute value for CategoryId, using the alias name CategoryId.
     * @return the CategoryId
     */
    public String getCategoryId() {
        return (String)getAttributeInternal(CATEGORYID);
    }

    /**
     * Sets <code>value</code> as the attribute value for CategoryId.
     * @param value value to set the CategoryId
     */
    public void setCategoryId(String value) {
        setAttributeInternal(CATEGORYID, value);
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


}
