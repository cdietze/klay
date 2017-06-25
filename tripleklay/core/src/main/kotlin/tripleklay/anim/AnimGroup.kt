package tripleklay.anim

import java.util.*

/**
 * Allows one to specify a group of one or more animations that will be queued up to be started on
 * an [Animator] at some later time. All animations added to the group will be started in
 * parallel.

 * <pre>`AnimGroup group = new AnimGroup();
 * group.tweenXY(...).then().tweenAlpha(...);
 * group.play(sound).then().action(...);
 * // the two animation chains (the tween chain and the play/action chain) will run in parallel
 * // after this group is added to an Animator
 * anim.add(group.toAnim());
`</pre> *

 * One can combine multiple animation groups to achieve any desired construction of sequential and
 * parallel animations.

 * <pre>`AnimGroup group2 = new AnimGroup();
 * group2.tweenXY(...).then().tweenAlpha(...);
 * group2.play(sound).then().action(...);
 * AnimGroup group1 = new AnimGroup();
 * group1.delay(1000).then().add(group2.toAnim());
 * group1.delay(500).then().play(sound);
 * // group 1's two animation chains will be queued up to run in parallel, and the first of its
 * // chains will delay 1s and then trigger group 2's chains, which themselves run in parallel
 * anim.add(group1.toAnim());
`</pre> *

 * It is of course also possible to add a group with a single animation chain, which will contain
 * no parallelism but can still be useful for situations where one wants to compose sequences of
 * animations internally and then return that package of animations to be sequenced with other
 * packages of animations by some outer mechanism:

 * <pre>`class Ship {
 * Animation createExplosionAnim () {
 * AnimGroup group = new AnimGroup();
 * group.play(sound).then().flipbook(...);
 * return group.toAnim();
 * }
 * }
`</pre> *
 */
class AnimGroup : AnimBuilder() {
    /**
     * Adds an animation to this group. This animation will be started in parallel with all other
     * animations added to this group when the group is turned into an animation and started via
     * [Animator.add] or added to another chain of animations that was added to an animator.

     * @throws IllegalStateException if this method is called directly or implicitly (by any of the
     * * [AnimBuilder] fluent methods) after [.toAnim] has been called.
     */
    override fun <T : Animation> add(anim: T): T {
        if (_anims == null) throw IllegalStateException("AnimGroup already animated.")
        _anims!!.add(anim)
        return anim
    }

    /**
     * Returns a single animation that will execute all of the animations in this group to
     * completion (in parallel) and will report itself as complete when the final animation in the
     * group is complete. After calling this method, this group becomes unusable. It is not valid
     * to call [.add] (or any other method) after [.toAnim].
     */
    fun toAnim(): Animation {
        val groupAnims = _anims!!.toTypedArray()
        _anims = null
        return object : Animation() {
            override fun init(time: Float) {
                super.init(time)
                for (ii in groupAnims.indices) {
                    _curAnims[ii] = groupAnims[ii]
                    _curAnims[ii]!!.init(time)
                }
            }

            override fun apply(animator: Animator?, time: Float): Float {
                _animator = animator
                return super.apply(animator, time)
            }

            override fun apply(time: Float): Float {
                var remain = java.lang.Float.NEGATIVE_INFINITY
                var processed = 0
                for (ii in _curAnims.indices) {
                    val anim = _curAnims[ii] ?: continue
                    val aremain = anim.apply(_animator, time)
                    // if this animation is now complete, remove it from the array
                    if (aremain <= 0) _curAnims[ii] = null
                    // note this animation's leftover time, we want our remaining time to be the
                    // highest remaining time of our internal animations
                    remain = Math.max(remain, aremain)
                    // note that we processed an animation
                    processed++
                }
                // if we somehow processed zero animations, return 0 (meaning we're done) rather
                // than -infinity which would throw off any animation queued up after this one
                return if (processed == 0) 0f else remain
            }

            override fun makeComplete() {
                // if we haven't started, complete all anims, otherwise just the active anims
                val anims = if (_start == 0f) groupAnims else _curAnims
                for (anim in anims) {
                    anim?.completeChain()
                }
            }

            private var _animator: Animator? = null
            private var _curAnims = arrayOfNulls<Animation>(groupAnims.size)
        }
    }

    private var _anims: MutableList<Animation>? = ArrayList()
}
