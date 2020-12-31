package models.guest;

import javafx.beans.property.SimpleStringProperty;
import sun.java2d.pipe.SpanShapeRenderer;

public class ClubNameDescr {
    private SimpleStringProperty name;
    private SimpleStringProperty descr;

    public ClubNameDescr(String name, String descr) {
        this.name = new SimpleStringProperty(name);
        this.descr = new SimpleStringProperty(descr);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDesc() {
        return descr.get();
    }

    public SimpleStringProperty descProperty() {
        return descr;
    }

    public void setDesc(String descr) {
        this.descr.set(descr);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
