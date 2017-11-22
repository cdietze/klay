package tripleklay.anim

import euklid.f.XY
import klay.core.Sound
import klay.core.assert
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import react.Closeable
import react.Signal
import react.Value
import tripleklay.sound.Playable
import tripleklay.util.Layers

/**
 * Provides a fluent interface for building single chains of animations. See [Animator] for a
 * concrete entry point.
 */
abstract class AnimBuilder {
    /**
     * Registers an animation with this builder. If this is the root animator, it will be started
     * on the next frame and continue until cancelled or it reports that it has completed. If this
     * is an animator returned from [Animation.then] then the queued animation will be
     * started when the animation on which `then` was called has completed.
     */
    abstract fun <T : Animation> add(anim: T): T

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    fun tweenTranslation(layer: Layer): Animation.Two {
        return tweenXY(layer)
    }

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    fun tweenXY(layer: Layer): Animation.Two {
        return tween(onXY(layer))
    }

    /**
     * Starts a tween on the supplied layer's x-translation.
     */
    fun tweenX(layer: Layer): Animation.One {
        return tween(onX(layer))
    }

    /**
     * Starts a tween on the supplied layer's y-translation.
     */
    fun tweenY(layer: Layer): Animation.One {
        return tween(onY(layer))
    }

    /**
     * Starts a tween on the supplied layer's origin.
     */
    fun tweenOrigin(layer: Layer): Animation.Two {
        return tween(onOrigin(layer))
    }

    /**
     * Starts a tween on the supplied layer's rotation.
     */
    fun tweenRotation(layer: Layer?): Animation.One {
        assert(layer != null)
        return tween(object : Animation.Value {
            override fun initial(): Float {
                return layer!!.rotation()
            }

            override fun set(value: Float) {
                layer!!.setRotation(value)
            }
        })
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    fun tweenScale(layer: Layer?): Animation.One {
        assert(layer != null)
        return tween(object : Animation.Value {
            override fun initial(): Float {
                return layer!!.scaleX()
            }

            override fun set(value: Float) {
                layer!!.setScale(value)
            }
        })
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    fun tweenScaleXY(layer: Layer): Animation.Two {
        return tween(onScaleXY(layer))
    }

    /**
     * Starts a tween on the supplied layer's x-scale.
     */
    fun tweenScaleX(layer: Layer): Animation.One {
        return tween(onScaleX(layer))
    }

    /**
     * Starts a tween on the supplied layer's y-scale.
     */
    fun tweenScaleY(layer: Layer): Animation.One {
        return tween(onScaleY(layer))
    }

    /**
     * Starts a tween on the supplied layer's transparency.
     */
    fun tweenAlpha(layer: Layer?): Animation.One {
        assert(layer != null)
        return tween(object : Animation.Value {
            override fun initial(): Float {
                return layer!!.alpha()
            }

            override fun set(value: Float) {
                layer!!.setAlpha(value)
            }
        })
    }

    /**
     * Starts a tween using the supplied custom value. [Animation.Value.initial] will be used
     * (if needed) to obtain the initial value before the tween begins. [Animation.Value.set]
     * will be called each time the tween is updated with the intermediate values.
     */
    fun tween(value: Animation.Value): Animation.One {
        return add(Animation.One(value))
    }

    /**
     * Starts a tween using the supplied custom X/Y value.
     */
    fun tween(value: Animation.XYValue): Animation.Two {
        return add(Animation.Two(value))
    }

    /**
     * Starts a flipbook animation that displays in `layer`. Note that the image layer in
     * question will have its translation adjusted based on the offset of the current frame. Thus
     * it should be placed into a [GroupLayer] if it is to be positioned and animated
     * separately.
     */
    fun flipbook(layer: ImageLayer, book: Flipbook): Animation.Flip {
        return add(Animation.Flip(layer, book))
    }

    /**
     * Starts a flipbook animation in a new image layer which is created and added to `box`.
     * When the flipbook animation is complete, the newly created image layer will not be disposed
     * automatically. This allows the animation to be repeated, if desired. The caller must dispose
     * eventually the image layer, or more likely, dispose `box` which will cause the created
     * image layer to be disposed.
     */
    fun flipbook(box: GroupLayer, book: Flipbook): Animation.Flip {
        val image = ImageLayer()
        box.add(image)
        return flipbook(image, book)
    }

    /**
     * Starts a flipbook animation that displays the supplied `book` at the specified
     * position in the supplied parent. The intermediate layers created to display the flipbook
     * animation will be disposed on completion.
     */
    fun flipbookAt(parent: GroupLayer, x: Float, y: Float, book: Flipbook): Animation {
        val box = GroupLayer()
        box.setTranslation(x, y)
        return add(parent, box).then().flipbook(box, book).then().dispose(box)
    }

    /**
     * Starts a flipbook animation that displays the supplied `book` at the specified
     * position in the supplied parent. The intermediate layers created to display the flipbook
     * animation will be disposed on completion.
     */
    fun flipbookAt(parent: GroupLayer, pos: XY, book: Flipbook): Animation {
        return flipbookAt(parent, pos.x, pos.y, book)
    }

    /**
     * Creates a shake animation on the specified layer.
     */
    fun shake(layer: Layer): Animation.Shake {
        return add(Animation.Shake(layer))
    }

    /**
     * Creates an animation that delays for the specified duration in milliseconds.
     */
    fun delay(duration: Float): Animation.Delay {
        return add(Animation.Delay(duration))
    }

    /**
     * Returns a builder which can be used to construct an animation that will be repeated until
     * the supplied layer has been removed from its parent. The layer must be added to a parent
     * before the next frame (if it's not already), or the cancellation will trigger immediately.
     */
    fun repeat(layer: Layer): AnimBuilder {
        return add(Animation.Repeat(layer)).then()
    }

    /**
     * Creates an animation that executes the supplied function and immediately completes.
     */
    fun action(action: () -> Unit): Animation.Action {
        return add(Animation.Action(action))
    }

    /**
     * Adds the supplied child to the supplied parent. This is generally done as the beginning of a
     * chain of animations, which itself may be delayed or subject to animation barriers.
     */
    fun add(parent: GroupLayer, child: Layer): Animation.Action {
        return action({ parent.add(child) })
    }

    /**
     * Adds the supplied child to the supplied parent at the specified translation. This is
     * generally done as the beginning of a chain of animations, which itself may be delayed or
     * subject to animation barriers.
     */
    fun addAt(parent: GroupLayer, child: Layer, pos: XY): Animation.Action {
        return addAt(parent, child, pos.x, pos.y)
    }

    /**
     * Adds the supplied child to the supplied parent at the specified translation. This is
     * generally done as the beginning of a chain of animations, which itself may be delayed or
     * subject to animation barriers.
     */
    fun addAt(parent: GroupLayer,
              child: Layer, x: Float, y: Float): Animation.Action {
        return action({ parent.addAt(child, x, y) })
    }

    /**
     * Reparents the supplied child to the supplied new parent. This involves translating the
     * child's current coordinates to screen coordinates, moving it to its new parent layer and
     * translating its coordinates into the coordinate space of the new parent. Thus the child does
     * not change screen position, even though its coordinates relative to its parent will most
     * likely have changed.
     */
    fun reparent(newParent: GroupLayer, child: Layer): Animation.Action {
        return action({ Layers.reparent(child, newParent) })
    }

    /**
     * Disposes the specified disposable.
     */
    fun dispose(dable: Closeable): Animation.Action {
        return action({ dable.close() })
    }

    /**
     * Sets the specified layer's depth to the specified value.
     */
    fun setDepth(layer: Layer, depth: Float): Animation.Action {
        return action({ layer.setDepth(depth) })
    }

    /**
     * Sets the specified layer to visible or not.
     */
    fun setVisible(layer: Layer, visible: Boolean): Animation.Action {
        return action({ layer.setVisible(visible) })
    }

    /**
     * Plays the supplied clip or loop.
     */
    fun play(sound: Playable): Animation.Action {
        return action({ sound.play() })
    }

    /**
     * Stops the supplied clip or loop.
     */
    fun stop(sound: Playable): Animation.Action {
        return action({ sound.stop() })
    }

    /**
     * Plays the supplied sound.
     */
    fun play(sound: Sound): Animation.Action {
        return action({ sound.play() })
    }

    /**
     * Tweens the volume of the supplied playable. Note, this does not play or stop the sound,
     * those must be enacted separately.
     */
    fun tweenVolume(sound: Playable?): Animation.One {
        assert(sound != null)
        return tween(object : Animation.Value {
            override fun initial(): Float {
                return sound!!.volume()
            }

            override fun set(value: Float) {
                sound!!.setVolume(value)
            }
        })
    }

    /**
     * Tweens the volume of the supplied sound. Useful for fade-ins and fade-outs. Note, this does
     * not play or stop the sound, those must be enacted separately.
     */
    fun tweenVolume(sound: Sound?): Animation.One {
        assert(sound != null)
        return tween(object : Animation.Value {
            override fun initial(): Float {
                return sound!!.volume()
            }

            override fun set(value: Float) {
                sound!!.setVolume(value)
            }
        })
    }

    /**
     * Stops the supplied sound from playing.
     */
    fun stop(sound: Sound): Animation.Action {
        return action({ sound.stop() })
    }

    /**
     * Emits `value` on `signal`.
     */
    fun <T> emit(signal: Signal<T>, value: T): Animation.Action {
        return action({ signal.emit(value) })
    }

    /**
     * Sets a value to the supplied constant.
     */
    fun <T> setValue(value: Value<T>, newValue: T): Animation.Action {
        return action({ value.update(newValue) })
    }

    /**
     * Increments (or decrements if `amount` is negative} an int value.
     */
    fun increment(value: Value<Int>, amount: Int): Animation.Action {
        return action({ value.update(value.get() + amount) })
    }

    companion object {

        protected fun onX(layer: Layer?): Animation.Value {
            assert(layer != null)
            return object : Animation.Value {
                override fun initial(): Float {
                    return layer!!.tx()
                }

                override fun set(value: Float) {
                    layer!!.setTx(value)
                }
            }
        }

        protected fun onY(layer: Layer?): Animation.Value {
            assert(layer != null)
            return object : Animation.Value {
                override fun initial(): Float {
                    return layer!!.ty()
                }

                override fun set(value: Float) {
                    layer!!.setTy(value)
                }
            }
        }

        protected fun onXY(layer: Layer?): Animation.XYValue {
            assert(layer != null)
            return object : Animation.XYValue {
                override fun initialX(): Float {
                    return layer!!.tx()
                }

                override fun initialY(): Float {
                    return layer!!.ty()
                }

                override fun set(x: Float, y: Float) {
                    layer!!.setTranslation(x, y)
                }
            }
        }

        protected fun onScaleX(layer: Layer?): Animation.Value {
            assert(layer != null)
            return object : Animation.Value {
                override fun initial(): Float {
                    return layer!!.scaleX()
                }

                override fun set(value: Float) {
                    layer!!.setScaleX(value)
                }
            }
        }

        protected fun onScaleY(layer: Layer?): Animation.Value {
            assert(layer != null)
            return object : Animation.Value {
                override fun initial(): Float {
                    return layer!!.scaleY()
                }

                override fun set(value: Float) {
                    layer!!.setScaleY(value)
                }
            }
        }

        protected fun onScaleXY(layer: Layer?): Animation.XYValue {
            assert(layer != null)
            return object : Animation.XYValue {
                override fun initialX(): Float {
                    return layer!!.scaleX()
                }

                override fun initialY(): Float {
                    return layer!!.scaleY()
                }

                override fun set(x: Float, y: Float) {
                    layer!!.setScale(x, y)
                }
            }
        }

        protected fun onOrigin(layer: Layer?): Animation.XYValue {
            assert(layer != null)
            return object : Animation.XYValue {
                override fun initialX(): Float {
                    return layer!!.originX()
                }

                override fun initialY(): Float {
                    return layer!!.originY()
                }

                override fun set(x: Float, y: Float) {
                    layer!!.setOrigin(x, y)
                }
            }
        }
    }
}
