package com.al4apps.chatshop.features.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.auth_stub_login
import chatshop.shared.generated.resources.auth_stub_title
import com.al4apps.chatshop.features.auth.AuthComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthScreen(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.auth_stub_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Button(onClick = component::onLoginClick) {
            Text(text = stringResource(Res.string.auth_stub_login))
        }
    }
}
