package com.csc205.project1;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An immutable 3D line segment defined by two endpoints (p0, p1).
 * <p>
 * ✅ Design goals:
 * - Works as a segment, with helpers to treat it as an infinite line when needed.
 * - Clear, Spring Getting-Started style documentation.
 * - Safe math: validation, parallel-line handling, robust closest-point routines.
 * - Logging with java.util.logging (INFO, WARNING, SEVERE).
 *
 * Geometry conventions:
 * - Segment parameterization: P(t) = p0 + t*(p1 - p0), with t in [0, 1] for the segment.
 * - Infinite-line parameterization uses the same P(t) but allows t ∈ (-∞, +∞).
 */
public final class Line3D {

    private static final Logger LOG = Logger.getLogger(Line3D.class.getName());
    private static final double EPS = 1e-12;

    private final Point3D p0;
    private final Point3D p1;

    // ---- Construction ---------------------------------------------------------------------------

    /**
     * Private constructor; use {@link #of(Point3D, Point3D)} to get validation + logging.
     */
    private Line3D(Point3D p0, Point3D p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    /**
     * Factory: create a line segment from two endpoints.
     * <p>
     * What it does:
     * - Validates non-null endpoints; warns if degenerate (same point).
     * - Logs creation (INFO) and any degenerate geometry (WARNING).
     */
    public static Line3D of(Point3D p0, Point3D p1) {
        if (p0 == null || p1 == null) {
            LOG.severe("Line3D.of called with null endpoint(s).");
            throw new NullPointerException("p0 and p1 must be non-null");
        }
        Line3D line = new Line3D(p0, p1);
        if (p0.equals(p1)) {
            LOG.warning("Creating a degenerate Line3D: p0 equals p1 (zero-length segment).");
        }
        LOG.log(Level.INFO, () -> String.format("Created Line3D(%s -> %s)", p0, p1));
        return line;
    }

    /**
     * Factory: build from a base point and a direction vector, with a specific segment length.
     * <p>
     * How it works:
     * - Normalizes the direction (WARNING if zero).
     * - Constructs the segment [p, p + dirNormalized * length].
     */
    public static Line3D fromPointAndDirection(Point3D p, Point3D direction, double length) {
        if (p == null || direction == null) {
            LOG.severe("fromPointAndDirection called with null argument(s).");
            throw new NullPointerException("p and direction must be non-null");
        }
        if (!(length >= 0.0)) { // also rejects NaN
            LOG.severe("fromPointAndDirection called with negative or NaN length.");
            throw new IllegalArgumentException("length must be >= 0");
        }
        Point3D dirNorm = direction.normalize();
        if (dirNorm == direction && direction.magnitude() == 0.0) {
            LOG.severe("fromPointAndDirection called with zero direction vector.");
            throw new IllegalArgumentException("direction must be non-zero");
        }
        Point3D p1 = p.translate(dirNorm.x() * length, dirNorm.y() * length, dirNorm.z() * length);
        Line3D line = of(p, p1);
        LOG.log(Level.INFO, () -> String.format(
                "fromPointAndDirection base=%s dir=%s length=%.6f -> %s", p, direction, length, line));
        return line;
    }

    // ---- Accessors ------------------------------------------------------------------------------

    /** Start point of the segment. */
    public Point3D p0() { return p0; }

    /** End point of the segment. */
    public Point3D p1() { return p1; }

    /**
     * Direction vector (p1 - p0), not normalized.
     * <p>
     * Why:
     * - Many formulas use the raw direction; separate from {@link #unitDirection()} which normalizes.
     * Logging:
     * - WARNING if degenerate (zero vector).
     */
    public Point3D direction() {
        Point3D d = sub(p1, p0);
        if (isZero(d)) {
            LOG.warning("direction() on a zero-length segment returns zero vector.");
        }
        return d;
    }

    /**
     * Unit direction vector from p0 to p1.
     * <p>
     * Validates:
     * - WARNING if degenerate (returns the original zero vector to avoid NaNs).
     */
    public Point3D unitDirection() {
        Point3D d = direction();
        if (isZero(d)) {
            LOG.warning("unitDirection() requested on zero-length segment; returning zero vector.");
            return d; // zero; normalization keeps it zero
        }
        Point3D out = d.normalize();
        LOG.log(Level.INFO, () -> String.format("unitDirection: %s -> %s", this, out));
        return out;
    }

    // ---- Core metrics ---------------------------------------------------------------------------

    /**
     * Segment length, i.e., |p1 - p0|.
     * <p>
     * How it works:
     * - Uses {@link Point3D#distanceTo(Point3D)}.
     * - Logs INFO with the computed length.
     */
    public double length() {
        double len = p0.distanceTo(p1);
        LOG.log(Level.INFO, () -> String.format("length(%s) = %.6f", this, len));
        return len;
    }

    /**
     * Returns the point at parameter t on the segment: P(t) = p0 + t*(p1 - p0).
     * <p>
     * Behavior:
     * - t ∈ [0,1] covers the segment; values outside extrapolate the infinite line.
     * Logging:
     * - WARNING when t ∉ [0,1].
     */
    public Point3D pointAt(double t) {
        if (t < 0.0 || t > 1.0) {
            LOG.warning(String.format("pointAt called with t=%.6f outside [0,1] (extrapolation).", t));
        }
        Point3D d = direction();
        Point3D out = p0.translate(d.x() * t, d.y() * t, d.z() * t);
        LOG.log(Level.INFO, () -> String.format("pointAt t=%.6f on %s -> %s", t, this, out));
        return out;
    }

    // ---- Distance & closest points --------------------------------------------------------------

    /**
     * Shortest distance from this segment to a point.
     * <p>
     * How it works:
     * - Projects the point onto the infinite line (p0, p1).
     * - Clamps the parameter to [0,1] to stay on the segment.
     * - Returns the Euclidean distance from the closest point to the query point.
     * Logging:
     * - INFO with the parameter and distance; SEVERE if point is null.
     */
    public double distanceTo(Point3D p) {
        if (p == null) {
            LOG.severe("distanceTo(Point3D) called with null point.");
            throw new NullPointerException("point is null");
        }
        Point3D v = direction();
        double vv = dot(v, v);
        if (vv < EPS) { // degenerate segment
            double d = p0.distanceTo(p);
            LOG.warning(String.format("distanceTo called on degenerate segment; using p0 distance=%.6f", d));
            return d;
        }
        double t = dot(sub(p, p0), v) / vv;
        double tClamped = clamp01(t);
        Point3D closest = pointAt(tClamped);
        double dist = closest.distanceTo(p);
        LOG.log(Level.INFO, () -> String.format(
                "distanceTo point=%s on %s -> t=%.6f (clamped=%.6f), dist=%.6f",
                p, this, t, tClamped, dist));
        return dist;
    }

    /**
     * Closest point on this segment to a query point.
     * <p>
     * Why:
     * - Useful for snapping, collision queries, and projections in DSA/graphics.
     */
    public Point3D closestPointTo(Point3D p) {
        if (p == null) {
            LOG.severe("closestPointTo called with null point.");
            throw new NullPointerException("point is null");
        }
        Point3D v = direction();
        double vv = dot(v, v);
        if (vv < EPS) {
            LOG.warning("closestPointTo on degenerate segment; returning p0.");
            return p0;
        }
        double t = clamp01(dot(sub(p, p0), v) / vv);
        Point3D out = pointAt(t);
        LOG.log(Level.INFO, () -> String.format("closestPointTo %s on %s -> t=%.6f, point=%s", p, this, t, out));
        return out;
    }

    /**
     * Shortest distance between the two **infinite lines** defined by this and other.
     * <p>
     * How it works:
     * - Let u = this.direction(), v = other.direction(), w0 = other.p0 - this.p0.
     * - If u × v ≈ 0 (parallel), distance = |(w0 × u)| / |u|.
     * - Else distance = |w0 · (u × v)| / |u × v|.
     * Logging:
     * - INFO for computed distance; SEVERE for null other; WARNING for degenerate directions.
     */
    public double shortestDistanceInfinite(Line3D other) {
        if (other == null) {
            LOG.severe("shortestDistanceInfinite called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        Point3D u = this.direction();
        Point3D v = other.direction();
        if (isZero(u) || isZero(v)) {
            LOG.warning("shortestDistanceInfinite with degenerate direction(s); falling back to point-to-line.");
        }
        Point3D w0 = sub(other.p0, this.p0);
        Point3D uxv = cross(u, v);
        double n = magnitude(uxv);

        double dist;
        if (n < EPS) { // parallel (or nearly)
            // distance from other.p0 to this infinite line
            Point3D w0xu = cross(w0, u);
            double un = magnitude(u);
            dist = (un < EPS) ? magnitude(w0) : magnitude(w0xu) / un;
        } else {
            dist = Math.abs(dot(w0, uxv)) / n;
        }
        LOG.log(Level.INFO, () -> String.format(
                "shortestDistanceInfinite(%s, %s) = %.6f", this, other, dist));
        return dist;
    }

    /**
     * Shortest distance between two segments (this ↔ other).
     * <p>
     * How it works:
     * - Solves for parameters s,t minimizing |(p0 + s*u) - (q0 + t*v)| with s,t ∈ [0,1].
     * - Clamps to [0,1] and evaluates distance at the best pair.
     * - Handles parallel and degenerate segments robustly.
     * Logging:
     * - INFO with (s,t) and distance; SEVERE for null other.
     */
    public double shortestDistanceSegment(Line3D other) {
        ClosestPoints cp = closestPointsOnSegments(other);
        double dist = cp.a.distanceTo(cp.b);
        LOG.log(Level.INFO, () -> String.format(
                "shortestDistanceSegment(%s, %s) -> dist=%.6f", this, other, dist));
        return dist;
    }

    /**
     * Closest points between two segments (returns both points on each segment).
     * <p>
     * Why:
     * - Many algorithms need the actual pair, not just the distance (e.g., collision response).
     * How it works:
     * - Computes (s,t) as in shortestDistanceSegment, including parallel handling.
     * - Returns P(s) on this and Q(t) on other.
     */
    public ClosestPoints closestPointsOnSegments(Line3D other) {
        if (other == null) {
            LOG.severe("closestPointsOnSegments called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        // Based on "Closest Point on Segment to Segment" (Christer Ericson, Real-Time Collision Detection)
        Point3D   p = this.p0;
        Point3D   q = other.p0;
        Point3D   d1 = sub(this.p1, this.p0); // segment directions
        Point3D   d2 = sub(other.p1, other.p0);
        Point3D   r = sub(p, q);

        double a = dot(d1, d1); // squared lengths
        double e = dot(d2, d2);
        double f = dot(d2, r);

        double s, t;

        if (a < EPS && e < EPS) {
            // both segments degenerate
            LOG.warning("Both segments are degenerate; returning (p0, q0).");
            return new ClosestPoints(this.p0, other.p0);
        }
        if (a < EPS) {
            // this degenerate -> project p onto other
            s = 0.0;
            t = clamp01(f / e);
        } else {
            double c = dot(d1, r);
            if (e < EPS) {
                // other degenerate -> project q onto this
                t = 0.0;
                s = clamp01(-c / a);
            } else {
                double b = dot(d1, d2);
                double denom = a * e - b * b;
                if (denom != 0.0) {
                    s = clamp01((b * f - c * e) / denom);
                } else {
                    // parallel -> pick s = 0 initially
                    s = 0.0;
                }
                // compute t for this s, then clamp, then recompute s if needed
                double tNom = (b * s + f);
                t = (tNom < 0.0) ? 0.0 : (tNom > e ? 1.0 : tNom / e);

                // Recompute s with clamped t
                double sNom = (b * t - c);
                s = (sNom < 0.0) ? 0.0 : (sNom > a ? 1.0 : sNom / a);
            }
        }

        Point3D cpThis  = this.pointAt(s);
        Point3D cpOther = other.pointAt(t);
        // Before the LOG.log(Level.INFO, ...) statement in closestPointsOnSegments:
        final double sFinal = s;
        final double tFinal = t;
        LOG.log(Level.INFO, () -> String.format(
            "closestPointsOnSegments s=%.6f, t=%.6f -> (%s, %s)", sFinal, tFinal, cpThis, cpOther));
        return new ClosestPoints(cpThis, cpOther);
    }

    /**
     * Shortest distance between the **infinite lines** if they intersect; otherwise, 0 only when lines meet.
     * <p>
     * Helper to check intersection of infinite lines by distance ≈ 0.
     */
    public boolean infiniteLinesIntersect(Line3D other, double epsilon) {
        if (!(epsilon >= 0.0)) {
            LOG.severe("infiniteLinesIntersect called with negative/NaN epsilon.");
            throw new IllegalArgumentException("epsilon must be >= 0");
        }
        double d = shortestDistanceInfinite(other);
        boolean ok = d <= epsilon;
        LOG.log(Level.INFO, () -> String.format(
                "infiniteLinesIntersect(%s, %s, eps=%.3g) -> %b (dist=%.6f)", this, other, epsilon, ok, d));
        return ok;
    }

    /**
     * Segment intersection test (treats both as segments).
     * <p>
     * What it does:
     * - Uses closestPointsOnSegments and checks if the distance ≈ 0.
     * - Returns true if the closest pair is essentially the same point.
     * Note:
     * - For skew segments in 3D, most pairs do not intersect; this robustly handles all cases.
     */
    public boolean segmentsIntersect(Line3D other, double epsilon) {
        ClosestPoints cp = closestPointsOnSegments(other);
        boolean ok = cp.a.epsilonEquals(cp.b, epsilon);
        LOG.log(Level.INFO, () -> String.format(
                "segmentsIntersect(%s, %s, eps=%.3g) -> %b", this, other, epsilon, ok));
        return ok;
    }

    // ---- Relations & utilities ------------------------------------------------------------------

    /**
     * Check if the directions are parallel (or nearly so).
     * <p>
     * How it works:
     * - Uses |u × v| ≈ 0 to test parallelism with tolerance.
     */
    public boolean isParallelTo(Line3D other, double epsilon) {
        if (other == null) {
            LOG.severe("isParallelTo called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        Point3D u = this.direction();
        Point3D v = other.direction();
        boolean parallel = magnitude(cross(u, v)) <= epsilon;
        LOG.log(Level.INFO, () -> String.format(
                "isParallelTo(%s, %s, eps=%.3g) -> %b", this, other, epsilon, parallel));
        return parallel;
    }

    /**
     * Check if two **infinite** lines are colinear (same infinite line).
     * <p>
     * How it works:
     * - Requires parallel directions, and the vector between any points lies in the same direction (w × u ≈ 0).
     */
    public boolean isColinearWith(Line3D other, double epsilon) {
        if (!isParallelTo(other, epsilon)) return false;
        Point3D u = this.direction();
        Point3D w = sub(other.p0, this.p0);
        boolean colinear = magnitude(cross(w, u)) <= epsilon;
        LOG.log(Level.INFO, () -> String.format(
                "isColinearWith(%s, %s, eps=%.3g) -> %b", this, other, epsilon, colinear));
        return colinear;
    }

    // ---- Equality, hashing, display -------------------------------------------------------------

    /** Value equality: both endpoints equal. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line3D)) return false;
        Line3D line3D = (Line3D) o;
        return Objects.equals(p0, line3D.p0) && Objects.equals(p1, line3D.p1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p0, p1);
    }

    @Override
    public String toString() {
        return String.format("Line3D[%s -> %s]", p0, p1);
    }

    // ---- Helper types ---------------------------------------------------------------------------

    /**
     * Simple holder for a pair of points (closest points on two segments).
     * <p>
     * Why:
     * - Keeps API clean without pulling in tuples or external libs.
     */
    public static final class ClosestPoints {
        public final Point3D a; // point on "this"
        public final Point3D b; // point on "other"
        public ClosestPoints(Point3D a, Point3D b) {
            this.a = Objects.requireNonNull(a, "a");
            this.b = Objects.requireNonNull(b, "b");
        }
        @Override public String toString() { return "ClosestPoints{" + a + " ; " + b + "}"; }
    }

    // ---- Private math helpers (keep dependencies minimal) ---------------------------------------

    private static Point3D sub(Point3D A, Point3D B) {
        return Point3D.of(A.x() - B.x(), A.y() - B.y(), A.z() - B.z());
    }

    private static Point3D cross(Point3D A, Point3D B) {
        return Point3D.of(
                A.y()*B.z() - A.z()*B.y(),
                A.z()*B.x() - A.x()*B.z(),
                A.x()*B.y() - A.y()*B.x()
        );
    }

    private static double dot(Point3D A, Point3D B) {
        return A.x()*B.x() + A.y()*B.y() + A.z()*B.z();
    }

    private static double magnitude(Point3D A) {
        return Math.sqrt(dot(A, A));
    }

    private static boolean isZero(Point3D v) {
        return Math.abs(v.x()) < EPS && Math.abs(v.y()) < EPS && Math.abs(v.z()) < EPS;
    }

    private static double clamp01(double t) {
        return (t < 0.0) ? 0.0 : (t > 1.0 ? 1.0 : t);
    }

    // ---- Pattern & Principle Notes --------------------------------------------------------------
    /*
     * Object-Oriented Design Patterns & Principles Demonstrated
     * ---------------------------------------------------------
     * 1) Value Object (DDD pattern):
     *    - Immutable endpoints; equality and hashCode are value-based. Thread-safe and referentially transparent.
     *
     * 2) Static Factory Method:
     *    - of(...) and fromPointAndDirection(...) centralize validation and logging.
     *    - Enables future pooling or alternative representations without changing call sites.
     *
     * 3) Fail-Fast Validation & Defensive Programming:
     *    - Null checks + degeneracy checks with clear SEVERE/WARNING logs prevent subtle numeric bugs.
     *
     * 4) Separation of Concerns (SRP):
     *    - Geometry only (no rendering). Logging is lightweight observability, not core logic.
     *
     * 5) Encapsulation & Immutability:
     *    - Private final fields and no mutators; operations yield new data or plain doubles.
     *
     * 6) Algorithmic Building Blocks (DSA Foundations):
     *    - Closest-point formulas (segment↔segment, point↔segment) are O(1) primitives used in:
     *      collision detection, spatial partitioning, k-d/ BVH queries, ICP alignment, robotics.
     *    - Robust handling of parallel and degenerate cases improves numerical stability of larger algorithms.
     *    - Parameterization (t in [0,1]) exposes a clean interface for interpolation and subdivision.
     *
     * 7) Composition Over Inheritance:
     *    - Reuses Point3D operations via composition; avoids fragile vector inheritance trees.
     */
}