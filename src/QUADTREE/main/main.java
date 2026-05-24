import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Point {

    double x, y;
    String namaSlot;
    int lantai;

    public Point(double x, double y, String namaSlot, int lantai) {
        this.x = x;
        this.y = y;
        this.namaSlot = namaSlot;
        this.lantai = lantai;
    }

    public double distance(Point other) {

        double penaltiLantai = (this.lantai != other.lantai) ? 1000 : 0;

        return Math.sqrt(
                Math.pow(this.x - other.x, 2) +
                Math.pow(this.y - other.y, 2)
        ) + penaltiLantai;
    }

    @Override
    public String toString() {
        return namaSlot + " | Lantai " + lantai;
    }
}

class Rectangle {

    double x, y, w, h;

    public Rectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean contains(Point p) {

        return (p.x >= x - w &&
                p.x <= x + w &&
                p.y >= y - h &&
                p.y <= y + h);
    }

    public boolean intersects(Rectangle range) {

        return !(range.x - range.w > x + w ||
                range.x + range.w < x - w ||
                range.y - range.h > y + h ||
                range.y + range.h < y - h);
    }
}

class Quadtree {

    Rectangle boundary;
    int capacity;

    List<Point> points;

    boolean divided = false;

    Quadtree northeast;
    Quadtree northwest;
    Quadtree southeast;
    Quadtree southwest;

    public Quadtree(Rectangle boundary, int capacity) {

        this.boundary = boundary;
        this.capacity = capacity;
        this.points = new ArrayList<>();
    }

    public void subdivide() {

        double x = boundary.x;
        double y = boundary.y;
        double w = boundary.w / 2;
        double h = boundary.h / 2;

        Rectangle ne = new Rectangle(x + w, y - h, w, h);
        northeast = new Quadtree(ne, capacity);

        Rectangle nw = new Rectangle(x - w, y - h, w, h);
        northwest = new Quadtree(nw, capacity);

        Rectangle se = new Rectangle(x + w, y + h, w, h);
        southeast = new Quadtree(se, capacity);

        Rectangle sw = new Rectangle(x - w, y + h, w, h);
        southwest = new Quadtree(sw, capacity);

        divided = true;
    }

    public boolean insert(Point point) {

        if (!boundary.contains(point)) {
            return false;
        }

        if (points.size() < capacity) {
            points.add(point);
            return true;
        }

        if (!divided) {
            subdivide();
        }

        if (northeast.insert(point)) return true;
        if (northwest.insert(point)) return true;
        if (southeast.insert(point)) return true;
        if (southwest.insert(point)) return true;

        return false;
    }

    public void query(Rectangle range, List<Point> found) {

        if (!boundary.intersects(range)) {
            return;
        }

        for (Point p : points) {

            if (range.contains(p)) {
                found.add(p);
            }
        }

        if (divided) {

            northeast.query(range, found);
            northwest.query(range, found);
            southeast.query(range, found);
            southwest.query(range, found);
        }
    }

    public Point nearest(Point target, Point best, double bestDist) {

        for (Point p : points) {

            double d = p.distance(target);

            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }

        if (divided) {

            best = northeast.nearest(target, best, bestDist);

            if (best != null)
                bestDist = best.distance(target);

            best = northwest.nearest(target, best, bestDist);

            if (best != null)
                bestDist = best.distance(target);

            best = southeast.nearest(target, best, bestDist);

            if (best != null)
                bestDist = best.distance(target);

            best = southwest.nearest(target, best, bestDist);
        }

        return best;
    }
}

public class main {

    public static void main(String[] args) {

        Random random = new Random();

        String[] lokasiMahasiswa = {
                "KPA-A1",
                "KPA-A2",
                "KPA-B1",
                "KPA-B2"
        };

        String posisiRandom =
                lokasiMahasiswa[random.nextInt(lokasiMahasiswa.length)];

        Rectangle areaITS = new Rectangle(500, 500, 500, 500);

        Quadtree quadtree = new Quadtree(areaITS, 4);

        quadtree.insert(new Point(100, 100, "KPA-A1", 1));
        quadtree.insert(new Point(120, 150, "KPA-A2", 1));
        quadtree.insert(new Point(400, 700, "ELEKTRO-C1", 1));

        quadtree.insert(new Point(700, 200, "KPA-B1", 2));
        quadtree.insert(new Point(650, 300, "KPA-B2", 2));
        quadtree.insert(new Point(450, 750, "ELEKTRO-C2", 2));
        quadtree.insert(new Point(800, 800, "CCWS-D1", 2));

        Point mahasiswa;

        if (posisiRandom.equals("KPA-A1")) {
            mahasiswa = new Point(100, 100, "Mahasiswa", 1);
        } else if (posisiRandom.equals("KPA-A2")) {
            mahasiswa = new Point(120, 150, "Mahasiswa", 1);
        } else if (posisiRandom.equals("KPA-B1")) {
            mahasiswa = new Point(700, 200, "Mahasiswa", 2);
        } else {
            mahasiswa = new Point(650, 300, "Mahasiswa", 2);
        }

        Point nearest = quadtree.nearest(
                mahasiswa,
                null,
                Double.MAX_VALUE
        );

        System.out.println("=== POSISI MAHASISWA ===");
        System.out.println("Mahasiswa berada di : " + posisiRandom);

        System.out.println("\n=== SLOT TERDEKAT ===");
        System.out.println(nearest);

        Rectangle areaKPA = new Rectangle(
                150,
                150,
                100,
                100
        );

        List<Point> hasil = new ArrayList<>();

        quadtree.query(areaKPA, hasil);

        System.out.println("\n=== SLOT AREA KPA ===");

        for (Point p : hasil) {
            System.out.println(p);
        }
    }
}