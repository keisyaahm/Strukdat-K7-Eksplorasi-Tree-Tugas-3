# Strukdat-K7-Eksplorasi-Tree-Tugas-3

# Implementasi Quadtree dan PR Quadtree untuk Navigasi Pencarian Slot Parkir Terdekat pada Sistem Smart Parking

---

# Daftar Isi

- [Kelompok 7](#kelompok-7)
- [Struktur Laporan](#struktur-laporan)
- [Problem Statement](#problem-statement)
- [Solusi yang Diusulkan](#solusi-yang-diusulkan)
- [Rumusan Masalah](#rumusan-masalah)
- [Penjelasan Struktur Tree dan Algoritma](#penjelasan-struktur-tree-dan-algoritma)
  - [PR Quadtree](#pr-quadtree)
  - [Quadtree](#quadtree)
- [Diagram / Visualisasi](#diagram--visualisasi)
  - [Spatial Subdivision Quadtree](#spatial-subdivision-quadtree)
  - [Struktur Pohon (Tree Structure)](#struktur-pohon-tree-structure)
  - [Nearest Neighbor Search + Pruning](#nearest-neighbor-search--pruning)
  - [Delete + Node Merge](#delete--node-merge)
- [Hasil Implementasi](#hasil-implementasi)
  - [Screenshot Output Quadtree](#screenshot-output-quadtree)
  - [Screenshot Output PR Quadtree](#screenshot-output-pr-quadtree)
- [Perbandingan Performa Real](#perbandingan-performa-real)
  - [Tabel Benchmark dari `pr quadtree/Main.java`](#tabel-benchmark-dari-pr-quadtreemainjava)
  - [Tabel Nodes Visited dari `pr quadtree/Main.java`](#tabel-nodes-visited-dari-pr-quadtreemainjava)
- [Analisis Benchmark](#analisis-benchmark)
- [Source Code](#source-code)
  - [Quadtree](#quadtree-1)
  - [PR Quadtree](#pr-quadtree-1)
- [Referensi Paper](#referensi-paper)

---
## Kelompok 7

| No | NRP | Nama |
|---|---|---|
| 1 | 5027251106 | Senna Bagus Harimurti |
| 2 | 5027251128 | Atik Putri Matulina |
| 3 | 5027251004 | Ni Putu Maqueenta Wijaya |
| 4 | 5027251044 | Arrumanta Ekna Luhkinasih |
| 5 | 5027251068 | Keisya Halimah Mulia |

---

# Struktur Laporan

1. Problem statement / permasalahan
2. Penjelasan struktur tree dan algoritma
3. Diagram / visualisasi
4. Aplikasi / implementasinya
5. Keunggulan
6. Kekurangan
7. Perbandingan antara tree dasar dan modifikasi secara teori
8. Analisis kompleksitas berdasarkan struktur tree
9. Potensi pengembangan ke depan
10. Hasil Implementasi
11. Perbandingan performa real

---

# Problem Statement

Setiap hari, kawasan perkuliahan Institut Teknologi Sepuluh Nopember (ITS) khususnya di sekitar Tower 1 dipadati ribuan kendaraan mahasiswa. Dua titik parkir yang paling sering menjadi rebutan adalah parkiran FASOR dan parkiran KPA dekat TW 1. Sayangnya, kondisi di lapangan sering menyulitkan mahasiswa yang ingin parkir, terutama saat pergantian jam kuliah ataupun rush hour.

Pertama, soal kapasitas. Parkiran FASOR dan KPA memiliki area yang luas, tetapi mahasiswa yang baru masuk hanya bisa melihat kondisi bagian depan. Ketika area depan sudah penuh, banyak mahasiswa langsung berasumsi bahwa seluruh parkiran sudah tidak bisa digunakan dan akhirnya memutar balik menuju parkiran lainnya yang jauh lebih jauh dari gedung TW 1. Padahal, kalau diteruskan masuk, bagian belakang FASOR masih sering memiliki banyak slot yang kosong hanya saja tidak terlihat dari depan.

Kedua, soal keakuratan dari kapasitas. Melihat parkiran lalu menganalisis dengan mata kosong tidak selalu efektif, masalahnya, motor yang diparkir tidak selalu rapi. Sering kali ada slot/tempat yang hitungan kapasitas masih bisa digunakan, tapi secara kondisi aslinya hanya menyisakan celah setengah motor dan tidak bisa lagi diisi. Artinya, sistem tidak bisa bergantung pada rata-rata kapasitas parkiran, melainkan harus mengandalkan titik koordinat spasial (X,Y)(X, Y) (X,Y) dari slot/tempat yang benar-benar bisa digunakan.

Ketiga, soal beban komputasi pencarian (saat mencari data). Untuk membangun sistem smart parking yang bisa menavigasi mahasiswa ke slot kosong terdekat secara real-time, sistem harus memproses ratusan hingga ribuan titik koordinat dalam hitungan detik. Jika pendekatannya adalah linear search yaitu mengecek slot satu per satu dari ujung FASOR sampai ke parkiran ujung KPA maupun Elektro kompleksitasnya adalah O(N)O(N) O(N). Ini tidak masalah jika skala kecil, tetapi ketika sistem diakses ratusan bahkan ribuan pengguna sekaligus di jam pergantian kelas maupun rush hour, waktu respons akan melambat secara drastis dan membebani server.

---

# Solusi yang Diusulkan

Untuk mengatasi masalah tersebut, kami menerapkan struktur data spasial Quadtree dan variasinya, PR (Point Region) Quadtree guna memetakan seluruh area parkir ITS ke dalam ruang dua dimensi. Berbeda dengan metode konvensional yang memeriksa slot/tempat parkir satu per satu, sistem ini membagi area parkir secara hierarkis menjadi empat wilayah (kuadran). Jika suatu wilayah seperti area depan FASOR sudah penuh dan tidak ada satupun titik slot/tempat kosong di sana, kuadran tersebut tidak menyimpan titik apapun, sehingga algoritma Nearest Neighbor Search akan langsung mengabaikan wilayah tersebut (pruning). Kemudian secara otomatis mengarahkan navigasi pencarian ke wilayah kuadran lain yang masih memiliki slot/tempat kosong, seperti area belakang FASOR, KPA, Manarul, CCWS maupun parkiran Teknik Elektro. Hasilnya hal ini berhasil memangkas kompleksitas waktu pencarian secara signifikan dari O(N)O(N) O(N) menjadi O(log⁡N)O(\log N) O(logN).

---

# Rumusan Masalah

1. Bagaimana merepresentasikan posisi slot/tempat parkir yang valid dan kosong di area FASOR, KPA, Manarul, CCWS maupun parkiran Teknik Elektro, dll secara spasial menggunakan struktur data Quadtree?

2. Bagaimana PR Quadtree dapat mengatasi ketidakakuratan pendeteksian slot dan mempercepat pencarian slot kosong terdekat dibandingkan Quadtree standar?

3. Seberapa besar perbedaan performa dari sisi waktu eksekusi query dan jumlah node yang diperiksa antara Quadtree dan PR Quadtree pada berbagai tingkat kepadatan area parkir?

---

# Penjelasan Struktur Tree dan Algoritma

## PR Quadtree

Point-Region Quadtree (PR Quadtree) merupakan struktur data spasial yang digunakan untuk menyimpan data berupa titik pada ruang dua dimensi. Struktur ini bekerja dengan cara membagi suatu area menjadi empat bagian yang sama besar secara berulang hingga jumlah titik dalam setiap bagian tidak melebihi batas kapasitas yang telah ditentukan.

Setiap node pada PR Quadtree merepresentasikan sebuah region atau wilayah tertentu. Ketika jumlah titik dalam region tersebut sudah melebihi kapasitas, maka region akan dibagi menjadi empat subregion yakni North-West (NW), North-East (NE), South-West (SW), dan South-East (SE). Setelah pembagian dilakukan, titik-titik yang ada akan ditempatkan kembali sesuai posisi koordinatnya masing-masing.

Perbedaannya dengan struktur data linear adalah PR Quadtree menyimpan data berdasarkan lokasi spesialnya. Node internal hanya berfungsi sebagai pembagi ruang sedangkan data titik disimpan pada leaf. Pendekatan ini membuat proses pencarian data berdasarkan posisi menjadi lebih terstruktur dan efisien.

Quadtree biasa sering digunakan untuk merepresentasikan pembagian area berdasarkan keseragaman nilai suatu region. Pada pendekatan ini, sebuah region akan dibagi apabila isi di dalamnya tidak homogen. Quadtree kerap diimplementasikan pada image compression atau computer graphics. Sedangkan, PR Quadtree dirancang khusus untuk data berbentuk titik. Pembagian region dilakukan bukan karena perbedaan karakteristik area, melainkan karena jumlah titik suatu region telah melewati batas kapasitas. Hal ini membuat PR Quadtree cocok digunakan untuk data koordinat seperti GPS dan lokasi kendaraan.

PR Quadtree lebih efisien untuk pengolahan data titik karena proses pencariannya yang tidak memerlukan pemeriksaan seluruh data secara satu per satu. Proses kerja dari PR Quadtree cukup hanya menelusuri region yang relevan berdasarkan koordinat yang dicari. PR Quadtree juga membagi region yang memiliki banyak titik namun area kosong tidak akan terus dipecah sehingga penggunaan memori menjadi lebih hemat.

---

## Quadtree

Quadtree adalah struktur data hierarkis yang membagi ruang 2D secara rekursif menjadi empat kuadran (NW, NE, SW, SE). Dalam konteks Smart Parking, setiap node menyimpan kumpulan ParkingSlot hingga batas kapasitas (CAPACITY). Ketika kapasitas terlampaui, node tersebut melakukan subdivisi menjadi 4 child node dan mendistribusikan ulang slot-slot yang ada ke child yang sesuai berdasarkan koordinat.

### Karakteristik utama:
- Setiap node bisa menyimpan N slot sekaligus (threshold CAPACITY)
- Subdivisi dipicu oleh jumlah data, bukan posisi data
- Cell hasil subdivisi berbentuk persegi panjang, ukuran bergantung boundary parent

---

# Diagram / Visualisasi

## Spatial Subdivision Quadtree

![Spatial Subdivision](img/Diagram%20Spatial%20Subdivision%20Quadtree.svg)

---

## Struktur Pohon (Tree Structure)

![Tree Structure](img/Diagram%20Struktur%20Pohon%20(Tree)%20untuk%20Subdivision%20di%20atas.svg)

---

## Nearest Neighbor Search + Pruning

![Nearest Neighbor](img/Diagram%20Nearest%20Neighbor%20Search%20+%20Pruning%20(Parkiran%20FASOR).svg)

---

## Delete + Node Merge

![Delete Merge](img/Diagram%20Delete%20+%20Node%20Merge%20(Eksklusif%20PR%20Quadtree).svg)

---

# Hasil Implementasi

## Screenshot Output Quadtree

![Quadtree Output](img/quadtree.png)

---

## Screenshot Output PR Quadtree

![PR Quadtree Output](img/prquadtree.png)

---

# Perbandingan Performa Real

## Tabel Benchmark dari `pr quadtree/Main.java`

### Judul:
Perbandingan Performa Real PR Quadtree dan Standard Quadtree

| N | Insert PR | Insert QT | Range Query PR | Range Query QT | Nearest PR | Nearest QT |
|---|---|---|---|---|---|---|
| 100 | 163.10 µs | 124.46 µs | 18.12 µs | 28.60 µs | 59.16 µs | 41.38 µs |
| 1000 | 912.18 µs | 553.58 µs | 195.30 µs | 67.58 µs | 93.16 µs | 102.92 µs |
| 10000 | 8977.64 µs | 5213.64 µs | 567.04 µs | 343.22 µs | 35.44 µs | 26.54 µs |

µs = microseconds, karena output program membagi waktu dengan 1000.

---

## Tabel Nodes Visited dari `pr quadtree/Main.java`

### Judul:
Perbandingan Jumlah Node yang Dikunjungi saat Nearest Neighbor Search

| N | PR Nodes Visited | QT Nodes Visited |
|---|---|---|
| 100 | 41 | 33 |
| 1000 | 65 | 57 |
| 10000 | 95 | 85 |

Ini menunjukkan efisiensi pruning: semakin sedikit node dikunjungi, semakin optimal nearest search.

---

# Analisis Benchmark

Berdasarkan hasil benchmark, Standard Quadtree pada beberapa pengujian menunjukkan waktu eksekusi yang lebih cepat dibandingkan PR Quadtree. Hal ini terjadi karena implementasi PR Quadtree memiliki struktur subdivisi yang lebih detail dan jumlah node yang lebih banyak sehingga overhead rekursi menjadi lebih besar.

Namun, PR Quadtree tetap memiliki keunggulan pada pengelolaan point data dan efisiensi struktur spasial. Hal ini terlihat pada kemampuan node merging serta representasi region yang lebih adaptif terhadap distribusi titik koordinat.

Pada proses delete operation, PR Quadtree berhasil mengurangi jumlah node secara signifikan melalui mekanisme merge node otomatis, sedangkan Standard Quadtree tetap mempertahankan subdivision lama meskipun node sudah kosong.

---

# Source Code

## Quadtree
- Insert
- Subdivide
- Range Query
- Nearest Neighbor
- Update Status
- Nodes Visited

## PR Quadtree
- Point Region Partitioning
- Node Merge
- Collision Split
- Adaptive Search
- Pruning Optimization

---

# Referensi Paper

1. A Brief Introduction to Quadtrees and Their Applications

2. A Dynamic Balanced Quadtree for Real-Time Streaming Data (2023)
