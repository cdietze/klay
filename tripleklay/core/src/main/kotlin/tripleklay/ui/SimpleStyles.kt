package tripleklay.ui

import klay.core.Graphics

/**
 * Provides a simple style sheet that is useful for development and testing.
 */
object SimpleStyles {
    /**
     * Creates and returns a simple default stylesheet.
     */
    fun newSheet(gfx: Graphics): Stylesheet {
        return newSheetBuilder(gfx).create()
    }

    /**
     * Creates and returns a stylesheet builder configured with some useful default styles. The
     * caller can augment the sheet with additional styles and call `create`.
     */
    fun newSheetBuilder(gfx: Graphics): Stylesheet.Builder {
        val bgColor = 0xFFCCCCCC.toInt()
        val ulColor = 0xFFEEEEEE.toInt()
        val brColor = 0xFFAAAAAA.toInt()
        val butBg = Background.roundRect(gfx, bgColor, 5f, ulColor, 2f).inset(5f, 6f, 2f, 6f)
        val butSelBg = Background.roundRect(gfx, bgColor, 5f, brColor, 2f).inset(6f, 5f, 1f, 7f)
        return Stylesheet.builder().add(Button::class.java,
                Style.BACKGROUND.`is`(butBg)).add(Button::class.java, Style.Mode.SELECTED,
                Style.BACKGROUND.`is`(butSelBg)).add(ToggleButton::class.java,
                Style.BACKGROUND.`is`(butBg)).add(ToggleButton::class.java, Style.Mode.SELECTED,
                Style.BACKGROUND.`is`(butSelBg)).add(CheckBox::class.java,
                Style.BACKGROUND.`is`(Background.roundRect(gfx, bgColor, 5f, ulColor, 2f).inset(3f, 2f, 0f, 3f))).add(CheckBox::class.java, Style.Mode.SELECTED,
                Style.BACKGROUND.`is`(Background.roundRect(gfx, bgColor, 5f, brColor, 2f).inset(3f, 2f, 0f, 3f))).add(Field::class.java,
                Style.BACKGROUND.`is`(Background.beveled(0xFFFFFFFF.toInt(), brColor, ulColor).inset(5f)),
                Style.HALIGN.left)// flip ul and br to make Field appear recessed
                .add(Field::class.java, Style.Mode.DISABLED,
                        Style.BACKGROUND.`is`(Background.beveled(0xFFCCCCCC.toInt(), brColor, ulColor).inset(5f)))
                //TODO(cdi) re-add once Menu is ported
//                .add(Menu::class.java,
//                Style.BACKGROUND.`is`(Background.bordered(0xFFFFFFFF.toInt(), 0x00000000, 1f).inset(6f)))
                .add(MenuItem::class.java,
                        Style.BACKGROUND.`is`(Background.solid(0xFFFFFFFF.toInt())),
                        Style.HALIGN.left).add(MenuItem::class.java, Style.Mode.SELECTED,
                Style.BACKGROUND.`is`(Background.solid(0xFF000000.toInt())),
                Style.COLOR.`is`(0xFFFFFFFF.toInt())).add(Tabs::class.java,
                Tabs.HIGHLIGHTER.`is`(Tabs.textColorHighlighter(0xFF000000.toInt(), 0xFFFFFFFF.toInt())))
    }
}
