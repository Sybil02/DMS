package team.epm.dms.view;

import oracle.jbo.server.ViewObjectImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Dec 17 16:29:01 CST 2014
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DmsUnGroupedRoleViewImpl extends ViewObjectImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public DmsUnGroupedRoleViewImpl() {
    }

    /**
     * Returns the bind variable value for locale.
     * @return bind variable value for locale
     */
    public String getlocale() {
        return (String)getNamedWhereClauseParam("locale");
    }

    /**
     * Sets <code>value</code> for bind variable locale.
     * @param value value to bind as locale
     */
    public void setlocale(String value) {
        setNamedWhereClauseParam("locale", value);
    }

    /**
     * Returns the bind variable value for groupId.
     * @return bind variable value for groupId
     */
    public String getgroupId() {
        return (String)getNamedWhereClauseParam("groupId");
    }

    /**
     * Sets <code>value</code> for bind variable groupId.
     * @param value value to bind as groupId
     */
    public void setgroupId(String value) {
        setNamedWhereClauseParam("groupId", value);
    }
}