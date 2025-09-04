package com.csc205.project1;

public class Point3D {
    private double x;
    private double y;
    private double z;

    // Constructor
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    // Method to calculate distance to another Point3D
    public double distanceTo(Point3D other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) +
                         Math.pow(this.y - other.y, 2) +
                         Math.pow(this.z - other.z, 2));
    }

    @Override
    public String toString() {
        return "Point3D(" + x + ", " + y + ", " + z + ")";
    }
}
