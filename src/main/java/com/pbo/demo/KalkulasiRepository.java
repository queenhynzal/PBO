package com.pbo.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KalkulasiRepository extends JpaRepository<Kalkulasi, Long> {

    List<Kalkulasi> findAllByOrderByIdAsc();
}