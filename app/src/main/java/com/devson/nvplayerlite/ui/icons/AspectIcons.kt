package com.devson.nvplayerlite.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.devson.nvplayerlite.model.AspectMode
import com.devson.nvplayerlite.ui.components.AspectIcons as ComponentAspectIcons
import com.devson.nvplayerlite.ui.components.getAspectIcon as componentGetAspectIcon

object AspectIcons {
    val Fit: ImageVector get() = ComponentAspectIcons.Fit
    val Stretch: ImageVector get() = ComponentAspectIcons.Stretch
    val Crop: ImageVector get() = ComponentAspectIcons.Crop
    val Original: ImageVector get() = ComponentAspectIcons.Original
}

fun getAspectIcon(mode: AspectMode): ImageVector = componentGetAspectIcon(mode)
