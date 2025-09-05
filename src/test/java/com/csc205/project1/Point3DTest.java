package com.csc205.project1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Point3D.
 * Uses a small epsilon for floating-point assertions.
 */
public class Point3DTest {

    private static final double EPS = 1e-9;

    private static void assertPointApprox(Point3D p, double x, double y, double z, double eps) {
        assertEquals(x, p.x(), eps, "x mismatch");
        assertEquals(y, p.y(), eps, "y mismatch");
        assertEquals(z, p.z(), eps, "z mismatch");
    }

    // ---- Construction & Factories ---------------------------------------------------------------

    @Test
    @DisplayName("of: creates finite point and logs INFO")
    void ofCreatesFinitePoint() {
        Point3D p = Point3D.of(1.25, -2.5, 3.75);
        assertPointApprox(p, 1.25, -2.5, 3.75, EPS);
    }

    @Test
    @DisplayName("of: rejects NaN and Infinity")
    void ofRejectsNonFinite() {
        assertThrows(IllegalArgumentException.class, () -> Point3D.of(Double.NaN, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Point3D.of(0, Double.POSITIVE_INFINITY, 0));
        assertThrows(IllegalArgumentException.class, () -> Point3D.of(0, 0, Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("fromArray: accepts length 3 and rejects bad inputs")
    void fromArrayValidation() {
        Point3D p = Point3D.fromArray(new double[]{3, 4, 5});
        assertPointApprox(p, 3, 4, 5, EPS);

        assertThrows(NullPointerException.class, () -> Point3D.fromArray(null));
        assertThrows(IllegalArgumentException.class, () -> Point3D.fromArray(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> Point3D.fromArray(new double[]{1, 2}));
        assertThrows(IllegalArgumentException.class, () -> Point3D.fromArray(new double[]{1, 2, Double.NaN}));
    }

    // ---- Accessors & Representation -------------------------------------------------------------

    @Test
    @DisplayName("toArray: round-trip shape and values")
    void toArrayRoundTrip() {
        Point3D p = Point3D.of(-1, 0.5, 2.25);
        double[] a = p.toArray();
        assertEquals(3, a.length);
        assertEquals(-1, a[0], EPS);
        assertEquals(0.5, a[1], EPS);
        assertEquals(2.25, a[2], EPS);
    }

    @Test
    @DisplayName("equals/hashCode: bitwise double equality semantics")
    void equalsAndHashCode() {
        Point3D a = Point3D.of(1, 2, 3);
        Point3D b = Point3D.of(1, 2, 3);
        Point3D c = Point3D.of(1, 2, 3.0000000001); // different at bit level

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    // ---- Metrics --------------------------------------------------------------------------------

    @Test
    @DisplayName("distanceToOrigin & magnitude: correct Euclidean norm")
    void distanceAndMagnitude() {
        Point3D p = Point3D.of(3, 4, 12); // sqrt(9+16+144)=13
        assertEquals(13.0, p.distanceToOrigin(), EPS);
        assertEquals(13.0, p.magnitude(), EPS);
    }

    @Test
    @DisplayName("distanceTo: symmetric and correct")
    void distanceToOther() {
        Point3D a = Point3D.of(1, 2, 3);
        Point3D b = Point3D.of(4, 6, 3);
        double d = a.distanceTo(b);
        assertEquals(b.distanceTo(a), d, EPS);
        assertEquals(5.0, d, EPS); // dx=3, dy=4, dz=0 => 5
    }

    @Test
    @DisplayName("distanceTo: null other throws")
    void distanceToNull() {
        Point3D a = Point3D.of(0, 0, 0);
        assertThrows(NullPointerException.class, () -> a.distanceTo(null));
    }

    // ---- Transforms -----------------------------------------------------------------------------

    @Test
    @DisplayName("translate: simple shift")
    void translateWorks() {
        Point3D p = Point3D.of(1, 1, 1).translate(2, -3, 0.5);
        assertPointApprox(p, 3, -2, 1.5, EPS);
    }

    @Test
    @DisplayName("scale: factor 0 collapses to origin (allowed with WARNING)")
    void scaleZero() {
        Point3D p = Point3D.of(2, -4, 6).scale(0.0);
        assertPointApprox(p, 0, 0, 0, EPS);
    }

    @Test
    @DisplayName("scale: non-finite factor rejected")
    void scaleRejectsNonFinite() {
        Point3D p = Point3D.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> p.scale(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> p.scale(Double.POSITIVE_INFINITY));
    }

    // ---- Rotations ------------------------------------------------------------------------------

    @Test
    @DisplayName("rotateZ: π/2 moves (1,0,0) to (0,1,0)")
    void rotateZQuarterTurn() {
        Point3D p = Point3D.of(1, 0, 0).rotateZ(Math.PI / 2);
        assertPointApprox(p, 0, 1, 0, 1e-12);
    }

    @Test
    @DisplayName("rotateX: π flips Y/Z signs correctly")
    void rotateXPi() {
        Point3D p = Point3D.of(0, 2, -3).rotateX(Math.PI);
        // y' = y*cosπ - z*sinπ = 2*(-1) - (-3)*0 = -2
        // z' = y*sinπ + z*cosπ = 2*0 + (-3)*(-1) = 3
        assertPointApprox(p, 0, -2, 3, 1e-12);
    }

    @Test
    @DisplayName("rotateAroundAxis: around Z by π turns (1,0,0) -> (-1,0,0)")
    void rotateAroundAxisZPi() {
        Point3D p = Point3D.of(1, 0, 0).rotateAroundAxis(0, 0, 1, Math.PI);
        assertPointApprox(p, -1, 0, 0, 1e-12);
    }

    @Test
    @DisplayName("rotateAroundAxis: invalid axis rejected")
    void rotateAroundAxisRejectsBadAxis() {
        Point3D p = Point3D.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> p.rotateAroundAxis(0, 0, 0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> p.rotateAroundAxis(Double.NaN, 0, 1, 1.0));
    }

    // ---- Vector Ops -----------------------------------------------------------------------------

    @Test
    @DisplayName("dot: correctness and null safety")
    void dotProduct() {
        Point3D a = Point3D.of(1, 2, 3);
        Point3D b = Point3D.of(-4, 5, 6);
        assertEquals(1*(-4) + 2*5 + 3*6, a.dot(b), EPS);
        assertThrows(NullPointerException.class, () -> a.dot(null));
    }

    @Test
    @DisplayName("cross: right-hand rule and null safety")
    void crossProduct() {
        Point3D i = Point3D.of(1, 0, 0);
        Point3D j = Point3D.of(0, 1, 0);
        Point3D k = i.cross(j);
        assertPointApprox(k, 0, 0, 1, EPS);

        assertThrows(NullPointerException.class, () -> i.cross(null));
    }

    @Test
    @DisplayName("normalize: unit length; zero vector returns itself")
    void normalizeBehavior() {
        Point3D v = Point3D.of(3, 0, 4).normalize();
        assertEquals(1.0, v.magnitude(), 1e-12);
        assertPointApprox(v, 0.6, 0, 0.8, 1e-12);

        Point3D z = Point3D.of(0, 0, 0);
        // For zero vector, normalize() returns this unchanged (as per implementation)
        assertSame(z, z.normalize());
    }

    @Test
    @DisplayName("lerp: endpoints, midpoint, and extrapolation")
    void lerpBehavior() {
        Point3D a = Point3D.of(0, 0, 0);
        Point3D b = Point3D.of(10, -10, 10);

        assertPointApprox(a.lerp(b, 0.0), 0, 0, 0, EPS);
        assertPointApprox(a.lerp(b, 1.0), 10, -10, 10, EPS);
        assertPointApprox(a.lerp(b, 0.5), 5, -5, 5, EPS);

        // Extrapolation (t outside [0,1]) should still compute correctly
        assertPointApprox(a.lerp(b, -0.5), -5, 5, -5, EPS);
        assertPointApprox(a.lerp(b, 1.5), 15, -15, 15, EPS);

        assertThrows(NullPointerException.class, () -> a.lerp(null, 0.5));
    }

    @Test
    @DisplayName("midpoint: equals lerp(0.5)")
    void midpointEqualsLerp() {
        Point3D a = Point3D.of(1, 2, 3);
        Point3D b = Point3D.of(5, 6, 7);
        Point3D m1 = a.midpoint(b);
        Point3D m2 = a.lerp(b, 0.5);
        assertPointApprox(m1, m2.x(), m2.y(), m2.z(), EPS);
    }

    // ---- Comparisons ----------------------------------------------------------------------------

    @Test
    @DisplayName("epsilonEquals: tolerant comparison and argument validation")
    void epsilonEqualsBehavior() {
        Point3D a = Point3D.of(1.0, 2.0, 3.0);
        Point3D b = Point3D.of(1.0 + 1e-10, 2.0 - 5e-10, 3.0 + 2e-10);

        assertTrue(a.epsilonEquals(b, 1e-9));
        assertFalse(a.epsilonEquals(b, 1e-12));

        assertThrows(NullPointerException.class, () -> a.epsilonEquals(null, 1e-6));
        assertThrows(IllegalArgumentException.class, () -> a.epsilonEquals(b, -1e-3));
        assertThrows(IllegalArgumentException.class, () -> a.epsilonEquals(b, Double.NaN));
    }

    // ---- String form ----------------------------------------------------------------------------

    @Test
    @DisplayName("toString: stable, human-readable")
    void toStringStable() {
        Point3D p = Point3D.of(1.23456789, -2.0, 0.0);
        String s = p.toString();
        assertTrue(s.contains("Point3D("));
        assertTrue(s.contains(","));
    }
}