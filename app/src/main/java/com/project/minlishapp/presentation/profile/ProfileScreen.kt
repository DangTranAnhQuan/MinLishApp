package com.project.minlishapp.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.minlishapp.R
import com.project.minlishapp.presentation.components.AppDropdownField
import com.project.minlishapp.presentation.components.AppTextField
import com.project.minlishapp.presentation.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAvatarDialog by remember { mutableStateOf(false) }
    //Start
//    ProfileContent(
//        uiState = uiState,
//        onNameChange = viewModel::onNameChange,
//        onLearningTargetChange = viewModel::onLearningTargetChange,
//        onCurrentLevelChange = viewModel::onCurrentLevelChange,
//        onProfilePictureChange = viewModel::onProfilePictureChange,
//        onSaveChanges = viewModel::updateUserProfile,
//        onLogoutClick = {
//            viewModel.logout()
//            onLogoutClick()
//        },
//        modifier = modifier
//    )
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProfileContent(
//    uiState: ProfileUiState,
//    onNameChange: (String) -> Unit,
//    onLearningTargetChange: (String) -> Unit,
//    onCurrentLevelChange: (String) -> Unit,
//    onProfilePictureChange: (String) -> Unit,
//    onSaveChanges: () -> Unit,
//    onLogoutClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var showAvatarDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccessBanner = true
            kotlinx.coroutines.delay(3000)
            showSuccessBanner = false
        }
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { avatarResName: String ->
                viewModel.onProfilePictureChange(avatarResName)
//            onAvatarSelected = { avatarResName ->
//                onProfilePictureChange(avatarResName)
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xff1e293b)
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    style = TextStyle(fontSize = 16.sp, color = Color(0xff64748b))
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogoutClick()
                    }
                ) {
                    Text(
                        text = "Log Out",
                        color = Color(0xffef4444),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = Color(0xff64748b),
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Profile Settings",
                            color = Color(0xff1e293b),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    windowInsets = WindowInsets(0)
                )
                
                if (showSuccessBanner) {
                    Surface(
                        color = Color(0xffecfdf5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xff10b981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Profile updated successfully!",
                                color = Color(0xff065f46),
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp)
            ) {
                item {
                    ProfileAvatar(
                        uiState = uiState,
                        onCameraClick = { showAvatarDialog = true }
                    )
                }
                
                item {
                    ProfileForm(
                        uiState = uiState,
                        onNameChange = { viewModel.onNameChange(it) },
                        onLearningTargetChange = { viewModel.onLearningTargetChange(it) },
                        onCurrentLevelChange = { viewModel.onCurrentLevelChange(it) }
//                        onNameChange = onNameChange,
//                        onLearningTargetChange = onLearningTargetChange,
//                        onCurrentLevelChange = onCurrentLevelChange
                    )
                }
                
                item {
                    ProfileActions(
                        uiState = uiState,
                        onSaveChanges = { viewModel.updateUserProfile() },
//                        onSaveChanges = onSaveChanges,
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    uiState: ProfileUiState,
    onCameraClick: () -> Unit
) {
    val context = LocalContext.current
    val avatarResId = remember(uiState.profilePictureUrl) {
        if (!uiState.profilePictureUrl.isNullOrEmpty()) {
            context.resources.getIdentifier(uiState.profilePictureUrl, "drawable", context.packageName)
        } else 0
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = Color(0xFF2BDCEE),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                painter = painterResource(id = if (avatarResId != 0) avatarResId else R.drawable.avatar_01),
                contentDescription = "Profile Avatar",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xff1a73e8))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onCameraClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Change Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = uiState.name.ifEmpty { "User Name" },
            color = Color(0xff1e293b),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.email,
            color = Color(0xff64748b).copy(alpha = 0.7f),
            style = TextStyle(fontSize = 14.sp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = Color(0xfff1f5f9),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Overall Accuracy: ${String.format("%.1f", uiState.overallAccuracy)}%",
                color = Color(0xff0f172a),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ProfileForm(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onLearningTargetChange: (String) -> Unit,
    onCurrentLevelChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = Modifier.fillMaxWidth()
    ) {
        FormSection(label = "Full Name") {
            AppTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                placeholder = "Enter your full name",
                leadingIconRes = R.drawable.ic_fullname
            )
        }

        FormSection(label = "Learning Target") {
            AppDropdownField(
                value = uiState.learningTarget,
                placeholder = "Select Learning Target",
                options = uiState.learningTargets,
                onOptionSelected = onLearningTargetChange,
                leadingIconRes = R.drawable.ic_toeic
            )
        }

        FormSection(label = "Current Level") {
            AppDropdownField(
                value = uiState.currentLevel,
                placeholder = "Select Current Level",
                options = uiState.levels.map { it.code },
                onOptionSelected = onCurrentLevelChange,
                leadingIconRes = R.drawable.ic_career
            )
        }
    }
}

@Composable
private fun FormSection(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = Color(0xff64748b),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun ProfileActions(
    uiState: ProfileUiState,
    onSaveChanges: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        uiState.errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Save Changes",
            onClick = onSaveChanges,
            isEnabled = !uiState.isLoading && uiState.name.trim().isNotEmpty(),
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLogoutClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Log Out",
                tint = Color(0xffef4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                color = Color(0xffef4444),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    val avatars = listOf(
        "avatar_01",
        "avatar_02",
        "avatar_03",
        "avatar_04",
        "avatar_05",
        "avatar_06",
        "avatar_07",
        "avatar_08",
        "avatar_09",
        "avatar_10",
        "avatar_11",
        "avatar_12",
        "avatar_13",
        "avatar_14"
    )

    var temporarySelectedAvatar by remember { mutableStateOf("avatar_01") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Avatar",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xff1e293b)
                )
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(avatars.size) { index ->
                    val resName = avatars[index]
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)

                    val isSelected = temporarySelectedAvatar == resName

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xfff1f5f9) else Color.Transparent)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xff1a73e8) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                temporarySelectedAvatar = resName
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.avatar_01),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAvatarSelected(temporarySelectedAvatar)
                    onDismiss()
                }
            ) {
                Text(
                    text = "Save",
                    color = Color(0xff1a73e8),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color(0xff64748b),
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
        //Start
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    ProfileContent(
//        uiState = ProfileUiState(
//            name = "Alex Robinson",
//            email = "alex.robinson@example.com",
//            learningTarget = "Business Communication",
//            currentLevel = "B1"
//        ),
//        onNameChange = {},
//        onLearningTargetChange = {},
//        onCurrentLevelChange = {},
//        onProfilePictureChange = {},
//        onSaveChanges = {},
//        onLogoutClick = {}
//        End
    )
}
