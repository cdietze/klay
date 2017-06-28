package tripleklay.demo.core.ui

import klay.core.Pointer
import pythagoras.f.MathUtil
import react.Slot
import react.UnitSlot
import react.Value
import tripleklay.anim.Animation
import tripleklay.anim.Animator
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout
import tripleklay.ui.util.BoxPoint
import tripleklay.util.Colors

class MenuDemo : DemoScreen() {
    override fun name(): String {
        return "Menus"
    }

    override fun title(): String {
        return "UI: Menu"
    }

    override fun createIface(root: Root): Group {
        val menuHost = MenuHost(iface, root)
        val popRight = BoxPoint(1f, 0f, 2f, 0f)
        val popUnder = BoxPoint(0f, 1f, 0f, 2f)
        val direction = Button("Select a direction \u25BC").addStyles(MenuHost.TRIGGER_POINT.`is`(MenuHost.relative(popRight))).onClick({ self: Button ->
            val pop = MenuHost.Pop(self,
                    createMenu("Directions", "North", "South", "East", "West"))
            pop.menu.itemTriggered().connect(updater(self))
            addIcons(pop.menu)
            menuHost.popup(pop)
        })
        val tree = Button("Select a tree \u25BC").addStyles(MenuHost.TRIGGER_POINT.`is`(MenuHost.relative(popUnder))).onClick({ self: Button ->
            val pop = MenuHost.Pop(self,
                    createMenu("Trees", "Elm", "Ash", "Maple", "Oak"))
            pop.menu.itemTriggered().connect(updater(self))
            menuHost.popup(pop)
        })
        val type = Button("Select a type \u25BC").addStyles(
                MenuHost.TRIGGER_POINT.`is`(MenuHost.relative(popUnder)),
                MenuHost.POPUP_ORIGIN.`is`(BoxPoint.BR)).onClick({ self: Button ->
            val pop = MenuHost.Pop(self,
                    createMenu(null, "Road", "Street", "Boulevard", "Avenue"))
            pop.menu.itemTriggered().connect(updater(self))
            pop.menu.addStyles(Menu.OPENER.`is`(object : Menu.AnimFn {
                override fun go(menu: Menu, animator: Animator): Animation {
                    // TODO: fix short delay where menu is visible at this scale
                    menu.layer.setScale(1f, .25f)
                    return animator.tweenScaleY(menu.layer).to(1f).easeOut().`in`(125f)
                }
            }))
            menuHost.popup(pop)
        })

        val subject = object : TrackingLabel(menuHost, "Subject \u25BC") {
            override fun createMenu(): Menu {
                return addIcons(this@MenuDemo.createMenu(null, "The giant", "Jack", "The goose", "Jack's mum"))
            }
        }
        val verb = object : TrackingLabel(menuHost, "Verb \u25BC") {
            override fun createMenu(): Menu {
                return addIcons(showText(this@MenuDemo.createMenu(null, "climbs", "lays", "crushes", "hugs"),
                        MenuItem.ShowText.WHEN_ACTIVE))
            }
        }
        val `object` = object : TrackingLabel(menuHost, "Object \u25BC") {
            override fun createMenu(): Menu {
                return this@MenuDemo.createMenu(null, "the beanstalk", "people", "golden eggs", "the boy")
            }
        }

        val depth = object : TrackingLabel(menuHost, "Floors \u25BC") {
            override fun createMenu(): Menu {
                val menu = Menu(AxisLayout.horizontal().offStretch(), Style.VALIGN.top)
                val g1 = Group(AxisLayout.vertical(), Style.VALIGN.top)
                g1.add(Group(AxisLayout.horizontal()).add(
                        MenuItem("1A"), MenuItem("1B"), MenuItem("1C")))
                g1.add(Group(AxisLayout.horizontal()).add(
                        MenuItem("2A"), MenuItem("2B")))
                g1.add(Group(AxisLayout.horizontal()).add(
                        MenuItem("3A"), MenuItem("3B"), MenuItem("3C")))
                val g2 = Group(AxisLayout.vertical(), Style.HALIGN.right)
                g2.add(MenuItem("Roof", tile(0)), MenuItem("Basement", tile(1)))
                return menu.add(g1, g2)
            }
        }
        val cells = object : TrackingLabel(menuHost, "Ship Locations \u25BC") {
            internal var menu: Menu? = null
            override fun createMenu(): Menu {
                if (menu != null) {
                    return menu!!
                }
                val letters = "ABCDEFGHIJ"
                menu = Menu(AxisLayout.horizontal().offStretch(), Style.VALIGN.top)
                val g = Group(TableLayout(10))
                g.setStylesheet(Stylesheet.builder().add(MenuItem::class.java, Styles.none().add(Style.BACKGROUND.`is`(Background.blank().inset(5f, 1f))).addSelected(Style.BACKGROUND.`is`(Background.solid(Colors.BLACK).inset(5f, 1f)))).create())
                for (col in 0..9) {
                    for (row in 0..9) {
                        g.add(MenuItem(letters.substring(col, col + 1) + (row + 1)))
                    }
                }
                return menu!!.add(g)
            }

            public override fun makePop(ev: Pointer.Event): MenuHost.Pop {
                return super.makePop(ev).retainMenu()
            }

            override fun wasRemoved() {
                super.wasRemoved()
                if (menu != null) menu!!.layer.close()
            }
        }

        val scrolled = object : TrackingLabel(menuHost, "Bits \u25BC") {
            override fun createMenu(): Menu {
                val menu = Menu(AxisLayout.vertical().offStretch())
                menu.add(Label("Select a byte").addStyles(Style.COLOR.`is`(0xFFFFFFFF.toInt()),
                        Style.BACKGROUND.`is`(Background.beveled(0xFF8F8F8F.toInt(), 0xFF4F4F4F.toInt(), 0xFFCFCFCF.toInt()).inset(4f))))
                val items = Group(AxisLayout.vertical())
                val scroller = Scroller(items)
                menu.add(SizableGroup(AxisLayout.vertical().offStretch(), 0f, 200f).add(
                        scroller.setBehavior(Scroller.Behavior.VERTICAL).setConstraint(AxisLayout.stretched())))

                val bits = StringBuilder()
                for (ii in 0..255) {
                    bits.setLength(0)
                    var mask = 128
                    while (mask > 0) {
                        bits.append(if (ii and mask != 0) 1 else 0)
                        mask = mask shr 1
                    }
                    items.add(MenuItem(bits.toString()))
                }
                return menu
            }
        }

        val bytes = object : TrackingLabel(menuHost, "Bytes \u25BC") {
            internal val HEX = "0123456789ABCDEF"
            override fun createMenu(): Menu {
                val menu = PagedMenu(TableLayout(2), 16)
                menu.add(Label("Select a byte").addStyles(Style.COLOR.`is`(0xFFFFFFFF.toInt()),
                        Style.BACKGROUND.`is`(Background.beveled(0xFF8F8F8F.toInt(), 0xFF4F4F4F.toInt(), 0xFFCFCFCF.toInt()).inset(4f))).setConstraint(TableLayout.Colspan(2)))
                val prev = Button("<< Previous").onClick(menu.incrementPage(-1))
                val next = Button("Next >>").onClick(menu.incrementPage(1))
                menu.add(prev, next)
                val updateEnabling: UnitSlot = {
                    prev.setEnabled(menu.page().get() > 0)
                    next.setEnabled(menu.page().get() < menu.numPages().get() - 1)
                }

                menu.page().connect(updateEnabling)
                menu.numPages().connect(updateEnabling)

                var sel = -1
                for (ii in 0..255) {
                    val hex = StringBuilder("0x").append(HEX[ii shr 4 and 0xf]).append(HEX[ii and 0xf]).toString()
                    if (text.get()!!.startsWith(hex)) sel = ii
                    menu.add(MenuItem(hex))
                }
                if (sel != -1) menu.setPage(sel / menu.itemsPerPage)
                updateEnabling.invoke(Unit)
                return menu
            }
        }

        val colors = object : TrackingLabel(menuHost, "Colors \u25BC") {
            override fun createMenu(): Menu {
                val menu = PagedMenu(AxisLayout.vertical(), 32)
                val slider = Slider().setIncrement(1f)
                slider.value.connect({ `val`: Float ->
                    menu.setPage(MathUtil.round(`val`))
                })
                menu.page().connect({ page: Int ->
                    slider.value.update(page.toFloat())
                })
                menu.numPages().connect({ numPages: Int ->
                    slider.range.update(Slider.Range(0f, (numPages.toInt() - 1).toFloat()))
                    slider.setEnabled(numPages > 0)
                })

                val itemStyles = Styles.none().add(Style.Mode.SELECTED,
                        Style.BACKGROUND.`is`(Background.solid(Colors.BLUE).inset(2f))).add(Style.BACKGROUND.`is`(Background.blank().inset(2f)))
                val colorTable = Group(TableLayout(4))
                for (ii in 0..255) {
                    val colorImg = graphics().createCanvas(16f, 16f)
                    colorImg.setFillColor(0xFF000000.toInt() or (ii shl 16))
                    colorImg.fillRect(0f, 0f, 16f, 16f)
                    colorTable.add(MenuItem("", Icons.image(colorImg.toTexture())).addStyles(itemStyles))
                }
                menu.add(colorTable, slider)
                return menu
            }
        }

        return Group(AxisLayout.vertical().offStretch()).add(
                Label("Button popups"),
                Group(AxisLayout.horizontal()).add(direction, tree, type),
                Shim(1f, 20f),
                Label("Continuous Tracking"),
                Group(AxisLayout.horizontal()).add(subject, verb, `object`),
                Shim(1f, 20f),
                Label("Intermediate groups"),
                Group(AxisLayout.horizontal()).add(depth, cells),
                Shim(1f, 20f),
                Label("Scrolling and Paging"),
                Group(AxisLayout.horizontal()).add(scrolled, bytes, colors))
    }

    protected fun updater(button: Button): Slot<MenuItem> {
        return updater(button.text, button.icon)
    }

    protected fun updater(text: Value<String?>, icon: Value<Icon?>): Slot<MenuItem> {
        return { item: MenuItem ->
            text.update(item.text.get() + " \u25BC")
            icon.update(item.icon.get())
        }
    }

    protected fun addIcons(menu: Menu): Menu {
        var tile = 0
        for (item in menu) {
            if (item is MenuItem) {
                item.icon.update(tile(tile++))
            }
        }
        return menu
    }

    protected fun showText(menu: Menu, showText: MenuItem.ShowText): Menu {
        for (item in menu) {
            if (item is MenuItem) {
                item.showText(showText)
            }
        }
        return menu
    }

    protected fun tile(index: Int): Icon {
        val iwidth = 16f
        val iheight = 16f
        return Icons.image(_squares.region(index * iwidth, 0f, iwidth, iheight))
    }

    protected fun createMenu(title: String?, vararg items: String): Menu {
        val menu = Menu(AxisLayout.vertical().offStretch().gap(3))
        if (title != null)
            menu.add(Label(title).addStyles(Style.COLOR.`is`(0xFFFFFFFF.toInt()),
                    Style.BACKGROUND.`is`(Background.beveled(0xFF8F8F8F.toInt(), 0xFF4F4F4F.toInt(), 0xFFCFCFCF.toInt()).inset(4f))))
        for (item in items) menu.add(MenuItem(item))
        return menu
    }

    protected abstract inner class TrackingLabel(var menuHost: MenuHost, text: String) : Label(text) {

        init {
            addStyles(MenuHost.TRIGGER_POINT.`is`(MenuHost.pointer()))
        }

        abstract fun createMenu(): Menu

        override fun createBehavior(): Behavior<Label>? {
            return object : Behavior.Select<Label>(this) {
                override fun onStart(iact: klay.scene.Pointer.Interaction) {
                    val pop = makePop(iact.event!!)
                    pop.menu.itemTriggered().connect(updater(text, icon))
                    menuHost.popup(pop)
                }
            }
        }

        protected open fun makePop(ev: Pointer.Event): MenuHost.Pop {
            return MenuHost.Pop(this, createMenu(), ev).relayEvents(layer)
        }
    }

    protected var _squares = assets().getImage("images/squares.png")
}
