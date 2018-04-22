package com.example.kushalgupta.ultrahack.models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RollNumber {

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