package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val staffShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(QuizTokens.cornerSmall),
    medium = RoundedCornerShape(QuizTokens.cornerMedium),
    large = RoundedCornerShape(QuizTokens.cornerLarge),
    extraLarge = RoundedCornerShape(QuizTokens.cornerExtraLarge),
)

@Composable
fun QuizStaffTheme(content: @Composable () -> Unit) {
    val fonts = rememberQuizFonts()
    MaterialTheme(
        colorScheme = QuizColors.staffDark(),
        typography = QuizTypography.material(fonts.fontFamily),
        shapes = staffShapes,
    ) {
        QuizFontGate(fonts.isReady, content)
    }
}
