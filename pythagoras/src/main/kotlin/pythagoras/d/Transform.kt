//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.NoninvertibleTransformException

/**
 * Represents a geometric transform. Specialized implementations exist for identity, rigid body,
 * uniform, non-uniform, and affine transforms.
 */
interface Transform {
    /** Returns the uniform scale applied by this transform. The uniform scale will be approximated
     * for non-uniform transforms.  */
    fun uniformScale(): Double

    /** Returns the scale vector for this transform.  */
    fun scale(): Vector

    /** Returns the x-component of the scale applied by this transform. Note that this will be
     * extracted and therefore approximate for affine transforms.  */
    fun scaleX(): Double

    /** Returns the y-component of the scale applied by this transform. Note that this will be
     * extracted and therefore approximate for affine transforms.  */
    fun scaleY(): Double

    /** Returns the rotation applied by this transform. Note that the rotation is extracted and
     * therefore approximate for affine transforms.
     * @throws NoninvertibleTransformException if the transform is not invertible.
     */
    fun rotation(): Double

    /** Returns the translation vector for this transform.  */
    fun translation(): Vector

    /** Returns the x-coordinate of the translation component.  */
    fun tx(): Double

    /** Returns the y-coordinate of the translation component.  */
    fun ty(): Double

    /** Copies the affine transform matrix into the supplied array.
     * @param matrix the array which receives `m00, m01, m10, m11, tx, ty`.
     */
    operator fun get(matrix: DoubleArray)

    /** Sets the uniform scale of this transform.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not uniform or greater.
     */
    fun setUniformScale(scale: Double): Transform

    /** Sets the x and y scale of this transform.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if either supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun setScale(scaleX: Double, scaleY: Double): Transform

    /** Sets the x scale of this transform.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun setScaleX(scaleX: Double): Transform

    /** Sets the y scale of this transform.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun setScaleY(scaleY: Double): Transform

    /** Sets the rotation component of this transform.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun setRotation(angle: Double): Transform

    /** Sets the translation component of this transform.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun setTranslation(tx: Double, ty: Double): Transform

    /** Sets the x-component of this transform's translation.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun setTx(tx: Double): Transform

    /** Sets the y-component of this transform's translation.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun setTy(ty: Double): Transform

    /** Sets the affine transform matrix.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not affine or greater.
     */
    fun setTransform(m00: Double, m01: Double, m10: Double, m11: Double,
                     tx: Double, ty: Double): Transform

    /** Scales this transform in a uniform manner by the specified amount.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not uniform or greater.
     */
    fun uniformScale(scale: Double): Transform

    /** Scales this transform by the specified amount in the x and y dimensions.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if either supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun scale(scaleX: Double, scaleY: Double): Transform

    /** Scales this transform by the specified amount in the x dimension.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun scaleX(scaleX: Double): Transform

    /** Scales this transform by the specified amount in the y dimension.
     * @return this instance, for chaining.
     * *
     * @throws IllegalArgumentException if the supplied scale is zero.
     * *
     * @throws UnsupportedOperationException if the transform is not non-uniform or greater.
     */
    fun scaleY(scaleY: Double): Transform

    /** Rotates this transform.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun rotate(angle: Double): Transform

    /** Translates this transform.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun translate(tx: Double, ty: Double): Transform

    /** Translates this transform in the x dimension.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun translateX(tx: Double): Transform

    /** Translates this transform in the y dimension.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not rigid body or greater.
     */
    fun translateY(ty: Double): Transform

    /** Shears this transform.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not affine or greater.
     */
    fun shear(tx: Double, ty: Double): Transform

    /** Shears this transform in the x dimension.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not affine or greater.
     */
    fun shearX(tx: Double): Transform

    /** Shears this transform in the y dimension.
     * @return this instance, for chaining.
     * *
     * @throws UnsupportedOperationException if the transform is not affine or greater.
     */
    fun shearY(ty: Double): Transform

    /** Returns a new transform that represents the inverse of this transform.
     * @throws NoninvertibleTransformException if the transform is not invertible.
     */
    fun invert(): Transform

    /** Returns a new transform comprised of the concatenation of `other` to this transform
     * (i.e. `this x other`).  */
    fun concatenate(other: Transform): Transform

    /** Returns a new transform comprised of the concatenation of this transform to `other`
     * (i.e. `other x this`).  */
    fun preConcatenate(other: Transform): Transform

    /** Returns a new transform comprised of the linear interpolation between this transform and
     * the specified other.  */
    fun lerp(other: Transform, t: Double): Transform

    /** Transforms the supplied point, writing the result into `into`.
     * @param into a point into which to store the result, may be the same object as `p`.
     * *
     * @return `into` for chaining.
     */
    fun transform(p: IPoint, into: Point): Point

    /** Transforms the supplied points.
     * @param src the points to be transformed.
     * *
     * @param srcOff the offset into the `src` array at which to start.
     * *
     * @param dst the points into which to store the transformed points. May be `src`.
     * *
     * @param dstOff the offset into the `dst` array at which to start.
     * *
     * @param count the number of points to transform.
     */
    fun transform(src: Array<IPoint>, srcOff: Int, dst: Array<Point>, dstOff: Int, count: Int)

    /** Transforms the supplied points.
     * @param src the points to be transformed (as `[x, y, x, y, ...]`).
     * *
     * @param srcOff the offset into the `src` array at which to start.
     * *
     * @param dst the points into which to store the transformed points. May be `src`.
     * *
     * @param dstOff the offset into the `dst` array at which to start.
     * *
     * @param count the number of points to transform.
     */
    fun transform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, count: Int)

    /** Inverse transforms the supplied point, writing the result into `into`.
     * @param into a point into which to store the result, may be the same object as `p`.
     * *
     * @return `into`, for chaining.
     * *
     * @throws NoninvertibleTransformException if the transform is not invertible.
     */
    fun inverseTransform(p: IPoint, into: Point): Point

    /** Transforms the supplied vector as a point (accounting for translation), writing the result
     * into `into`.
     * @param into a vector into which to store the result, may be the same object as `v`.
     * *
     * @return `into`, for chaining.
     */
    fun transformPoint(v: IVector, into: Vector): Vector

    /** Transforms the supplied vector, writing the result into `into`.
     * @param into a vector into which to store the result, may be the same object as `v`.
     * *
     * @return `into`, for chaining.
     */
    fun transform(v: IVector, into: Vector): Vector

    /** Inverse transforms the supplied vector, writing the result into `into`.
     * @param into a vector into which to store the result, may be the same object as `v`.
     * *
     * @return `into`, for chaining.
     * *
     * @throws NoninvertibleTransformException if the transform is not invertible.
     */
    fun inverseTransform(v: IVector, into: Vector): Vector

    @Deprecated("Use {@link #copy}. ")
    fun clone(): Transform

    /** Returns a copy of this transform.  */
    fun copy(): Transform

    /** Returns an integer that increases monotonically with the generality of the transform
     * implementation. Used internally when combining transforms.  */
    fun generality(): Int
}
