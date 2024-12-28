package org.aoc.day09

import java.nio.file.Paths
import kotlin.io.path.readText

sealed class Block(open val size: Int) {
    data class Free(override val size: Int) : Block(size)

    data class File(val id: Int, override val size: Int) : Block(size)
}

fun solve() {
    // part1("day09/sample.txt")
    part1("day09/input.txt")
    // part2("day09/sample.txt")
    part2("day09/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val disk = readDataFile(path).trim()

    // Parse the disk map
    val diskMap =
            disk.withIndex().map {
                if (it.index % 2 == 0) {
                    Block.File(it.index / 2, it.value.digitToInt())
                } else {
                    Block.Free(it.value.digitToInt())
                }
            }

    // Run compaction
    val compacted = runCompaction(diskMap)

    // Compute the checksum
    println(checksum(compacted))
}

fun part2(path: String) {
    val disk = readDataFile(path).trim()

    // Parse the disk map
    val diskMap =
            disk.withIndex().map {
                if (it.index % 2 == 0) {
                    Block.File(it.index / 2, it.value.digitToInt())
                } else {
                    Block.Free(it.value.digitToInt())
                }
            }

    // Run compaction
    val compacted = runDefragmentation(diskMap)

    // Compute the checksum
    println(checksum(compacted))
}

fun runCompaction(disk: List<Block>): List<Block> {
    require(disk.count() > 1)
    require(disk.last() is Block.File) { "The last block in a disk must be a file" }

    val compacted = mutableListOf<Block.File>()

    var leftCursor: Int = 0
    var leftCompactedSize: Int = 0

    var rightCursor: Int = disk.count() - 1
    var rightCompactedSize: Int = 0

    while (rightCursor >= leftCursor) {
        // Pick the next block from the left
        val leftBlock = disk[leftCursor]

        when (leftBlock) {
            is Block.File -> {
                // Check if we are in the last iteration, in which the two are equal
                if (leftCursor == rightCursor) {
                    // This means the rightBlock is partially compacted, so we
                    // can compact the rest and finish
                    val remainingSize = leftBlock.size - rightCompactedSize
                    compacted.add(leftBlock.copy(size = remainingSize))
                } else {
                    compacted.add(leftBlock)
                }
                leftCursor += 1
                continue
            }
            is Block.Free -> {
                // Check if the block is full
                val freeSize = leftBlock.size - leftCompactedSize
                if (freeSize == 0) {
                    leftCursor += 1
                    leftCompactedSize = 0
                    continue
                }

                // Start compacting files from the right
                val rightBlock = disk[rightCursor]
                check(rightBlock is Block.File)

                val remainingSize = rightBlock.size - rightCompactedSize

                if (freeSize >= remainingSize) {
                    // The remaining block fits in the free block, so just fill it
                    compacted.add(Block.File(id = rightBlock.id, size = remainingSize))
                    // Advance the right cursor to the next file
                    rightCursor -= 2
                    rightCompactedSize = 0
                    leftCompactedSize += remainingSize
                } else {
                    // The remaining block does not fit, so we need to insert just part of it
                    compacted.add(Block.File(id = rightBlock.id, size = freeSize))
                    rightCompactedSize += freeSize
                    leftCompactedSize += freeSize
                }
            }
        }
    }

    return compacted
}

fun runDefragmentation(disk: List<Block>): List<Block> {
    require(disk.count() > 1)
    require(disk.last() is Block.File) { "The last block in a disk must be a file" }

    // Create a copy of the disk that we will modify in place
    val defragged = disk.toMutableList()

    // From right to left, try to move the files into the head of the freelist
    var cursor = disk.count() - 1
    val visited = mutableSetOf<Block.File>()

    // NOTE(alvaro): Technically we would need to check the 0th element, but we would
    // never end up moving it since it's already at the leftmost position
    while (cursor > 0) {
        val block = defragged.getOrNull(cursor)
        if ((block == null) || (block is Block.Free) || (visited.contains(block))) {
            cursor -= 1
            continue
        }
        val file = block as Block.File

        // Find the leftmost place in the defragged disk where we can fit the file
        defragged
                .withIndex()
                .find {
                    (it.index < cursor) && (it.value is Block.Free) && (it.value.size >= file.size)
                }
                ?.let { (newPosition, freeBlock) ->
                    // We found a place to move the file to
                    check(freeBlock is Block.Free)
                    check(newPosition < cursor)

                    // 1. Remove the file from the original spot
                    val removedFile = defragged.removeAt(cursor)
                    check(removedFile == file)

                    // 2. Update the block in the new position
                    val newFreeSize = freeBlock.size - file.size
                    if (newFreeSize == 0) {
                        // We can just replace the block in the position with the file
                        defragged[newPosition] = file
                    } else {
                        // We need to leave a new free block with the new free size in its place
                        defragged[newPosition] = Block.Free(size = newFreeSize)
                        // And we can now add the file right before that
                        defragged.add(newPosition, file)
                        // Make sure the cursor is still pointing to the block to the right of the
                        // original file
                        cursor += 1
                    }

                    // 3. Leave a free block in the old position, coalescing with surrounding free
                    // blocks

                    // At this point the cursor points at the block right after the moved file
                    // Check if we have a Free block right after the position
                    val oldRightFreeBlock = defragged.getOrNull(cursor)
                    var isRightFree = oldRightFreeBlock is Block.Free
                    val newOldFreeBlock =
                            if (isRightFree) {
                                Block.Free((oldRightFreeBlock as Block.Free).size + file.size)
                            } else {
                                Block.Free(size = file.size)
                            }

                    // Check the file right before the cursor in case we can coalesce the files
                    val oldLeftFreeBlock = defragged[cursor - 1]
                    if (oldLeftFreeBlock is Block.Free) {
                        // We can coalesce both elements into a single one and insert it
                        defragged[cursor - 1] =
                                Block.Free(size = oldLeftFreeBlock.size + newOldFreeBlock.size)
                        // We can remove the other free block at the cursor if it exists
                        if (isRightFree) {
                            defragged.removeAt(cursor)
                        }
                    } else {
                        if (isRightFree) {
                            // We need to make sure to update the old location
                            defragged[cursor] = newOldFreeBlock
                        } else {
                            // There's no coalescing possible, we just add a new free block
                            defragged.add(cursor, newOldFreeBlock)
                        }
                    }
                }
        // Mark this file as visited
        visited.add(file)

        // NOTE(alvaro): We don't update the cursor if we moved a file, since
        // the new file to check will be shifted into the current cursor position
        // automatically
    }

    return defragged
}

fun prettyDisk(disk: List<Block>): String {
    return disk
            .map { block ->
                when (block) {
                    is Block.Free -> ".".repeat(block.size)
                    is Block.File -> block.id.toString().repeat(block.size)
                }
            }
            .joinToString("")
}

fun checksum(disk: List<Block>): Long {
    var position = 0L
    var checksum = 0L
    for (block in disk) {
        if (block is Block.File) {
            check(block.id >= 0) // In case we get into i32 limits
            checksum += (0 until block.size).sumOf { (it + position) * block.id }
        }
        position += block.size
    }
    return checksum
}
