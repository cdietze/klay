package tripleklay.demo.core.entity

import klay.core.*
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.MathUtil
import pythagoras.f.Point
import pythagoras.f.Vector
import react.Signal
import tripleklay.demo.core.DemoScreen
import tripleklay.entity.Component
import tripleklay.entity.Entity
import tripleklay.entity.System
import tripleklay.entity.World
import tripleklay.ui.Group
import tripleklay.ui.Root
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.StyledText
import tripleklay.util.TextStyle
import tripleklay.util.getFloat
import tripleklay.util.getInRange

class AsteroidsDemo : DemoScreen() {
    val asteroids = assets().getImage("images/asteroids.png")

    enum class Size(val size: Int) {
        TINY(20), SMALL(40), MEDIUM(60), LARGE(80)
    }

    inner class AsteroidsWorld(val stage: GroupLayer, val swidth: Float, val sheight: Float) : World() {
        val random = Random()

        val type = Component.IMask(this)
        val opos = Component.FXY(this)
        val pos = Component.FXY(this)
        val vel = Component.FXY(this) // pixels/ms
        val sprite = Component.Generic<Layer>(this)
        val size = Component.Generic<Size>(this)
        val spin = Component.FScalar(this) // rads/ms
        val radius = Component.FScalar(this)
        val expires = Component.IScalar(this)

        val keyDown = Signal<Key>()
        val keyUp = Signal<Key>()

        var now: Int = 0 // ms elapsed since world start, used by expirer/expires

        private fun wrapx(x: Float): Float {
            return if (x > swidth) x - swidth else if (x < 0) x + swidth else x
        }

        private fun wrapy(y: Float): Float {
            return if (y > sheight) y - sheight else if (y < 0) y + sheight else y
        }

        // handles player input
        fun registerControls() {
            this.register(object : System() {
                val ACCEL = 0.01f
                val ROT = 0.005f
                val MAX_VEL = 1f
                val BULLET_LIFE = 1000 // ms
                val BULLET_VEL = 0.25f

                /* ctor */ init {
                    keyDown.connect({ key: Key ->
                        when (key) {
                            Key.LEFT -> _angvel = -ROT
                            Key.RIGHT -> _angvel = ROT
                            Key.UP -> _accel = ACCEL
                            Key.SPACE -> if (_wave >= 0) fireBullet()
                            Key.S -> if (_wave == -1) startWave(0)
                            else -> {
                            }
                        }
                    })
                    keyUp.connect({ key: Key ->
                        when (key) {
                            Key.LEFT -> _angvel = 0f
                            Key.RIGHT -> _angvel = 0f
                            Key.UP -> _accel = 0f
                            else -> {
                            }
                        }
                    })
                }

                fun fireBullet() {
                    val sid = _ship.id
                    val ang = sprite[sid].rotation()
                    val vx = vel.getX(sid)
                    val vy = vel.getY(sid)
                    val bvx = vx + BULLET_VEL * MathUtil.cos(ang)
                    val bvy = vy + BULLET_VEL * MathUtil.sin(ang)
                    createBullet(pos.getX(sid), pos.getY(sid), bvx, bvy, ang, now + BULLET_LIFE)
                    vel.set(sid, vx - bvx / 100, vy - bvy / 100) // decrease ship's velocity a smidgen
                }

                override fun update(clock: Clock, entities: Entities) {
                    val v = _vel
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid = entities[ii]
                        spin[eid] = _angvel
                        if (_accel != 0f) {
                            val s = sprite[eid]
                            val ang = s.rotation()
                            vel.get(eid, v)
                            v.x = MathUtil.clamp(v.x + MathUtil.cos(ang) * _accel, -MAX_VEL, MAX_VEL)
                            v.y = MathUtil.clamp(v.y + MathUtil.sin(ang) * _accel, -MAX_VEL, MAX_VEL)
                            vel[eid] = v
                        }
                        ii++
                    }
                }

                override fun wasAdded(entity: Entity) {
                    super.wasAdded(entity)
                    _ship = entity
                }

                override fun isInterested(entity: Entity): Boolean {
                    return type[entity.id] == SHIP
                }

                protected var _vel = Vector()
                protected var _angvel: Float = 0.toFloat()
                protected var _accel: Float = 0.toFloat()
                protected lateinit var _ship: Entity
            })
        }

        // checks for collisions (modeling everything as a sphere)
        fun registerCollider() {
            this.register(object : System() {
                override fun update(clock: Clock, entities: Entities) {
                    // simple O(n^2) collision check; no need for anything fancy here
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid1 = entities[ii]
                        val e1 = this@AsteroidsWorld.entity(eid1)
                        if (e1.isDisposed) {
                            ii++
                            continue
                        }
                        pos.get(eid1, _p1)
                        val r1 = radius[eid1]
                        for (jj in ii + 1 until ll) {
                            val eid2 = entities[jj]
                            val e2 = this@AsteroidsWorld.entity(eid2)
                            if (e2.isDisposed) continue
                            pos.get(eid2, _p2)
                            val r2 = radius[eid2]
                            val dr = r2 + r1
                            val dist2 = _p1.distanceSq(_p2)
                            if (dist2 <= dr * dr) {
                                collide(e1, e2)
                                break // don't collide e1 with any other entities
                            }
                        }
                        ii++
                    }
                }

                override fun isInterested(entity: Entity): Boolean {
                    return entity.has(pos) && entity.has(radius)
                }

                private fun collide(e1: Entity, e2: Entity) {
                    when (type[e1.id] or type[e2.id]) {
                        SHIP_ASTEROID -> {
                            explode(if (type[e1.id] == SHIP) e1 else e2, 10, 0.75f)
                            setMessage("Game Over. Press 's' to restart")
                            _wave = -1
                        }
                        BULLET_ASTEROID -> if (type[e1.id] == ASTEROID) {
                            sunder(e1)
                            e2.close()
                        } else {
                            sunder(e2)
                            e1.close()
                        }
                    // TODO: asteroid asteroid?
                        else -> {
                        }
                    }// nada
                }

                protected val SHIP_ASTEROID = SHIP or ASTEROID
                protected val BULLET_ASTEROID = BULLET or ASTEROID

                protected val _p1 = Point()
                protected val _p2 = Point()
            })
        }

        // handles updating entity position based on entity velocity
        fun registerMover() {
            this.register(object : System(0) {
                override fun update(clock: Clock, entities: tripleklay.entity.System.Entities) {
                    val p = _pos
                    val v = _vel
                    val delta = clock.dt
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid = entities[ii]
                        pos.get(eid, p) // get our current pos
                        p.x = wrapx(p.x) // wrap it around the screen if necessary
                        p.y = wrapy(p.y)
                        opos[eid] = p // copy wrapped pos to opos
                        vel.get(eid, v).scaleLocal(delta.toFloat()) // turn velocity into delta pos
                        pos.set(eid, p.x + v.x, p.y + v.y) // add velocity (but don't wrap)
                        ii++
                    }
                }

                override fun isInterested(entity: Entity): Boolean {
                    return entity.has(opos) && entity.has(pos) && entity.has(vel)
                }

                protected val _pos = Point()
                protected val _vel = Vector()
            })
        }

        // updates sprites to interpolated position of entities on each paint() call
        fun registerSpriter() {
            this.register(object : System(0) {
                override fun paint(clock: PaintClock, entities: Entities) {
                    val alpha = clock.alpha
                    val op = _oldPos
                    val p = _pos
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid = entities[ii]
                        // interpolate between opos and pos and use that to update the sprite position
                        opos.get(eid, op)
                        pos.get(eid, p)
                        // wrap our interpolated position as we may interpolate off the screen
                        sprite[eid].setTranslation(wrapx(MathUtil.lerp(op.x, p.x, alpha)),
                                wrapy(MathUtil.lerp(op.y, p.y, alpha)))
                        ii++
                    }
                }

                override fun wasAdded(entity: Entity) {
                    super.wasAdded(entity)
                    stage.addAt(sprite[entity.id], pos.getX(entity.id), pos.getX(entity.id))
                }

                override fun wasRemoved(entity: Entity, index: Int) {
                    super.wasRemoved(entity, index)
                    stage.remove(sprite[entity.id])
                }

                override fun isInterested(entity: Entity): Boolean {
                    return entity.has(opos) && entity.has(pos) && entity.has(sprite)
                }

                protected val _oldPos = Point()
                protected val _pos = Point()
            })
        }

        // spins things
        fun registerSpinner() {
            this.register(object : System(0) {
                override fun paint(clock: PaintClock, entities: Entities) {
                    val dt = clock.dt.toFloat()
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid = entities[ii]
                        val angvel = spin[eid]
                        if (angvel == 0f) {
                            ii++
                            continue
                        }
                        val s = sprite[eid]
                        s.setRotation(s.rotation() + angvel * dt)
                        ii++
                    }
                }

                override fun isInterested(entity: Entity): Boolean {
                    return entity.has(spin) && entity.has(sprite)
                }
            })
        }

        // expires things with limited lifespan (like bullets)
        fun registerExpirer() {
            this.register(object : System(0) {
                override fun update(clock: Clock, entities: Entities) {
                    val now = this@AsteroidsWorld.now
                    var ii = 0
                    val ll = entities.size()
                    while (ii < ll) {
                        val eid = entities[ii]
                        if (expires[eid] <= now) this@AsteroidsWorld.entity(eid).close()
                        ii++
                    }
                }

                override fun isInterested(entity: Entity): Boolean {
                    return entity.has(expires)
                }
            })
        }

        // handles progression to next wave
        fun registerWaver() {
            this.register(object : System(0) {
                override fun update(clock: Clock, entities: Entities) {
                    // if the only entity left is the player's ship; move to the next wave
                    if (entities.size() == 1 && type[entities[0]] == SHIP) {
                        startWave(++_wave)
                    }
                }

                override fun isInterested(entity: Entity): Boolean {
                    return true
                }
            })
        }

        init {
            registerControls()
            registerCollider()
            registerMover()
            registerSpriter()
            registerSpinner()
            registerExpirer()
            registerWaver()

            closeOnHide(input().keyboardEvents.connect(Keyboard.keySlot { event: Keyboard.KeyEvent ->
                (if (event.down) keyDown else keyUp).emit(event.key)
            }))
        }

        fun setMessage(text: String?) {
            if (_msg != null) _msg!!.close()
            if (text != null) {
                _msg = StyledText.span(graphics(), text, MSG_STYLE).toLayer()
                _msg!!.setDepth(1f)
                stage.addAt(_msg!!, (swidth - _msg!!.width()) / 2, (sheight - _msg!!.height()) / 2)
            }
        }

        fun attract() {
            setMessage("Press 's' to start.")
            for (ii in 0..4)
                createAsteroid(
                        Size.LARGE, random.getFloat(swidth), random.getFloat(sheight))
            _wave = -1
        }

        fun startWave(wave: Int) {
            // if this is wave 0, destroy any existing entities and add our ship
            if (wave == 0) {
                for (e in this) e.close()
                createShip(swidth / 2, sheight / 2)
                setMessage(null)
            }
            var ii = 0
            val ll = minOf(10, wave + 2)
            while (ii < ll) {
                val x = random.getFloat(swidth)
                val y = random.getFloat(sheight)
                // TODO: make sure x/y doesn't overlap ship
                createAsteroid(Size.LARGE, x, y)
                ii++
            }
            _wave = wave
        }

        fun explode(target: Entity, frags: Int, maxvel: Float) {
            val x = pos.getX(target.id)
            val y = pos.getY(target.id)
            // create a bunch of bullets going in random directions from the ship
            for (ii in 0 until frags) {
                val ang = random.getInRange(-MathUtil.PI, MathUtil.PI)
                val vel = random.getInRange(maxvel / 3, maxvel)
                val vx = MathUtil.cos(ang) * vel
                val vy = MathUtil.sin(ang) * vel
                createBullet(x, y, vx, vy, ang, now + 300/*ms*/)
            }
            // and destroy the target
            target.close()
        }

        override fun update(clock: Clock) {
            now += clock.dt
            super.update(clock)
        }

        protected fun typeName(id: Int): String {
            when (type[id]) {
                SHIP -> return "ship"
                BULLET -> return "bullet"
                ASTEROID -> return "asteroid"
                else -> return "unknown:" + type[id]
            }
        }

        protected fun toString(id: Int): String {
            return typeName(id) + ":" + id + "@" + pos.getX(id) + "/" + pos.getY(id)
        }

        protected fun createShip(x: Float, y: Float): Entity {
            val ship = create(true)
            ship.add(type, sprite, opos, pos, vel, spin, radius)

            val canvas = graphics().createCanvas(30f, 20f)
            val path = canvas.createPath()
            path.moveTo(0f, 0f).lineTo(30f, 10f).lineTo(0f, 20f).close()
            canvas.setFillColor(0xFFCC99FF.toInt()).fillPath(path)
            val layer = ImageLayer(canvas.toTexture())
            layer.setOrigin(15f, 10f)
            layer.setRotation(-MathUtil.HALF_PI)

            val id = ship.id
            type[id] = SHIP
            sprite[id] = layer
            opos.set(id, x, y)
            pos.set(id, x, y)
            vel.set(id, 0f, 0f)
            radius[id] = 10f
            return ship
        }

        protected fun createAsteroid(size: Size, x: Float, y: Float): Entity {
            return createAsteroid(size, x, y, random.getInRange(-MAXVEL, MAXVEL),
                    random.getInRange(-MAXVEL, MAXVEL))
        }

        protected fun createAsteroid(sz: Size, x: Float, y: Float, vx: Float, vy: Float): Entity {
            val ast = create(true)
            ast.add(type, size, sprite, opos, pos, vel, spin, radius)

            val side = sz.size.toFloat()
            val iidx = random.nextInt(8)
            val ah = asteroids.height
            val layer = ImageLayer(asteroids.region(iidx * ah, 0f, ah, ah))
            layer.setOrigin(ah / 2, ah / 2)
            layer.setScale(side / ah)
            layer.setRotation(random.getFloat(MathUtil.TAU))

            val id = ast.id
            type[id] = ASTEROID
            size[id] = sz
            sprite[id] = layer
            spin[id] = random.getInRange(-MAXSPIN, MAXSPIN)
            opos.set(id, x, y)
            pos.set(id, x, y)
            vel.set(id, vx, vy)
            radius[id] = side * 0.425f
            return ast
        }

        protected fun createBullet(x: Float, y: Float, vx: Float, vy: Float, angle: Float, exps: Int): Entity {
            val bullet = create(true)
            bullet.add(type, sprite, opos, pos, vel, radius, expires)

            val canvas = graphics().createCanvas(5f, 2f)
            canvas.setFillColor(0xFFFFFFFF.toInt()).fillRect(0f, 0f, 5f, 2f)
            val layer = ImageLayer(canvas.toTexture())
            layer.setOrigin(2.5f, 1f)
            layer.setRotation(angle)

            val id = bullet.id
            type[id] = BULLET
            sprite[id] = layer
            opos.set(id, x, y)
            pos.set(id, x, y)
            vel.set(id, vx, vy)
            radius[id] = 2f
            expires[id] = exps
            return bullet
        }

        protected fun sunder(ast: Entity) {
            val smaller: Size
            when (size[ast.id]) {
                AsteroidsDemo.Size.SMALL -> smaller = Size.TINY
                AsteroidsDemo.Size.MEDIUM -> smaller = Size.SMALL
                AsteroidsDemo.Size.LARGE -> smaller = Size.MEDIUM
                else -> {
                    explode(ast, 4, 0.25f)
                    return
                }
            }
            val x = pos.getX(ast.id)
            val y = pos.getY(ast.id)
            val vx = vel.getX(ast.id)
            val vy = vel.getY(ast.id)
            // break the asteroid into two pieces, spinning in opposite directions and headed at
            // roughly right angles to the original
            createAsteroid(smaller, x, y, -vy, vx)
            createAsteroid(smaller, x, y, vy, -vx)
            ast.close() // and destroy ourself
        }

        protected var _wave = -1
        protected var _msg: ImageLayer? = null
    }

    override fun name(): String {
        return "Asteroids"
    }

    override fun title(): String {
        return "Asteroids Demo"
    }

    override fun showTransitionCompleted() {
        super.showTransitionCompleted()
        val world = AsteroidsWorld(layer, size().width, size().height)
        closeOnHide(world.connect(update, paint))
        world.attract()
    }

    override fun createIface(root: Root): Group? {
        root.layer.setDepth(-1f) // render below our game
        return Group(AxisLayout.vertical())
    }

    companion object {

        val MSG_STYLE = TextStyle.DEFAULT.withTextColor(0xFFFFFFFF.toInt()).withFont(Font("Helvetica", 24f))

        val SHIP = 1 shl 0
        val ASTEROID = 1 shl 1
        val BULLET = 1 shl 2

        val MAXVEL = 0.02f
        val MAXSPIN = 0.001f
    }
}
