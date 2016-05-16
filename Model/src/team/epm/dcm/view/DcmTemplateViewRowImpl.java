package team.epm.dcm.view;

import oracle.jbo.RowIterator;
import oracle.jbo.RowSet;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.ViewRowImpl;

import team.epm.dcm.model.DcmTemplateImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Feb 04 16:07:18 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DcmTemplateViewRowImpl extends ViewRowImpl {
    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getId();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        Locale {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLocale();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        Name {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getName();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setName((String)value);
            }
        }
        ,
        CreatedAt {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        Readonly {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getReadonly();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setReadonly((String)value);
            }
        }
        ,
        Seq {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getSeq();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setSeq((Number)value);
            }
        }
        ,
        Description {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDescription();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setDescription((String)value);
            }
        }
        ,
        DbTable {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDbTable();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setDbTable((String)value);
            }
        }
        ,
        DbView {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDbView();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setDbView((String)value);
            }
        }
        ,
        TmpTable {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getTmpTable();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setTmpTable((String)value);
            }
        }
        ,
        PreProgram {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getPreProgram();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setPreProgram((String)value);
            }
        }
        ,
        HandleProgram {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getHandleProgram();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setHandleProgram((String)value);
            }
        }
        ,
        AfterProgram {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getAfterProgram();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAfterProgram((String)value);
            }
        }
        ,
        HandleMode {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getHandleMode();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setHandleMode((String)value);
            }
        }
        ,
        TemplateFile {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getTemplateFile();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setTemplateFile((String)value);
            }
        }
        ,
        DataStartLine {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDataStartLine();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setDataStartLine((Number)value);
            }
        }
        ,
        CombinationId {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getCombinationId();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setCombinationId((String)value);
            }
        }
        ,
        CategoryId {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getCategoryId();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setCategoryId((String)value);
            }
        }
        ,
        IsSpecial {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getIsSpecial();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setIsSpecial((String)value);
            }
        }
        ,
        IsCloseRecord {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getIsCloseRecord();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setIsCloseRecord((String)value);
            }
        }
        ,
        DcmTemplateColumnView {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDcmTemplateColumnView();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        DcmTemplateValidationView {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getDcmTemplateValidationView();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_YES_NO {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_YES_NO();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LST_USER {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLST_USER();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_LANG {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_LANG();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_CAT {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_CAT();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_COM {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_COM();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_IMPORT_MODE {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_IMPORT_MODE();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        LKP_TMP_TABLE {
            public Object get(DcmTemplateViewRowImpl obj) {
                return obj.getLKP_TMP_TABLE();
            }

            public void put(DcmTemplateViewRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DcmTemplateViewRowImpl object);

        public abstract void put(DcmTemplateViewRowImpl object, Object value);

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
    public static final int ISSPECIAL = AttributesEnum.IsSpecial.index();
    public static final int ISCLOSERECORD = AttributesEnum.IsCloseRecord.index();
    public static final int DCMTEMPLATECOLUMNVIEW = AttributesEnum.DcmTemplateColumnView.index();
    public static final int DCMTEMPLATEVALIDATIONVIEW = AttributesEnum.DcmTemplateValidationView.index();
    public static final int LKP_YES_NO = AttributesEnum.LKP_YES_NO.index();
    public static final int LST_USER = AttributesEnum.LST_USER.index();
    public static final int LKP_LANG = AttributesEnum.LKP_LANG.index();
    public static final int LKP_CAT = AttributesEnum.LKP_CAT.index();
    public static final int LKP_COM = AttributesEnum.LKP_COM.index();
    public static final int LKP_IMPORT_MODE = AttributesEnum.LKP_IMPORT_MODE.index();
    public static final int LKP_TMP_TABLE = AttributesEnum.LKP_TMP_TABLE.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DcmTemplateViewRowImpl() {
    }

    /**
     * Gets DcmTemplate entity object.
     * @return the DcmTemplate
     */
    public DcmTemplateImpl getDcmTemplate() {
        return (DcmTemplateImpl)getEntity(0);
    }

    /**
     * Gets the attribute value for ID using the alias name Id.
     * @return the ID
     */
    public String getId() {
        return (String) getAttributeInternal(ID);
    }

    /**
     * Sets <code>value</code> as attribute value for ID using the alias name Id.
     * @param value value to set the ID
     */
    public void setId(String value) {
        setAttributeInternal(ID, value);
    }

    /**
     * Gets the attribute value for LOCALE using the alias name Locale.
     * @return the LOCALE
     */
    public String getLocale() {
        return (String) getAttributeInternal(LOCALE);
    }

    /**
     * Sets <code>value</code> as attribute value for LOCALE using the alias name Locale.
     * @param value value to set the LOCALE
     */
    public void setLocale(String value) {
        setAttributeInternal(LOCALE, value);
    }

    /**
     * Gets the attribute value for NAME using the alias name Name.
     * @return the NAME
     */
    public String getName() {
        return (String) getAttributeInternal(NAME);
    }

    /**
     * Sets <code>value</code> as attribute value for NAME using the alias name Name.
     * @param value value to set the NAME
     */
    public void setName(String value) {
        setAttributeInternal(NAME, value);
    }

    /**
     * Gets the attribute value for CREATED_AT using the alias name CreatedAt.
     * @return the CREATED_AT
     */
    public Date getCreatedAt() {
        return (Date) getAttributeInternal(CREATEDAT);
    }

    /**
     * Sets <code>value</code> as attribute value for CREATED_AT using the alias name CreatedAt.
     * @param value value to set the CREATED_AT
     */
    public void setCreatedAt(Date value) {
        setAttributeInternal(CREATEDAT, value);
    }

    /**
     * Gets the attribute value for UPDATED_AT using the alias name UpdatedAt.
     * @return the UPDATED_AT
     */
    public Date getUpdatedAt() {
        return (Date) getAttributeInternal(UPDATEDAT);
    }

    /**
     * Sets <code>value</code> as attribute value for UPDATED_AT using the alias name UpdatedAt.
     * @param value value to set the UPDATED_AT
     */
    public void setUpdatedAt(Date value) {
        setAttributeInternal(UPDATEDAT, value);
    }

    /**
     * Gets the attribute value for UPDATED_BY using the alias name UpdatedBy.
     * @return the UPDATED_BY
     */
    public String getUpdatedBy() {
        return (String) getAttributeInternal(UPDATEDBY);
    }

    /**
     * Sets <code>value</code> as attribute value for UPDATED_BY using the alias name UpdatedBy.
     * @param value value to set the UPDATED_BY
     */
    public void setUpdatedBy(String value) {
        setAttributeInternal(UPDATEDBY, value);
    }

    /**
     * Gets the attribute value for CREATED_BY using the alias name CreatedBy.
     * @return the CREATED_BY
     */
    public String getCreatedBy() {
        return (String) getAttributeInternal(CREATEDBY);
    }

    /**
     * Sets <code>value</code> as attribute value for CREATED_BY using the alias name CreatedBy.
     * @param value value to set the CREATED_BY
     */
    public void setCreatedBy(String value) {
        setAttributeInternal(CREATEDBY, value);
    }

    /**
     * Gets the attribute value for READONLY using the alias name Readonly.
     * @return the READONLY
     */
    public String getReadonly() {
        return (String) getAttributeInternal(READONLY);
    }

    /**
     * Sets <code>value</code> as attribute value for READONLY using the alias name Readonly.
     * @param value value to set the READONLY
     */
    public void setReadonly(String value) {
        setAttributeInternal(READONLY, value);
    }

    /**
     * Gets the attribute value for SEQ using the alias name Seq.
     * @return the SEQ
     */
    public Number getSeq() {
        return (Number) getAttributeInternal(SEQ);
    }

    /**
     * Sets <code>value</code> as attribute value for SEQ using the alias name Seq.
     * @param value value to set the SEQ
     */
    public void setSeq(Number value) {
        setAttributeInternal(SEQ, value);
    }

    /**
     * Gets the attribute value for DESCRIPTION using the alias name Description.
     * @return the DESCRIPTION
     */
    public String getDescription() {
        return (String) getAttributeInternal(DESCRIPTION);
    }

    /**
     * Sets <code>value</code> as attribute value for DESCRIPTION using the alias name Description.
     * @param value value to set the DESCRIPTION
     */
    public void setDescription(String value) {
        setAttributeInternal(DESCRIPTION, value);
    }

    /**
     * Gets the attribute value for DB_TABLE using the alias name DbTable.
     * @return the DB_TABLE
     */
    public String getDbTable() {
        return (String) getAttributeInternal(DBTABLE);
    }

    /**
     * Sets <code>value</code> as attribute value for DB_TABLE using the alias name DbTable.
     * @param value value to set the DB_TABLE
     */
    public void setDbTable(String value) {
        setAttributeInternal(DBTABLE, value);
    }

    /**
     * Gets the attribute value for DB_VIEW using the alias name DbView.
     * @return the DB_VIEW
     */
    public String getDbView() {
        return (String) getAttributeInternal(DBVIEW);
    }

    /**
     * Sets <code>value</code> as attribute value for DB_VIEW using the alias name DbView.
     * @param value value to set the DB_VIEW
     */
    public void setDbView(String value) {
        setAttributeInternal(DBVIEW, value);
    }

    /**
     * Gets the attribute value for TMP_TABLE using the alias name TmpTable.
     * @return the TMP_TABLE
     */
    public String getTmpTable() {
        return (String) getAttributeInternal(TMPTABLE);
    }

    /**
     * Sets <code>value</code> as attribute value for TMP_TABLE using the alias name TmpTable.
     * @param value value to set the TMP_TABLE
     */
    public void setTmpTable(String value) {
        setAttributeInternal(TMPTABLE, value);
    }

    /**
     * Gets the attribute value for PRE_PROGRAM using the alias name PreProgram.
     * @return the PRE_PROGRAM
     */
    public String getPreProgram() {
        return (String) getAttributeInternal(PREPROGRAM);
    }

    /**
     * Sets <code>value</code> as attribute value for PRE_PROGRAM using the alias name PreProgram.
     * @param value value to set the PRE_PROGRAM
     */
    public void setPreProgram(String value) {
        setAttributeInternal(PREPROGRAM, value);
    }

    /**
     * Gets the attribute value for HANDLE_PROGRAM using the alias name HandleProgram.
     * @return the HANDLE_PROGRAM
     */
    public String getHandleProgram() {
        return (String) getAttributeInternal(HANDLEPROGRAM);
    }

    /**
     * Sets <code>value</code> as attribute value for HANDLE_PROGRAM using the alias name HandleProgram.
     * @param value value to set the HANDLE_PROGRAM
     */
    public void setHandleProgram(String value) {
        setAttributeInternal(HANDLEPROGRAM, value);
    }

    /**
     * Gets the attribute value for AFTER_PROGRAM using the alias name AfterProgram.
     * @return the AFTER_PROGRAM
     */
    public String getAfterProgram() {
        return (String) getAttributeInternal(AFTERPROGRAM);
    }

    /**
     * Sets <code>value</code> as attribute value for AFTER_PROGRAM using the alias name AfterProgram.
     * @param value value to set the AFTER_PROGRAM
     */
    public void setAfterProgram(String value) {
        setAttributeInternal(AFTERPROGRAM, value);
    }

    /**
     * Gets the attribute value for HANDLE_MODE using the alias name HandleMode.
     * @return the HANDLE_MODE
     */
    public String getHandleMode() {
        return (String) getAttributeInternal(HANDLEMODE);
    }

    /**
     * Sets <code>value</code> as attribute value for HANDLE_MODE using the alias name HandleMode.
     * @param value value to set the HANDLE_MODE
     */
    public void setHandleMode(String value) {
        setAttributeInternal(HANDLEMODE, value);
    }

    /**
     * Gets the attribute value for TEMPLATE_FILE using the alias name TemplateFile.
     * @return the TEMPLATE_FILE
     */
    public String getTemplateFile() {
        return (String) getAttributeInternal(TEMPLATEFILE);
    }

    /**
     * Sets <code>value</code> as attribute value for TEMPLATE_FILE using the alias name TemplateFile.
     * @param value value to set the TEMPLATE_FILE
     */
    public void setTemplateFile(String value) {
        setAttributeInternal(TEMPLATEFILE, value);
    }

    /**
     * Gets the attribute value for DATA_START_LINE using the alias name DataStartLine.
     * @return the DATA_START_LINE
     */
    public Number getDataStartLine() {
        return (Number) getAttributeInternal(DATASTARTLINE);
    }

    /**
     * Sets <code>value</code> as attribute value for DATA_START_LINE using the alias name DataStartLine.
     * @param value value to set the DATA_START_LINE
     */
    public void setDataStartLine(Number value) {
        setAttributeInternal(DATASTARTLINE, value);
    }

    /**
     * Gets the attribute value for COMBINATION_ID using the alias name CombinationId.
     * @return the COMBINATION_ID
     */
    public String getCombinationId() {
        return (String) getAttributeInternal(COMBINATIONID);
    }

    /**
     * Sets <code>value</code> as attribute value for COMBINATION_ID using the alias name CombinationId.
     * @param value value to set the COMBINATION_ID
     */
    public void setCombinationId(String value) {
        setAttributeInternal(COMBINATIONID, value);
    }

    /**
     * Gets the attribute value for CATEGORY_ID using the alias name CategoryId.
     * @return the CATEGORY_ID
     */
    public String getCategoryId() {
        return (String) getAttributeInternal(CATEGORYID);
    }

    /**
     * Sets <code>value</code> as attribute value for CATEGORY_ID using the alias name CategoryId.
     * @param value value to set the CATEGORY_ID
     */
    public void setCategoryId(String value) {
        setAttributeInternal(CATEGORYID, value);
    }

    /**
     * Gets the attribute value for IS_SPECIAL using the alias name IsSpecial.
     * @return the IS_SPECIAL
     */
    public String getIsSpecial() {
        return (String) getAttributeInternal(ISSPECIAL);
    }

    /**
     * Sets <code>value</code> as attribute value for IS_SPECIAL using the alias name IsSpecial.
     * @param value value to set the IS_SPECIAL
     */
    public void setIsSpecial(String value) {
        setAttributeInternal(ISSPECIAL, value);
    }

    /**
     * Gets the attribute value for IS_CLOSE_RECORD using the alias name IsCloseRecord.
     * @return the IS_CLOSE_RECORD
     */
    public String getIsCloseRecord() {
        return (String) getAttributeInternal(ISCLOSERECORD);
    }

    /**
     * Sets <code>value</code> as attribute value for IS_CLOSE_RECORD using the alias name IsCloseRecord.
     * @param value value to set the IS_CLOSE_RECORD
     */
    public void setIsCloseRecord(String value) {
        setAttributeInternal(ISCLOSERECORD, value);
    }

    /**
     * Gets the associated <code>RowIterator</code> using master-detail link DcmTemplateColumnView.
     */
    public RowIterator getDcmTemplateColumnView() {
        return (RowIterator)getAttributeInternal(DCMTEMPLATECOLUMNVIEW);
    }

    /**
     * Gets the associated <code>RowIterator</code> using master-detail link DcmTemplateValidationView.
     */
    public RowIterator getDcmTemplateValidationView() {
        return (RowIterator)getAttributeInternal(DCMTEMPLATEVALIDATIONVIEW);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_YES_NO.
     */
    public RowSet getLKP_YES_NO() {
        return (RowSet)getAttributeInternal(LKP_YES_NO);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LST_USER.
     */
    public RowSet getLST_USER() {
        return (RowSet)getAttributeInternal(LST_USER);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_LANG.
     */
    public RowSet getLKP_LANG() {
        return (RowSet)getAttributeInternal(LKP_LANG);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_CAT.
     */
    public RowSet getLKP_CAT() {
        return (RowSet)getAttributeInternal(LKP_CAT);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_COM.
     */
    public RowSet getLKP_COM() {
        return (RowSet)getAttributeInternal(LKP_COM);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_IMPORT_MODE.
     */
    public RowSet getLKP_IMPORT_MODE() {
        return (RowSet)getAttributeInternal(LKP_IMPORT_MODE);
    }

    /**
     * Gets the view accessor <code>RowSet</code> LKP_TMP_TABLE.
     */
    public RowSet getLKP_TMP_TABLE() {
        return (RowSet)getAttributeInternal(LKP_TMP_TABLE);
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
}
