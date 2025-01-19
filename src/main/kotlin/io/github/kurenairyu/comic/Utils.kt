package io.github.kurenairyu.comic

import net.lingala.zip4j.ZipFile
import org.jetbrains.skia.*

object Utils {
    val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp")
    val ZIP_EXTENSIONS = listOf(".zip", ".cbz")

    fun ZipFile.imageFileNames(): List<String> = runCatching {
            this.fileHeaders
                .asSequence()
                .filter {
                    !it.isDirectory &&
                            IMAGE_EXTENSIONS.any { ext -> it.fileName.endsWith(ext, true) }
                }.map { it.fileName }
                .sortedWith { a: String, b: String ->
                    val aFlag = a.contains("cover")
                    val bFlag = b.contains("cover")
                    if (aFlag) {
                        if (bFlag) {
                            a.compareTo(b)
                        } else {
                            -1
                        }
                    } else if (bFlag) {
                        1
                    } else {
                        a.compareTo(b)
                    }
                }.toList()
    }.onFailure {

    }.getOrDefault(emptyList())

    fun Number.pad(length: Int): String {
        return this.toString().padStart(length, '0')
    }

    fun mergeImages(images: List<Image>, maxHeight: Int): Bitmap {
        val originalWidth = images.sumOf { it.width }
        val originalHeight = images.maxOf { it.height }

        log.info("Original size: $originalWidth x $originalHeight")

//        val scaleFactor = maxHeight.toFloat() / originalHeight
        val scaleFactor = 1F

        val newWidth = (originalWidth * scaleFactor).toInt()
        val newHeight = maxHeight

        log.info("Resize to : $newWidth x $newHeight")

        val resultBitmap = Bitmap().apply {
            allocN32Pixels(originalWidth, originalHeight)
        }

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val canvas = Canvas(resultBitmap)
//        canvas.scale(scaleFactor, scaleFactor)

        var widthOffset = 0F
        for (image in images) {
            canvas.drawImage(image, widthOffset, 0F, paint)
            widthOffset += image.width
        }
        canvas.save()

        return resultBitmap
    }

    fun resizeImageToMaxHeight(image: Image, maxHeight: Int): Bitmap {

        val originalWidth = image.width
        val originalHeight = image.height

        val scaleFactor = maxHeight.toFloat() / originalHeight

        val newWidth = (originalWidth * scaleFactor).toInt()
        val newHeight = maxHeight

        val resizedBitmap = Bitmap().apply {
            allocN32Pixels(newWidth, newHeight)
        }

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 创建一个 Canvas，在这个 Bitmap 上进行绘制
        val canvas = Canvas(resizedBitmap)

        canvas.scale(scaleFactor, scaleFactor)
        // 绘制缩放后的图片
        canvas.drawImage(image, 0f, 0f, paint)

        return resizedBitmap
    }
}