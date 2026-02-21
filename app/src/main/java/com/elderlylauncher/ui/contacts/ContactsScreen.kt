package com.elderlylauncher.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ripple
import coil.compose.AsyncImage
import com.elderlylauncher.R
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun QuickContactsSection(
    contacts: List<QuickContact>,
    onCallContact: (QuickContact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.contacts_quick_call),
            style = MaterialTheme.typography.labelLarge,
            color = LauncherColors.Gray500,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        if (contacts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LauncherColors.Gray50)
                    .border(2.dp, LauncherColors.Gray200, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contacts_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LauncherColors.Gray400
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                contacts.take(3).forEach { contact ->
                    QuickContactCard(
                        contact = contact,
                        onClick = { onCallContact(contact) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if less than 3 contacts
                repeat(3 - contacts.size.coerceAtMost(3)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuickContactCard(
    contact: QuickContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callDescription = stringResource(R.string.contacts_call_confirm, contact.name)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LauncherColors.Green50)
            .border(2.dp, LauncherColors.Green200, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = LauncherColors.Green500),
                onClick = onClick
            )
            .padding(16.dp)
            .semantics { contentDescription = callDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, LauncherColors.Green500, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = contact.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Green600,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = contact.name,
            style = MaterialTheme.typography.titleMedium,
            color = LauncherColors.Green700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Relation
        if (contact.relation.isNotEmpty()) {
            Text(
                text = contact.relation,
                style = MaterialTheme.typography.labelMedium,
                color = LauncherColors.Green600,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ContactListItem(
    contact: QuickContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val itemDescription = "${contact.name}, ${contact.phoneNumber}"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Gray50)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = LauncherColors.Green500),
                onClick = onClick
            )
            .padding(16.dp)
            .semantics { contentDescription = itemDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(LauncherColors.Blue100),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = contact.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = LauncherColors.Blue600,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and number
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = LauncherColors.Gray800
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.Gray500
            )
        }

        // Trailing content (e.g., call button)
        if (trailing != null) {
            trailing()
        } else {
            // Default call icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LauncherColors.Green500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = stringResource(R.string.action_call),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ContactPickerDialog(
    contacts: List<QuickContact>,
    onContactSelected: (QuickContact) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.contacts_picker_title),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = { onContactSelected(contact) },
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.action_add),
                                tint = LauncherColors.Blue500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
