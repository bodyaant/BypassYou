package com.example.bypassyou.ui.tokens

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.min

/**
 * Compose [Shape] implementation directly powered by [RoundedPolygon] from AndroidX Graphics Shapes
 * and Material 3 Expressive. Ensures un-distorted vector outlines, centered within the bounds.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class PolygonShape(
    val polygon: RoundedPolygon,
    val startAngle: Int = 0
) : Shape {
    private val morph = Morph(polygon, polygon)

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }
        val path = Path()
        morph.toPath(progress = 0f, path = path, startAngle = startAngle)
        val bounds = path.getBounds()
        if (bounds.width <= 0f || bounds.height <= 0f) {
            return Outline.Rectangle(Rect(Offset.Zero, size))
        }
        val minDim = min(size.width, size.height)
        val scaleMatrix = Matrix().apply { scale(x = minDim, y = minDim) }
        path.transform(scaleMatrix)
        val transformedBounds = path.getBounds()
        path.translate(size.center - transformedBounds.center)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object ExpressiveShapes {

    // --- 1. COOKIES & CLOVERS ---
    val Cookie4: RoundedPolygon = MaterialShapes.Cookie4Sided
    val Cookie6: RoundedPolygon = MaterialShapes.Cookie6Sided
    val Cookie7: RoundedPolygon = MaterialShapes.Cookie7Sided
    val Cookie9: RoundedPolygon = MaterialShapes.Cookie9Sided
    val Cookie12: RoundedPolygon = MaterialShapes.Cookie12Sided
    val Clover4: RoundedPolygon = MaterialShapes.Clover4Leaf
    val Clover8: RoundedPolygon = MaterialShapes.Clover8Leaf

    val Cookie4Shape: Shape = PolygonShape(Cookie4)
    val Cookie6Shape: Shape = PolygonShape(Cookie6)
    val Cookie7Shape: Shape = PolygonShape(Cookie7)
    val Cookie9Shape: Shape = PolygonShape(Cookie9)
    val Cookie12Shape: Shape = PolygonShape(Cookie12)
    val Clover4Shape: Shape = PolygonShape(Clover4)
    val Clover8Shape: Shape = PolygonShape(Clover8)

    // --- 2. BURSTS & STARS ---
    val Burst: RoundedPolygon = MaterialShapes.Burst
    val SoftBurst: RoundedPolygon = MaterialShapes.SoftBurst
    val Boom: RoundedPolygon = MaterialShapes.Boom
    val SoftBoom: RoundedPolygon = MaterialShapes.SoftBoom
    val Sunny: RoundedPolygon = MaterialShapes.Sunny
    val VerySunny: RoundedPolygon = MaterialShapes.VerySunny

    val BurstShape: Shape = PolygonShape(Burst)
    val SoftBurstShape: Shape = PolygonShape(SoftBurst)
    val BoomShape: Shape = PolygonShape(Boom)
    val SoftBoomShape: Shape = PolygonShape(SoftBoom)
    val SunnyShape: Shape = PolygonShape(Sunny)
    val VerySunnyShape: Shape = PolygonShape(VerySunny)

    // --- 3. ORGANIC & PLAYFUL ---
    val Flower: RoundedPolygon = MaterialShapes.Flower
    val Bun: RoundedPolygon = MaterialShapes.Bun
    val Heart: RoundedPolygon = MaterialShapes.Heart
    val Puffy: RoundedPolygon = MaterialShapes.Puffy
    val PuffyDiamond: RoundedPolygon = MaterialShapes.PuffyDiamond
    val Ghostish: RoundedPolygon = MaterialShapes.Ghostish
    val PixelCircle: RoundedPolygon = MaterialShapes.PixelCircle
    val PixelTriangle: RoundedPolygon = MaterialShapes.PixelTriangle
    val ClamShell: RoundedPolygon = MaterialShapes.ClamShell

    val FlowerShape: Shape = PolygonShape(Flower)
    val BunShape: Shape = PolygonShape(Bun)
    val HeartShape: Shape = PolygonShape(Heart)
    val PuffyShape: Shape = PolygonShape(Puffy)
    val PuffyDiamondShape: Shape = PolygonShape(PuffyDiamond)
    val GhostishShape: Shape = PolygonShape(Ghostish)
    val PixelCircleShape: Shape = PolygonShape(PixelCircle)
    val PixelTriangleShape: Shape = PolygonShape(PixelTriangle)
    val ClamShellShape: Shape = PolygonShape(ClamShell)

    // --- 4. ARCHITECTURAL & POLYGONS ---
    val Arch: RoundedPolygon = MaterialShapes.Arch
    val SemiCircle: RoundedPolygon = MaterialShapes.SemiCircle
    val Oval: RoundedPolygon = MaterialShapes.Oval
    val Pill: RoundedPolygon = MaterialShapes.Pill
    val Gem: RoundedPolygon = MaterialShapes.Gem
    val Diamond: RoundedPolygon = MaterialShapes.Diamond
    val Pentagon: RoundedPolygon = MaterialShapes.Pentagon
    val Triangle: RoundedPolygon = MaterialShapes.Triangle
    val Fan: RoundedPolygon = MaterialShapes.Fan
    val Arrow: RoundedPolygon = MaterialShapes.Arrow
    val Slanted: RoundedPolygon = MaterialShapes.Slanted
    val Square: RoundedPolygon = MaterialShapes.Square
    val Circle: RoundedPolygon = MaterialShapes.Circle

    val ArchShape: Shape = PolygonShape(Arch)
    val SemiCircleShape: Shape = PolygonShape(SemiCircle)
    val OvalShape: Shape = PolygonShape(Oval)
    val PillShape: Shape = PolygonShape(Pill)
    val GemShape: Shape = PolygonShape(Gem)
    val DiamondShape: Shape = PolygonShape(Diamond)
    val PentagonShape: Shape = PolygonShape(Pentagon)
    val TriangleShape: Shape = PolygonShape(Triangle)
    val FanShape: Shape = PolygonShape(Fan)
    val ArrowShape: Shape = PolygonShape(Arrow)
    val SlantedShape: Shape = PolygonShape(Slanted)
    val SquareShape: Shape = PolygonShape(Square)
    val CircleShape: Shape = PolygonShape(Circle)

    /**
     * Returns a whimsical, distinct expressive shape for avatars based on a contact name.
     */
    fun getAvatarShape(identifier: String): Shape {
        val list = listOf(
            Cookie9Shape,
            FlowerShape,
            SoftBurstShape,
            Clover4Shape,
            SoftBoomShape,
            BunShape,
            SunnyShape,
            GemShape,
            PuffyDiamondShape,
            ClamShellShape,
            Cookie7Shape,
            Clover8Shape
        )
        val index = (identifier.hashCode() and 0x7FFFFFFF) % list.size
        return list[index]
    }
}
