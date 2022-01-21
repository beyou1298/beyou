package com.beyou.checkout;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.beyou.Utility;
import com.beyou.address.AddressService;
import com.beyou.checkout.paypal.PayPalApiException;
import com.beyou.checkout.paypal.PayPalService;
import com.beyou.common.entity.Address;
import com.beyou.common.entity.CartItem;
import com.beyou.common.entity.Customer;
import com.beyou.common.entity.ShippingRate;
import com.beyou.common.entity.order.Order;
import com.beyou.common.entity.order.PaymentMethod;
import com.beyou.customer.CustomerService;
import com.beyou.order.OrderService;
import com.beyou.setting.CurrencySettingBag;
import com.beyou.setting.EmailSettingBag;
import com.beyou.setting.PaymentSettingBag;
import com.beyou.setting.SettingService;
import com.beyou.shipping.ShippingRateService;
import com.beyou.shoppingcart.ShoppingCartService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckOutController {
    
    @Autowired
    private CheckOutService checkOutService;
    
    @Autowired
    private CustomerService customerService;

    @Autowired 
    private AddressService addressService;

    @Autowired
    private ShippingRateService shipService;

    @Autowired
    private ShoppingCartService cartService;

    @Autowired private OrderService orderService;

    @Autowired private SettingService settingService;

    @Autowired private PayPalService paypalService;

    @GetMapping("/checkout")
    public String showCheckOutPage(Model model, HttpServletRequest request){
        Customer customer = getAuthenticatedCustomer(request);

        Address defaultAddress = addressService.getDefaultAddress(customer);
        ShippingRate shippingRate = null;

        if(defaultAddress != null ){
            model.addAttribute("shippingAddress", defaultAddress.toString());
            shippingRate = shipService.getShippingRateForAddress(defaultAddress);
        }
        else{
            model.addAttribute("shippingAddress", customer.toString());
            shippingRate = shipService.getShippingRateForCustomer(customer);
        }

        if(shippingRate == null){
            return "redirect:/cart";
        }

        List<CartItem> cartItems = cartService.listCartItem(customer);
        CheckOutInfo checkOutInfo = checkOutService.prepareCheckOut(cartItems, shippingRate);

        String currencyCode = settingService.getCurrencyCode();
        PaymentSettingBag paymentSettings = settingService.getPaymentSettings();
        String paypalClientId = paymentSettings.getClientID();
        String paypalClientSecret = paymentSettings.getClientSecret();

        model.addAttribute("paypalClientId", paypalClientId);
        model.addAttribute("paypalClientSecret", paypalClientSecret);
        model.addAttribute("customer", customer);
        model.addAttribute("currencyCode", currencyCode);
        model.addAttribute("checkOutInfo", checkOutInfo);
        model.addAttribute("cartItems", cartItems);

        return "checkout/checkout";
    }


    private Customer getAuthenticatedCustomer(HttpServletRequest request){
        String email = Utility.getEmailOfAuthenticatedCustomer(request);

        return customerService.getCustomerByEmail(email);
    }

    @PostMapping("/place_order")
    public String placeOrder(HttpServletRequest request) throws UnsupportedEncodingException, MessagingException{
        String paymentType = request.getParameter("paymentMethod");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);

        Customer customer = getAuthenticatedCustomer(request);

        Address defaultAddress = addressService.getDefaultAddress(customer);
        ShippingRate shippingRate = null;

        if(defaultAddress != null ){
            shippingRate = shipService.getShippingRateForAddress(defaultAddress);
        }
        else{
            shippingRate = shipService.getShippingRateForCustomer(customer);
        }

        List<CartItem> cartItems = cartService.listCartItem(customer);
        CheckOutInfo checkOutInfo = checkOutService.prepareCheckOut(cartItems, shippingRate);

        Order createdOrder = orderService.createOrder(customer, defaultAddress, cartItems, paymentMethod, checkOutInfo);
        cartService.deleteByCustomer(customer);
        sendOrderConfirmationEmail(request, createdOrder);

        return "checkout/order_completed";
    }


    private void sendOrderConfirmationEmail(HttpServletRequest request, Order order) throws MessagingException, UnsupportedEncodingException {
        EmailSettingBag emailSettings = settingService.getEmailSettings();
        JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
        mailSender.setDefaultEncoding("utf-8");

        String toAddress = order.getCustomer().getEmail();
        String subject = emailSettings.getOrderConfirmationSubject();
        String content = emailSettings.getOrderConfirmationContent();

        subject = subject.replace("[[orderId]]", String.valueOf(order.getId()));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailSettings.getfromAddress(), emailSettings.getSenderName());
        helper.setTo(toAddress);
        helper.setSubject(subject);

        DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss E, dd MMM yyyy");
        String orderTime = dateFormatter.format(order.getOrderTime());

        CurrencySettingBag currencySettings = settingService.getCurrencySettings();
        String totalAmount = Utility.formatCurrency(order.getTotal(), currencySettings);

        content = content.replace("[[name]]", order.getCustomer().getFullName());
        content = content.replace("[[orderId]]", String.valueOf(order.getId()));
        content = content.replace("[[orderTime]]", orderTime);
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        content = content.replace("[[total]]", totalAmount);
        content = content.replace("[[paymentMethod]]", order.getPaymentMethod().toString());

        helper.setText(content, true);
        mailSender.send(message);
    }

    @PostMapping("/process_paypal_order")
	public String processPayPalOrder(HttpServletRequest request, Model model) 
			throws UnsupportedEncodingException, MessagingException {
		String orderId = request.getParameter("orderId");
		
		String pageTitle = "Checkout Failure";
		String message = null;
		
		try {
			if (paypalService.validateOrder(orderId)) {
                return placeOrder(request);
			} else {
				pageTitle = "Checkout Failure";
				message = "ERROR: Transaction could not be completed because order information is invalid";
			}
		} catch (PayPalApiException e) {
			message = "ERROR: Transaction failed due to error: " + e.getMessage();
		}
		
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("title", pageTitle);
		model.addAttribute("message", message);
		
		return "message";
	}
}
