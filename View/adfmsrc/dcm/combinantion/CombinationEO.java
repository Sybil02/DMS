package dcm.combinantion;

import oracle.jbo.domain.Date;

import team.epm.dcm.model.DcmCombinationImpl;
import team.epm.dcm.view.DcmCombinationViewRowImpl;

public class CombinationEO {
    public CombinationEO(DcmCombinationViewRowImpl row) {
        this.id=row.getId();
        this.name=row.getName();
        this.code=row.getCode();
        this.locale=row.getLocale();
        this.createdAt=row.getCreatedAt();
        this.updatedAt=row.getUpdatedAt();
        this.updatedBy=row.getUpdatedBy();
        this.createdBy=row.getCreatedBy();
    }
    private String id;
    private String name;
    private String code;
    private String locale;
    private Date createdAt;
    private Date updatedAt;
    private String updatedBy;
    private String createdBy;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
