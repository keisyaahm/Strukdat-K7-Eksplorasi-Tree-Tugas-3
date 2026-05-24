# Strukdat-K7-Eksplorasi-Tree-Tugas-3
---

# Implementasi Quadtree dan PR Quadtree untuk Navigasi Pencarian Slot Parkir Terdekat pada Sistem Smart Parking

## Kelompok 7

| No | NRP | Nama |
|---|---|---|
| 1 | 5027251004 | Ni Putu Maqueenta Wijaya |
| 2 | 5027251044 | Arrumanta Ekna Luhkinasih |
| 3 | 5027251068 | Keisya Halimah Mulia |
| 4 | 5027251044 | Arrumanta Ekna Luhkinasih |
| 5 | 5027251106 | Senna Bagus Harimurti |
| 6 | 5027251128 | Atik Putri Matulina |

---

# Deskripsi Proyek

Proyek ini merupakan implementasi struktur data **Quadtree** dan variasinya yaitu **PR (Point-Region) Quadtree** untuk melakukan navigasi pencarian slot parkir terdekat pada sistem Smart Parking berbasis spatial search.

Implementasi dilakukan menggunakan bahasa pemrograman **Java** dengan fokus pada:
- spatial partitioning,
- nearest neighbor search,
- range query,
- benchmarking performa,
- serta perbandingan efisiensi antara Quadtree standar dan PR Quadtree.

---

# Problem Statement

Setiap hari, kawasan perkuliahan Institut Teknologi Sepuluh Nopember (ITS) khususnya di sekitar Tower 1 dipadati ribuan kendaraan mahasiswa. Dua titik parkir yang paling sering menjadi rebutan adalah parkiran FASOR dan parkiran KPA dekat TW 1. Sayangnya, kondisi di lapangan sering menyulitkan mahasiswa yang ingin parkir, terutama saat pergantian jam kuliah ataupun rush hour.

Pertama, soal kapasitas. Parkiran FASOR dan KPA memiliki area yang luas, tetapi mahasiswa yang baru masuk hanya bisa melihat kondisi bagian depan. Ketika area depan sudah penuh, banyak mahasiswa langsung berasumsi bahwa seluruh parkiran sudah tidak bisa digunakan dan akhirnya memutar balik menuju parkiran lainnya yang jauh lebih jauh dari gedung TW 1. Padahal, kalau diteruskan masuk, bagian belakang FASOR masih sering memiliki banyak slot yang kosong hanya saja tidak terlihat dari depan.

Kedua, soal keakuratan dari kapasitas. Melihat parkiran lalu menganalisis dengan mata kosong tidak selalu efektif, masalahnya, motor yang diparkir tidak selalu rapi. Sering kali ada slot/tempat yang hitungan kapasitas masih bisa digunakan, tapi secara kondisi aslinya hanya menyisakan celah setengah motor dan tidak bisa lagi diisi. Artinya, sistem tidak bisa bergantung pada rata-rata kapasitas parkiran, melainkan harus mengandalkan titik koordinat spasial `(X,Y)` dari slot/tempat yang benar-benar bisa digunakan.

Ketiga, soal beban komputasi pencarian (saat mencari data). Untuk membangun sistem smart parking yang bisa menavigasi mahasiswa ke slot kosong terdekat secara real-time, sistem harus memproses ratusan hingga ribuan titik koordinat dalam hitungan detik. Jika pendekatannya adalah linear search yaitu mengecek slot satu per satu dari ujung FASOR sampai ke parkiran ujung KPA maupun Elektro kompleksitasnya adalah `O(N)`. Ini tidak masalah jika skala kecil, tetapi ketika sistem diakses ratusan bahkan ribuan pengguna sekaligus di jam pergantian kelas maupun rush hour, waktu respons akan melambat secara drastis dan membebani server.

## Solusi yang Diusulkan

Untuk mengatasi masalah tersebut, kami menerapkan struktur data spasial **Quadtree** dan variasinya, **PR (Point Region) Quadtree** guna memetakan seluruh area parkir ITS ke dalam ruang dua dimensi.

Berbeda dengan metode konvensional yang memeriksa slot/tempat parkir satu per satu, sistem ini membagi area parkir secara hierarkis menjadi empat wilayah (kuadran). Jika suatu wilayah seperti area depan FASOR sudah penuh dan tidak ada satupun titik slot/tempat kosong di sana, kuadran tersebut tidak menyimpan titik apapun, sehingga algoritma **Nearest Neighbor Search** akan langsung mengabaikan wilayah tersebut (*pruning*).

Kemudian secara otomatis mengarahkan navigasi pencarian ke wilayah kuadran lain yang masih memiliki slot/tempat kosong, seperti area belakang FASOR, KPA, Manarul, CCWS maupun parkiran Teknik Elektro.

Hasilnya hal ini berhasil memangkas kompleksitas waktu pencarian secara signifikan dari `O(N)` menjadi `O(log N)`.

---

# Rumusan Masalah

1. Bagaimana merepresentasikan posisi slot/tempat parkir yang valid dan kosong di area FASOR, KPA, Manarul, CCWS maupun parkiran Teknik Elektro secara spasial menggunakan struktur data Quadtree?

2. Bagaimana PR Quadtree dapat mengatasi ketidakakuratan pendeteksian slot dan mempercepat pencarian slot kosong terdekat dibandingkan Quadtree standar?

3. Seberapa besar perbedaan performa dari sisi waktu eksekusi query dan jumlah node yang diperiksa antara Quadtree dan PR Quadtree pada berbagai tingkat kepadatan area parkir?

---

# Struktur Data yang Digunakan

## 1. Quadtree

Quadtree adalah struktur data hierarkis yang membagi ruang 2D secara rekursif menjadi empat kuadran:
- North-West (NW)
- North-East (NE)
- South-West (SW)
- South-East (SE)

Dalam implementasi ini:
- setiap node memiliki batas area (*boundary*),
- node menyimpan beberapa `ParkingSlot`,
- subdivision dilakukan ketika kapasitas node melebihi threshold tertentu.

---

## 2. PR Quadtree (Point Region Quadtree)

PR Quadtree merupakan variasi Quadtree yang dirancang khusus untuk data berbentuk titik koordinat.

Karakteristik:
- subdivision dilakukan berdasarkan collision antar titik,
- leaf node hanya menyimpan satu titik,
- node internal hanya berfungsi sebagai pembagi region,
- lebih efisien untuk spatial search dan nearest neighbor query.

---

# Fitur Implementasi

## Quadtree
- `insert(ParkingSlot)`
- `subdivide()`
- `rangeQuery(Rectangle)`
- `nearestAvailable(x, y)`
- `updateStatus(id, status)`
- `delete()`
- `printTree()`

---

## PR Quadtree
- adaptive subdivision
- collision-based split
- node merging
- nearest neighbor search
- range query
- pruning optimization

---

# Benchmark yang Dilakukan

Perbandingan performa dilakukan berdasarkan:
- waktu insert data,
- waktu range query,
- waktu nearest neighbor search,
- jumlah node yang dikunjungi,
- jumlah total node,
- efisiensi pruning.

---

# Struktur Program

```text
src/
├── Main.java
├── ParkingSlot.java
├── Rectangle.java
├── QuadTree.java
├── PRQuadTree.java
└── BenchmarkRunner.java
````

---

# Teknologi yang Digunakan

* Java
* Object-Oriented Programming (OOP)
* Spatial Data Structure
* Recursive Tree Traversal

---

# Referensi Paper

## Quadtree

* *A Brief Introduction to Quadtrees and Their Applications*

## PR Quadtree

* *A Dynamic Balanced Quadtree for Real-Time Streaming Data* (2023)

---

# Kesimpulan Singkat

Quadtree dan PR Quadtree mampu meningkatkan efisiensi pencarian slot parkir dibandingkan metode linear search konvensional. Berdasarkan karakteristik data koordinat parkir yang bersifat spasial dan tidak merata, PR Quadtree memberikan performa yang lebih optimal karena subdivision dilakukan secara adaptif dan mendukung pruning yang lebih efisien saat proses pencarian nearest slot.

```
```
