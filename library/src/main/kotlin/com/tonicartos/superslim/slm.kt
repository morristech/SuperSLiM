package com.tonicartos.superslim

import android.support.annotation.VisibleForTesting
import com.tonicartos.superslim.adapter.HeaderStyle
import com.tonicartos.superslim.internal.SectionState

interface SectionLayoutManager<T : SectionState> {
    fun onLayout(helper: LayoutHelper, section: T)
    fun fillTopScrolledArea(dy: Int, helper: LayoutHelper, section: T): Int
    fun fillBottomScrolledArea(dy: Int, helper: LayoutHelper, section: T): Int
}

interface Child {
    companion object {
        const val INVALID = -1
        const val ANIM_NONE = 0
        const val ANIM_APPEARING = 1
        const val ANIM_DISAPPEARING = 2
    }

    fun done()

    /**
     * True if the child is being removed in this layout.
     */
    val isRemoved: Boolean

    val measuredWidth: Int
    val measuredHeight: Int
    fun measure(usedWidth: Int = 0, usedHeight: Int = 0)

    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
    fun layout(left: Int = 0, top: Int = 0) = layout(left, top, 0, 0)
    fun layout(left: Int, top: Int, right: Int, bottom: Int)

    val width: Int
    val height: Int

    /**
     * The animation state for the child in this layout pass. An appearing child is one that will start offscreen and animate
     * onscreen. A disappearing child is the opposite. A normal child does neither. Valid values are per the
     * [AnimationState] annotation.
     */
    @AnimationState var animationState: Int

    /**
     * Adds child to the recycler view. Handles disappearing or appearing state per value set in [animationState].
     */
    fun addToRecyclerView() = addToRecyclerView(-1)

    /**
     * Adds child to the recycler view.
     */
    fun addToRecyclerView(i: Int)
}

/**
 * Configuration of a section.
 */
abstract class SectionConfig(gutterStart: Int = SectionConfig.DEFAULT_GUTTER, gutterEnd: Int = SectionConfig.DEFAULT_GUTTER,
                             @HeaderStyle var headerStyle: Int = SectionConfig.DEFAULT_HEADER_STYLE) {
    var gutterStart = 0
        get() = field
        set(value) {
            field = if (value < 0) GUTTER_AUTO else value
        }
    var gutterEnd = 0
        get() = field
        set(value) {
            field = if (value < 0) GUTTER_AUTO else value
        }

    init {
        this.gutterStart = gutterStart
        this.gutterEnd = gutterEnd
    }

    // Remap names since internally left and right are used since section coordinates are LTR, TTB. The start and
    // end intention will be applied correctly (from left and right) through the config transformations.
    internal var gutterLeft: Int
        get() = gutterStart
        set(value) {
            gutterStart = value
        }
    internal var gutterRight: Int
        get() = gutterEnd
        set(value) {
            gutterEnd = value
        }

    internal fun makeSection(oldState: SectionState? = null) = onMakeSection(oldState)
    abstract protected fun onMakeSection(oldState: SectionState?): SectionState

    /**
     * Copy the configuration. Section configs are always copied when they are passed to the layout manager.
     */
    fun copy(): SectionConfig {
        return onCopy()
    }

    abstract protected fun onCopy(): SectionConfig

    companion object {
        /**
         * Header is positioned at the head of the section content. Content starts below the header. Inline headers
         * are always sticky. Use the embedded style if you want an inline header that is not sticky.
         */
        const val HEADER_INLINE = 1

        /**
         * Header is positioned at the head of the section content. Content starts below the header, but the header
         * never becomes sticky. Embedded headers can not float and ignores that flag if set.
         */
        const val HEADER_EMBEDDED = 1 shl 1

        /**
         * Header is placed inside the gutter at the start edge of the section. This is the left for LTR locales.
         * Gutter headers are always sticky.
         */
        const val HEADER_START = 1 shl 2

        /**
         * Header is placed inside the gutter at the end edge of the section. This is the right for LTR locales.
         * Gutter headers are always sticky. Overridden
         */
        const val HEADER_END = 1 shl 3

        /**
         * Float header above the content. Floating headers are always sticky.
         */
        const val HEADER_FLOAT = 1 shl 4

        /**
         * Header is placed at the tail of the section. If sticky, it will stick to the bottom edge rather than the
         * top. Combines with all other options.
         */
        const val HEADER_TAIL = 1 shl 5

        const val GUTTER_AUTO = -1

        internal const val DEFAULT_GUTTER = GUTTER_AUTO
        internal const val DEFAULT_HEADER_STYLE = HEADER_INLINE
    }

    /****************************************************
     * Test access
     ****************************************************/
    interface TestAccess {
        fun makeSection(): SectionState
    }

    @VisibleForTesting
    val testAccess = object : TestAccess {
        override fun makeSection(): SectionState = this@SectionConfig.makeSection()
    }
}
