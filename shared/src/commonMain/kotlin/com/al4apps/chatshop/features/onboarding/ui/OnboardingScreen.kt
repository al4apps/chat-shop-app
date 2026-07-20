package com.al4apps.chatshop.features.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.onboarding_next
import chatshop.shared.generated.resources.onboarding_page1_text
import chatshop.shared.generated.resources.onboarding_page1_title
import chatshop.shared.generated.resources.onboarding_page2_text
import chatshop.shared.generated.resources.onboarding_page2_title
import chatshop.shared.generated.resources.onboarding_page3_text
import chatshop.shared.generated.resources.onboarding_page3_title
import chatshop.shared.generated.resources.onboarding_start
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppPrimaryButton
import com.al4apps.chatshop.features.onboarding.OnboardingComponent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class OnboardingPage(
    val icon: ImageVector,
    val title: StringResource,
    val text: StringResource,
)

@Composable
fun OnboardingScreen(
    component: OnboardingComponent,
    modifier: Modifier = Modifier,
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                icon = AppIcons.Chat,
                title = Res.string.onboarding_page1_title,
                text = Res.string.onboarding_page1_text,
            ),
            OnboardingPage(
                icon = AppIcons.Mic,
                title = Res.string.onboarding_page2_title,
                text = Res.string.onboarding_page2_text,
            ),
            OnboardingPage(
                icon = AppIcons.CheckCircle,
                title = Res.string.onboarding_page3_title,
                text = Res.string.onboarding_page3_text,
            ),
        )
    }
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(16.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }
        PagerDots(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppPrimaryButton(
            text = stringResource(
                if (isLastPage) Res.string.onboarding_start else Res.string.onboarding_next,
            ),
            onClick = {
                if (isLastPage) {
                    component.onFinishClick()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            tint = AppTheme.colors.accent,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(page.title),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(page.text),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PagerDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage) {
                            AppTheme.colors.accent
                        } else {
                            AppTheme.colors.divider
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}
