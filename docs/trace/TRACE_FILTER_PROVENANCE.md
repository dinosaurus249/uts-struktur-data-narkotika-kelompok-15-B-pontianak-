# Skenario 5 – Filter gabungan dicocokkan dengan provenance

**Filter:** jenis narkotika mengandung `sabu` **DAN** berat 1–10 gram **DAN** pasal terbukti mengandung `114` **DAN** vonis 60–96 bulan.

**Hasil program** (menu 6, lihat `docs/demo/demo-cli-transkrip.txt` dan `docs/demo/hasil-filter-gabungan.csv`): 3 record.

| caseId | Nomor perkara | jenis | berat (g) | pasal terbukti | vonis (bln) |
|---|---|---|---|---|---|
| P015 | 602/Pid.Sus/2025/PN Ptk | sabu | 2.25 | Pasal 114 ayat (1) UU 35/2009 | 84 |
| P022 | 727/Pid.Sus/2025/PN Ptk | sabu | 3.52 | Pasal 114 ayat (1) UU 35/2009 | 84 |
| P045 | 352/Pid.Sus/2026/PN Ptk | sabu | 1.07 | Pasal 114 ayat (1) UU 35/2009 | 60 |

## Pemeriksaan terhadap `docs/data-provenance.csv` dan PDF sumber

| caseId | field | nilai | halaman | yang tertulis di PDF |
|---|---|---|---|---|
| P015 | beratBarangBuktiGram | 2.25 | 29 | 5 klip: 0,49 + 0,75 + 0,41 + 0,20 + 0,40 = total netto 2,25 g |
| P015 | pasalTerbukti | Pasal 114 ayat (1) UU 35/2009 | 28 | Memperhatikan Pasal 114 ayat (1) UU 35/2009 |
| P015 | vonisBulan | 84 | 29 | pidana penjara 7 tahun |
| P022 | beratBarangBuktiGram | 3.52 | 21 | sabu kode A netto 3,52 g (A1 3,26 g + A2 0,26 g) |
| P022 | pasalTerbukti | Pasal 114 ayat (1) UU 35/2009 | 21 | amar: perantara jual beli; Memperhatikan Pasal 114 ayat (1) |
| P022 | vonisBulan | 84 | 21 | pidana penjara 7 tahun |
| P045 | beratBarangBuktiGram | 1.07 | 25 | sabu netto 1,07 g (A1 0,47 g + B 0,60 g) |
| P045 | pasalTerbukti | Pasal 114 ayat (1) UU 35/2009 | 25 | Memperhatikan Pasal 114 ayat (1) UU 35/2009 |
| P045 | vonisBulan | 60 | 25 | pidana penjara 5 tahun |

Semua record `jenisNarkotika = sabu` sesuai amar. Pemeriksaan otomatis yang sama: `RealDataScenarioTest.filterGabunganCocokDenganProvenance`;
`RealDataScenarioTest.setiapRecordPunyaProvenanceYangKonsisten` memastikan nilai CSV dan provenance sama untuk 50 record.

## Riwayat perubahan hasil filter
Sebelum review PDF (lihat `REVIEW_REPORT.md`) filter ini menghasilkan 2 record (P015, P045):
- P015 tercatat 4,5 g karena total 2,25 g ikut dijumlahkan sebagai barang bukti tambahan (hitung ganda); setelah dikoreksi tetap lolos filter.
- P022 tercatat 0 g karena berat tidak terbaca OCR; setelah dikoreksi menjadi 3,52 g, record ini ikut lolos.

Ini contoh bahwa kesalahan ekstraksi langsung mengubah hasil fitur, sehingga verifikasi manual tetap wajib.

## Yang masih harus dilakukan anggota
- [ ] Buka PDF ketiga perkara pada halaman di atas dan pastikan nilainya sendiri.
- [ ] Setelah diperiksa, isi `extractor`/`reviewer` dan ubah `status` menjadi `verified` untuk baris-baris tersebut.

Diperiksa oleh: ____________ (anggota)  Tanggal: ____________
