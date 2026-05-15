package com.webempresarial.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, String> {
}
