package com.ubit.voicecontentsearcher.model;

public class Record {

    private String name;
    private String Value;


    public Record() {
    }

    public Record(String name, String Value) {
        this.Value = Value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String Value) {
        this.Value = Value;
    }
}
