import java.util.Scanner;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Main {

    // ======================================================================
    // BAGIAN 1: GRAPH (Adjacency List berbasis pointer)
    // ======================================================================
    static class EdgeNode {
        int tujuan;
        double jarak;
        EdgeNode next;

        EdgeNode(int tujuan, double jarak, EdgeNode next) {
            this.tujuan = tujuan;
            this.jarak = jarak;
            this.next = next;
        }
    }

    static class Graph {
        int jumlahNode;
        String[] namaLokasi;
        EdgeNode[] adjList;

        Graph(String[] namaLokasi) {
            this.jumlahNode = namaLokasi.length;
            this.namaLokasi = namaLokasi;
            this.adjList = new EdgeNode[jumlahNode];
        }

        void tambahEdge(int a, int b, double jarak) {
            adjList[a] = new EdgeNode(b, jarak, adjList[a]);
            adjList[b] = new EdgeNode(a, jarak, adjList[b]);
        }

        boolean[] bfs(int start) {
            boolean[] dikunjungi = new boolean[jumlahNode];
            int[] antrian = new int[jumlahNode];
            int head = 0, tail = 0;

            antrian[tail++] = start;
            dikunjungi[start] = true;

            while (head < tail) {
                int u = antrian[head++];
                for (EdgeNode e = adjList[u]; e != null; e = e.next) {
                    if (!dikunjungi[e.tujuan]) {
                        dikunjungi[e.tujuan] = true;
                        antrian[tail++] = e.tujuan;
                    }
                }
            }
            return dikunjungi;
        }

        double[] dijkstra(int start, int[] previous) {
            double[] dist = new double[jumlahNode];
            boolean[] selesai = new boolean[jumlahNode];

            for (int i = 0; i < jumlahNode; i++) {
                dist[i] = Double.MAX_VALUE;
                previous[i] = -1;
            }
            dist[start] = 0;

            for (int iter = 0; iter < jumlahNode; iter++) {
                int u = -1;
                for (int j = 0; j < jumlahNode; j++) {
                    if (!selesai[j] && (u == -1 || dist[j] < dist[u])) u = j;
                }
                if (u == -1 || dist[u] == Double.MAX_VALUE) break;
                selesai[u] = true;

                for (EdgeNode e = adjList[u]; e != null; e = e.next) {
                    if (dist[u] + e.jarak < dist[e.tujuan]) {
                        dist[e.tujuan] = dist[u] + e.jarak;
                        previous[e.tujuan] = u;
                    }
                }
            }
            return dist;
        }

        String rekonstruksiRute(int start, int tujuan, int[] previous) {
            if (tujuan == start) return namaLokasi[start];
            if (previous[tujuan] == -1) return "(tidak ada rute)";
            return rekonstruksiRute(start, previous[tujuan], previous) + " -> " + namaLokasi[tujuan];
        }
    }

    // ======================================================================
    // DATASET LOKASI (Area Kampus ITS & Sekitarnya)
    // ======================================================================
    static String[] DAFTAR_LOKASI = {
            "Keputih", "BME Utara", "Klampis", "Manyar", "Jalan Teknik Sipil",
            "Jalan Teknik Mesin", "Tower 2 ITS", "Tower 1 ITS", "Gedung Rektorat", "Masjid Manarul",
            "Asrama Mahasiswa", "Perumdos Depan", "Jalan FTK", "Lapangan Fasor", "Gedung Informatika",
            "Gedung FDKBD ITS", "Mulyosari", "PENS", "PPNS", "Sakinah",
            "Gedung Perpustakaan", "Gedung Sistem Informasi", "Perumdos Belakang", "Fak. FTIRS", "Parkiran Elektro"
    };

    static Graph buatGraphKota() {
        Graph g = new Graph(DAFTAR_LOKASI);
        // Angka jarak disimulasikan sedemikian rupa agar ada yang di atas 10 km
        g.tambahEdge(0, 1, 1.5); g.tambahEdge(0, 4, 1.2); g.tambahEdge(0, 3, 2.0);
        g.tambahEdge(1, 2, 1.8); g.tambahEdge(1, 21, 3.0); g.tambahEdge(2, 3, 1.3);
        g.tambahEdge(2, 13, 2.5); g.tambahEdge(3, 4, 1.0); g.tambahEdge(3, 12, 1.7);
        g.tambahEdge(3, 16, 5.5); g.tambahEdge(4, 5, 2.2); g.tambahEdge(4, 11, 1.9);
        g.tambahEdge(5, 6, 2.0); g.tambahEdge(5, 9, 1.5); g.tambahEdge(5, 10, 1.1);
        g.tambahEdge(6, 7, 1.4); g.tambahEdge(6, 10, 1.8); g.tambahEdge(7, 8, 1.0);
        g.tambahEdge(7, 24, 6.2); g.tambahEdge(8, 9, 1.6); g.tambahEdge(9, 10, 1.3);
        g.tambahEdge(10, 11, 1.0); g.tambahEdge(11, 12, 1.2); g.tambahEdge(12, 13, 2.1);
        g.tambahEdge(13, 14, 1.5); g.tambahEdge(13, 17, 2.8); g.tambahEdge(14, 15, 1.7);
        g.tambahEdge(14, 21, 2.3); g.tambahEdge(15, 16, 1.9); g.tambahEdge(15, 20, 2.6);
        g.tambahEdge(16, 17, 1.4); g.tambahEdge(16, 18, 2.0); g.tambahEdge(17, 18, 1.3);
        g.tambahEdge(18, 19, 2.5); g.tambahEdge(19, 20, 1.8); g.tambahEdge(20, 21, 3.0);
        g.tambahEdge(21, 22, 1.2); g.tambahEdge(22, 23, 1.5); g.tambahEdge(23, 24, 1.9);
        g.tambahEdge(24, 9, 7.0); 
        return g;
    }

    // ======================================================================
    // BAGIAN 2: TREE / MIN-HEAP & MANAJEMEN WAKTU
    // ======================================================================
    static DateTimeFormatter formatWaktu = DateTimeFormatter.ofPattern("HH:mm:ss");

    static class Order {
        String id;
        String namaPelanggan;
        String namaRestoran;
        double jarak;
        boolean isVIP;
        double prioritas;
        int idLokasiRestoran;
        int idLokasiPelanggan;
        String rute;

        // Manajemen Waktu
        LocalTime waktuPesan;
        LocalTime waktuMasukSistem;
        LocalTime waktuDikirim;
        LocalTime waktuSampai;
        int estimasiMin; 
        int estimasiMax; 

        Order(String id, String namaPelanggan, String namaRestoran, boolean isVIP, 
              int idLokasiRestoran, int idLokasiPelanggan) {
            this.id = id; this.namaPelanggan = namaPelanggan; this.namaRestoran = namaRestoran;
            this.isVIP = isVIP;
            this.idLokasiRestoran = idLokasiRestoran; this.idLokasiPelanggan = idLokasiPelanggan;
            this.jarak = 0; this.prioritas = 0; this.rute = "";
            this.waktuPesan = LocalTime.now(); 
            this.waktuMasukSistem = this.waktuPesan.plusSeconds(5); // delay 5 detik sistem
        }
    }

    static final double KECEPATAN_KURIR_KMJAM = 30.0;
    
    static double hitungPrioritas(Order order) {
        double BOBOT_ESTIMASI = 0.5, BOBOT_JARAK = 0.3, BOBOT_VIP = 0.2;
        return (order.estimasiMin * BOBOT_ESTIMASI) + (order.jarak * BOBOT_JARAK) + (order.isVIP ? -20 * BOBOT_VIP : 0);
    }

    static class MinHeap {
        Order[] heap;
        int ukuran;

        MinHeap(int kapasitas) { heap = new Order[kapasitas]; ukuran = 0; }
        int indukDari(int i) { return (i - 1) / 2; }
        int anakKiri(int i) { return 2 * i + 1; }
        int anakKanan(int i) { return 2 * i + 2; }

        void tukar(int i, int j) { Order temp = heap[i]; heap[i] = heap[j]; heap[j] = temp; }

        void insert(Order order) {
            if (ukuran == heap.length) { System.out.println("[SISTEM] Antrian penuh."); return; }
            order.prioritas = hitungPrioritas(order);
            heap[ukuran] = order; ukuran++;
            bubbleUp(ukuran - 1);
        }

        void bubbleUp(int i) {
            while (i > 0) {
                int induk = indukDari(i);
                if (heap[i].prioritas < heap[induk].prioritas) { tukar(i, induk); i = induk; } 
                else break;
            }
        }

        Order ambilPesananTeratas() {
            if (ukuran == 0) return null;
            Order pesanan = heap[0]; heap[0] = heap[ukuran - 1]; ukuran--; heap[ukuran] = null;
            bubbleDown(0); return pesanan;
        }

        void bubbleDown(int i) {
            while (true) {
                int terkecil = i, kiri = anakKiri(i), kanan = anakKanan(i);
                if (kiri < ukuran && heap[kiri].prioritas < heap[terkecil].prioritas) terkecil = kiri;
                if (kanan < ukuran && heap[kanan].prioritas < heap[terkecil].prioritas) terkecil = kanan;
                if (terkecil != i) { tukar(i, terkecil); i = terkecil; } 
                else break;
            }
        }

        void cariPesanan(String keyword) {
            String kw = keyword.toLowerCase(); int ditemukan = 0;
            for (int i = 0; i < ukuran; i++) {
                Order o = heap[i];
                if (o.id.toLowerCase().contains(kw) || o.namaPelanggan.toLowerCase().contains(kw)) {
                    if (ditemukan == 0) System.out.println("Hasil pencarian:");
                    System.out.println(" -> [" + o.id + "] " + o.namaPelanggan + " | Status: " + (o.isVIP ? "[MEMBER VIP]" : "[NON-VIP]"));
                    ditemukan++;
                }
            }
            if (ditemukan == 0) System.out.println("Pesanan tidak ditemukan.");
        }
        
        static void prosesPesanan(String nama, String restoran, int estimasiMasak, boolean vip, int idResto, int idPelanggan) {
    String id = "ORD-" + String.format("%03d", counter++);
    Order o = new Order(id, nama, restoran, vip, idResto, idPelanggan);

    int[] previous = new int[25];
    double[] dist = graphKota.dijkstra(idResto, previous);

    o.jarak = dist[idPelanggan];


    // =====================================================
    // ESTIMASI WAKTU BERDASARKAN JARAK
    // =====================================================
    if (o.jarak < 1.0) {
        o.estimasiMin = estimasiMasak + 2;
        o.estimasiMax = estimasiMasak + 5;
    }
    else if (o.jarak < 2.0) {
        o.estimasiMin = estimasiMasak + 7;
        o.estimasiMax = estimasiMasak + 9;
    }
    else if (o.jarak < 5.0) {
        o.estimasiMin = estimasiMasak + 6;
        o.estimasiMax = estimasiMasak + 10;
    }
    else { // jarak = 5 km
        o.estimasiMin = estimasiMasak + 17;
        o.estimasiMax = estimasiMasak + 20;
    }


    o.rute = graphKota.rekonstruksiRute(idResto, idPelanggan, previous);

    antrian.insert(o);

    String statusVip = vip ? "[MEMBER VIP]" : "[NON-VIP]";

    System.out.println("================================================");
    System.out.println("ORDER BARU MASUK: " + id + " " + statusVip);
    System.out.println("Pelanggan : " + nama);
    System.out.println("Lokasi    : " + restoran + " (" +
            DAFTAR_LOKASI[idResto] + ") -> Menuju -> " +
            DAFTAR_LOKASI[idPelanggan]);

    System.out.println("Jarak     : " +
            String.format("%.2f", o.jarak) + " km");

    System.out.println("Waktu     : Jam Pemesanan " +
            o.waktuPesan.format(formatWaktu) +
            " | Masuk Sistem " +
            o.waktuMasukSistem.format(formatWaktu));

    System.out.println("Estimasi  : " +
            o.estimasiMin + " - " +
            o.estimasiMax + " menit tiba di tujuan.");

    System.out.println("================================================");
}

        void tampilkanBisaDiupgrade() {
            int count = 0;
            System.out.println("--- DAFTAR PESANAN NON-VIP (BISA DI-UPGRADE) ---");
            for (int i = 0; i < ukuran; i++) {
                if (!heap[i].isVIP) {
                    System.out.println(" - [" + heap[i].id + "] a.n. " + heap[i].namaPelanggan);
                    count++;
                }
            }
            if (count == 0) System.out.println("  [Semua pesanan saat ini sudah VIP atau antrian kosong]");
            System.out.println("------------------------------------------------");
        }

        void updatePriority(String idPesanan, boolean jadikanVIP) {
            int posisi = -1;
            for (int i = 0; i < ukuran; i++) { 
                if (heap[i].id.equals(idPesanan)) { posisi = i; break; } 
            }
            
            if (posisi == -1) { 
                System.out.println(">> [GAGAL] Pesanan dengan ID '" + idPesanan + "' tidak ditemukan!"); 
                return; 
            }
            
            if (heap[posisi].isVIP) {
                System.out.println(">> [DITOLAK] Pesanan " + idPesanan + " a.n. " + heap[posisi].namaPelanggan + " SUDAH berstatus [MEMBER VIP]!");
                return;
            }

            heap[posisi].isVIP = jadikanVIP; 
            heap[posisi].prioritas = hitungPrioritas(heap[posisi]);
            System.out.println(">> [BERHASIL] Status " + idPesanan + " a.n. " + heap[posisi].namaPelanggan + " diupdate jadi [MEMBER VIP]!");
            bubbleUp(posisi); bubbleDown(posisi);
        }

        void cetakDetailPesanan(Order o, int nomor) {
            String asal = DAFTAR_LOKASI[o.idLokasiRestoran];
            String tujuan = DAFTAR_LOKASI[o.idLokasiPelanggan];
            String status = o.isVIP ? "[MEMBER VIP]" : "[NON-VIP]";

            System.out.println(nomor + ". " + status + " [" + o.id + "] a.n. " + o.namaPelanggan);
            System.out.println("   Lokasi Tujuan    : " + tujuan);
            System.out.println("   Rute Pengantaran : " + o.namaRestoran + " (" + asal + ") -> " + o.rute);
            System.out.println("   Jarak Tempuh     : " + String.format("%.2f", o.jarak) + " km");
            System.out.println("   Estimasi Waktu   : " + o.estimasiMin + " - " + o.estimasiMax + " menit");
            System.out.println("   Skala Prioritas  : " + String.format("%.2f", o.prioritas) + "\n");
        }

        Order[] salinTerurutByPrioritas() {
            Order[] terurut = new Order[ukuran];
            for (int i = 0; i < ukuran; i++) terurut[i] = heap[i];
            for (int i = 0; i < ukuran - 1; i++)
                for (int j = i + 1; j < ukuran; j++)
                    if (terurut[i].prioritas > terurut[j].prioritas) {
                        Order tmp = terurut[i]; terurut[i] = terurut[j]; terurut[j] = tmp;
                    }
            return terurut;
        }

        void tampilkanListVIP() {
            if (ukuran == 0) { System.out.println("[Antrian kosong - belum ada pesanan masuk]"); return; }
            Order[] terurut = salinTerurutByPrioritas();
            System.out.println("============= LIST PESANAN VIP =============");
            int count = 0;
            for (int i = 0; i < ukuran; i++) {
                if (terurut[i].isVIP) cetakDetailPesanan(terurut[i], ++count);
            }
            if (count == 0) System.out.println("  [Tidak ada pesanan VIP saat ini]\n");
            System.out.println("============================================");
        }

        void tampilkanListNonVIP() {
            if (ukuran == 0) { System.out.println("[Antrian kosong - belum ada pesanan masuk]"); return; }
            Order[] terurut = salinTerurutByPrioritas();
            System.out.println("========= LIST PESANAN NON-VIP (REGULER) =========");
            int count = 0;
            for (int i = 0; i < ukuran; i++) {
                if (!terurut[i].isVIP) cetakDetailPesanan(terurut[i], ++count);
            }
            if (count == 0) System.out.println("  [Tidak ada pesanan Non-VIP saat ini]\n");
            System.out.println("==================================================");
        }

        void tampilkanUrutanRuteTerdekat() {
            if (ukuran == 0) { System.out.println("[Antrian kosong]"); return; }
            Order[] terurut = new Order[ukuran];
            for (int i = 0; i < ukuran; i++) terurut[i] = heap[i];
            for (int i = 0; i < ukuran - 1; i++)
                for (int j = i + 1; j < ukuran; j++)
                    if (terurut[i].jarak > terurut[j].jarak) {
                        Order tmp = terurut[i]; terurut[i] = terurut[j]; terurut[j] = tmp;
                    }
            
            System.out.println("----- REKOMENDASI URUTAN PENGANTARAN (Terdekat) -----");
            for (int i = 0; i < ukuran; i++) {
                Order o = terurut[i];
                System.out.println((i + 1) + ". [" + o.id + "] " + o.namaPelanggan + " | Jarak: " + String.format("%.2f", o.jarak) + " km");
            }
        }
    }

    // ======================================================================
    // BAGIAN 3: PROGRAM UTAMA & LIST SEDANG DIANTAR
    // ======================================================================
    static Scanner sc = new Scanner(System.in);
    static MinHeap antrian = new MinHeap(100);
    static Graph graphKota = buatGraphKota();
    static Random rand = new Random();
    static int counter = 1;

    static Order[] pesananDiJalan = new Order[100];
    static int jumlahDiJalan = 0;

    static String[] namaPelangganList = {"puput", "jeno", "zara", "fatur", "radit", "lina", "queenta", "keisya", "tulip", "mawar"};
    static String[] restoranList = {"Gepuk Geprek", "McD Mulyos", "Ayam Pak Gembus", "Baso Minus Keputih", "Nasi Padang Sakinah", "Kantin Teknik Sipil", "Kantin Pusat ITS"};

    public static void main(String[] args) throws InterruptedException {
        System.out.println("====================================================");
        System.out.println("       GoFOOD-LIKE DELIVERY SIMULATOR");
        System.out.println("====================================================\n");

        boolean jalan = true;
        while (jalan) {
            tampilkanMenu();
            int pilihan = bacaInt("Pilih menu: ");
            System.out.println();
            switch (pilihan) {
                case 1: tambahPesananManual(); break;
                case 2: simulasiPesananOtomatis(); break;
                case 3: antrian.cariPesanan(bacaString("Masukkan ID/Nama: ")); break;
                case 4: 
                    antrian.tampilkanBisaDiupgrade();
                    if (antrian.ukuran > 0) antrian.updatePriority(bacaString("Masukkan ID pesanan yang ingin dijadikan VIP: "), true);
                    break;
                case 5: antrian.tampilkanListVIP(); break;
                case 6: antrian.tampilkanListNonVIP(); break;
                case 7: antrian.tampilkanUrutanRuteTerdekat(); break;
                case 8: kurirAmbilOrder(); break;
                case 9: tampilkanPesananDiJalan(); break;
                case 10: selesaikanPesanan(); break;
                case 0: jalan = false; System.out.println("Sistem dimatikan."); break;
                default: System.out.println("Pilihan tidak valid!");
            }
            System.out.println();
        }
    }

    static void tampilkanMenu() {
        System.out.println("================ MENU UTAMA ================");
        System.out.println("1. Input pesanan manual (Pilih Lokasi)");
        System.out.println("2. Simulasi pesanan masuk otomatis");
        System.out.println("3. Cari pesanan");
        System.out.println("4. Pesanan jadi VIP mendadak");
        System.out.println("5. Lihat List Pesanan VIP");
        System.out.println("6. Lihat List Pesanan Non-VIP (Reguler)");
        System.out.println("7. Lihat Rekomendasi Urutan (Jarak Terdekat)");
        System.out.println("8. Kurir ambil order dari antrian");
        System.out.println("9. Lihat Daftar Pesanan Sedang Diantar");
        System.out.println("10. Selesaikan Pesanan (Sampai tujuan)");
        System.out.println("0. Keluar");
        System.out.println("==============================================");
    }

    static void tampilkanDaftarLokasi() {
        System.out.println("\n----- DAFTAR LOKASI TERSEDIA -----");
        for (int i = 0; i < DAFTAR_LOKASI.length; i++) {
            System.out.printf("%-2d. %-22s", i, DAFTAR_LOKASI[i]);
            if ((i + 1) % 2 == 0) System.out.println();
        }
        System.out.println("\n----------------------------------");
    }

    static int bacaIndexLokasi(String prompt) {
        while (true) {
            int idx = bacaInt(prompt);
            if (idx >= 0 && idx < DAFTAR_LOKASI.length) return idx;
            System.out.println("Index tidak valid. Pilih angka 0 - " + (DAFTAR_LOKASI.length - 1));
        }
    }

    static void tambahPesananManual() {
        System.out.println(">> FORM INPUT PESANAN MANUAL");
        String nama = bacaString("Nama Pelanggan: ");
        String restoran = bacaString("Nama Restoran : ");
        int estimasiMasak = bacaInt("Estimasi Waktu Masak Resto (menit): ");
        boolean vip = bacaBoolean("Pelanggan VIP? (y/n)  : ");
        
        tampilkanDaftarLokasi();
        int idResto = bacaIndexLokasi("Pilih Nomor Lokasi RESTORAN   (0-24): ");
        int idPel = bacaIndexLokasi("Pilih Nomor Lokasi PENGANTARAN(0-24): ");
        
        prosesPesanan(nama, restoran, estimasiMasak, vip, idResto, idPel);
    }

    static void simulasiPesananOtomatis() {
        System.out.println(">> Memproses pesanan otomatis masuk...");
        String nama = namaPelangganList[rand.nextInt(namaPelangganList.length)];
        String resto = restoranList[rand.nextInt(restoranList.length)];
        int estimasiMasak = 5 + rand.nextInt(15);
        boolean vip = rand.nextInt(100) < 30; 
        
        int idResto = rand.nextInt(25);
        int idPelanggan = rand.nextInt(25);
        
        prosesPesanan(nama, resto, estimasiMasak, vip, idResto, idPelanggan);
    }

    static void prosesPesanan(String nama, String restoran, int estimasiMasak, boolean vip, int idResto, int idPelanggan) {
        String id = "ORD-" + String.format("%03d", counter++);
        Order o = new Order(id, nama, restoran, vip, idResto, idPelanggan);
        int[] previous = new int[25];
        double[] dist = graphKota.dijkstra(idResto, previous);
        
        o.jarak = dist[idPelanggan];

        // Validasi Jarak Maksimal (Cancel dari pihak resto jika > 10 km)
        if (o.jarak > 10.0) {
            System.out.println("================================================");
            System.out.println("[DIBATALKAN] Order " + id + " ditolak oleh pihak resto!");
            System.out.println("Alasan : Jarak pengantaran terlalu jauh (" + String.format("%.2f", o.jarak) + " km).");
            System.out.println("Info   : Batas maksimal pengantaran kurir adalah 10 km.");
            System.out.println("================================================");
            return;
        }

        // Hitung Rentang Estimasi Waktu (Masak + Jalan + Toleransi Macet)
        int waktuJalan = (int) Math.ceil((o.jarak / KECEPATAN_KURIR_KMJAM) * 60.0);
        o.estimasiMin = estimasiMasak + waktuJalan;
        o.estimasiMax = o.estimasiMin + 10; // Tambah toleransi 10 menit untuk macet

        o.rute = graphKota.rekonstruksiRute(idResto, idPelanggan, previous);
        antrian.insert(o);
        
        String statusVip = vip ? "[MEMBER VIP]" : "[NON-VIP]";

        System.out.println("================================================");
        System.out.println("ORDER BARU MASUK: " + id + " " + statusVip);
        System.out.println("Pelanggan : " + nama);
        System.out.println("Lokasi    : " + restoran + " (" + DAFTAR_LOKASI[idResto] + ") -> Menuju -> " + DAFTAR_LOKASI[idPelanggan]);
        System.out.println("Jarak     : " + String.format("%.2f", o.jarak) + " km (Minimal pengantaran terdekat ~500m)");
        System.out.println("Waktu     : Jam Pemesanan " + o.waktuPesan.format(formatWaktu) + " | Masuk Sistem " + o.waktuMasukSistem.format(formatWaktu));
        System.out.println("Estimasi  : " + o.estimasiMin + " - " + o.estimasiMax + " menit tiba di tujuan.");
        System.out.println("================================================");
    }

    static void kurirAmbilOrder() {
        Order o = antrian.ambilPesananTeratas();
        if (o == null) { System.out.println("[KURIR] Tidak ada order di antrian masuk."); return; }
        
        o.waktuDikirim = LocalTime.now(); // Catat waktu kurir jalan
        pesananDiJalan[jumlahDiJalan++] = o; 
        
        LocalTime estimasiTibaMin = o.waktuDikirim.plusMinutes(o.estimasiMin);
        LocalTime estimasiTibaMax = o.waktuDikirim.plusMinutes(o.estimasiMax);

        System.out.println(">> [NOTIFIKASI] Kurir sedang mengantarkan makanan ke lokasi Anda...");
        System.out.println("[KURIR AMBIL] Order " + o.id + " " + (o.isVIP ? "[MEMBER VIP]" : "[NON-VIP]"));
        System.out.println(">> Pelanggan      : " + o.namaPelanggan + " (Menuju: " + DAFTAR_LOKASI[o.idLokasiPelanggan] + ")");
        System.out.println(">> Jam Dikirim    : " + o.waktuDikirim.format(formatWaktu));
        System.out.println(">> Jam Estimasi   : " + estimasiTibaMin.format(formatWaktu) + " sampai " + estimasiTibaMax.format(formatWaktu));
    }

    static void tampilkanPesananDiJalan() {
        if (jumlahDiJalan == 0) { System.out.println("[Tidak ada makanan yang sedang diantar saat ini]"); return; }
        System.out.println("----- MAKANAN SEDANG DI PERJALANAN -----");
        for (int i = 0; i < jumlahDiJalan; i++) {
            Order o = pesananDiJalan[i];
            String statusVip = o.isVIP ? "[VIP]" : "[REG]";
            System.out.println((i + 1) + ". " + statusVip + " [" + o.id + "] " + o.namaPelanggan + " | Jam Dikirim: " + o.waktuDikirim.format(formatWaktu));
        }
        System.out.println("----------------------------------------");
    }

    static void selesaikanPesanan() {
        if (jumlahDiJalan == 0) { System.out.println("[Tidak ada pesanan di perjalanan untuk diselesaikan]"); return; }
        Order selesai = pesananDiJalan[0]; 
        
        for (int i = 0; i < jumlahDiJalan - 1; i++) {
            pesananDiJalan[i] = pesananDiJalan[i + 1];
        }
        pesananDiJalan[jumlahDiJalan - 1] = null;
        jumlahDiJalan--;

        // Simulasi acak durasi perjalanan untuk menentukan telat atau tidak
        // Acak antara (estimasiMin - 5) sampai (estimasiMax + 10)
        int rentangAcak = (selesai.estimasiMax + 10) - (selesai.estimasiMin - 5);
        int durasiSimulasi = rand.nextInt(rentangAcak) + (selesai.estimasiMin - 5);
        
        selesai.waktuSampai = selesai.waktuDikirim.plusMinutes(durasiSimulasi);

        System.out.println("================================================");
        System.out.println("[SELESAI] Pesanan " + selesai.id + " milik " + selesai.namaPelanggan + " tiba di " + DAFTAR_LOKASI[selesai.idLokasiPelanggan]);
        System.out.println("Timeline Pesanan:");
        System.out.println(" - Jam Pemesanan : " + selesai.waktuPesan.format(formatWaktu));
        System.out.println(" - Jam Dikirim   : " + selesai.waktuDikirim.format(formatWaktu));
        System.out.println(" - Jam Sampai    : " + selesai.waktuSampai.format(formatWaktu));
        System.out.println(" - Total Durasi  : " + durasiSimulasi + " menit (Estimasi awal: " + selesai.estimasiMin + " - " + selesai.estimasiMax + " menit)");
        
        System.out.println("\n>> Status Pengiriman:");
        if (durasiSimulasi > selesai.estimasiMax) {
            System.out.println("   [NOTIFIKASI] Mohon maaf, makanan TELAT tiba di tujuan karena kendala kemacetan di jalan.");
        } else {
            System.out.println("   [NOTIFIKASI] Mantap! Makanan telah sampai TEPAT WAKTU di lokasi Anda.");
        }
        System.out.println("================================================");
    }

    // --- Helper Input ---
    static int bacaInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); } 
            catch (Exception e) { System.out.println("Masukkan angka bulat."); }
        }
    }
    
    static String bacaString(String prompt) { 
        System.out.print(prompt); 
        return sc.nextLine().trim(); 
    }
    
    static boolean bacaBoolean(String prompt) {
        while (true) {
            System.out.print(prompt); 
            String in = sc.nextLine().trim().toLowerCase();
            if (in.equals("y") || in.equals("ya")) return true; 
            if (in.equals("n") || in.equals("tidak")) return false;
            System.out.println("Input 'y' atau 'n'.");
        }
    }
}