package com.brainheap.android.ui.worddetail

import com.brainheap.android.model.Item
import java.util.regex.Pattern

class HtmlTextBuilder(val item: Item?) {

    companion object {
        @JvmStatic
        val TRANSLATION_SEPARATOR = "///"
    }

    fun process(): String? =
        convertToHtml(splitDescription())
            .let { it.description + it.translation }

    private fun convertToHtml(src: SplitDescription): SplitDescription =
        SplitDescription(convertDescriptionToHtml(src.description), convertTranslationToHtml(src.translation))

    private fun convertDescriptionToHtml(description: String?): String {
        var result = description ?: ""
        for (fragment in findAllFragmentsToReplace(item?.title, result).asReversed()) {
            result = "${result.substring(0, fragment.start)}<i><b>${fragment.text}</b></i>${result.substring(fragment.end, result.length)}"
        }
        return result
    }

    private fun convertTranslationToHtml(translation: String?): String =
        translation
            ?.takeIf { it.isNotEmpty() }
            ?.let { "<br><br><i><small>$it</small><i>" }
            ?: ""

    private fun findAllFragmentsToReplace(title: String?, description: String?): List<ReplaceFragment> {
        val result = ArrayList<ReplaceFragment>()
        title?.split(" ")
            ?.forEach { it ->
                val matcher = Pattern.compile("(?i)$it").matcher(description)
                while (matcher.find()) {
                    result.add(ReplaceFragment(matcher.start(), matcher.end(), matcher.group()))
                }
            }
        return result.sortedWith(Comparator { first, second ->
            when {
                first.start > second.start -> 1
                first.start < second.start -> -1
                else -> 0
            }
        })
    }

    private fun splitDescription(): SplitDescription {
        val src = item?.description
        return src
            ?.indexOf(TRANSLATION_SEPARATOR)
            ?.takeIf { it > 0 && it < src.length - TRANSLATION_SEPARATOR.length }
            ?.let {
                SplitDescription(
                    src.substring(0, it),
                    src.substring(it + TRANSLATION_SEPARATOR.length, src.length)
                )
            }
            ?: SplitDescription(src, "")
    }

    private class SplitDescription(var description: String?, var translation: String?)

    private class ReplaceFragment(val start: Int, val end: Int, val text: String)
}
