//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Matrix3].
 */
interface IMatrix3 {
    /** Returns column 0, row 0 of the matrix.  */
    val m00: Float

    /** Returns column 1, row 0 of the matrix.  */
    val m10: Float

    /** Returns column 2, row 0 of the matrix.  */
    val m20: Float

    /** Returns column 0, row 1 of the matrix.  */
    val m01: Float

    /** Returns column 1, row 1 of the matrix.  */
    val m11: Float

    /** Returns column 2, row 1 of the matrix.  */
    val m21: Float

    /** Returns column 0, row 2 of the matrix.  */
    val m02: Float

    /** Returns column 1, row 2 of the matrix.  */
    val m12: Float

    /** Returns column 2, row 2 of the matrix.  */
    val m22: Float

    /** Returns the matrix element at the specified row and column.  */
    fun element(row: Int, col: Int): Float

    /** Copies the requested row (0, 1, 2) into `result`.  */
    fun getRow(row: Int, result: Vector3)

    /** Copies the requested column (0, 1, 2) into `result`.  */
    fun getColumn(col: Int, result: Vector3)

    /**
     * Transposes this matrix.

     * @return a new matrix containing the result.
     */
    fun transpose(): Matrix3

    /**
     * Transposes this matrix, storing the result in the provided object.

     * @return the result matrix, for chaining.
     */
    fun transpose(result: Matrix3): Matrix3

    /**
     * Multiplies this matrix by another.

     * @return a new matrix containing the result.
     */
    fun mult(other: IMatrix3): Matrix3

    /**
     * Multiplies this matrix by another and stores the result in the object provided.

     * @return a reference to the result matrix, for chaining.
     */
    fun mult(other: IMatrix3, result: Matrix3): Matrix3

    /**
     * Adds this matrix to another.

     * @return a new matrix containing the result.
     */
    fun add(other: IMatrix3): Matrix3

    /**
     * Adds this matrix to another and stores the result in the object provided.

     * @return a reference to the result matrix, for chaining.
     */
    fun add(other: IMatrix3, result: Matrix3): Matrix3

    /**
     * Determines whether this matrix represents an affine transformation.
     */
    val isAffine: Boolean

    /**
     * Multiplies this matrix by another, treating the matrices as affine.

     * @return a new matrix containing the result.
     */
    fun multAffine(other: IMatrix3): Matrix3

    /**
     * Multiplies this matrix by another, treating the matrices as affine, and stores the result
     * in the object provided.

     * @return a reference to the result matrix, for chaining.
     */
    fun multAffine(other: IMatrix3, result: Matrix3): Matrix3

    /**
     * Inverts this matrix.

     * @return a new matrix containing the result.
     */
    fun invert(): Matrix3

    /**
     * Inverts this matrix and places the result in the given object.

     * @return a reference to the result matrix, for chaining.
     */
    fun invert(result: Matrix3): Matrix3

    /**
     * Inverts this matrix as an affine matrix.

     * @return a new matrix containing the result.
     */
    fun invertAffine(): Matrix3

    /**
     * Inverts this matrix as an affine matrix and places the result in the given object.

     * @return a reference to the result matrix, for chaining.
     */
    fun invertAffine(result: Matrix3): Matrix3

    /**
     * Linearly interpolates between this and the specified other matrix.

     * @return a new matrix containing the result.
     */
    fun lerp(other: IMatrix3, t: Float): Matrix3

    /**
     * Linearly interpolates between this and the specified other matrix, placing the result in
     * the object provided.

     * @return a reference to the result object, for chaining.
     */
    fun lerp(other: IMatrix3, t: Float, result: Matrix3): Matrix3

    /**
     * Linearly interpolates between this and the specified other matrix, treating the matrices as
     * affine.

     * @return a new matrix containing the result.
     */
    fun lerpAffine(other: IMatrix3, t: Float): Matrix3

    /**
     * Linearly interpolates between this and the specified other matrix (treating the matrices as
     * affine), placing the result in the object provided.

     * @return a reference to the result object, for chaining.
     */
    fun lerpAffine(other: IMatrix3, t: Float, result: Matrix3): Matrix3

    /**
     * Transforms a vector in-place by the inner 3x3 part of this matrix.

     * @return a reference to the vector, for chaining.
     */
    fun transformLocal(vector: Vector3): Vector3

    /**
     * Transforms a vector by this matrix.

     * @return a new vector containing the result.
     */
    fun transform(vector: IVector3): Vector3

    /**
     * Transforms a vector by this matrix and places the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun transform(vector: IVector3, result: Vector3): Vector3

    /**
     * Transforms a point in-place by this matrix.

     * @return a reference to the point, for chaining.
     */
    fun transformPointLocal(point: Vector): Vector

    /**
     * Transforms a point by this matrix.

     * @return a new vector containing the result.
     */
    fun transformPoint(point: IVector): Vector

    /**
     * Transforms a point by this matrix and places the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun transformPoint(point: IVector, result: Vector): Vector

    /**
     * Transforms a vector in-place by the inner 2x2 part of this matrix.

     * @return a reference to the vector, for chaining.
     */
    fun transformVectorLocal(vector: Vector): Vector

    /**
     * Transforms a vector by this inner 2x2 part of this matrix.

     * @return a new vector containing the result.
     */
    fun transformVector(vector: IVector): Vector

    /**
     * Transforms a vector by the inner 2x2 part of this matrix and places the result in the object
     * provided.

     * @return a reference to the result, for chaining.
     */
    fun transformVector(vector: IVector, result: Vector): Vector

    /**
     * Extracts the rotation component of the matrix.
     */
    fun extractRotation(): Float

    /**
     * Extracts the scale component of the matrix.

     * @return a new vector containing the result.
     */
    fun extractScale(): Vector

    /**
     * Extracts the scale component of the matrix and places it in the provided result vector.

     * @return a reference to the result vector, for chaining.
     */
    fun extractScale(result: Vector): Vector

    /**
     * Returns an approximation of the uniform scale for this matrix (the square root of the
     * signed area of the parallelogram spanned by the axis vectors).
     */
    fun approximateUniformScale(): Float
}
