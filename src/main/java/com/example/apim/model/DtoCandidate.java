package com.example.apim.model;

import java.util.ArrayList;
import java.util.List;

public class DtoCandidate {
    private String name;
    private String purpose;
    private List<DtoFieldCandidate> fields;

    public DtoCandidate() {
        this("", "", new ArrayList<>());
    }

    public DtoCandidate(String name, String purpose, List<DtoFieldCandidate> fields) {
        this.name = name;
        this.purpose = purpose;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public List<DtoFieldCandidate> getFields() {
        return fields;
    }

    public void setFields(List<DtoFieldCandidate> fields) {
        this.fields = fields;
    }
}
