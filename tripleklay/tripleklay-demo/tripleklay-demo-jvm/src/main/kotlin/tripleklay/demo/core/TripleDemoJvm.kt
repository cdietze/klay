package tripleklay.demo.core

import klay.core.Image
import klay.jvm.JavaPlatform
import klay.jvm.LWJGLPlatform

object TripleDemoJvm {
    internal enum class Toolkit {
        NONE, AWT, SWT
    }

    @JvmStatic fun main(args: Array<String>) {
        val config = JavaPlatform.Config()
        config.appName = "Tripleklay Demo"

        var tk = Toolkit.NONE
        val mainArgs = mutableListOf<String>()
        val size = "--size="
        for (ii in args.indices) {
            if (args[ii].startsWith(size)) {
                val wh = args[ii].substring(size.length).split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                config.width = Integer.parseInt(wh[0])
                config.height = Integer.parseInt(wh[1])
            } else if (args[ii] == "--swt")
                tk = Toolkit.SWT
            else if (args[ii] == "--awt")
                tk = Toolkit.AWT
            else
                mainArgs.add(args[ii])// else if (args[ii].equals("--retina")) config.scaleFactor = 2;
        }

        TripleDemo.mainArgs = mainArgs.toTypedArray()
        val plat: JavaPlatform
        when (tk) {
        // TODO(cdi) either re-implement TPPlatform or really remove this code
//            TripleDemoJvm.Toolkit.SWT -> {
//                config.appName += " (SWT)"
//                val splat = SWTPlatform(config)
//                val tpplat = SWTTPPlatform(splat, config)
//                tpplat.setIcon(loadIcon(splat))
//                plat = splat
//            }
//            TripleDemoJvm.Toolkit.AWT -> {
//                val jplat = LWJGLPlatform(config)
//                val tpplat = JavaTPPlatform(jplat, config)
//                tpplat.setIcon(loadIcon(jplat))
//                plat = jplat
//            }
            else ->
                // no native integration
                plat = LWJGLPlatform(config)
        }
        TripleDemo(plat)
        plat.start()
    }

    fun loadIcon(plat: JavaPlatform): Image {
        return plat.assets.getImageSync("icon.png")
    }
}
