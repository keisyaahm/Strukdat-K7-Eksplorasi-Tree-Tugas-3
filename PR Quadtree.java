import java.util.*;

// Slot Parkir: Menyimpan data ID, koordinat (x, y), dan status ketersediaan
class ParkingSlot {
    public enum Status { AVAILABLE, OCCUPIED }

    public final String id;
    public final double x, y;
    public Status status;

    public ParkingSlot(String id, double x, double y, Status status) {
        this.id = id; this.x = x; this.y = y; this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Slot[%s @(%.1f,%.1f) %s]", id, x, y, status);
    }
}

// Bounding Box: Helper untuk menentukan batas wilayah node dan deteksi tabrakan koordinat
class Rectangle {
    public final double x, y, width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    // Cek apakah suatu titik berada di dalam wilayah rectangle
    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    // Cek apakah wilayah rectangle ini berpotongan dengan wilayah rectangle lain
    public boolean intersects(Rectangle o) {
        return !(o.x >= x + width || o.x + o.width <= x
              || o.y >= y + height || o.y + o.height <= y);
    }

    // Helper untuk membagi wilayah menjadi 4 kuadran (NW, NE, SW, SE)
    public Rectangle nw() { return new Rectangle(x,           y + height/2, width/2, height/2); }
    public Rectangle ne() { return new Rectangle(x + width/2, y + height/2, width/2, height/2); }
    public Rectangle sw() { return new Rectangle(x,           y,            width/2, height/2); }
    public Rectangle se() { return new Rectangle(x + width/2, y,            width/2, height/2); }

    @Override
    public String toString() {
        return String.format("Rect(%.1f,%.1f w=%.1f h=%.1f)", x, y, width, height);
    }
}

// STANDARD QUADTREE (Menggunakan Bucket/Kapasitas Group)
class QuadTree {
    private static final int CAPACITY  = 4; // Node akan split jika data > 4
    private static final int MAX_DEPTH = 20;

    static class QTNode {
        Rectangle boundary;
        List<ParkingSlot> slots = new ArrayList<>(); // Menampung list data selama masih LEAF
        QTNode nw, ne, sw, se;
        boolean divided = false;

        QTNode(Rectangle b) { this.boundary = b; }
        boolean isLeaf() { return !divided; }
    }

    private final QTNode root;
    public int nodesVisited = 0;

    public QuadTree(Rectangle boundary) { root = new QTNode(boundary); }

    public boolean insert(ParkingSlot s) { return insert(root, s, 0); }

    private boolean insert(QTNode node, ParkingSlot s, int depth) {
        if (!node.boundary.contains(s.x, s.y)) return false;
        
        // Jika leaf, masukkan data selama kapasitas masih cukup
        if (node.isLeaf()) {
            if (node.slots.size() < CAPACITY || depth >= MAX_DEPTH) {
                node.slots.add(s); return true;
            }
            subdivide(node); // Split jika penuh
        }
        return insertChildren(node, s, depth + 1);
    }

    private boolean insertChildren(QTNode node, ParkingSlot s, int depth) {
        return insert(node.nw, s, depth) || insert(node.ne, s, depth)
            || insert(node.sw, s, depth) || insert(node.se, s, depth);
    }

    // Membagi node menjadi 4 anak kuadran dan mendistribusikan ulang data lama
    private void subdivide(QTNode node) {
        Rectangle b = node.boundary;
        node.nw = new QTNode(b.nw()); node.ne = new QTNode(b.ne());
        node.sw = new QTNode(b.sw()); node.se = new QTNode(b.se());
        node.divided = true;
        for (ParkingSlot s : node.slots) insertChildren(node, s, 0);
        node.slots.clear(); // Bersihkan list data di internal node
    }

    public boolean delete(String id, double x, double y) {
        return delete(root, id, x, y);
    }

    // KELEMAHAN STANDARD QT: Data dihapus begitu saja tanpa menggabungkan kembali node yang kosong
    private boolean delete(QTNode node, String id, double x, double y) {
        if (node == null || !node.boundary.contains(x, y)) return false;
        if (node.isLeaf()) return node.slots.removeIf(s -> s.id.equals(id));
        return delete(node.nw, id, x, y) || delete(node.ne, id, x, y)
             || delete(node.sw, id, x, y) || delete(node.se, id, x, y);
    }

    public List<ParkingSlot> rangeQuery(Rectangle range) {
        nodesVisited = 0;
        List<ParkingSlot> res = new ArrayList<>();
        rangeQuery(root, range, res);
        return res;
    }

    private void rangeQuery(QTNode node, Rectangle range, List<ParkingSlot> res) {
        if (node == null || !node.boundary.intersects(range)) return;
        nodesVisited++;
        if (node.isLeaf()) {
            for (ParkingSlot s : node.slots)
                if (range.contains(s.x, s.y)) res.add(s);
            return;
        }
        rangeQuery(node.nw, range, res); rangeQuery(node.ne, range, res);
        rangeQuery(node.sw, range, res); rangeQuery(node.se, range, res);
    }

    public ParkingSlot nearestAvailable(double qx, double qy) {
        nodesVisited = 0;
        double[] bd = { Double.MAX_VALUE };
        ParkingSlot[] best = { null };
        nearestAvailable(root, qx, qy, bd, best);
        return best[0];
    }

    private void nearestAvailable(QTNode node, double qx, double qy, double[] bd, ParkingSlot[] best) {
        if (node == null) return;
        nodesVisited++;
        if (minDist(node.boundary, qx, qy) >= bd[0]) return; // Pruning jika jarak kotak lebih jauh
        if (node.isLeaf()) {
            for (ParkingSlot s : node.slots) {
                if (s.status == ParkingSlot.Status.AVAILABLE) {
                    double d = dist(s.x, s.y, qx, qy);
                    if (d < bd[0]) { bd[0] = d; best[0] = s; }
                }
            }
            return;
        }
        // Urutkan kuadran anak terdekat dari titik query untuk efisiensi pencarian
        for (QTNode c : orderChildren(node, qx, qy))
            nearestAvailable(c, qx, qy, bd, best);
    }

    public boolean updateStatus(String id, double x, double y, ParkingSlot.Status st) {
        return updateStatus(root, id, x, y, st);
    }

    private boolean updateStatus(QTNode node, String id, double x, double y, ParkingSlot.Status st) {
        if (node == null || !node.boundary.contains(x, y)) return false;
        if (node.isLeaf()) {
            for (ParkingSlot s : node.slots)
                if (s.id.equals(id)) { s.status = st; return true; }
            return false;
        }
        return updateStatus(node.nw, id, x, y, st) || updateStatus(node.ne, id, x, y, st)
             || updateStatus(node.sw, id, x, y, st) || updateStatus(node.se, id, x, y, st);
    }

    private double dist(double ax, double ay, double bx, double by) {
        double dx=ax-bx, dy=ay-by; return Math.sqrt(dx*dx+dy*dy);
    }
    private double minDist(Rectangle r, double px, double py) {
        double cx=Math.max(r.x,Math.min(px,r.x+r.width));
        double cy=Math.max(r.y,Math.min(py,r.y+r.height));
        return dist(px,py,cx,cy);
    }
    private QTNode[] orderChildren(QTNode node, double qx, double qy) {
        QTNode[] c = { node.nw, node.ne, node.sw, node.se };
        for (int i=1;i<4;i++){
            QTNode key=c[i];
            double kd=c[i]==null?Double.MAX_VALUE:minDist(c[i].boundary,qx,qy);
            int j=i-1;
            while(j>=0&&(c[j]==null||minDist(c[j].boundary,qx,qy)>kd)){c[j+1]=c[j];j--;}
            c[j+1]=key;
        }
        return c;
    }

    public int countNodes() { return countNodes(root); }
    private int countNodes(QTNode n) {
        if (n==null) return 0;
        return 1+countNodes(n.nw)+countNodes(n.ne)+countNodes(n.sw)+countNodes(n.se);
    }
}

// PR QUADTREE (Point-Region: Tipe Node Eksplisit & Menggunakan Merge)
class PRQuadTree {
    enum NodeType { EMPTY, LEAF, INTERNAL } // Representasi state node yang jelas

    static class PRNode {
        NodeType type;
        Rectangle boundary;
        ParkingSlot slot; // Hanya terisi jika type == LEAF (Tepat 1 Data)
        PRNode nw, ne, sw, se;

        PRNode(Rectangle b) { this.boundary = b; this.type = NodeType.EMPTY; }
        boolean isLeaf()     { return type == NodeType.LEAF;     }
        boolean isEmpty()    { return type == NodeType.EMPTY;    }
        boolean isInternal() { return type == NodeType.INTERNAL; }
    }

    private final PRNode root;
    private static final int MAX_DEPTH = 30;
    public int nodesVisited = 0;

    public PRQuadTree(Rectangle boundary) { root = new PRNode(boundary); }

    public boolean insert(ParkingSlot slot) { return insert(root, slot, 0); }

    private boolean insert(PRNode node, ParkingSlot slot, int depth) {
        if (!node.boundary.contains(slot.x, slot.y)) return false;

        switch (node.type) {
            case EMPTY:
                // Jika kosong langsung klaim sebagai LEAF
                node.slot = slot;
                node.type = NodeType.LEAF;
                return true;

            case LEAF:
                // Terjadi tabrakan koordinat (2 data di sel yang sama) -> Paksa split wilayah
                if (depth >= MAX_DEPTH) return false;
                ParkingSlot existing = node.slot;
                node.slot = null;
                node.type = NodeType.INTERNAL;
                subdivide(node);
                insert(node, existing, depth + 1); // Redistribusi data lama ke anak kuadran baru
                return insert(node, slot, depth + 1);     // Masukkan data baru

            case INTERNAL:
                return insertChildren(node, slot, depth + 1);

            default: return false;
        }
    }

    private boolean insertChildren(PRNode node, ParkingSlot slot, int depth) {
        return insert(node.nw, slot, depth) || insert(node.ne, slot, depth)
            || insert(node.sw, slot, depth) || insert(node.se, slot, depth);
    }

    private void subdivide(PRNode node) {
        Rectangle b = node.boundary;
        node.nw = new PRNode(b.nw()); node.ne = new PRNode(b.ne());
        node.sw = new PRNode(b.sw()); node.se = new PRNode(b.se());
    }

    public boolean delete(String id, double x, double y) {
        return delete(root, id, x, y);
    }

    private boolean delete(PRNode node, String id, double x, double y) {
        if (node == null || !node.boundary.contains(x, y)) return false;

        if (node.isLeaf()) {
            if (node.slot.id.equals(id)) {
                node.slot = null;
                node.type = NodeType.EMPTY; // Kosongkan tipe node setelah data hilang
                return true;
            }
            return false;
        }

        if (node.isInternal()) {
            boolean removed = delete(node.nw, id, x, y)
                           || delete(node.ne, id, x, y)
                           || delete(node.sw, id, x, y)
                           || delete(node.se, id, x, y);
            if (removed) tryMerge(node); // FITUR EKSKLUSIF: Evaluasi struktur pohon pasca-penghapusan
            return removed;
        }
        return false;
    }

    // Menggabungkan kembali cabang yang sudah kosong atau tinggal menyisakan 1 leaf
    private void tryMerge(PRNode node) {
        if (!node.isInternal()) return;
        PRNode[] ch = { node.nw, node.ne, node.sw, node.se };
        int emptyCount = 0;
        PRNode onlyLeaf = null;
        for (PRNode c : ch) {
            if (c.isEmpty()) emptyCount++;
            else if (c.isLeaf()) onlyLeaf = c;
        }
        
        if (emptyCount == 4) {
            // Jika 4 anak semuanya kosong -> Ubah parent ini menjadi node EMPTY
            node.type = NodeType.EMPTY;
            node.nw = node.ne = node.sw = node.se = null;
        } else if (emptyCount == 3 && onlyLeaf != null) {
            // Jika 3 kosong dan hanya 1 anak berisi data -> Naikkan kelas anak menjadi LEAF di level parent
            node.slot = onlyLeaf.slot;
            node.type = NodeType.LEAF;
            node.nw = node.ne = node.sw = node.se = null;
        }
    }

    public List<ParkingSlot> rangeQuery(Rectangle range) {
        nodesVisited = 0;
        List<ParkingSlot> res = new ArrayList<>();
        rangeQuery(root, range, res);
        return res;
    }

    private void rangeQuery(PRNode node, Rectangle range, List<ParkingSlot> res) {
        if (node == null || node.isEmpty() || !node.boundary.intersects(range)) return;
        nodesVisited++;
        if (node.isLeaf()) {
            if (range.contains(node.slot.x, node.slot.y)) res.add(node.slot);
            return;
        }
        rangeQuery(node.nw, range, res); rangeQuery(node.ne, range, res);
        rangeQuery(node.sw, range, res); rangeQuery(node.se, range, res);
    }

    public ParkingSlot nearestAvailable(double qx, double qy) {
        nodesVisited = 0;
        double[] bd = { Double.MAX_VALUE };
        ParkingSlot[] best = { null };
        nearestAvailable(root, qx, qy, bd, best);
        return best[0];
    }

    private void nearestAvailable(PRNode node, double qx, double qy, double[] bd, ParkingSlot[] best) {
        if (node == null || node.isEmpty()) return;
        nodesVisited++;
        if (minDist(node.boundary, qx, qy) >= bd[0]) return;
        if (node.isLeaf()) {
            if (node.slot.status == ParkingSlot.Status.AVAILABLE) {
                double d = dist(node.slot.x, node.slot.y, qx, qy);
                if (d < bd[0]) { bd[0] = d; best[0] = node.slot; }
            }
            return;
        }
        for (PRNode c : orderChildren(node, qx, qy))
            nearestAvailable(c, qx, qy, bd, best);
    }

    public boolean updateStatus(String id, double x, double y, ParkingSlot.Status st) {
        return updateStatus(root, id, x, y, st);
    }

    private boolean updateStatus(PRNode node, String id, double x, double y, ParkingSlot.Status st) {
        if (node == null || node.isEmpty() || !node.boundary.contains(x, y)) return false;
        if (node.isLeaf()) {
            if (node.slot.id.equals(id)) { node.slot.status = st; return true; }
            return false;
        }
        return updateStatus(node.nw, id, x, y, st) || updateStatus(node.ne, id, x, y, st)
             || updateStatus(node.sw, id, x, y, st) || updateStatus(node.se, id, x, y, st);
    }

    private double dist(double ax, double ay, double bx, double by) {
        double dx=ax-bx, dy=ay-by; return Math.sqrt(dx*dx+dy*dy);
    }
    private double minDist(Rectangle r, double px, double py) {
        double cx=Math.max(r.x,Math.min(px,r.x+r.width));
        double cy=Math.max(r.y,Math.min(py,r.y+r.height));
        return dist(px,py,cx,cy);
    }
    private PRNode[] orderChildren(PRNode node, double qx, double qy) {
        PRNode[] c = { node.nw, node.ne, node.sw, node.se };
        for (int i=1;i<4;i++){
            PRNode key=c[i];
            double kd=(c[i]==null||c[i].isEmpty())?Double.MAX_VALUE:minDist(c[i].boundary,qx,qy);
            int j=i-1;
            while(j>=0){
                double jd=(c[j]==null||c[j].isEmpty())?Double.MAX_VALUE:minDist(c[j].boundary,qx,qy);
                if(jd<=kd) break;
                c[j+1]=c[j]; j--;
            }
            c[j+1]=key;
        }
        return c;
    }

    public int countNodes() { return countNodes(root); }
    private int countNodes(PRNode n) {
        if (n==null||n.isEmpty()) return 0;
        if (n.isLeaf()) return 1;
        return 1+countNodes(n.nw)+countNodes(n.ne)+countNodes(n.sw)+countNodes(n.se);
    }
}

// Runner utama program untuk melakukan simulasi fungsi dan performa uji (Benchmark)
public class Main {
    static final int    WARMUP  = 3;
    static final int    TRIALS  = 5;
    static final int[]  SIZES   = { 100, 1000, 10000 };
    static final double WORLD   = 1000.0;
    static Random rng = new Random(42);

    public static void main(String[] args) {
        System.out.println("Parking Slot Management — QuadTree Benchmark\n");

        functionalDemo();

        System.out.println("\nPERFORMANCE BENCHMARK");
        printHeader();
        for (int n : SIZES) benchmark(n);
        System.out.println();

        nodesVisitedDemo();
        deleteAndMergeDemo();
    }

    // Menjalankan demonstrasi fitur dasar (Insert, Update, Range Query, Nearest Search, Delete)
    static void functionalDemo() {
        System.out.println("FUNCTIONAL DEMO  (N=20 slots)");

        Rectangle world = new Rectangle(0, 0, WORLD, WORLD);
        PRQuadTree pr = new PRQuadTree(world);
        QuadTree   qt = new QuadTree(world);

        List<ParkingSlot> slots = generateSlots(20);
        for (ParkingSlot s : slots) { pr.insert(s); qt.insert(s); }

        Rectangle range = new Rectangle(200, 200, 400, 400);
        System.out.printf("Range query (200,200)->(600,600): PR=%d slots, QT=%d slots%n",
            pr.rangeQuery(range).size(), qt.rangeQuery(range).size());

        ParkingSlot first = slots.get(0);
        pr.updateStatus(first.id, first.x, first.y, ParkingSlot.Status.OCCUPIED);
        qt.updateStatus(first.id, first.x, first.y, ParkingSlot.Status.OCCUPIED);
        System.out.println("Updated slot " + first.id + " -> OCCUPIED");

        ParkingSlot prN = pr.nearestAvailable(500, 500);
        ParkingSlot qtN = qt.nearestAvailable(500, 500);
        System.out.println("Nearest available from (500,500):");
        System.out.println("  PR QuadTree -> " + (prN != null ? prN : "none"));
        System.out.println("  QuadTree    -> " + (qtN != null ? qtN : "none"));

        boolean prDel = pr.delete(first.id, first.x, first.y);
        boolean qtDel = qt.delete(first.id, first.x, first.y);
        System.out.printf("Delete slot %s: PR=%b  QT=%b%n", first.id, prDel, qtDel);
        System.out.printf("Node count setelah delete: PR=%d  QT=%d%n",
            pr.countNodes(), qt.countNodes());
    }

    // Melakukan benchmark kecepatan waktu eksekusi struktur data pada skala data N yang berbeda
    static void benchmark(int n) {
        List<ParkingSlot> slots = generateSlots(n);
        Rectangle world = new Rectangle(0, 0, WORLD, WORLD);
        Rectangle range = new Rectangle(100, 100, WORLD/2, WORLD/2);

        // Tahap pemanasan (Warmup) mesin JVM agar optimasi JIT compiler berjalan stabil
        for (int w=0; w<WARMUP; w++) {
            PRQuadTree p=new PRQuadTree(world); QuadTree q=new QuadTree(world);
            for (ParkingSlot s:slots){p.insert(s);q.insert(s);}
            p.rangeQuery(range);q.rangeQuery(range);
            p.nearestAvailable(500,500);q.nearestAvailable(500,500);
        }

        long prIns=0, qtIns=0;
        for (int t=0; t<TRIALS; t++){
            PRQuadTree p=new PRQuadTree(world);
            long s=System.nanoTime(); for(ParkingSlot sl:slots)p.insert(sl); prIns+=System.nanoTime()-s;
            QuadTree q=new QuadTree(world);
            s=System.nanoTime(); for(ParkingSlot sl:slots)q.insert(sl); qtIns+=System.nanoTime()-s;
        }

        PRQuadTree prF=new PRQuadTree(world); QuadTree qtF=new QuadTree(world);
        for (ParkingSlot s:slots){prF.insert(s);qtF.insert(s);}

        long prR=0,qtR=0,prNN=0,qtNN=0;
        for (int t=0; t<TRIALS; t++){
            long s=System.nanoTime(); prF.rangeQuery(range);   prR +=System.nanoTime()-s;
            s=System.nanoTime();      qtF.rangeQuery(range);   qtR +=System.nanoTime()-s;
            s=System.nanoTime();      prF.nearestAvailable(500,500); prNN+=System.nanoTime()-s;
            s=System.nanoTime();      qtF.nearestAvailable(500,500); qtNN+=System.nanoTime()-s;
        }

        System.out.printf("| %-6d | PR %7.2f us | QT %7.2f us |  PR %6.2f us | QT %6.2f us |  PR %6.2f us | QT %6.2f us |%n",
            n,
            prIns/(double)(TRIALS*1000), qtIns/(double)(TRIALS*1000),
            prR  /(double)(TRIALS*1000), qtR  /(double)(TRIALS*1000),
            prNN /(double)(TRIALS*1000), qtNN /(double)(TRIALS*1000));
    }

    static void printHeader() {
        System.out.println("|   N    |    INSERT        |    RANGE QUERY    |   NEAREST AVAIL  |");
        System.out.println("|--------|------------------|-------------------|------------------|");
    }

    // Demo untuk melihat berapa banyak node pohon yang harus diperiksa saat pencarian spasial terdekat
    static void nodesVisitedDemo() {
        System.out.println("\nNODES VISITED saat nearestAvailable()");
        System.out.printf("  %-8s | %-20s | %-18s%n","N","PR QuadTree visited","QuadTree visited");
        System.out.println("  ---------|----------------------|--------------------");
        for (int n : SIZES) {
            Rectangle world = new Rectangle(0, 0, WORLD, WORLD);
            PRQuadTree pr = new PRQuadTree(world);
            QuadTree   qt = new QuadTree(world);
            List<ParkingSlot> slots = generateSlots(n);
            for (ParkingSlot s:slots){pr.insert(s);qt.insert(s);}
            pr.nearestAvailable(500,500);
            qt.nearestAvailable(500,500);
            System.out.printf("  %-8d | %-20d | %-18d%n", n, pr.nodesVisited, qt.nodesVisited);
        }
    }

    // Demo pembuktian efisiensi memori PR QuadTree yang sukses melakukan pemangkasan node kosong (tryMerge)
    static void deleteAndMergeDemo() {
        System.out.println("\nDELETE + NODE MERGING (eksklusif PR QuadTree)");

        Rectangle world = new Rectangle(0, 0, WORLD, WORLD);
        PRQuadTree pr = new PRQuadTree(world);
        QuadTree   qt = new QuadTree(world);
        List<ParkingSlot> slots = generateSlots(100);
        for (ParkingSlot s:slots){pr.insert(s);qt.insert(s);}

        int prB=pr.countNodes(), qtB=qt.countNodes();
        System.out.printf("Setelah insert 100 slot  -> PR nodes: %d   QT nodes: %d%n", prB, qtB);

        // Hapus massal data untuk memicu merger node otomatis pada PR QuadTree
        for (int i=0; i<80; i++){
            ParkingSlot s=slots.get(i);
            pr.delete(s.id,s.x,s.y); qt.delete(s.id,s.x,s.y);
        }

        int prA=pr.countNodes(), qtA=qt.countNodes();
        System.out.printf("Setelah delete 80 slot   -> PR nodes: %d   QT nodes: %d%n", prA, qtA);
        System.out.printf("Reduksi node             -> PR: -%d (%.0f%%)   QT: -%d (%.0f%%)%n",
            prB-prA, 100.0*(prB-prA)/prB, qtB-qtA, 100.0*(qtB-qtA)/qtB);
    }

    // Helper generator koordinat titik slot parkir secara random dengan status acak
    static List<ParkingSlot> generateSlots(int n) {
        List<ParkingSlot> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i=0; i<n; i++){
            double x,y; String key;
            do {
                x=rng.nextDouble()*(WORLD-1);
                y=rng.nextDouble()*(WORLD-1);
                key=String.format("%.4f,%.4f",x,y);
            } while(seen.contains(key));
            seen.add(key);
            ParkingSlot.Status st = (rng.nextDouble()<0.6)
                ? ParkingSlot.Status.AVAILABLE : ParkingSlot.Status.OCCUPIED;
            list.add(new ParkingSlot("P"+i, x, y, st));
        }
        return list;
    }
}