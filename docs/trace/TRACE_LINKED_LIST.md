# Trace manual – SinglyLinkedList (arsip) dan ArrayStack (audit)

Skenario 4.1 no. 2, 3, 4 dan 6 dijalankan pada **data asli** `data/putusan-50.csv` (50 record, P001–P050).
Setiap baris di bawah adalah keluaran nyata program (bukan perkiraan). Notasi `X.next=Y` berarti pointer `next` node X menunjuk node Y.

## 1. Kondisi awal (skenario 1)
```
size=50  head=P001  head.next=P002  tail=P050  tail.next=null
[24]P025.next=P026  [25]P026.next=P027  [26]P027.next=P028
```

## 2. Hapus elemen pertama, tengah, terakhir (skenario 3)

| Operasi | Pointer yang berubah | Kondisi sesudah |
|---|---|---|
| `remove(0)` → P001 | `head` dipindah ke `head.next` (P002); `P001.next = null` | size=49, head=P002, tail=P050 |
| `remove(24)` → P026 | traversal ke node posisi 23 (P025); `P025.next = P026.next` (P027); `P026.next = null` | size=48, `[23]P025.next=P027` |
| `remove(47)` → P050 | traversal ke posisi 46 (P049); `P049.next = null`; karena yang dihapus = tail, `tail = P049` | size=47, tail=P049, `tail.next=null` |

Keluaran program:
```
HAPUS PERTAMA (P001):       size=49 head=P002 head.next=P003 tail=P050 tail.next=null
  sebelum hapus tengah:     [22]P024.next=P025  [23]P025.next=P026  [24]P026.next=P027
HAPUS TENGAH pos 24 (P026): size=48 head=P002 head.next=P003 tail=P050 tail.next=null
  sesudah:                  [23]P025.next=P027  [24]P027.next=P028  [25]P028.next=P029
  sebelum hapus terakhir:   [46]P049.next=P050  [47]P050.next=null
HAPUS TERAKHIR (P050):      size=47 head=P002 head.next=P003 tail=P049 tail.next=null
  validateInvariant OK
```
Tidak ada pergeseran elemen seperti pada array: hanya satu pointer `next` (atau `head`/`tail`) yang diubah. Biaya terletak pada traversal ke node sebelumnya (O(n)), kecuali hapus pertama (O(1)).

## 3. Tambahkan kembali tiga record (skenario 4)

| Operasi | Pointer yang berubah | Kondisi sesudah |
|---|---|---|
| `insert(0, P001)` = `addFirst` | `P001.next = head` (P002); `head = P001` | size=48, head=P001 |
| `insert(25, P026)` | traversal ke posisi 24 (P025); `P026.next = P025.next` (P027); `P025.next = P026` | size=49, `[24]P025.next=P026` |
| `addLast(P050)` | `tail.next = P050` (P049→P050); `tail = P050` | size=50, tail=P050, `tail.next=null` |

Keluaran program:
```
SISIP KEMBALI P001 di 0:      size=48 head=P001 head.next=P002 tail=P049 tail.next=null
SISIP KEMBALI P026 di 25:     size=49 head=P001 head.next=P002 tail=P049 tail.next=null
  sesudah:                    [24]P025.next=P026  [25]P026.next=P027  [26]P027.next=P028
TAMBAH KEMBALI P050 di akhir: size=50 head=P001 head.next=P002 tail=P050 tail.next=null
  validateInvariant OK; urutan P001..P050 pulih: true
```
Catatan: posisi sisip P026 adalah 25 (bukan 24) karena P001 sudah dimasukkan kembali di depan, sehingga indeks bergeser satu.

## 4. Pencarian dan jumlah langkah (skenario 2)
```
cari caseId P001 -> posisi 0,  langkah 1   (batas awal, kasus terbaik)
cari caseId P050 -> posisi 49, langkah 50  (batas akhir)
cari caseId P999 -> tidak ditemukan, langkah 50 (kasus terburuk: seluruh node dikunjungi)
```

## 5. Pencarian non-destruktif pada Stack (skenario 6)
Isi stack audit setelah operasi di atas (puncak = nomor 1):
```
1. TAMBAH P050 di posisi 49
2. SISIP P026 di posisi 25
3. SISIP P001 di posisi 0
4. HAPUS P050 dari posisi 47
5. HAPUS P026 dari posisi 24
6. HAPUS P001 dari posisi 0
7. IMPOR data/putusan-50.csv valid=50 error=0
```
`searchPreserve("HAPUS")`: `pop` elemen 1, 2, 3, 4 lalu `push` masing-masing ke stack sementara; elemen 4 cocok → berhenti
(kedalaman 3, 4 langkah). `restore(temp)` lalu `pop` dari stack sementara dan `push` kembali ke stack audit, sehingga elemen 4, 3, 2, 1 kembali dalam urutan asal.
```
searchPreserve("HAPUS") kedalaman=3 langkah=4; urutan sama setelah restore: true
```
Biaya: O(k) untuk pencarian + O(k) untuk restore, k = kedalaman elemen yang ditemukan (n jika tidak ditemukan).

## Cara mengulang trace
Pengujian otomatis yang setara: `SinglyLinkedListTest.hapusPertamaTengahTerakhir`, `PutusanRepositoryTest.skenario3dan4_hapusLaluTambahKembali`,
`ArrayStackTest.searchPreserveTidakMengubahUrutan`. Lewat aplikasi: lihat `docs/demo/`.
