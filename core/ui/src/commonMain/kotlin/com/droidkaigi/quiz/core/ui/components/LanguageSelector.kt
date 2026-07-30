package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.locale_english
import com.droidkaigi.quiz.core.ui.generated.resources.locale_japanese
import com.droidkaigi.quiz.core.ui.generated.resources.locale_system
import com.droidkaigi.quiz.core.ui.locale.AppLocalePreference
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageSelector(
    selected: AppLocalePreference,
    onSelect: (AppLocalePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = AppLocalePreference.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, preference ->
            SegmentedButton(
                selected = selected == preference,
                onClick = { onSelect(preference) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(text = preference.label())
            }
        }
    }
}

@Composable
private fun AppLocalePreference.label(): String = when (this) {
    AppLocalePreference.System -> stringResource(Res.string.locale_system)
    AppLocalePreference.Japanese -> stringResource(Res.string.locale_japanese)
    AppLocalePreference.English -> stringResource(Res.string.locale_english)
}
