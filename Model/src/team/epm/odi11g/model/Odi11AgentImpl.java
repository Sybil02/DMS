package team.epm.odi11g.model;

import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

import team.epm.dms.common.DmsEntityImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Thu Jan 22 20:37:04 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class Odi11AgentImpl extends DmsEntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(Odi11AgentImpl obj) {
                return obj.getId();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        AgentAlias {
            public Object get(Odi11AgentImpl obj) {
                return obj.getAgentAlias();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setAgentAlias((String)value);
            }
        }
        ,
        AgentName {
            public Object get(Odi11AgentImpl obj) {
                return obj.getAgentName();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setAgentName((String)value);
            }
        }
        ,
        AgentHost {
            public Object get(Odi11AgentImpl obj) {
                return obj.getAgentHost();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setAgentHost((String)value);
            }
        }
        ,
        AgentContext {
            public Object get(Odi11AgentImpl obj) {
                return obj.getAgentContext();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setAgentContext((String)value);
            }
        }
        ,
        AgentPort {
            public Object get(Odi11AgentImpl obj) {
                return obj.getAgentPort();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setAgentPort((String)value);
            }
        }
        ,
        Protocol {
            public Object get(Odi11AgentImpl obj) {
                return obj.getProtocol();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setProtocol((String)value);
            }
        }
        ,
        Locale {
            public Object get(Odi11AgentImpl obj) {
                return obj.getLocale();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        CreatedAt {
            public Object get(Odi11AgentImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(Odi11AgentImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(Odi11AgentImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(Odi11AgentImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(Odi11AgentImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(Odi11AgentImpl object);

        public abstract void put(Odi11AgentImpl object, Object value);

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
    public static final int AGENTALIAS = AttributesEnum.AgentAlias.index();
    public static final int AGENTNAME = AttributesEnum.AgentName.index();
    public static final int AGENTHOST = AttributesEnum.AgentHost.index();
    public static final int AGENTCONTEXT = AttributesEnum.AgentContext.index();
    public static final int AGENTPORT = AttributesEnum.AgentPort.index();
    public static final int PROTOCOL = AttributesEnum.Protocol.index();
    public static final int LOCALE = AttributesEnum.Locale.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();

    /**
     * This is the default constructor (do not remove).
     */
    public Odi11AgentImpl() {
    }

    /**
     * @return the definition object for this instance class.
     */
    public static synchronized EntityDefImpl getDefinitionObject() {
        if (mDefinitionObject == null) {
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.odi11g.model.Odi11Agent");
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
     * Gets the attribute value for AgentAlias, using the alias name AgentAlias.
     * @return the AgentAlias
     */
    public String getAgentAlias() {
        return (String)getAttributeInternal(AGENTALIAS);
    }

    /**
     * Sets <code>value</code> as the attribute value for AgentAlias.
     * @param value value to set the AgentAlias
     */
    public void setAgentAlias(String value) {
        setAttributeInternal(AGENTALIAS, value);
    }

    /**
     * Gets the attribute value for AgentName, using the alias name AgentName.
     * @return the AgentName
     */
    public String getAgentName() {
        return (String)getAttributeInternal(AGENTNAME);
    }

    /**
     * Sets <code>value</code> as the attribute value for AgentName.
     * @param value value to set the AgentName
     */
    public void setAgentName(String value) {
        setAttributeInternal(AGENTNAME, value);
    }

    /**
     * Gets the attribute value for AgentHost, using the alias name AgentHost.
     * @return the AgentHost
     */
    public String getAgentHost() {
        return (String)getAttributeInternal(AGENTHOST);
    }

    /**
     * Sets <code>value</code> as the attribute value for AgentHost.
     * @param value value to set the AgentHost
     */
    public void setAgentHost(String value) {
        setAttributeInternal(AGENTHOST, value);
    }

    /**
     * Gets the attribute value for AgentContext, using the alias name AgentContext.
     * @return the AgentContext
     */
    public String getAgentContext() {
        return (String)getAttributeInternal(AGENTCONTEXT);
    }

    /**
     * Sets <code>value</code> as the attribute value for AgentContext.
     * @param value value to set the AgentContext
     */
    public void setAgentContext(String value) {
        setAttributeInternal(AGENTCONTEXT, value);
    }

    /**
     * Gets the attribute value for AgentPort, using the alias name AgentPort.
     * @return the AgentPort
     */
    public String getAgentPort() {
        return (String)getAttributeInternal(AGENTPORT);
    }

    /**
     * Sets <code>value</code> as the attribute value for AgentPort.
     * @param value value to set the AgentPort
     */
    public void setAgentPort(String value) {
        setAttributeInternal(AGENTPORT, value);
    }

    /**
     * Gets the attribute value for Protocol, using the alias name Protocol.
     * @return the Protocol
     */
    public String getProtocol() {
        return (String)getAttributeInternal(PROTOCOL);
    }

    /**
     * Sets <code>value</code> as the attribute value for Protocol.
     * @param value value to set the Protocol
     */
    public void setProtocol(String value) {
        setAttributeInternal(PROTOCOL, value);
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


}
