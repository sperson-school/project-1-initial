package com.csc205.project1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An immutable cube in 3D space.
 *
 * ✅ Design goals:
 * - Value-object semantics: center + edge length + orientation basis (ux, uy, uz).
 * - Orientation-aware: supports arbitrary rotations via a local orthonormal basis.
 * - Safe geometry: validation, orthonormalization (Gram–Schmidt), robust helpers.
 * - Logging via java.util.logging at INFO/WARNING/SEVERE levels.
 *
 * Representation:
 * - center: the cube's geometric center.
 * - edge: edge length (a > 0).
 * - ux, uy, uz: right-handed, unit-length, mutually orthogonal axes of the cube.
 *   By default they align with world axes (1,0,0), (0,1,0), (0,0,1).
 */
public final class Cube3D {

    private static final Logger LOG = Logger.getLogger(Cube3D.class.getName());
    private static final double EPS = 1e-12;

    private final Point3D center;
    private final double edge;
    private final Point3D ux; // unit X-axis of cube's local frame
    private final Point3D uy; // unit Y-axis of cube's local frame
    private final Point3D uz; // unit Z-axis of cube's local frame (ux × uy)

    // ---- Construction ---------------------------------------------------------------------------

    private Cube3D(Point3D center, double edge, Point3D ux, Point3D uy, Point3D uz) {
        this.center = center;
        this.edge = edge;
        this.ux = ux;
        this.uy = uy;
        this.uz = uz;
    }

    /**
     * Factory: axis-aligned unit-basis cube.
     * <p>
     * What it does:
     * - Validates inputs (non-null center, positive finite edge).
     * - Uses default orthonormal basis aligned to world axes.
     * Logging:
     * - SEVERE on invalid inputs, INFO on creation.
     */
    public static Cube3D of(Point3D center, double edge) {
        if (center == null) {
            LOG.severe("Cube3D.of called with null center.");
            throw new NullPointerException("center is null");
        }
        if (!(edge > 0.0) || Double.isInfinite(edge)) {
            LOG.severe("Cube3D.of called with non-positive or non-finite edge.");
            throw new IllegalArgumentException("edge must be > 0 and finite");
        }
        Cube3D c = new Cube3D(center, edge,
                Point3D.of(1, 0, 0),
                Point3D.of(0, 1, 0),
                Point3D.of(0, 0, 1));
        LOG.log(Level.INFO, () -> String.format("Created Cube3D(center=%s, edge=%.6f, axis=I)", center, edge));
        return c;
    }

    /**
     * Factory: oriented cube with a custom local basis (ux, uy, uz).
     * <p>
     * How it works:
     * - Validates basis vectors are finite and non-zero; applies Gram–Schmidt to produce an
     *   orthonormal right-handed frame (WARNING if corrections were needed).
     * - Ensures uz ≈ ux × uy to maintain right-handedness.
     */
    public static Cube3D ofOriented(Point3D center, double edge, Point3D ux, Point3D uy, Point3D uz) {
        if (center == null || ux == null || uy == null || uz == null) {
            LOG.severe("ofOriented called with null argument(s).");
            throw new NullPointerException("center/ux/uy/uz must be non-null");
        }
        if (!(edge > 0.0) || Double.isInfinite(edge)) {
            LOG.severe("ofOriented called with non-positive or non-finite edge.");
            throw new IllegalArgumentException("edge must be > 0 and finite");
        }

        // Orthonormalize (Gram–Schmidt); warn if we had to fix the input.
        boolean fixed = false;
        Point3D ex = safeNormalize(ux);
        if (isZero(ex)) {
            LOG.severe("ofOriented: ux is zero or non-finite.");
            throw new IllegalArgumentException("ux must be non-zero and finite");
        }

        // Remove ex component from uy, then normalize
        Point3D uyProj = subtract(uy, scale(ex, dot(uy, ex)));
        if (isZero(uyProj)) {
            LOG.warning("ofOriented: uy was colinear with ux; adjusted during Gram–Schmidt.");
            fixed = true;
            // choose any vector not parallel to ex
            uyProj = orthogonalFallback(ex);
        }
        Point3D ey = safeNormalize(uyProj);
        if (ey != uy) fixed = true;

        // ez = ex × ey
        Point3D ez = cross(ex, ey);
        if (isZero(ez)) {
            LOG.warning("ofOriented: computed uz was zero; repairing basis.");
            fixed = true;
            ez = orthogonalFallback(ex); // fallback (rare)
            ez = safeNormalize(cross(ex, ez)); // ensure perpendicular to ex
            ey = safeNormalize(cross(ez, ex)); // fix ey to keep right-handedness
        } else {
            ez = safeNormalize(ez);
        }

        // Compare supplied uz to computed ez; warn if far off.
        if (!approxParallel(uz, ez, 1e-6)) {
            LOG.warning("ofOriented: provided uz not orthogonal/right-handed with ux,uy; using computed uz.");
            fixed = true;
        }

        Cube3D c = new Cube3D(center, edge, ex, ey, ez);
        if (fixed) {
            LOG.warning("ofOriented: basis adjusted to be orthonormal and right-handed.");
        }
        final boolean fixedFinal = fixed;
        LOG.log(Level.INFO, () -> String.format("Created Cube3D(center=%s, edge=%.6f, axes fixed=%b)", center, edge, fixedFinal));
        return c;
    }

    // ---- Accessors ------------------------------------------------------------------------------

    /** Cube center. */
    public Point3D center() { return center; }

    /** Edge length (a). */
    public double edge() { return edge; }

    /** Local X axis (unit). */
    public Point3D axisX() { return ux; }

    /** Local Y axis (unit). */
    public Point3D axisY() { return uy; }

    /** Local Z axis (unit). */
    public Point3D axisZ() { return uz; }

    // ---- Core geometry --------------------------------------------------------------------------

    /**
     * Vertices of the cube in right-handed order.
     * <p>
     * How it works:
     * - Uses center ± h*ux ± h*uy ± h*uz, where h = edge/2.
     * - Vertex order is stable and suitable for face construction.
     * Logging:
     * - INFO with a compact summary (center, edge, count=8).
     */
    public List<Point3D> vertices() {
        double h = edge / 2.0;
        Point3D ex = scale(ux, h), ey = scale(uy, h), ez = scale(uz, h);

        // 8 combinations: (-,-,-), (+,-,-), (+,+,-), (-,+,-), (-,-,+), (+,-,+), (+,+,+), (-,+,+)
        List<Point3D> V = new ArrayList<>(8);
        V.add(add(add(add(center, negate(ex)), negate(ey)), negate(ez)));
        V.add(add(add(add(center,      ex   ), negate(ey)), negate(ez)));
        V.add(add(add(add(center,      ex   ),      ey   ), negate(ez)));
        V.add(add(add(add(center, negate(ex)),      ey   ), negate(ez)));
        V.add(add(add(add(center, negate(ex)), negate(ey)),      ez   ));
        V.add(add(add(add(center,      ex   ), negate(ey)),      ez   ));
        V.add(add(add(add(center,      ex   ),      ey   ),      ez   ));
        V.add(add(add(add(center, negate(ex)),      ey   ),      ez   ));

        LOG.log(Level.INFO, () -> String.format("vertices: center=%s, edge=%.6f -> 8 vertices", center, edge));
        return Collections.unmodifiableList(V);
    }

    /**
     * Edges of the cube as 12 Line3D segments.
     * <p>
     * Why:
     * - Useful for wireframe rendering, collision checks, and graph algorithms.
     * Implementation:
     * - Builds from the vertex list using standard cube topology.
     */
    public List<Line3D> edges() {
        List<Point3D> v = vertices();
        int[][] E = new int[][]{
                // bottom square (z-)
                {0,1},{1,2},{2,3},{3,0},
                // top square (z+)
                {4,5},{5,6},{6,7},{7,4},
                // verticals
                {0,4},{1,5},{2,6},{3,7}
        };
        List<Line3D> out = new ArrayList<>(12);
        for (int[] e : E) {
            out.add(Line3D.of(v.get(e[0]), v.get(e[1])));
        }
        LOG.log(Level.INFO, () -> "edges: produced 12 segments.");
        return Collections.unmodifiableList(out);
    }

    /**
     * Perimeter length (sum of all edge lengths).
     * <p>
     * For a cube: 12 * a where a = edge length.
     */
    public double perimeterLength() {
        double p = 12.0 * edge;
        LOG.log(Level.INFO, () -> String.format("perimeterLength=%.6f", p));
        return p;
    }

    /**
     * Surface area of the cube: 6 * a^2.
     * <p>
     * Why:
     * - Common in shading, physics, and visibility calculations.
     */
    public double surfaceArea() {
        double s = 6.0 * edge * edge;
        LOG.log(Level.INFO, () -> String.format("surfaceArea=%.6f", s));
        return s;
    }

    /**
     * Volume of the cube: a^3.
     * <p>
     * Why:
     * - Used in physics and spatial queries (e.g., occupancy, density).
     */
    public double volume() {
        double v = edge * edge * edge;
        LOG.log(Level.INFO, () -> String.format("volume=%.6f", v));
        return v;
    }

    /**
     * Length of a face diagonal: a * sqrt(2).
     */
    public double faceDiagonal() {
        double d = edge * Math.sqrt(2.0);
        LOG.log(Level.INFO, () -> String.format("faceDiagonal=%.6f", d));
        return d;
    }

    /**
     * Length of the space diagonal: a * sqrt(3).
     */
    public double spaceDiagonal() {
        double d = edge * Math.sqrt(3.0);
        LOG.log(Level.INFO, () -> String.format("spaceDiagonal=%.6f", d));
        return d;
    }

    // ---- Transforms ------------------------------------------------------------------------------

    /**
     * Translate the cube by (dx, dy, dz).
     * <p>
     * What it does:
     * - Shifts the center; orientation axes remain unchanged.
     * - Returns a NEW Cube3D; this instance stays unchanged (immutability).
     * Logging:
     * - INFO describing the translation vector and new center.
     */
    public Cube3D translate(double dx, double dy, double dz) {
        Point3D newCenter = center.translate(dx, dy, dz);
        Cube3D out = new Cube3D(newCenter, edge, ux, uy, uz);
        LOG.log(Level.INFO, () -> String.format("translate by (%.6f, %.6f, %.6f): %s -> %s", dx, dy, dz, center, newCenter));
        return out;
    }

    /**
     * Uniformly scale the cube about its center (edge' = edge * factor).
     * <p>
     * Validates:
     * - SEVERE if factor is non-finite or <= 0.
     * - WARNING if factor == 0 (degenerate); not allowed here.
     */
    public Cube3D scale(double factor) {
        if (!isFinite(factor) || factor <= 0.0) {
            LOG.severe("scale called with non-finite or non-positive factor.");
            throw new IllegalArgumentException("factor must be finite and > 0");
        }
        Cube3D out = new Cube3D(center, edge * factor, ux, uy, uz);
        LOG.log(Level.INFO, () -> String.format("scale by %.6f: edge %.6f -> %.6f", factor, edge, out.edge));
        return out;
    }

    /**
     * Rotate cube around an arbitrary axis that passes through the cube's CENTER.
     * <p>
     * How it works:
     * - Rotates the local axes (ux, uy, uz) using Rodrigues' formula via Point3D.rotateAroundAxis.
     * - Keeps the center fixed.
     * Use cases:
     * - Local/object-space rotation common in 3D engines and editors.
     */
    public Cube3D rotateAroundAxisThroughCenter(double uxAxis, double uyAxis, double uzAxis, double radians) {
        Point3D rux = this.ux.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();
        Point3D ruy = this.uy.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();
        Point3D ruz = this.uz.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();

        // Re-orthonormalize slightly to avoid drift
        Cube3D out = ofOriented(center, edge, rux, ruy, ruz);
        LOG.log(Level.INFO, () -> String.format(
                "rotateAroundAxisThroughCenter axis=(%.6f,%.6f,%.6f) θ=%.6f -> orientation updated", uxAxis, uyAxis, uzAxis, radians));
        return out;
    }

    /**
     * Rotate the entire cube around an arbitrary axis that passes through the ORIGIN (world-space).
     * <p>
     * What it does:
     * - Rotates center and axes together, as if the cube orbits/rotates in world-space.
     * - Useful for camera/world transforms.
     */
    public Cube3D rotateAroundAxisThroughOrigin(double uxAxis, double uyAxis, double uzAxis, double radians) {
        Point3D newCenter = center.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians);
        Point3D rux = this.ux.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();
        Point3D ruy = this.uy.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();
        Point3D ruz = this.uz.rotateAroundAxis(uxAxis, uyAxis, uzAxis, radians).normalize();

        Cube3D out = ofOriented(newCenter, edge, rux, ruy, ruz);
        LOG.log(Level.INFO, () -> String.format(
                "rotateAroundAxisThroughOrigin axis=(%.6f,%.6f,%.6f) θ=%.6f -> center %s -> %s",
                uxAxis, uyAxis, uzAxis, radians, center, newCenter));
        return out;
    }

    /**
     * Convenience rotations around world axes through the cube's center.
     * <p>
     * Right-handed rotations about the world X/Y/Z axes.
     */
    public Cube3D rotateX(double radians) { return rotateAroundAxisThroughCenter(1,0,0, radians); }
    public Cube3D rotateY(double radians) { return rotateAroundAxisThroughCenter(0,1,0, radians); }
    public Cube3D rotateZ(double radians) { return rotateAroundAxisThroughCenter(0,0,1, radians); }

    // ---- Queries & utilities --------------------------------------------------------------------

    /**
     * Test whether a point lies inside (or on) the cube.
     * <p>
     * How it works:
     * - Project vector (p - center) onto local axes (ux,uy,uz).
     * - Check |projection| <= edge/2 + epsilon for each axis.
     * Logging:
     * - INFO with the projections and result; SEVERE for null input.
     */
    public boolean contains(Point3D p, double epsilon) {
        if (p == null) {
            LOG.severe("contains called with null point.");
            throw new NullPointerException("point is null");
        }
        if (!(epsilon >= 0.0)) {
            LOG.severe("contains called with negative/NaN epsilon.");
            throw new IllegalArgumentException("epsilon must be >= 0");
        }
        double h = edge / 2.0 + epsilon;
        Point3D d = subtract(p, center);
        double px = dot(d, ux), py = dot(d, uy), pz = dot(d, uz);
        boolean inside = Math.abs(px) <= h && Math.abs(py) <= h && Math.abs(pz) <= h;
        LOG.log(Level.INFO, () -> String.format(
                "contains p=%s -> proj(%.6f, %.6f, %.6f), h=%.6f => %b", p, px, py, pz, h, inside));
        return inside;
    }

    /**
     * Axis-aligned bounding box (AABB) of the oriented cube.
     * <p>
     * What it returns:
     * - Two points {min, max} representing the world-aligned AABB that encloses the cube.
     * Implementation:
     * - Enumerates 8 vertices and takes component-wise min/max.
     */
    public List<Point3D> axisAlignedBoundingBox() {
        List<Point3D> v = vertices();
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (Point3D p : v) {
            minX = Math.min(minX, p.x()); minY = Math.min(minY, p.y()); minZ = Math.min(minZ, p.z());
            maxX = Math.max(maxX, p.x()); maxY = Math.max(maxY, p.y()); maxZ = Math.max(maxZ, p.z());
        }
        Point3D min = Point3D.of(minX, minY, minZ);
        Point3D max = Point3D.of(maxX, maxY, maxZ);
        LOG.log(Level.INFO, () -> String.format("axisAlignedBoundingBox -> min=%s, max=%s", min, max));
        return List.of(min, max);
    }

    /**
     * Returns the six face centers (useful for billboarding, decals, or light placement).
     * <p>
     * Faces are ordered ±X, ±Y, ±Z relative to the cube's local axes.
     */
    public List<Point3D> faceCenters() {
        double h = edge / 2.0;
        List<Point3D> faces = List.of(
                add(center, scale(ux,  h)),
                add(center, scale(ux, -h)),
                add(center, scale(uy,  h)),
                add(center, scale(uy, -h)),
                add(center, scale(uz,  h)),
                add(center, scale(uz, -h))
        );
        LOG.log(Level.INFO, () -> "faceCenters: produced 6 points.");
        return faces;
    }

    // ---- Equality, hashing, display -------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cube3D)) return false;
        Cube3D c = (Cube3D) o;
        return Double.doubleToLongBits(edge) == Double.doubleToLongBits(c.edge)
                && Objects.equals(center, c.center)
                && Objects.equals(ux, c.ux)
                && Objects.equals(uy, c.uy)
                && Objects.equals(uz, c.uz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center,
                Double.doubleToLongBits(edge),
                ux, uy, uz);
    }

    @Override
    public String toString() {
        return String.format("Cube3D(center=%s, edge=%.6f)", center, edge);
    }

    // ---- Private helpers ------------------------------------------------------------------------

    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private static boolean isZero(Point3D v) {
        return Math.abs(v.x()) < EPS && Math.abs(v.y()) < EPS && Math.abs(v.z()) < EPS;
    }

    private static Point3D add(Point3D a, Point3D b) {
        return Point3D.of(a.x()+b.x(), a.y()+b.y(), a.z()+b.z());
    }

    private static Point3D subtract(Point3D a, Point3D b) {
        return Point3D.of(a.x()-b.x(), a.y()-b.y(), a.z()-b.z());
    }

    private static Point3D scale(Point3D v, double s) {
        return Point3D.of(v.x()*s, v.y()*s, v.z()*s);
    }

    private static Point3D negate(Point3D v) {
        return Point3D.of(-v.x(), -v.y(), -v.z());
    }

    private static double dot(Point3D a, Point3D b) {
        return a.x()*b.x() + a.y()*b.y() + a.z()*b.z();
    }

    private static Point3D cross(Point3D a, Point3D b) {
        return Point3D.of(
                a.y()*b.z() - a.z()*b.y(),
                a.z()*b.x() - a.x()*b.z(),
                a.x()*b.y() - a.y()*b.x()
        );
    }

    private static Point3D safeNormalize(Point3D v) {
        Point3D out = v.normalize();
        return out;
    }

    private static boolean approxParallel(Point3D a, Point3D b, double eps) {
        Point3D axb = cross(a, b);
        double n = Math.sqrt(dot(axb, axb));
        return n <= eps;
    }

    private static Point3D orthogonalFallback(Point3D v) {
        // Pick a vector least aligned with v to start an orthogonal basis
        Point3D k = (Math.abs(v.x()) < 0.9) ? Point3D.of(1,0,0) : Point3D.of(0,1,0);
        Point3D u = cross(v, k);
        if (isZero(u)) u = cross(v, Point3D.of(0,0,1));
        return safeNormalize(u);
    }

    // ---- Pattern & Principle Notes --------------------------------------------------------------
    /*
     * Object-Oriented Design Patterns & Principles Demonstrated
     * ---------------------------------------------------------
     * 1) Value Object (DDD):
     *    - Cube3D is immutable and defined purely by its values (center, edge, basis).
     *    - Equality/hashCode are value-based; thread-safe and referentially transparent.
     *
     * 2) Static Factory Methods:
     *    - of(...) and ofOriented(...) centralize validation, logging, and basis preparation.
     *    - Decouples callers from constructor details and allows future caching/variants.
     *
     * 3) Composition over Inheritance:
     *    - Reuses Point3D and Line3D for math and edge generation instead of inheriting.
     *    - Favors small, focused types that compose into larger systems (graphics engines).
     *
     * 4) Fail-Fast Validation & Defensive Programming:
     *    - Input checks and Gram–Schmidt orthonormalization guard against numerical pitfalls.
     *    - Clear SEVERE/WARNING logs provide observability in complex pipelines.
     *
     * 5) Single Responsibility Principle (SRP):
     *    - Encapsulates cube geometry & transforms; avoids rendering or I/O concerns.
     *
     * 6) DSA Foundations:
     *    - O(1) primitives (vertices/edges, containment, AABB, transforms) are building blocks
     *      in collision detection (BVH/AABB trees), culling, ray casting, and spatial hashing.
     *    - Stable, orientation-aware representation supports algorithmic correctness and
     *      predictable performance in higher-level O(n log n) structures.
     *
     * 7) Immutability Enables Safe Pipelines:
     *    - Transform methods return NEW instances for easier reasoning and parallelism.
     */
}