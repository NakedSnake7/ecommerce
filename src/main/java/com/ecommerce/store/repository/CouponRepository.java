package com.ecommerce.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.store.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, String> {
}
