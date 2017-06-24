package tripleklay.demo.jvm.anim

// TODO(cdi) re-add when tripleplay.util.PackedFrames is ported which required JSON
//class FramesDemo : DemoScreen() {
//    override fun name(): String {
//        return "Flipbook"
//    }
//
//    override fun title(): String {
//        return "Flipbook Demo"
//    }
//
//    override fun createIface(root: Root): Group? {
//        val width = size().width
//        val height = size().height
//        val bg = object : Layer() {
//            override fun paintImpl(surf: Surface) {
//                surf.setFillColor(0xFFCCCCCC.toInt())
//                surf.fillRect(0f, 0f, width, height)
//            }
//        }
//        bg.setDepth(-1f)
//        layer.add(bg)
//
//        // test our simple frames
//        val box = GroupLayer()
//        layer.addAt(box, 0f, 100f)
//        val sheet = assets().getImage("images/spritesheet.png")
//        val frames = SimpleFrames(sheet, 60f, 60f, 60)
//        iface.anim.repeat(box).flipbook(box, Flipbook(frames, 66f))
//        iface.anim.repeat(box).tweenX(box).to(width - frames.width()).`in`(2000f).easeInOut().then().tweenX(box).to(0f).`in`(2000f).easeInOut()
//
//        // test our packed frames
//        val packed = assets().getImage("images/orb_burst.png")
//        assets().getText("images/orb_burst.json").onSuccess({json: String ->
//                val box = GroupLayer()
//                layer.addAt(box, 100f, 200f)
//                iface.anim.repeat(box).flipbook(
//                        box, Flipbook(PackedFrames(packed, json().parse(json)), 99)).then().setVisible(box, false).then().delay(500).then().setVisible(box, true)
//
//                val pbox = GroupLayer()
//                layer.addAt(pbox, 300, 200)
//                iface.anim.repeat(pbox).flipbook(
//                        pbox, Flipbook(PackedFrames(packed, PACKED), 99)).then().setVisible(pbox, false).then().delay(500).then().setVisible(pbox, true)
//        })
//
//        return null
//    }
//
//    companion object {
//
//        // copied from assets/target/classes/assets/images/orb_burst.java
//        protected val PACKED = arrayOf(floatArrayOf(202.0f, 204.0f), floatArrayOf(41.0f, 50.0f), floatArrayOf(320.0f, 162.0f, 117.0f, 117.0f), floatArrayOf(42.0f, 50.0f), floatArrayOf(438.0f, 162.0f, 117.0f, 117.0f), floatArrayOf(43.0f, 50.0f), floatArrayOf(320.0f, 280.0f, 117.0f, 117.0f), floatArrayOf(42.0f, 50.0f), floatArrayOf(438.0f, 280.0f, 117.0f, 117.0f), floatArrayOf(28.0f, 31.0f), floatArrayOf(176.0f, 162.0f, 143.0f, 161.0f), floatArrayOf(41.0f, 28.0f), floatArrayOf(176.0f, 324.0f, 119.0f, 147.0f), floatArrayOf(32.0f, 0.0f), floatArrayOf(0.0f, 346.0f, 134.0f, 174.0f), floatArrayOf(16.0f, 18.0f), floatArrayOf(402.0f, 0.0f, 166.0f, 143.0f), floatArrayOf(0.0f, 45.0f), floatArrayOf(201.0f, 0.0f, 200.0f, 130.0f), floatArrayOf(0.0f, 30.0f), floatArrayOf(0.0f, 0.0f, 200.0f, 161.0f), floatArrayOf(10.0f, 21.0f), floatArrayOf(0.0f, 162.0f, 175.0f, 183.0f))
//    }
//}
