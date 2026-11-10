package com.pbo.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kalkulasi")
public class Kalkulasi {

    // Hanya 4 kolom di database: id, bil1, operasi, bil2 (hasil TIDAK disimpan)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bil1", nullable = false, precision = 38, scale = 10)
    private BigDecimal bil1;

    @Column(name = "operasi", nullable = false, length = 1)
    private String operator;

    @Column(name = "bil2", nullable = false, precision = 38, scale = 10)
    private BigDecimal bil2;

    // Hitung hasil dari bil1, operator, bil2
    public static BigDecimal hitung(BigDecimal a, String op, BigDecimal b) {
        switch (op) {
            case "+":
                return a.add(b);
            case "-":
                return a.subtract(b);
            case "x":
                return a.multiply(b).setScale(10, RoundingMode.HALF_UP);
            case "/":
                if (b.signum() == 0) {
                    throw new IllegalArgumentException("Tidak bisa membagi dengan nol.");
                }
                return a.divide(b, 10, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Operator tidak valid.");
        }
    }

    // Ubah angka jadi teks rapi: 7.0000000000 -> "7", 2.5000000000 -> "2.5"
    public static String format(BigDecimal angka) {
        if (angka == null) {
            return "";
        }
        return angka.stripTrailingZeros().toPlainString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getBil1() { return bil1; }
    public void setBil1(BigDecimal bil1) { this.bil1 = bil1; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public BigDecimal getBil2() { return bil2; }
    public void setBil2(BigDecimal bil2) { this.bil2 = bil2; }

    // Dipakai di tabel HTML (bukan kolom database)
    public String getBil1Tampil() { return format(bil1); }
    public String getBil2Tampil() { return format(bil2); }

    public String getHasilTampil() {
        if (bil1 == null || bil2 == null || operator == null) {
            return "-";
        }
        try {
            return format(hitung(bil1, operator, bil2));
        } catch (IllegalArgumentException e) {
            return "-";
        }
    }
}