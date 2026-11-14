# UTS Struktur Data – Putusan Narkotika (Kelompok 15)

Mata kuliah Struktur Data, kelas 3 B, Universitas Muhammadiyah Malang, semester ganjil 2026/2027.

| Anggota                 | NIM               | Peran utama |
|-------------------------|-------------------|---|
| _Ahmad Naufal Nasution_ | _202510370110051_ | _(mis. struktur data + test)_ |
| _Ramadani Dino Yulianto Saputra_  | _202510370110049_ | _(mis. ekstraksi data + fitur aplikasi)_ |

**Struktur utama:** `SinglyLinkedList<T>` (dibuat dari nol, head + tail). **Struktur sekunder:** `ArrayStack<T>` (riwayat audit, LIFO).
Tidak ada koleksi `java.util` (ArrayList, LinkedList, Stack, dll.) pada penyimpanan inti. Alasan: `docs/adr/0001-pemilihan-struktur-data.md`.

## Prasyarat
JDK 17 atau lebih baru dan Maven 3.x.

## Build, test, jalankan
```
mvn clean package        # build + seluruh test
mvn test                 # hanya test (26 test, 5 kelas)
mvn -q compile exec:java # menjalankan aplikasi CLI dengan data/putusan-50.csv
mvn -q compile exec:java -Dexec.args="data/putusan-50.csv"
```

### Contoh perintah di aplikasi
```
1  -> Enter                 impor data/putusan-50.csv (Dibaca 50, valid 50, ditolak 0)
5  -> 1 -> P050             cari caseId; tampil posisi 49 dan "Jumlah langkah: 50"
6  -> sabu, 1, 10, 114, 60, 96   filter gabungan (jenis, berat, pasal, vonis) -> 2 record
7                           agregasi: jumlah perkara, rata-rata vonis, total denda, distribusi jenis
8  -> 2                     tampilan terurut vonis menurun (arsip asli tidak berubah)
9                           riwayat audit (Stack, terbaru di atas)
10 -> hasil.csv             ekspor hasil terakhir ke CSV
13 -> laporan.txt           ekspor laporan teks (agregasi + riwayat audit)
12                          cek invariant struktur utama dan sekunder
```
Transkrip lengkap satu sesi yang mencakup ketujuh skenario uji ada di `docs/demo/`.

## Struktur kode
- `uts.ds` : `StrukturData<T>`, `SinglyLinkedList<T>`, `ArrayStack<T>`, `SearchResult<T>`
- `uts.app`: `Putusan`, `PutusanParser`, `CsvUtil`, `PutusanRepository`, `FilterCriteria`, `Ringkasan`, `ImportResult`, `Main`
- `uts.tools`: alat bantu ekstraksi PDF (di luar struktur data inti, lihat di bawah)

## Pemetaan kontrak `StrukturData<T>`
| Kontrak | SinglyLinkedList | ArrayStack | Alasan penyimpangan |
|---|---|---|---|
| `add(v)` | `addLast(v)` | `push(v)` | Stack hanya boleh menambah di puncak |
| `get(i)` | node ke-i dari head | elemen ke-i dari puncak (0 = puncak) | Posisi stack dihitung dari puncak agar sesuai LIFO |
| `remove(i)` | lepas node ke-i, sambung pointer | hapus ke-i dari puncak (0 = `pop`), sisanya digeser | `remove(i>0)` hanya untuk kelengkapan kontrak; operasi stack normal memakai `pop` |
| `contains`, `size`, `isEmpty`, `clear` | sama | sama | – |
| tambahan | `addFirst`, `insert`, `set`, `search` | `pop`, `peek`, `searchPreserve`, `restore` | Operasi minimum Bagian 3 panduan |

Keduanya memiliki `validateInvariant()` yang dipanggil di test dan menu 12.

## Pengujian
| Kelas test | Isi |
|---|---|
| `SinglyLinkedListTest` | operasi list, batas indeks, hapus pertama/tengah/terakhir, invariant |
| `ArrayStackTest` | LIFO, resize, `searchPreserve` tidak mengubah urutan |
| `PutusanRepositoryTest` | skenario 4.1 no. 1–7 dengan data dummy terkendali |
| `DataFileTest` | `data/putusan-50.csv` terimpor tepat 50 record tanpa error |
| `RealDataScenarioTest` | skenario 5 pada data asli: filter gabungan dicocokkan dengan `docs/data-provenance.csv` |

Trace manual perubahan pointer dan pencacah langkah: `docs/trace/TRACE_LINKED_LIST.md`. Pencocokan filter dengan provenance: `docs/trace/TRACE_FILTER_PROVENANCE.md`.

## Aturan data
- **Berat barang bukti (`beratBarangBuktiGram`)**: jumlah berat netto semua narkotika yang tercantum pada barang bukti di amar putusan (semua jenis dijumlahkan; sisa setelah pengujian laboratorium tidak dipakai).
- **Tanggal putusan (`tanggalPutusan`)**: tanggal putusan **diucapkan** dalam sidang terbuka, bukan tanggal musyawarah ("Demikianlah diputuskan…"). Bila tertulis "pada hari dan tanggal itu juga", keduanya sama.
- Vonis/tuntutan dalam bulan (1 tahun = 12 bulan). Pasal terbukti diambil dari amar/pertimbangan, pasal dakwaan dari surat dakwaan (dakwaan alternatif dipisah dengan "atau").
- Penulisan pasal diseragamkan: `Pasal X ayat (Y) UU 35/2009`, `UU 1/2023 (KUHP)`, `UU 1/2026` (Penyesuaian Pidana).
- **Satu putusan dengan beberapa terdakwa** (P016, P041): satu record, kode terdakwa mewakili terdakwa I; vonis, tuntutan, dan denda diambil untuk terdakwa I.
- Perkara terpisah dari satu kejadian (P019/P020, P027/P028, P035/P036) memakai barang bukti yang sama; total berat lintas record menghitungnya lebih dari sekali.
- Nilai yang tidak ditemukan ditulis kosong (`null`) atau `0`, tidak diperkirakan.
- Delimiter daftar pada `jenisNarkotika` dan `sourcePages`: `;`. Format `sourcePages`: `kelompokAtribut:halaman`, mis. `amar:33;tanggal:33`.
- Nama terdakwa diganti kode `T001`–`T050`; parser menolak `terdakwaKode` yang bukan kode anonim.
- Agregasi: vonis/denda kosong dihitung 0; distribusi jenis memecah `jenisNarkotika` berdasarkan `;`.

## Dataset dan pembagian ekstraksi
`data/putusan-50.csv` berisi 50 record dari Pengadilan Negeri Pontianak. Berkas PDF sumber tidak diunggah; identitas sumber ada di kolom `source_file` pada `docs/data-provenance.csv` (12 atribut per putusan, 600 baris).

**Pengadilan:** menurut dosen, "pengadilan negeri yang berbeda" berarti berbeda antar kelompok. Bukti konfirmasi: _(lampirkan tautan/tangkapan layar, mis. `docs/konfirmasi-dosen.png`)_.

| Anggota      | caseId yang diekstrak | caseId yang direview |
|--------------|-----------------------|--------------------|
| _Ahmad Naufal Nasution_           | _P001–P025_           | _P026–P050_        |
| _Ramadani Dino Yulianto Saputra_ | _P026–P050_           | _P001–P025_        |

**Status verifikasi:** P001–P005 dan P049–P050 dibaca langsung dari PDF; P006–P048 diekstrak semi-otomatis dari OCR. Pada 2026-11-14 seluruh 50 record sudah dipra-review terhadap PDF sumber (dibantu AI; 171 koreksi di 44 perkara, a.l. 27 tanggal, 4 berat, 2 denda, 1 pelanggaran anonimisasi). Rincian dan daftar hal yang belum diperiksa: `docs/trace/REVIEW_REPORT.md`. Karena pra-review ini belum diperiksa anggota, **semua baris provenance tetap `unverified`** dan kolom `extractor`/`reviewer` masih `TODO_NAMA`.

Halaman PDF yang hilang dari berkas unduhan: P025 (hal cetak 29 dari 31) dan P037 (hal cetak 2–3 dari 22); nilai yang ada di halaman tersebut tetap kosong/0.

## Ekstraksi PDF dengan PDFBox (alat bantu, di luar struktur data inti)
`uts.tools.PdfExtractor` memakai Apache PDFBox 1.8.16. Untuk PDFBox 3.x cukup ubah method `bacaHalaman`: `Loader.loadPDF(f)` dan `org.apache.pdfbox.text.PDFTextStripper`.
```
mvn -q compile exec:java -Dexec.mainClass=uts.tools.PdfExtractor -Dexec.args="folder-pdf folder-keluaran"
```
Keluaran: `teks/<berkas>/halaman-NNN.txt` (jangan di-commit karena memuat nama terdakwa) dan `kandidat.csv` (kandidat field + status `TEKS` atau `SCAN_ISI_MANUAL`).
Batasan: 25 dari 50 PDF adalah hasil scan tanpa lapisan teks sehingga tidak bisa dibaca PDFBox; pada 25 PDF lainnya lapisan teksnya berderau. Setiap kandidat wajib diverifikasi manual terhadap PDF asli.

## Dokumen lain
- `docs/adr/` – keputusan desain (struktur data; strategi pencarian dan pengurutan)
- `docs/trace/` – trace manual dan laporan pemeriksaan data
- `docs/demo/` – transkrip demo aplikasi
- `CONTRIBUTIONS.md` – kontribusi tiap anggota (issue, PR, review, commit)
- `AI_USAGE.md` – penggunaan AI
