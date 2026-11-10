package com.pbo.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/kalkulator")
public class KalkulatorController {

    // Batas input supaya angka tidak melebihi kapasitas kolom database
    private static final BigDecimal BATAS = new BigDecimal("1E12");

    private final KalkulasiRepository repo;

    public KalkulatorController(KalkulasiRepository repo) {
        this.repo = repo;
    }

    // READ: tampilkan form + tabel. Kalau ada ?edit=ID, form diisi data baris itu.
    @GetMapping
    public String tampil(@RequestParam(required = false) Long edit, Model model) {
        if (edit != null) {
            repo.findById(edit).ifPresent(k -> {
                model.addAttribute("editId", k.getId());
                model.addAttribute("bil1", Kalkulasi.format(k.getBil1()));
                model.addAttribute("op", k.getOperator());
                model.addAttribute("bil2", Kalkulasi.format(k.getBil2()));
                model.addAttribute("hasil", "");
            });
        }
        // Kalau belum ada nilai (halaman baru dibuka), pakai nilai awal
        if (!model.containsAttribute("op")) {
            model.addAttribute("bil1", "");
            model.addAttribute("op", "+");
            model.addAttribute("bil2", "");
            model.addAttribute("hasil", "");
        }
        model.addAttribute("daftar", repo.findAllByOrderByIdAsc());
        return "Kalkulator";
    }

    // CREATE (id kosong) atau UPDATE (id terisi): hitung, simpan, lalu tampilkan hasil
    @PostMapping("/hitung")
    public String hitung(@RequestParam(required = false) Long id,
                         @RequestParam String bil1,
                         @RequestParam String op,
                         @RequestParam String bil2,
                         Model model,
                         RedirectAttributes ra) {

        BigDecimal a;
        BigDecimal b;
        BigDecimal hasil;
        try {
            a = parse(bil1, "Bilangan 1");
            b = parse(bil2, "Bilangan 2");
            hasil = Kalkulasi.hitung(a, op, b); // hanya untuk ditampilkan, tidak disimpan
        } catch (IllegalArgumentException e) {
            // Input salah: tampilkan lagi halaman dengan nilai yang tadi diketik
            model.addAttribute("error", e.getMessage());
            model.addAttribute("bil1", bil1);
            model.addAttribute("op", op);
            model.addAttribute("bil2", bil2);
            model.addAttribute("hasil", "");
            model.addAttribute("editId", id);
            model.addAttribute("daftar", repo.findAllByOrderByIdAsc());
            return "Kalkulator";
        }

        // Database hanya menyimpan bil1, operator, bil2
        Kalkulasi k = (id != null) ? repo.findById(id).orElse(new Kalkulasi()) : new Kalkulasi();
        k.setBil1(a);
        k.setOperator(op);
        k.setBil2(b);
        repo.save(k);

        // Flash attribute: input dipertahankan (tidak ke-reset) + hasil terisi
        ra.addFlashAttribute("bil1", Kalkulasi.format(a));
        ra.addFlashAttribute("op", op);
        ra.addFlashAttribute("bil2", Kalkulasi.format(b));
        ra.addFlashAttribute("hasil", Kalkulasi.format(hasil));
        ra.addFlashAttribute("sukses", id != null ? "Data berhasil diubah." : "Data berhasil disimpan.");
        return "redirect:/kalkulator";
    }

    // DELETE
    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("sukses", "Data berhasil dihapus.");
        return "redirect:/kalkulator";
    }

    private BigDecimal parse(String teks, String nama) {
        if (teks == null || teks.isBlank()) {
            throw new IllegalArgumentException(nama + " wajib diisi.");
        }
        BigDecimal angka;
        try {
            angka = new BigDecimal(teks.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nama + " harus berupa angka.");
        }
        if (angka.abs().compareTo(BATAS) > 0) {
            throw new IllegalArgumentException(nama + " terlalu besar (maksimal 1 triliun).");
        }
        return angka.setScale(10, RoundingMode.HALF_UP);
    }
}