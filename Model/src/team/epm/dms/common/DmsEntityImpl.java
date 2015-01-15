package team.epm.dms.common;

import java.lang.reflect.Method;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.AttributeDef;
import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.EntityCache;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;
import oracle.jbo.server.TransactionEvent;

import team.epm.dms.model.DmsGroupCollImpl;
import team.epm.dms.model.DmsGroupDefImpl;
import team.epm.dms.model.DmsGroupImpl;

public class DmsEntityImpl extends EntityImpl {
    private static ADFLogger logger =
        ADFLogger.createADFLogger(DmsEntityImpl.class);

    public DmsEntityImpl() {
        super();
    }

    @Override
    protected void prepareForDML(int operation,
                                 TransactionEvent transactionEvent) {
        super.prepareForDML(operation, transactionEvent);
        if (operation == DML_UPDATE) {
            //update the updatedat and updatedby attributes
            this.setAttribute("UpdatedAt",
                              new Date(new java.sql.Timestamp(System.currentTimeMillis())));
            this.setAttribute("UpdatedBy",
                              this.getDBTransaction().getSession().getUserData().get("userId") +
                              "");
        }
    }
    //获取所有语言列表

    private List<String> getLangList() {
        List<String> lang = new ArrayList<String>();
        DBTransaction trans = getDBTransaction();
        String sql =
            "select distinct t.code　from dms_lookup t where t.lookup_type='DMS_LANGUAGE'";
        PreparedStatement stat =
            trans.createPreparedStatement(sql, DBTransaction.DEFAULT);
        ResultSet res = null;
        try {
            res = stat.executeQuery();
            while (res.next()) {
                lang.add(res.getString(1));
            }
        } catch (Exception e) {
            logger.severe(e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    logger.severe(e);
                }
            }
        }
        return lang;
    }

    @Override
    protected StringBuffer buildDMLStatement(int operation,
                                             AttributeDefImpl[] attributeDefImpl,
                                             AttributeDefImpl[] attributeDefImpl2,
                                             AttributeDefImpl[] attributeDefImpl3,
                                             boolean b) {
        return super.buildDMLStatement(operation, attributeDefImpl,
                                       attributeDefImpl2, attributeDefImpl3,
                                       b);
    }

    @Override
    protected int bindDMLStatement(int operation,
                                   PreparedStatement preparedStatement,
                                   AttributeDefImpl[] attributeDefImpl,
                                   AttributeDefImpl[] attributeDefImpl2,
                                   AttributeDefImpl[] attributeDefImpl3,
                                   HashMap hashMap,
                                   boolean b) throws SQLException {
        if (operation == DML_UPDATE || operation == DML_DELETE) {
            StringBuffer sqlBuf =
                super.buildDMLStatement(operation, trimMultiLangAttr(attributeDefImpl),
                                        trimMultiLangAttr(attributeDefImpl2),
                                        trimMultiLangAttr(attributeDefImpl3),
                                        b);
            String sql = sqlBuf.toString();
            sql = sql.replaceAll("LOCALE=", "'-1'!=");
            PreparedStatement stat =
                preparedStatement.getConnection().prepareStatement(sql);
            super.bindDMLStatement(operation, stat,
                                   trimMultiLangAttr(attributeDefImpl),
                                   trimMultiLangAttr(attributeDefImpl2),
                                   trimMultiLangAttr(attributeDefImpl3),
                                   hashMap, b);
            stat.execute();
            stat.close();
        } else if (operation == DML_INSERT) {
            StringBuffer sqlBuf =
                super.buildDMLStatement(operation, attributeDefImpl,
                                        attributeDefImpl2, attributeDefImpl3,
                                        b);
            String sql = sqlBuf.toString();
            String curLang = this.getAttribute("Locale").toString();
            PreparedStatement stat =
                preparedStatement.getConnection().prepareStatement(sql);
            for (String lang : this.getLangList()) {
                if (lang.equals(curLang))
                    continue;
                this.setAttributeInternal("Locale", lang);
                super.bindDMLStatement(operation, stat, attributeDefImpl,
                                       attributeDefImpl2, attributeDefImpl3,
                                       hashMap, b);
                stat.execute();
            }
            stat.close();
            this.setAttributeInternal("Locale", curLang);
        }
        return super.bindDMLStatement(operation, preparedStatement,
                                      attributeDefImpl, attributeDefImpl2,
                                      attributeDefImpl3, hashMap, b);
    }
    //filter the multi language attributes

    private AttributeDefImpl[] trimMultiLangAttr(AttributeDefImpl[] attributes) {
        List<AttributeDefImpl> attrs = new ArrayList<AttributeDefImpl>();
        if (attributes != null) {
            for (AttributeDefImpl attr : attributes) {
                if (!"Locale".equals(attr.getName())) {
                    if (!"true".equals(attr.getProperty("isMultiLangAttr"))) {
                        attrs.add(attr);
                    }
                }
            }
        }
        return attrs.toArray(new AttributeDefImpl[] { });
    }
}
