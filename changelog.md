# 2.3.0

first release of the continued version, the mod was dead for 2 years

- ported to fabric 1.21 - 26.3, one jar per version range
- mojang mappings + stonecutter, the forge/neoforge part is dropped
- screenshot on window close works now (the original only took it from the pause menu)
- leaving waits until the screenshot is actually written, so it isn't lost when closing the game
- fade timing is in ticks now like the settings say (it was counted in frames, so it depended on fps). default delay is 2 seconds
- the server "default display mode" setting actually works now, default is enabled
- the not loaded chunks of the chunk map are see-through so they don't cover the screenshot (not on 1.21 - 1.21.1)
- blur uses the vanilla menu blur strength, the blur quality slider is gone
- removed the screenshot resolution and disable camera settings
- fixed the ukrainian translation not loading, added russian
