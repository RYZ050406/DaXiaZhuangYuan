package com.daxia.betterinput.mixin;

import net.minecraft.client.gui.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface EditBoxAccessor {
    @Accessor("cursor")
    int betterinput$getCursor();

    @Accessor("selectionEnd")
    int betterinput$getSelectionEnd();
}
