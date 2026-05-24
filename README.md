# Strukdat-K7-Eksplorasi-Tree-Tugas-3

# Implementasi Quadtree dan PR Quadtree untuk Navigasi Pencarian Slot Parkir Terdekat pada Sistem Smart Parking

# Kelompok 7

| No | NRP | Nama |
|---|---|---|
| 1 | 5027251106 | Senna Bagus Harimurti |
| 2 | 5027251128 | Atik Putri Matulina |
| 3 | 5027251004 | Ni Putu Maqueenta Wijaya |
| 4 | 5027251044 | Arrumanta Ekna Luhkinasih |
| 5 | 5027251068 | Keisya Halimah Mulia |

---

# Mata Kuliah

ET234203 Struktur Data dan Pemrograman Berorientasi Objek

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

## Solusi yang Diusulkan

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

# Penjelasan Method per Method

## Quadtree (Tree Dasar)

### 1. insert(ParkingSlot)

Tambahkan slot ke node sesuai koordinatnya. Cek apakah jumlah slot di node sudah melebihi CAPACITY. Jika ya, panggil subdivide() lalu redistribusi. Jika tidak, simpan slot di node ini.

### 2. subdivide()

Pecah 1 node menjadi 4 child (NW, NE, SW, SE) berdasarkan titik tengah boundary node saat ini. Setelah child dibuat, semua slot lama di node ini di-insert ulang ke child yang sesuai berdasarkan koordinat slot.

### 3. rangeQuery(Rectangle)

Cari semua slot dalam area bounding box Rectangle. Jika boundary node tidak overlap dengan Rectangle, skip seluruh subtree (pruning). Jika overlap, periksa tiap slot di node ini, lalu rekursif ke semua child.

### 4. nearestSlot(x, y)

Cari ParkingSlot kosong (isOccupied = false) yang paling dekat dari koordinat (x, y). Gunakan pruning by bounding box — jika jarak minimum dari bounding box node ke titik (x, y) sudah lebih besar dari jarak kandidat terbaik saat ini, skip subtree tersebut.

### 5. updateStatus(id, status)

Telusuri tree untuk menemukan slot dengan id tertentu, lalu ubah isOccupied-nya menjadi OCCUPIED atau AVAILABLE.

### 6. printTree()

Cetak struktur tree ke console secara rekursif dengan indentasi untuk menunjukkan level. Tampilkan boundary tiap node dan daftar slot yang tersimpan di dalamnya.

### 7. nodesVisited counter

Variabel counter yang di-increment setiap kali sebuah node dikunjungi saat rangeQuery atau nearestSlot. Di-reset di awal tiap query baru. Digunakan untuk benchmarking efisiensi.

---

# Algoritma

## 1. insert(Point point)

```text
INSERT(node, point):
    if point tidak berada dalam boundary node:
        return false

    if jumlah points di node < capacity:
        tambahkan point ke node.points
        return true

    if node belum subdivide:
        SUBDIVIDE(node)

    coba insert ke northeast → jika berhasil, return true
    coba insert ke northwest → jika berhasil, return true
    coba insert ke southeast → jika berhasil, return true
    coba insert ke southwest → jika berhasil, return true

    return false
````

## 2. subdivide()

```text
SUBDIVIDE(node):
    x ← node.boundary.x
    y ← node.boundary.y
    w ← node.boundary.w / 2
    h ← node.boundary.h / 2

    northeast ← Quadtree(boundary(x+w, y-h, w, h), capacity)
    northwest ← Quadtree(boundary(x-w, y-h, w, h), capacity)
    southeast ← Quadtree(boundary(x+w, y+h, w, h), capacity)
    southwest ← Quadtree(boundary(x-w, y+h, w, h), capacity)

    node.divided ← true
```

## 3. query(Rectangle range, List found)

```text
QUERY(node, range, found):
    if node.boundary tidak intersect range:
        return   // pruning

    for setiap point di node.points:
        if range.contains(point):
            found.add(point)

    if node.divided:
        QUERY(northeast, range, found)
        QUERY(northwest, range, found)
        QUERY(southeast, range, found)
        QUERY(southwest, range, found)
```

## 4. nearest(Point target, Point best, double bestDist)

```text
NEAREST(node, target, best, bestDist):
    for setiap point di node.points:
        d ← jarak(point, target)
        if d < bestDist:
            bestDist ← d
            best ← point

    if node.divided:
        best ← NEAREST(northeast, target, best, bestDist)
        if best != null → bestDist ← jarak(best, target)

        best ← NEAREST(northwest, target, best, bestDist)
        if best != null → bestDist ← jarak(best, target)

        best ← NEAREST(southeast, target, best, bestDist)
        if best != null → bestDist ← jarak(best, target)

        best ← NEAREST(southwest, target, best, bestDist)

    return best
```

---

# Diagram / Visualisasi

## Struktur Pembagian Quadtree

```text
                ROOT
        ┌────────┼────────┐
       NW       NE       SW       SE
```

## Struktur Spatial Partitioning

```text
+-----------------------+
|           |           |
|    NW     |    NE     |
|-----------+-----------|
|    SW     |    SE     |
|           |           |
+-----------------------+
```

---

# Aplikasi / Implementasi

Implementasi dilakukan menggunakan bahasa Java untuk melakukan:

* insert slot parkir,
* nearest available parking search,
* range query,
* delete node,
* update status,
* benchmark performa Quadtree dan PR Quadtree.

---

# Keunggulan

## Quadtree

* Spatial search lebih cepat dibanding linear search
* Mendukung partitioning area 2D
* Mendukung nearest neighbor search
* Efisien untuk query spasial

## PR Quadtree

* Lebih optimal untuk point data
* Mendukung pruning lebih efisien
* Struktur lebih hemat memori
* Mendukung node merging
* Cocok untuk GPS dan koordinat kendaraan

---

# Kekurangan

## Quadtree

* Tree dapat menjadi sangat dalam pada distribusi data tidak merata
* Tidak melakukan merge node otomatis setelah delete
* Internal node dapat menyimpan phantom subdivision

## PR Quadtree

* Lebih kompleks diimplementasikan
* Membutuhkan subdivisi lebih sering pada collision point
* Overhead rekursi lebih besar

---

# Perbandingan antara Tree Dasar dan Modifikasi secara Teori

| Aspek            | Quadtree               | PR Quadtree                 |
| ---------------- | ---------------------- | --------------------------- |
| Jenis Data       | Bucket / Region        | Point Data                  |
| Pembagian Region | Berdasarkan kapasitas  | Berdasarkan collision point |
| Internal Node    | Bisa menyimpan data    | Hanya pembagi region        |
| Leaf Node        | Menyimpan banyak titik | Menyimpan satu titik        |
| Delete           | Tidak merge otomatis   | Mendukung merge node        |
| Efisiensi Search | Baik                   | Lebih optimal               |
| Efisiensi Memori | Sedang                 | Lebih hemat                 |

---

# Analisis Kompleksitas Berdasarkan Struktur Tree

| Operasi          | Quadtree         | PR Quadtree      |
| ---------------- | ---------------- | ---------------- |
| Insert           | O(log N) average | O(log N) average |
| Search           | O(log N) average | O(log N) average |
| Worst Case       | O(N)             | O(N)             |
| Range Query      | O(log N + k)     | O(log N + k)     |
| Nearest Neighbor | O(log N) average | O(log N) average |

---

# Potensi Pengembangan ke Depan

* Integrasi GPS real-time
* Visualisasi GUI interaktif
* Integrasi IoT Smart Parking
* Dynamic streaming spatial data
* Implementasi Compressed Quadtree
* Integrasi database dan cloud service

---

# Hasil Implementasi

## Struktur Program

```text
src/
├── Main.java
├── ParkingSlot.java
├── Rectangle.java
├── QuadTree.java
├── PRQuadTree.java
└── BenchmarkRunner.java
```

## Implementasi yang Telah Dibuat

* Standard Quadtree
* PR Quadtree
* Range Query
* Nearest Neighbor Search
* Delete + Node Merge
* Benchmark Performance
* Nodes Visited Analysis

---

# Perbandingan Performa Real

Benchmark dilakukan berdasarkan:

* waktu insert data,
* waktu range query,
* nearest neighbor search,
* jumlah node yang dikunjungi,
* total node,
* efisiensi pruning.

## Benchmark Parameter

```text
Data Size:
- 100
- 1000
- 10000
```

## Pengujian

* Insert Performance
* Range Query Performance
* Nearest Neighbor Performance
* Delete + Merge Efficiency
* Nodes Visited Comparison

---

# Source Code

## Quadtree

* Insert
* Subdivide
* Range Query
* Nearest Neighbor
* Update Status
* Nodes Visited

## PR Quadtree

* Point Region Partitioning
* Node Merge
* Collision Split
* Adaptive Search
* Pruning Optimization

---

# Referensi Paper

1. A Brief Introduction to Quadtrees and Their Applications

2. A Dynamic Balanced Quadtree for Real-Time Streaming Data (2023)

```
```
