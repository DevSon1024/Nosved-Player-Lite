package com.devson.nvplayerlite.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devson.nvplayerlite.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var animateTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_credits),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CreditsHeroCard()

            AnimatedVisibility(
                visible = animateTrigger,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_open_source_libraries),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    val libraries = remember { getOpenSourceLibraries() }
                    libraries.forEach { library ->
                        LibraryCard(context = context, library = library)
                    }
                }
            }
        }
    }
}

@Composable
fun CreditsScreen(onBack: () -> Unit) = CreditScreen(onBack = onBack)

@Composable
private fun CreditsHeroCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = "Open Source Acknowledgements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Nosved Player Lite is proudly built on top of amazing open source technologies. We are incredibly grateful to the global developer community for making these libraries freely available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun LibraryCard(
    context: Context,
    library: OpenSourceLibrary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openUrl(context, library.url) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = library.version,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = library.license,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.about_open),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = library.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No browser found to open link", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}

data class OpenSourceLibrary(
    val name: String,
    val version: String,
    val license: String,
    val description: String,
    val url: String
)

private fun getOpenSourceLibraries(): List<OpenSourceLibrary> {
    return listOf(
        OpenSourceLibrary(
            name = "AndroidX Media3 (ExoPlayer)",
            version = "1.11.0",
            license = "Apache-2.0",
            description = "The powerhouse media playback engine for Android, providing extensible media streaming, caching, and playback controls.",
            url = "https://github.com/androidx/media"
        ),
        OpenSourceLibrary(
            name = "NosvedMedia",
            version = "1.0.0",
            license = "LGPL-2.1 / GPL-3.0",
            description = "High-performance software video decoder extension for Media3 built with FFmpeg 7.1 and dav1d 1.5.0 providing fallback decoding for modern codecs (HEVC 10-bit, AV1, VP9).",
            url = "https://github.com/DevSon1024/Nosved-Player-Lite"
        ),
        OpenSourceLibrary(
            name = "Jetpack Compose",
            version = "1.7.0+",
            license = "Apache-2.0",
            description = "Android's modern declarative toolkit for building responsive, performant, and delightful native user interfaces.",
            url = "https://developer.android.com/jetpack/compose"
        ),
        OpenSourceLibrary(
            name = "Material 3",
            version = "1.3.0+",
            license = "Apache-2.0",
            description = "Google's latest Material Design 3 components, theme tokens, and dynamic coloring for Android.",
            url = "https://m3.material.io"
        ),
        OpenSourceLibrary(
            name = "Room Database",
            version = "2.7.0",
            license = "Apache-2.0",
            description = "SQLite object-mapping abstraction library providing robust, type-safe persistence for watch history and metadata caching.",
            url = "https://developer.android.com/training/data-storage/room"
        ),
        OpenSourceLibrary(
            name = "Coil",
            version = "2.6.0",
            license = "Apache-2.0",
            description = "Fast, lightweight image and video frame thumbnail loading library for Android powered by Kotlin Coroutines.",
            url = "https://github.com/coil-kt/coil"
        ),
        OpenSourceLibrary(
            name = "Kotlin Coroutines",
            version = "1.9.0",
            license = "Apache-2.0",
            description = "Lightweight concurrency framework and reactive Flow pipelines powering smooth UI and asynchronous I/O.",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        OpenSourceLibrary(
            name = "Reorderable",
            version = "3.1.0",
            license = "MIT",
            description = "Smooth drag-and-drop list and grid reordering library designed specifically for Jetpack Compose.",
            url = "https://github.com/Calvin-Sh/Reorderable"
        ),
        OpenSourceLibrary(
            name = "DataStore Preferences",
            version = "1.0.0",
            license = "Apache-2.0",
            description = "Modern, safe, and asynchronous key-value storage solution replacing SharedPreferences with Coroutine Flow integration.",
            url = "https://developer.android.com/topic/libraries/architecture/datastore"
        ),
        OpenSourceLibrary(
            name = "DocumentFile",
            version = "1.0.1",
            license = "Apache-2.0",
            description = "Utility library for working with document trees and Storage Access Framework (SAF) file management.",
            url = "https://developer.android.com/jetpack/androidx/releases/documentfile"
        ),
        OpenSourceLibrary(
            name = "AndroidX Core KTX",
            version = "1.17.0",
            license = "Apache-2.0",
            description = "Essential Kotlin extensions providing concise, idiomatic wrappers around Android platform APIs.",
            url = "https://developer.android.com/jetpack/androidx"
        )
    )
}

