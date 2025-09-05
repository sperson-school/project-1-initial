package com.csc205.project1;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small, immutable value object representing a point (and vector) in 3D space.
 * <p>
 * ✅ Design goals:
 * - Immutability for thread-safety and referential transparency
 * - Clear, Spring Getting-Started style documentation
 * - Useful vector math (distance, dot, cross, normalization, rotations)
 * - Logging with java.util.logging (INFO, WARNING, SEVERE)
 */
public final class Point3D {

    private static final Logger LOG = Logger.getLogger(Point3D.class.getName());

    private final double x;
    private final double y;
    private final double z;

    // ---- Construction ---------------------------------------------------------------------------

    /**
     * Create a new point. Prefer {@link #of(double, double, double)} for validation + logging.
     */
    private Point3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    /**
     * Factory method creating a point with validation and logging.
     * <p>
     * What it does:
     * - Validates inputs are finite (not NaN/Infinity) to avoid downstream math errors.
     * - Logs the creation at INFO for traceability.
     * - Returns an immutable value object.
     */
    public static Point3D of(double x, double y, double z) {
        if (!isFinite(x) || !isFinite(y) || !isFinite(z)) {
            LOG.severe("Attempted to create Point3D with non-finite coordinates.");
            throw new IllegalArgumentException("Coordinates must be finite numbers.");
        }
        Point3D p = new Point3D(x, y, z);
        LOG.log(Level.INFO, () -> String.format("Created Point3D(x=%.6f, y=%.6f, z=%.6f)", x, y, z));
        return p;
    }

    /**
     * Factory from a double[3] array.
     * <p>
     * What it does:
     * - Validates array length and finiteness.
     * - Provides ergonomic interop with APIs using arrays.
     */
    public static Point3D fromArray(double[] xyz) {
        if (xyz == null) {
            LOG.severe("fromArray called with null array.");
            throw new NullPointerException("xyz array is null");
        }
        if (xyz.length != 3) {
            LOG.severe("fromArray called with array length != 3.");
            throw new IllegalArgumentException("xyz must have length 3");
        }
        return of(xyz[0], xyz[1], xyz[2]);
    }

    // ---- Accessors ------------------------------------------------------------------------------

    /** X coordinate. Small and simple: no logging needed. */
    public double x() { return x; }

    /** Y coordinate. */
    public double y() { return y; }

    /** Z coordinate. */
    public double z() { return z; }

    /** Returns a new double[3] with (x, y, z). */
    public double[] toArray() {
        return new double[] { x, y, z };
    }

    // ---- Core metrics ---------------------------------------------------------------------------

    /**
     * Euclidean distance to the origin (0,0,0).
     * <p>
     * How it works:
     * - Computes sqrt(x^2 + y^2 + z^2), logging the result.
     * - Useful for norms, thresholds, and spatial queries.
     */
    public double distanceToOrigin() {
        double d = Math.sqrt(x*x + y*y + z*z);
        LOG.log(Level.INFO, () -> String.format("distanceToOrigin=%.6f for %s", d, this));
        return d;
    }

    /**
     * Euclidean distance to another point.
     * <p>
     * How it works:
     * - Computes sqrt((dx)^2 + (dy)^2 + (dz)^2).
     * - Logs INFO for traceability; SEVERE if other is null to prevent NPEs.
     */
    public double distanceTo(Point3D other) {
        if (other == null) {
            LOG.severe("distanceTo called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        double dx = other.x - x;
        double dy = other.y - y;
        double dz = other.z - z;
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
        LOG.log(Level.INFO, () -> String.format("distanceTo(%s)=%.6f from %s", other, d, this));
        return d;
    }

    /**
     * Magnitude (vector length) treating this point as a vector from origin.
     * <p>
     * Equivalent to {@link #distanceToOrigin()} but named for vector contexts.
     */
    public double magnitude() {
        return distanceToOrigin();
    }

    // ---- Transformations ------------------------------------------------------------------------

    /**
     * Translate (shift) this point by (dx, dy, dz).
     * <p>
     * Why:
     * - Fundamental for moving points in world/local coordinates.
     * Logging:
     * - INFO on the resulting point.
     */
    public Point3D translate(double dx, double dy, double dz) {
        Point3D out = of(x + dx, y + dy, z + dz);
        LOG.log(Level.INFO, () -> String.format("translate by (%.6f, %.6f, %.6f): %s -> %s", dx, dy, dz, this, out));
        return out;
    }

    /**
     * Uniform scale around the origin.
     * <p>
     * Validates:
     * - Warns if factor is 0 (collapses to origin) or non-finite.
     * Logging:
     * - WARNING for risky inputs; INFO for normal use.
     */
    public Point3D scale(double factor) {
        if (!isFinite(factor)) {
            LOG.severe("scale called with non-finite factor.");
            throw new IllegalArgumentException("Scale factor must be finite");
        }
        if (factor == 0.0) {
            LOG.warning("Scaling by 0 collapses point to origin.");
        }
        Point3D out = of(x * factor, y * factor, z * factor);
        LOG.log(Level.INFO, () -> String.format("scale by %.6f: %s -> %s", factor, this, out));
        return out;
    }

    /**
     * Rotate around the X-axis by angle (radians), right-handed, about the origin.
     * <p>
     * Formula:
     * y' = y*cosθ - z*sinθ
     * z' = y*sinθ + z*cosθ
     * Logging:
     * - INFO with angles and result for reproducibility.
     */
    public Point3D rotateX(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double ny = y * cos - z * sin;
        double nz = y * sin + z * cos;
        Point3D out = of(x, ny, nz);
        LOG.log(Level.INFO, () -> String.format("rotateX θ=%.6f rad: %s -> %s", radians, this, out));
        return out;
    }

    /**
     * Rotate around the Y-axis by angle (radians), right-handed, about the origin.
     * <p>
     * Formula:
     * x' =  x*cosθ + z*sinθ
     * z' = -x*sinθ + z*cosθ
     */
    public Point3D rotateY(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double nx =  x * cos + z * sin;
        double nz = -x * sin + z * cos;
        Point3D out = of(nx, y, nz);
        LOG.log(Level.INFO, () -> String.format("rotateY θ=%.6f rad: %s -> %s", radians, this, out));
        return out;
    }

    /**
     * Rotate around the Z-axis by angle (radians), right-handed, about the origin.
     * <p>
     * Formula:
     * x' = x*cosθ - y*sinθ
     * y' = x*sinθ + y*cosθ
     */
    public Point3D rotateZ(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;
        Point3D out = of(nx, ny, z);
        LOG.log(Level.INFO, () -> String.format("rotateZ θ=%.6f rad: %s -> %s", radians, this, out));
        return out;
    }

    /**
     * Rotate around an arbitrary axis (unit vector ux,uy,uz) by angle (radians) using Rodrigues' formula.
     * <p>
     * Why:
     * - Essential for 3D graphics and robotics; generalizes axis rotations.
     * Validates:
     * - SEVERE if axis is non-finite or near zero length.
     * Logging:
     * - INFO describing axis, angle, and result; WARNING if axis normalization was needed.
     */
    public Point3D rotateAroundAxis(double ux, double uy, double uz, double radians) {
        double len = Math.sqrt(ux*ux + uy*uy + uz*uz);
        if (!isFinite(ux) || !isFinite(uy) || !isFinite(uz) || len == 0.0) {
            LOG.severe("rotateAroundAxis called with invalid axis.");
            throw new IllegalArgumentException("Axis must be finite and non-zero length");
        }
        // normalize axis if needed
        if (Math.abs(len - 1.0) > 1e-12) {
            LOG.warning("Axis not unit length; normalizing for rotation accuracy.");
            ux /= len; uy /= len; uz /= len;
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dot = ux*x + uy*y + uz*z;

        double nx = x * cos + (uy*z - uz*y) * sin + ux * dot * (1 - cos);
        double ny = y * cos + (uz*x - ux*z) * sin + uy * dot * (1 - cos);
        double nz = z * cos + (ux*y - uy*x) * sin + uz * dot * (1 - cos);

        Point3D out = of(nx, ny, nz);
        final double uxFinal = ux;
        final double uyFinal = uy;
        final double uzFinal = uz;
        LOG.log(Level.INFO, () -> String.format(
                "rotateAroundAxis u=(%.6f,%.6f,%.6f), θ=%.6f rad: %s -> %s",
                uxFinal, uyFinal, uzFinal, radians, this, out));
        return out;
    }

    // ---- Vector operations (treating point as a vector from origin) -----------------------------

    /**
     * Dot product with another vector (point from origin).
     * <p>
     * Uses:
     * - Angle computations, projections, similarity.
     * Validates:
     * - SEVERE if other is null to guard against NPEs.
     */
    public double dot(Point3D other) {
        if (other == null) {
            LOG.severe("dot called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        double v = x*other.x + y*other.y + z*other.z;
        LOG.log(Level.INFO, () -> String.format("dot(%s)=%.6f for %s", other, v, this));
        return v;
    }

    /**
     * Cross product with another vector (point from origin).
     * <p>
     * Result:
     * - A vector perpendicular to both, with right-hand rule orientation.
     * Logging:
     * - INFO for the resulting vector; SEVERE for null input.
     */
    public Point3D cross(Point3D other) {
        if (other == null) {
            LOG.severe("cross called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        double nx = y*other.z - z*other.y;
        double ny = z*other.x - x*other.z;
        double nz = x*other.y - y*other.x;
        Point3D out = of(nx, ny, nz);
        LOG.log(Level.INFO, () -> String.format("cross(%s) -> %s for %s", other, out, this));
        return out;
    }

    /**
     * Normalize to unit length.
     * <p>
     * Validates:
     * - WARNING if magnitude is ~0 (cannot normalize); returns this unchanged.
     * - WARNING if magnitude is non-finite.
     */
    public Point3D normalize() {
        double m = magnitude();
        if (m == 0.0) {
            LOG.warning("normalize called on zero vector; returning original.");
            return this;
        }
        if (!isFinite(m)) {
            LOG.warning("normalize called with non-finite magnitude; returning original.");
            return this;
        }
        Point3D out = scale(1.0 / m);
        LOG.log(Level.INFO, () -> String.format("normalize: %s -> %s", this, out));
        return out;
    }

    /**
     * Linear interpolation to another point by factor t.
     * <p>
     * Behavior:
     * - t=0 returns this; t=1 returns other; values outside [0,1] extrapolate.
     * Logging:
     * - WARNING if t is outside [0,1] (extrapolation).
     */
    public Point3D lerp(Point3D other, double t) {
        if (other == null) {
            LOG.severe("lerp called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        if (t < 0.0 || t > 1.0) {
            LOG.warning(String.format("lerp with t=%.6f outside [0,1] (extrapolation).", t));
        }
        Point3D out = of(
                x + (other.x - x) * t,
                y + (other.y - y) * t,
                z + (other.z - z) * t
        );
        LOG.log(Level.INFO, () -> String.format("lerp to %s with t=%.6f: %s -> %s", other, t, this, out));
        return out;
    }

    /**
     * Midpoint between this and another point (i.e., lerp with t=0.5).
     * <p>
     * Why:
     * - Common in geometry; a stable default measure between two points.
     */
    public Point3D midpoint(Point3D other) {
        return lerp(other, 0.5);
    }

    // ---- Equality, hashing, and display ---------------------------------------------------------

    /**
     * Exact equality (bitwise equals for doubles). For tolerance-based, see {@link #epsilonEquals(Point3D, double)}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point3D)) return false;
        Point3D p = (Point3D) o;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(p.x)
            && Double.doubleToLongBits(y) == Double.doubleToLongBits(p.y)
            && Double.doubleToLongBits(z) == Double.doubleToLongBits(p.z);
    }

    /** Hash based on bitwise double values for stability across runs. */
    @Override
    public int hashCode() {
        return Objects.hash(
                Double.doubleToLongBits(x),
                Double.doubleToLongBits(y),
                Double.doubleToLongBits(z)
        );
    }

    /** Human-friendly representation, useful in logs and debugging. */
    @Override
    public String toString() {
        return String.format("Point3D(%.6f, %.6f, %.6f)", x, y, z);
    }

    /**
     * Approximate equality within an absolute tolerance epsilon.
     * <p>
     * Why:
     * - Floating-point computations accumulate error; exact equality is brittle.
     * Logging:
     * - INFO with the comparison result; SEVERE for invalid epsilon.
     */
    public boolean epsilonEquals(Point3D other, double epsilon) {
        if (other == null) {
            LOG.severe("epsilonEquals called with null 'other'.");
            throw new NullPointerException("other is null");
        }
        if (!(epsilon >= 0.0)) { // also filters NaN
            LOG.severe("epsilonEquals called with negative or NaN epsilon.");
            throw new IllegalArgumentException("epsilon must be >= 0");
        }
        boolean eq = Math.abs(x - other.x) <= epsilon
                  && Math.abs(y - other.y) <= epsilon
                  && Math.abs(z - other.z) <= epsilon;
        LOG.log(Level.INFO, () -> String.format("epsilonEquals(%s, eps=%.3g) -> %b", other, epsilon, eq));
        return eq;
    }

    // ---- Helpers --------------------------------------------------------------------------------

    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    // ---- Pattern & Principle Notes --------------------------------------------------------------
    /*
     * Object-Oriented Design Patterns & Principles Demonstrated
     * ---------------------------------------------------------
     * 1) Value Object (DDD pattern):
     *    - Instances are immutable and defined solely by their values (x,y,z).
     *    - Equality and hashCode are value-based; no identity or lifecycle.
     *
     * 2) Static Factory Method:
     *    - of(...) and fromArray(...) centralize validation and logging at creation.
     *    - Improves readability and allows caching or alternate implementations later.
     *
     * 3) Fluent/Functional Style via Immutability:
     *    - Transformations (translate/scale/rotate/lerp/normalize) return NEW instances.
     *    - Encourages method chaining and safer concurrent use.
     *
     * 4) Fail-Fast Validation:
     *    - Early checks (null, finite numbers, axis length) prevent subtle math bugs.
     *    - Uses SEVERE log + exceptions for invalid states.
     *
     * 5) Single Responsibility Principle (SRP):
     *    - Class focuses on geometric data + common vector/point operations.
     *    - No rendering or I/O beyond lightweight logging for observability.
     *
     * 6) Encapsulation:
     *    - Fields are private final; mutation only through constructor/factories.
     *    - Public API provides safe, well-documented operations.
     *
     * 7) Substitutability & Reuse (LSP / Composition over Inheritance):
     *    - Treats a point as a vector when useful (dot/cross), avoiding inheritance tangles.
     *
     * Foundations for Data Structures & Algorithms (DSA)
     * --------------------------------------------------
     * - Numerical Stability: epsilonEquals supports robust comparisons in geometry and graphics.
     * - Algorithmic Building Blocks: dot/cross/normalize/rotations are primitives in:
     *      * Spatial indexing, collision detection, physics, rendering, robotics kinematics.
     * - Complexity Awareness: All operations are O(1); combine into higher-level O(n) or O(log n)
     *   algorithms (e.g., nearest-neighbor search, convex hulls, ICP alignment).
     * - Immutability aids correctness proofs and reasoning about algorithmic state transitions.
     */
}