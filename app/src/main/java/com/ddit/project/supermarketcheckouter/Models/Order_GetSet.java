package com.ddit.project.supermarketcheckouter.Models;

public class Order_GetSet {

    String order_id;
    String payment_refid;
    String user_id;
    String user_name;
    String productlist;
    String total_amount;
    String ondate;
    String payment_status;
    String admin_approve;

    public Order_GetSet() {
    }

    public Order_GetSet(String id, String rid, String u_id, String u_name, String list, String amount, String date, String p_status, String approve){
        order_id = id;
        payment_refid = rid;
        user_id = u_id;
        user_name = u_name;
        productlist = list;
        total_amount = amount;
        ondate = date;
        payment_status =  p_status;
        admin_approve = approve;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPayment_refid() {
        return payment_refid;
    }

    public void setPayment_refid(String payment_refid) {
        this.payment_refid = payment_refid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getProductlist() {
        return productlist;
    }

    public void setProductlist(String productlist) {
        this.productlist = productlist;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getOndate() {
        return ondate;
    }

    public void setOndate(String ondate) {
        this.ondate = ondate;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getAdmin_approve() {
        return admin_approve;
    }

    public void setAdmin_approve(String admin_approve) {
        this.admin_approve = admin_approve;
    }
}
