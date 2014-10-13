package dms.login;

import java.io.Serializable;

import team.epm.dms.view.DmsUserViewRowImpl;

public class Person implements Serializable {
    private String name;
    private String acc;
    private String locale;
    private String id;
    private String mail;

    public Person(DmsUserViewRowImpl row) {
        this.updateUser(row);
    }

    public void updateUser(DmsUserViewRowImpl row) {
        this.name=row.getName();
        this.locale=row.getLocale();
        this.acc=row.getAcc();
        this.id=row.getId();
        this.mail=row.getMail();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getAcc() {
        return acc;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }
}
