package com.elderlylauncher.ui.photos

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.elderlylauncher.R
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val SLIDESHOW_INTERVAL_MS = 4_000L

data class PhotoItem(
    val uri: Uri,
    val isFromSettings: Boolean = false
)

@Composable
fun PhotoCarouselScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val carouselPhotos by viewModel.carouselPhotos.collectAsState()
    var recentPhotos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reloadToken++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(reloadToken) {
        recentPhotos = loadRecentPhotos(context, 40)
    }

    // Favorites chosen in settings become the slideshow; otherwise recent gallery photos.
    val allPhotos = remember(carouselPhotos, recentPhotos) {
        if (carouselPhotos.isNotEmpty()) {
            carouselPhotos.map { uriString ->
                PhotoItem(uri = Uri.parse(uriString), isFromSettings = true)
            }
        } else {
            recentPhotos
        }
    }

    LaunchedEffect(allPhotos.size) {
        if (allPhotos.isEmpty()) {
            currentIndex = 0
        } else if (currentIndex >= allPhotos.size) {
            currentIndex = 0
        }
    }

    LaunchedEffect(isPlaying, allPhotos.size, currentIndex) {
        if (!isPlaying || allPhotos.size <= 1) return@LaunchedEffect
        delay(SLIDESHOW_INTERVAL_MS)
        currentIndex = (currentIndex + 1) % allPhotos.size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.photos_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(
                    if (isPlaying && allPhotos.size > 1) R.string.photos_subtitle_playing
                    else R.string.photos_subtitle
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
        }

        if (allPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = LauncherColors.Gray300,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.photos_empty),
                        style = MaterialTheme.typography.titleLarge,
                        color = LauncherColors.Gray500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.photos_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val photo = allPhotos[currentIndex.coerceIn(0, allPhotos.lastIndex)]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    PhotoPage(photo = photo)
                }

                if (allPhotos.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${currentIndex + 1} / ${allPhotos.size}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = LauncherColors.Gray600,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                currentIndex = if (currentIndex > 0) {
                                    currentIndex - 1
                                } else {
                                    allPhotos.lastIndex
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LauncherColors.Gray800
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.photos_previous),
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) {
                                    LauncherColors.Orange500
                                } else {
                                    LauncherColors.Green500
                                }
                            )
                        ) {
                            Text(
                                text = stringResource(
                                    if (isPlaying) R.string.photos_pause_short
                                    else R.string.photos_play_short
                                ),
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = {
                                currentIndex = (currentIndex + 1) % allPhotos.size
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LauncherColors.Gray800
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.photos_next),
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPage(photo: PhotoItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.Gray100),
        contentAlignment = Alignment.Center
    ) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build()
        )

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        if (photo.isFromSettings) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LauncherColors.Green500)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "★",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Load recent photos from the device media store
 */
suspend fun loadRecentPhotos(context: Context, limit: Int): List<PhotoItem> = withContext(Dispatchers.IO) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        return@withContext emptyList()
    }

    val photos = mutableListOf<PhotoItem>()

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

        var count = 0
        while (cursor.moveToNext() && count < limit) {
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            photos.add(PhotoItem(uri = contentUri, isFromSettings = false))
            count++
        }
    }

    photos
}
