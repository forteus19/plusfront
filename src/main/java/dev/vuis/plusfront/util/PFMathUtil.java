package dev.vuis.plusfront.util;

import java.util.List;

public final class PFMathUtil {
	private PFMathUtil() {
		throw new AssertionError();
	}

	public static boolean isPointWithinPolygon(List<Vec2d> polygon, Vec2d position) {
		int points = polygon.size();

		if (points < 3) {
			throw new IllegalArgumentException("Not a closed polygon");
		}

		boolean inside = false;

		double x = position.x();
		double y = position.y();

		for (int i = 0, j = points - 1; i < points; j = i++) {
			Vec2d p1 = polygon.get(i);
			Vec2d p2 = polygon.get(j);

			double x1 = p1.x();
			double y1 = p1.y();
			double x2 = p2.x();
			double y2 = p2.y();

			if (onSegment(x1, y1, x2, y2, x, y)) {
				return true;
			}

			if (segmentIntersect(x1, y1, x2, y2, x, y)) {
				inside = !inside;
			}
		}

		return inside;
	}

	private static boolean onSegment(double x1, double y1, double x2, double y2, double x, double y) {
		double c = (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);

		if (Math.abs(c) > 1e-7) {
			return false;
		}

		return Math.min(x1, x2) <= x && x <= Math.max(x1, x2)
			&& Math.min(y1, y2) <= y && y <= Math.max(y1, y2);
	}

	private static boolean segmentIntersect(double x1, double y1, double x2, double y2, double x, double y) {
		return ((y1 > y) != (y2 > y))
			&& (x < (x2 - x1) * (y - y1) / (y2 - y1) + x1);
	}
}
