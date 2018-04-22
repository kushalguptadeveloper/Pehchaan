package com.example.kushalgupta.ultrahack.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class s3 {

    @SerializedName("roll_number")
    @Expose
    private RollNumber rollNumber;
    @SerializedName("confidence")
    @Expose
    private Confidence confidence;
    @SerializedName("date")
    @Expose
    private Date date;

    public RollNumber getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(RollNumber rollNumber) {
        this.rollNumber = rollNumber;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}