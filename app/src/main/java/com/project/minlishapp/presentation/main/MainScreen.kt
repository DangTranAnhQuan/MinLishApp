package com.project.minlishapp.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.minlishapp.R
import com.project.minlishapp.core.navigation.MainNavGraph
import com.project.minlishapp.core.navigation.Screen

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Pair(Screen.MainDashboard, R.drawable.ic_nav_dashboard),
        Pair(Screen.MainDecks, R.drawable.ic_nav_decks),
        Pair(Screen.MainPractice, R.drawable.ic_nav_practice),
        Pair(Screen.MainProfile, R.drawable.ic_nav_profile)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    items.forEach { (screen, iconRes) ->
                        val selected = currentRoute == screen.route || 
                                     (currentRoute == null && screen == Screen.MainDashboard)
                        BottomNavItem(
                            screen = screen,
                            iconRes = iconRes,
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    bottomNavController.navigate(screen.route) {
                                        popUpTo(bottomNavController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
{ innerPadding ->
        MainNavGraph(
            navController = bottomNavController,
            rootNavController = rootNavController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavItem(
    screen: Screen,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        if (selected) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xff1a73e8))
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = screen.title,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = screen.title ?: "",
                color = Color(0xff1a73e8),
                lineHeight = 1.5.em,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(40.dp)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = screen.title,
                    colorFilter = ColorFilter.tint(Color(0xff64748b)),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = screen.title ?: "",
                color = Color(0xff64748b),
                lineHeight = 1.5.em,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
