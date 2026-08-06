package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.noto_sans_jp
import org.jetbrains.compose.resources.Font

private val themeWeights = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
)

@Composable
actual fun quizFontFamily(): FontFamily? = FontFamily(
    themeWeights.map { weight ->
        // One variable font file covers every weight through the wght axis.
        Font(
            resource = Res.font.noto_sans_jp,
            weight = weight,
            style = FontStyle.Normal,
            variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
        )
    },
)
