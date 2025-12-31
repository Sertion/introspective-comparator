# Introspective Comparator - A NeoForge Mod for Minecraft

Introspective Comparator adds two blocks:

* Introspective Comparator
* Inverted Introspective Comparator

Both blocks read the [analog output signal](https://minecraft.wiki/w/Redstone_Comparator#Measure_block_state), like a
comparator, and not the block's redstone signal strength. This means it will not be powered by being attached to the
side of a redstone block, but can send a powered signal to an adjacent inventory like a barrel or modded blocks.

It does not support item frames.

Like with a regular Minecraft comparator it is possible to toggle between compare and subtract modes and as with a
regular comparator the Introspective Comparators accepts the signal to compare with or subtract from the sides.

It is possible to read the signal through solid blocks like with a regular Minecraft comparator. If this is done the
output signal will be sent to the solid block in between the target and the introspective comparator.

The inverted variant sends a signal strength of 15 when it reads an analog output signal of 0 and a signal strength of 0
when it reads an analog output signal of 15. Compare and subtraction modes execute on the inverted signal.
