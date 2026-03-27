package com.ecomerce.store.repository;

import org.springframework.data.jpa.repository.JpaRepository; 
import com.ecomerce.store.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, String> {
}
