package com.beyou.common.entity.order;

import java.beans.Transient;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.beyou.common.entity.AbstractAddress;
import com.beyou.common.entity.Address;
import com.beyou.common.entity.Customer;

@Entity
@Table(name = "orders")
public class Order extends AbstractAddress{

    @Column(nullable = false, length = 45)
    private String country;

    private Date orderTime;

    private float shippingCost;
    private float productCost;
    private float subTotal;
    private float tax;
    private float total;

    private int deliverDays;
    private Date deliveryDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedTime ASC")
    private List<OrderTrack> orderTracks = new ArrayList<>();

    public Order() {
	}
	
	public Order(Integer id, Date orderTime, float productCost, float subTotal, float total) {
		this.id = id;
		this.orderTime = orderTime;
		this.productCost = productCost;
		this.subTotal = subTotal;
		this.total = total;
	}

    public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }

    public float getProductCost() {
        return productCost;
    }

    public void setProductCost(float productCost) {
        this.productCost = productCost;
    }

    public float getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(float subTotal) {
        this.subTotal = subTotal;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public int getDeliverDays() {
        return deliverDays;
    }

    public void setDeliverDays(int deliverDays) {
        this.deliverDays = deliverDays;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(Set<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public List<OrderTrack> getOrderTracks() {
        return orderTracks;
    }

    public void setOrderTracks(List<OrderTrack> orderTracks) {
        this.orderTracks = orderTracks;
    }

    public void copyAddressFromCustomer(){

        setFirstName(customer.getFirstName());
        setLastName(customer.getLastName());
        setPhoneNumber(customer.getPhoneNumber());
        setAddressLine1(customer.getAddressLine1());
        setAddressLine2(customer.getAddressLine2());
        setCity(customer.getCity());
        setCountry(customer.getCountry().getName());
        setPostalCode(customer.getPostalCode());
        setState(customer.getState());
    }

    @Override
    public String toString() {
        return "Order [customer=" + customer + ", id=" + id + ", orderStatus=" + orderStatus + ", paymentMethod="
                + paymentMethod + ", subTotal=" + subTotal + "]";
    }
    
    @Transient
	public String getDestination() {
		String destination =  city + ", ";
		if (state != null && !state.isEmpty()) destination += state + ", ";
		destination += country;
		
		return destination;
	}

    public void copyShippingAddress(Address address){

        setFirstName(address.getFirstName());
        setLastName(address.getLastName());
        setPhoneNumber(address.getPhoneNumber());
        setAddressLine1(address.getAddressLine1());
        setAddressLine2(address.getAddressLine2());
        setCity(address.getCity());
        setCountry(address.getCountry().getName());
        setPostalCode(address.getPostalCode());
        setState(address.getState());
    }

    @Transient
    public String getShippingAddress() {
        String address = firstName;

        if(lastName != null && !lastName.isEmpty()){
            address += " " + lastName;
        }

        if(!addressLine1.isEmpty()){
            address += ", " + addressLine1;
        }

        if(addressLine2 != null && !addressLine2.isEmpty()){
            address += ", " + addressLine2;
        }

        if(!city.isEmpty()){
            address += ", " + city;
        }

        if(state != null && !state.isEmpty()){
            address += ", " + state;
        }

        address += ", " + country;

        if(!postalCode.isEmpty()){
            address += ". Postal Code: " + postalCode;
        }

        if(!phoneNumber.isEmpty()){
            address += ". Phone Number: " + phoneNumber;
        }

        return address;
    }

    @Transient
	public String getDeliveryDateOnForm() {
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormatter.format(this.deliveryDate);
	}

    public void setDeliveryDateOnForm(String dateString) {
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
 		
		try {
			this.deliveryDate = dateFormatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		} 		
	}

    @Transient
    public String getRecipientName(){
        String name = firstName;
        if(lastName != null && !lastName.isEmpty()) name += " " + lastName;
        return name;
    }

    @Transient
    public String getRecipientAddress() {

        String address = addressLine1;

        if(addressLine2 != null && !addressLine2.isEmpty()){
            address += ", " + addressLine2;
        }

        if(!city.isEmpty()){
            address += ", " + city;
        }

        if(state != null && !state.isEmpty()){
            address += ", " + state;
        }

        address += ", " + country;

        if(!postalCode.isEmpty()){
            address += ". " + postalCode;
        }

        return address;
    }

    @Transient
    public boolean isCOD(){
        return paymentMethod.equals(PaymentMethod.COD);
    }

    @Transient
    public boolean isPicked(){
        return hasStatus(OrderStatus.PICKED);
    }

    @Transient
    public boolean isShipping(){
        return hasStatus(OrderStatus.SHIPPING);
    }

    @Transient
    public boolean isDelivered(){
        return hasStatus(OrderStatus.DELIVERED); 
    }

    @Transient
    public boolean isReturnedRequested(){
        return hasStatus(OrderStatus.RETURN_REQUESTED);
    }

    @Transient
    public boolean isProcessing(){
        return hasStatus(OrderStatus.PROCESSING);
    } 

    @Transient
    public boolean isReturned(){
        return hasStatus(OrderStatus.RETURNED);
    }

    public boolean hasStatus(OrderStatus status){
        for(OrderTrack aTrack : orderTracks){
            if(aTrack.getStatus().equals(status)){
                return true;
            }
        }

        return false;
    }

    @Transient
    public String getProductNames(){
        String productNames = "";

        productNames = "<ul>";

        for(OrderDetail detail : orderDetails){
            productNames += "<li>" + detail.getProduct().getShortName() + "</li>";
        }

        productNames += "</ul>";

        return productNames;
    }
}
