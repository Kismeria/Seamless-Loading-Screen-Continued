plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3"

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"

    replacements {
        string(current.parsed >= "26.1") {
            replace("net.minecraft.client.gui.GuiGraphics;", "net.minecraft.client.gui.GuiGraphicsExtractor;")
            replace("GuiGraphics graphics", "GuiGraphicsExtractor graphics")
            replace("gui/GuiGraphics;", "gui/GuiGraphicsExtractor;")
        }

        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
        }
    }
}
