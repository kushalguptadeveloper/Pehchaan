package com.example.kushalgupta.ultrahack.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Confidence {

    @SerializedName("S")
    @Expose
    private String s;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

}