package dev.theotime.niagara

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml

/**
 * The home screen layout that hosts both the round Niagara Button (`large_fab`) and its
 * extended pill (`large_fab_extended`).
 */
private const val HOME_LAYOUT = "res/layout/home_activity.xml"

/** The two views the button is drawn with, depending on the configured action. */
private val BUTTON_VIEW_IDS = listOf("large_fab", "large_fab_extended")

/**
 * In Niagara, both button views are anchored to `@id/guideline_end`, a vertical guideline positioned
 * `@dimen/alphabet_wave_width` from the right edge, which is where the alphabet wave lives.
 *
 * Mirroring the anchor to the start edge and reserving the same column on the left moves the button
 * without touching any code: nothing in `HomeActivity` re-anchors these views at runtime.
 */
val niagaraButtonOnLeftPatch = patch(
    name = "Niagara Button on the left",
    description = "Anchors the Niagara Button to the left edge of the home screen instead of the right edge."
) {
    compatibleWith(Constants.NIAGARA)

    execute {
        resourceXml(HOME_LAYOUT).use { xml ->
            val buttons = xmlnsButtons(xml)

            buttons.forEach { button ->
                // The right edge anchor is what places the button above the alphabet wave.
                button.removeAttribute("app:layout_constraintEnd_toEndOf")
                button.setAttribute("app:layout_constraintStart_toStartOf", "parent")
                // Keep the same distance from the left edge the guideline used on the right.
                button.setAttribute("android:layout_marginStart", "@dimen/alphabet_wave_width")
            }
        }
    }
}

private fun xmlnsButtons(xml: app.morphe.patcher.xml.ResourceXml) =
    xml.elements()
        .filter { element ->
            val id = element.attribute("android:id") ?: return@filter false
            BUTTON_VIEW_IDS.any { id.endsWith("/$it") }
        }
        .toList()
        .also { buttons ->
            if (buttons.size != BUTTON_VIEW_IDS.size) {
                throw PatchException(
                    "$HOME_LAYOUT has ${buttons.size} of the ${BUTTON_VIEW_IDS.size} Niagara Button views " +
                        "(${BUTTON_VIEW_IDS.joinToString()}). This version of Niagara Launcher is not supported."
                )
            }
        }
