//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.lang.Math

/**
 * A pyramidal frustum.
 */
class Frustum {
    /** Intersection types indicating that the frustum does not intersect, intersects, or fully
     * contains, respectively, the parameter.  */
    enum class IntersectionType {
        NONE, INTERSECTS, CONTAINS
    }

    /** The vertices of the frustum.  */
    protected var _vertices: Array<Vector3> = Array(8, { _ -> Vector3()} )

    /** The planes of the frustum (as derived from the vertices). The plane normals point out of
     * the frustum.  */
    protected var _planes: Array<Plane> = Array(6, { _ -> Plane()})

    /** The frustum's bounding box (as derived from the vertices).  */
    protected var _bounds = Box()

    /**
     * Creates an empty (invalid) frustum.
     */
    init {
    }

    /**
     * Returns a reference to the frustum's array of vertices.
     */
    fun vertices(): Array<out IVector3> {
        return _vertices
    }

    /**
     * Returns a reference to the bounds of this frustum.
     */
    fun bounds(): Box {
        return _bounds
    }

    /**
     * Sets this frustum to one pointing in the Z- direction with the specified parameters
     * determining its size and shape (see the OpenGL documentation for
     * `gluPerspective`).

     * @param fovy the vertical field of view, in radians.
     * *
     * @param aspect the aspect ratio (width over height).
     * *
     * @param znear the distance to the near clip plane.
     * *
     * @param zfar the distance to the far clip plane.
     * *
     * @return a reference to this frustum, for chaining.
     */
    fun setToPerspective(fovy: Float, aspect: Float, znear: Float, zfar: Float): Frustum {
        val top = znear * FloatMath.tan(fovy / 2f)
        val bottom = -top
        val right = top * aspect
        val left = -right
        return setToFrustum(left, right, bottom, top, znear, zfar)
    }

    /**
     * Sets this frustum to one pointing in the Z- direction with the specified parameters
     * determining its size and shape (see the OpenGL documentation for `glFrustum`).

     * @return a reference to this frustum, for chaining.
     */
    fun setToFrustum(
            left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Frustum {
        return setToProjection(left, right, bottom, top, near, far, Vector3.UNIT_Z, false, false)
    }

    /**
     * Sets this frustum to an orthographic one pointing in the Z- direction with the specified
     * parameters determining its size (see the OpenGL documentation for `glOrtho`).

     * @return a reference to this frustum, for chaining.
     */
    fun setToOrtho(
            left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Frustum {
        return setToProjection(left, right, bottom, top, near, far, Vector3.UNIT_Z, true, false)
    }

    /**
     * Sets this frustum to a perspective or orthographic projection with the specified parameters
     * determining its size and shape.

     * @return a reference to this frustum, for chaining.
     */
    fun setToProjection(
            left: Float, right: Float, bottom: Float, top: Float, near: Float,
            far: Float, nearFarNormal: IVector3, ortho: Boolean, mirrored: Boolean): Frustum {
        val nfnx = nearFarNormal.x
        val nfny = nearFarNormal.y
        val nfnz = nearFarNormal.z
        if (ortho) {
            val nrz = -1f / nfnz
            val xl = nfnx * left * nrz
            val xr = nfnx * right * nrz
            val yb = nfny * bottom * nrz
            val yt = nfny * top * nrz
            _vertices[0].set(left, bottom, xl + yb - near)
            _vertices[if (mirrored) 3 else 1].set(right, bottom, xr + yb - near)
            _vertices[2].set(right, top, xr + yt - near)
            _vertices[if (mirrored) 1 else 3].set(left, top, xl + yt - near)
            _vertices[4].set(left, bottom, xl + yb - far)
            _vertices[if (mirrored) 7 else 5].set(right, bottom, xr + yb - far)
            _vertices[6].set(right, top, xr + yt - far)
            _vertices[if (mirrored) 5 else 7].set(left, top, xl + yt - far)

        } else {
            val rn = 1f / near
            val lrn = left * rn
            val rrn = right * rn
            val brn = bottom * rn
            val trn = top * rn

            val nz = near * nfnz
            val z0 = nz / (nfnx * lrn + nfny * brn - nfnz)
            _vertices[0].set(-z0 * lrn, -z0 * brn, z0)
            val z1 = nz / (nfnx * rrn + nfny * brn - nfnz)
            _vertices[if (mirrored) 3 else 1].set(-z1 * rrn, -z1 * brn, z1)
            val z2 = nz / (nfnx * rrn + nfny * trn - nfnz)
            _vertices[2].set(-z2 * rrn, -z2 * trn, z2)
            val z3 = nz / (nfnx * lrn + nfny * trn - nfnz)
            _vertices[if (mirrored) 1 else 3].set(-z3 * lrn, -z3 * trn, z3)

            val fz = far * nfnz
            val z4 = fz / (nfnx * lrn + nfny * brn - nfnz)
            _vertices[4].set(-z4 * lrn, -z4 * brn, z4)
            val z5 = fz / (nfnx * rrn + nfny * brn - nfnz)
            _vertices[if (mirrored) 7 else 5].set(-z5 * rrn, -z5 * brn, z5)
            val z6 = fz / (nfnx * rrn + nfny * trn - nfnz)
            _vertices[6].set(-z6 * rrn, -z6 * trn, z6)
            val z7 = fz / (nfnx * lrn + nfny * trn - nfnz)
            _vertices[if (mirrored) 5 else 7].set(-z7 * lrn, -z7 * trn, z7)
        }

        updateDerivedState()
        return this
    }

    // /**
    //  * Transforms this frustum in-place by the specified transformation.
    //  *
    //  * @return a reference to this frustum, for chaining.
    //  */
    // public Frustum transformLocal (Transform3D transform)
    // {
    //     return transform(transform, this);
    // }

    // /**
    //  * Transforms this frustum by the specified transformation.
    //  *
    //  * @return a new frustum containing the result.
    //  */
    // public Frustum transform (Transform3D transform)
    // {
    //     return transform(transform, new Frustum());
    // }

    // /**
    //  * Transforms this frustum by the specified transformation, placing the result in the object
    //  * provided.
    //  *
    //  * @return a reference to the result frustum, for chaining.
    //  */
    // public Frustum transform (Transform3D transform, Frustum result)
    // {
    //     // transform all of the vertices
    //     for (int ii = 0; ii < 8; ii++) {
    //         transform.transformPoint(_vertices[ii], result._vertices[ii]);
    //     }
    //     result.updateDerivedState();
    //     return result;
    // }

    /**
     * Determines the maximum signed distance of the point from the planes of the frustum. If
     * the distance is less than or equal to zero, the point lies inside the frustum.
     */
    fun distance(point: Vector3): Float {
        var distance = -Float.MAX_VALUE
        for (plane in _planes) {
            distance = Math.max(distance, plane.distance(point))
        }
        return distance
    }

    /**
     * Checks whether the frustum intersects the specified box.
     */
    fun intersectionType(box: Box): IntersectionType {
        // exit quickly in cases where the bounding boxes don't overlap (equivalent to a separating
        // axis test using the axes of the box)
        if (!_bounds.intersects(box)) {
            return IntersectionType.NONE
        }

        // consider each side of the frustum as a potential separating axis
        var ccount = 0
        for (ii in 0..5) {
            // determine how many vertices fall inside/outside the plane
            var inside = 0
            val plane = _planes[ii]
            for (jj in 0..7) {
                if (plane.distance(box.vertex(jj, _vertex)) <= 0f) {
                    inside++
                }
            }
            if (inside == 0) {
                return IntersectionType.NONE
            } else if (inside == 8) {
                ccount++
            }
        }
        return if (ccount == 6) IntersectionType.CONTAINS else IntersectionType.INTERSECTS
    }

    /**
     * Computes the bounds of the frustum under the supplied rotation and places the results in
     * the box provided.

     * @return a reference to the result box, for chaining.
     */
    fun boundsUnderRotation(matrix: Matrix3, result: Box): Box {
        result.setToEmpty()
        for (vertex in _vertices) {
            result.addLocal(matrix.transform(vertex, _vertex))
        }
        return result
    }

    /**
     * Sets the planes and bounding box of the frustum based on its vertices.
     */
    protected fun updateDerivedState() {
        _planes[0].fromPoints(_vertices[0], _vertices[1], _vertices[2]) // near
        _planes[1].fromPoints(_vertices[5], _vertices[4], _vertices[7]) // far
        _planes[2].fromPoints(_vertices[1], _vertices[5], _vertices[6]) // left
        _planes[3].fromPoints(_vertices[4], _vertices[0], _vertices[3]) // right
        _planes[4].fromPoints(_vertices[3], _vertices[2], _vertices[6]) // top
        _planes[5].fromPoints(_vertices[4], _vertices[5], _vertices[1]) // bottom
        _bounds.fromPoints(*_vertices)
    }

    companion object {

        /** A working vertex.  */
        protected var _vertex = Vector3()
    }
}
