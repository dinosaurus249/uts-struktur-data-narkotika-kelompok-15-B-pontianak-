# ADR 0002 – Strategi pencarian dan pengurutan

**Status:** diterima (2026-11-14) – disetujui oleh: _Ahmad Naufal Nasution_, _Ramadani Dino Yulianto Saputra_

**Konteks:** pencarian harus melaporkan jumlah langkah, dan pengurutan berdasarkan vonis tidak boleh merusak invariant arsip.

**Pencarian:** linear search melalui `SinglyLinkedList.search(Predicate)`; mengembalikan `SearchResult` (nilai, posisi, jumlah langkah). Kasus terbaik 1 langkah, terburuk n langkah. Pada Stack, `searchPreserve` memindahkan elemen ke stack sementara lalu `restore` mengembalikannya sehingga urutan tetap.

**Pengurutan (vonis):** insertion sort ke list baru, stabil, O(n²) waktu, O(n) memori tambahan. Dipilih karena n = 50 kecil, implementasi sederhana, dan arsip asli tidak disentuh.

**Bukti terukur (data asli, lihat `docs/trace/TRACE_LINKED_LIST.md`):** cari P001 = 1 langkah, P050 = 50 langkah, caseId yang tidak ada = 50 langkah. Pencarian riwayat pada Stack (`searchPreserve`) = k langkah pop + k langkah restore, dan urutan stack sama sebelum dan sesudah pencarian.

**Alternatif:** merge sort pada linked list (O(n log n)) – lebih rumit dan tidak diperlukan untuk n = 50.
