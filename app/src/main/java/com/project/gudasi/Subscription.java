package com.project.gudasi;

import com.google.firebase.firestore.PropertyName;

public class Subscription {
    private int id;
    private String subject;
    private String date;
    private String serviceName;
    private String renewalPrice;

    public Subscription() {}  // Firebase는 빈 생성자 필요

    // 생성자
    public Subscription(int id, String subject, String date, String serviceName, String purchaseDate, String renewalPrice) {
        this.id = id;
        this.subject = subject;
        this.date = date;
        this.serviceName = serviceName;
        this.renewalPrice = renewalPrice;
    }

    @PropertyName("subject")
    public String getSubject() { return subject; }
    @PropertyName("subject")
    public void setSubject(String subject) { this.subject = subject; }

    @PropertyName("date")
    public String getDate() { return date; }
    @PropertyName("date")
    public void setDate(String date) { this.date = date; }

    @PropertyName("service_name")
    public String getServiceName() { return serviceName; }
    @PropertyName("service_name")
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }


    @PropertyName("renewal_price")
    public String getRenewalPrice() { return renewalPrice; }
    @PropertyName("renewal_price")
    public void setRenewalPrice(String renewalPrice) { this.renewalPrice = renewalPrice; }
}

