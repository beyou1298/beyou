package com.beyou.shipping;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.beyou.common.entity.Country;
import com.beyou.common.entity.ShippingRate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ShippingRateRepositoryTests {
    
    @Autowired
    private ShippingRateRepository repo;

    @Test
    public void testFindByCountryAndState(){
        Country usa = new Country(234);
        String state = "New York";
        ShippingRate shippingRate = repo.findByCountryAndState(usa, state);

        System.out.println(shippingRate);
    }
}