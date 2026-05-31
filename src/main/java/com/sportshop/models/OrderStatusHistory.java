package com.sportshop.models;

import java.sql.Timestamp;

public class OrderStatusHistory {

    private int id;
    private int orderId;
    private String oldStatus;
    private String newStatus;
    private Integer changedByUserId;
    private String comment;
    private Timestamp createdAt;

    public OrderStatusHistory() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Integer getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Integer changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
