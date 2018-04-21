package com.example.kushalgupta.ultrahack;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "present")

public class nosqlModel {
    private String rollNumber;
    private String confidence;
    private String date;
    @DynamoDBHashKey(attributeName = "roll_number")
    @DynamoDBAttribute(attributeName = "roll_number")
    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(final String rollNumber1) {
        this.rollNumber = rollNumber1;
    }

    @DynamoDBAttribute(attributeName = "confidence")
    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(final String confidence1) {
        this.confidence= confidence1;
    }

    @DynamoDBAttribute(attributeName = "date")
    public String getDate() {
        return date;
    }

    public void setDate(final String date1) {
        this.date= date1;
    }

}