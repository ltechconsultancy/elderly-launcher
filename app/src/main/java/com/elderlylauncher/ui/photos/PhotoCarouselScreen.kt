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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.elderlylauncher.R
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhotoItem(
    val uri: Uri,
    val isFromSettings: Boolean = false
)

@Composable
fun PhotoCarouselScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val carouselPhotos by viewModel.carouselPhotos.collectAsState()
    var recentPhotos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Load recent photos from device
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            recentPhotos = loadRecentPhotos(context, 20)
        }
    }

    // Combine settings photos with recent photos
    val allPhotos = remember(carouselPhotos, recentPhotos) {
        val settingsPhotoItems = carouselPhotos.map { uriString ->
            PhotoItem(uri = Uri.parse(uriString), isFromSettings = true)
        }
        // Combine and remove duplicates (settings photos first)
        val combinedList = settingsPhotoItems + recentPhotos.filter { recentPhoto ->
            !carouselPhotos.contains(recentPhoto.uri.toString())
        }
        combinedList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.photos_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(R.string.photos_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 18.sp
            )
        }

        if (allPhotos.isEmpty()) {
            // Empty state
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
            // Photo carousel
            val pagerState = rememberPagerState(pageCount = { allPhotos.size })

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Main photo pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                ) { page ->
                    PhotoPage(photo = allPhotos[page])
                }

                // Navigation arrows
                if (allPhotos.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous button
                        val coroutineScopeNav = rememberCoroutineScope()
                        IconButton(
                            onClick = {
                                coroutineScopeNav.launch {
                                    if (pagerState.currentPage > 0) {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage > 0,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage > 0)
                                        Color.Black.copy(alpha = 0.5f)
                                    else
                                        Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = stringResource(R.string.photos_previous),
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Next button
                        IconButton(
                            onClick = {
                                coroutineScopeNav.launch {
                                    if (pagerState.currentPage < allPhotos.size - 1) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage < allPhotos.size - 1,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage < allPhotos.size - 1)
                                        Color.Black.copy(alpha = 0.5f)
                                    else
                                        Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.photos_next),
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Page indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${allPhotos.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp
                        )
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

        // Show badge if from settings
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
    // Check permission
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
