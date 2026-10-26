package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.common_back
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Топбар экрана. Принадлежит конкретному экрану (не общий), размещается
 * экраном под его собственными insets.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val dividerColor = AppTheme.colors.divider
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke,
                )
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            AppIconButton(
                icon = AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.common_back),
                onClick = onBackClick,
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = AppTheme.typography.title,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}
