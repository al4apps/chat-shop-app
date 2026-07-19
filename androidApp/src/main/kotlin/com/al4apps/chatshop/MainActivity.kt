package com.al4apps.chatshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.al4apps.chatshop.root.DefaultRootComponent
import com.arkivanov.decompose.retainedComponent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(componentContext)
        }

        setContent {
            App(root)
        }
    }
}
