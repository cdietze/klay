//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d;

import java.util.NoSuchElementException;

/**
 * Provides most of the implementation of {@link ICubicCurve}, obtaining only the start, end and
 * control points from the derived class.
 */
public abstract class AbstractCubicCurve implements ICubicCurve
{
    @Override // from interface ICubicCurve
    public Point p1 () {
        return new Point(x1(), y1());
    }

    @Override // from interface ICubicCurve
    public Point ctrlP1 () {
        return new Point(ctrlX1(), ctrlY1());
    }

    @Override // from interface ICubicCurve
    public Point ctrlP2 () {
        return new Point(ctrlX2(), ctrlY2());
    }

    @Override // from interface ICubicCurve
    public Point p2 () {
        return new Point(x2(), y2());
    }

    @Override // from interface ICubicCurve
    public double flatnessSq () {
        return CubicCurves.flatnessSq(x1(), y1(), ctrlX1(), ctrlY1(),
                                         ctrlX2(), ctrlY2(), x2(), y2());
    }

    @Override // from interface ICubicCurve
    public double flatness () {
        return CubicCurves.flatness(x1(), y1(), ctrlX1(), ctrlY1(),
                                       ctrlX2(), ctrlY2(), x2(), y2());
    }

    @Override // from interface ICubicCurve
    public void subdivide (CubicCurve left, CubicCurve right) {
        CubicCurves.subdivide(this, left, right);
    }

    @Override // from interface ICubicCurve
    public CubicCurve clone () {
        return new CubicCurve(x1(), y1(), ctrlX1(), ctrlY1(),
                              ctrlX2(), ctrlY2(), x2(), y2());
    }

    @Override // from interface IShape
    public boolean isEmpty () {
        return true; // curves contain no space
    }

    @Override // from interface IShape
    public boolean contains (double px, double py) {
        return Crossing.isInsideEvenOdd(Crossing.crossShape(this, px, py));
    }

    @Override // from interface IShape
    public boolean contains (double rx, double ry, double rw, double rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return (cross != Crossing.CROSSING) && Crossing.isInsideEvenOdd(cross);
    }

    @Override // from interface IShape
    public boolean contains (XY p) {
        return contains(p.x(), p.y());
    }

    @Override // from interface IShape
    public boolean contains (IRectangle r) {
        return contains(r.x(), r.y(), r.width(), r.height());
    }

    @Override // from interface IShape
    public boolean intersects (double rx, double ry, double rw, double rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return (cross == Crossing.CROSSING) || Crossing.isInsideEvenOdd(cross);
    }

    @Override // from interface IShape
    public boolean intersects (IRectangle r) {
        return intersects(r.x(), r.y(), r.width(), r.height());
    }

    @Override // from interface IShape
    public Rectangle bounds () {
        return bounds(new Rectangle());
    }

    @Override // from interface IShape
    public Rectangle bounds (Rectangle target) {
        double x1 = x1(), y1 = y1(), x2 = x2(), y2 = y2();
        double ctrlx1 = ctrlX1(), ctrly1 = ctrlY1();
        double ctrlx2 = ctrlX2(), ctrly2 = ctrlY2();
        double rx1 = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
        double ry1 = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
        double rx2 = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
        double ry2 = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
        target.setBounds(rx1, ry1, rx2 - rx1, ry2 - ry1);
        return target;
    }

    @Override // from interface IShape
    public PathIterator pathIterator (Transform t) {
        return new Iterator(this, t);
    }

    @Override // from interface IShape
    public PathIterator pathIterator (Transform at, double flatness) {
        return new FlatteningPathIterator(pathIterator(at), flatness);
    }

    /** An iterator over an {@link ICubicCurve}. */
    protected static class Iterator implements PathIterator
    {
        private ICubicCurve c;
        private Transform t;
        private int index;

        Iterator (ICubicCurve c, Transform t) {
            this.c = c;
            this.t = t;
        }

        @Override public int windingRule () {
            return WIND_NON_ZERO;
        }

        @Override public boolean isDone () {
            return index > 1;
        }

        @Override public void next () {
            index++;
        }

        @Override public int currentSegment (double[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                coords[0] = c.x1();
                coords[1] = c.y1();
                count = 1;
            } else {
                type = SEG_CUBICTO;
                coords[0] = c.ctrlX1();
                coords[1] = c.ctrlY1();
                coords[2] = c.ctrlX2();
                coords[3] = c.ctrlY2();
                coords[4] = c.x2();
                coords[5] = c.y2();
                count = 3;
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }
}
