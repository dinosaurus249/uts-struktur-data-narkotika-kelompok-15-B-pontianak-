# ADR 0001 – Pemilihan struktur data

**Status:** diterima (2026-11-14) – disetujui oleh: _Ahmad Naufal Nasution_, _Ramadani Dino Yulianto Saputra_

**Konteks:** aplikasi menyimpan 50 objek `Putusan` yang diimpor berurutan dari CSV, lalu ditampilkan, dicari, difilter, diurutkan, ditambah dan dihapus di posisi mana pun. Struktur utama harus generik, dibuat dari nol, dan tidak boleh memakai koleksi `java.util`.

**Keputusan:** struktur utama = `SinglyLinkedList<T>` (head + tail); struktur sekunder = `ArrayStack<T>` untuk audit.

**Alasan:** operasi dominan aplikasi adalah impor berurutan (`addLast` O(1) berkat tail), hapus/sisip di posisi sembarang, dan penelusuran linear untuk cari/filter/agregasi. Linked list tidak memerlukan resize atau pergeseran elemen. Stack cocok untuk audit karena riwayat terbaru dibaca lebih dulu (LIFO).

**Konsekuensi:** `get(i)`, `remove(i)`, `insert(i)` = O(n); pencarian = O(n); tidak ada akses acak. Pengurutan dibuat sebagai tampilan baru (insertion sort O(n²), stabil) agar invariant arsip tidak rusak.

**Alternatif ditolak:** array dinamis (akses O(1) tetapi sisip/hapus menggeser elemen), queue (tidak cocok untuk arsip yang dicari dan dihapus di tengah).
