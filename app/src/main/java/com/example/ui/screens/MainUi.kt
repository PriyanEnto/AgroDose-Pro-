package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.utils.CalculatorUtils
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen = viewModel.currentScreen

    // Handle system back press
    BackHandler(enabled = currentScreen != Screen.Home) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "AgroDose Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Pesticide & AI Formulation Calculator",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { if (currentScreen != Screen.Home) viewModel.navigateBack() }) {
                            Icon(
                                imageVector = if (currentScreen != Screen.Home) Icons.Default.ArrowBack else Icons.Default.Science,
                                contentDescription = if (currentScreen != Screen.Home) "Navigate Back" else "Agri Science Logo",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (currentScreen == Screen.Home) {
                                    viewModel.navigateTo(Screen.History)
                                } else {
                                    viewModel.navigateToHome()
                                }
                            },
                            modifier = Modifier.testTag(if (currentScreen == Screen.Home) "history_button" else "home_button")
                        ) {
                            Icon(
                                imageVector = if (currentScreen == Screen.Home) Icons.Default.History else Icons.Default.Home,
                                contentDescription = if (currentScreen == Screen.Home) "View Saved Calculations" else "Go to Home",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Home -> HomeScreen(viewModel)
                    is Screen.Input -> InputScreen(viewModel, targetScreen.mode)
                    is Screen.Result -> ResultScreen(
                        viewModel = viewModel,
                        title = targetScreen.title,
                        mode = targetScreen.mode,
                        input = targetScreen.input,
                        output = targetScreen.output,
                        historyId = targetScreen.historyId
                    )
                    is Screen.History -> HistoryScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header subtitle in body is not needed as it's elegantly in the TopAppBar now,
        // so we jump directly into the 2x2 quick task selector from the design mockup.
        
        item {
            Text(
                text = "Select Calculation Task",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp, top = 2.dp)
            )
        }

        // 2x2 grid structure mimicking <div class="grid grid-cols-2 gap-4">
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AgriModeCard(
                        title = "Dose to AI Delivery",
                        description = "Calculate AI concentration from known formulation",
                        icon = Icons.Default.Hub,
                        isHighlight = true,
                        onClick = {
                            viewModel.resetForm()
                            viewModel.navigateTo(Screen.Input(CalculationMode.DOSE_TO_AI))
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AgriModeCard(
                        title = "AI to Dose Calc",
                        description = "Reverse calculator for desired AI levels",
                        icon = Icons.Default.SettingsBackupRestore,
                        isHighlight = false,
                        onClick = {
                            viewModel.resetForm()
                            viewModel.navigateTo(Screen.Input(CalculationMode.AI_TO_DOSE))
                        }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AgriModeCard(
                        title = "Farmer Practice",
                        description = "Tank size & field practice adjustment",
                        icon = Icons.Default.Agriculture,
                        isHighlight = false,
                        onClick = {
                            viewModel.resetForm()
                            viewModel.navigateTo(Screen.Input(CalculationMode.FARMER_PRACTICE))
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AgriModeCard(
                        title = "Unit Converter",
                        description = "Switch between Hectare and Acre doses",
                        icon = Icons.Default.SwapCalls,
                        isHighlight = true,
                        onClick = {
                            viewModel.resetForm()
                            viewModel.navigateTo(Screen.Input(CalculationMode.UNIT_CONVERTER))
                        }
                    )
                }
            }
        }

        // Quick Stats/Disclaimer Banner from the design mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AgriBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = AgriPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Note: AI concentration is automatically rounded to the nearest full number. Always refer to product labels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AgriModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlight: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isHighlight) AgriHighlight else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isHighlight) AgriOnHighlight else MaterialTheme.colorScheme.onBackground
    val iconBgColor = if (isHighlight) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.6f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon at top left
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Text labels content at bottom
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.75f),
                    lineHeight = 13.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputScreen(viewModel: MainViewModel, mode: CalculationMode) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Task Title Banner
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                val config = getModeConfig(mode)
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = config.tint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = config.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Product Identity Segment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AgriBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Product Details",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = viewModel.productName,
                        onValueChange = { viewModel.productName = it },
                        label = { Text("Product/Formulation Name (e.g., Engage, XYZ)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input"),
                        leadingIcon = { Icon(Icons.Default.Label, null) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    if (mode == CalculationMode.DOSE_TO_AI) {
                        Column {
                            Text("Formulation Delivery Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { viewModel.formulationType = "Solo" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.formulationType == "Solo") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (viewModel.formulationType == "Solo") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Solo (1 AI)")
                                }
                                Button(
                                    onClick = { viewModel.formulationType = "Premix" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.formulationType == "Premix") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (viewModel.formulationType == "Premix") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Premix (2-3 AI)")
                                }
                            }
                        }
                    }

                    if (mode == CalculationMode.DOSE_TO_AI || mode == CalculationMode.AI_TO_DOSE) {
                        Column {
                            Text("Dose Metric Unit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ml/ha", "g/ha", "ml/acre", "g/acre").forEach { unit ->
                                    FilterChip(
                                        selected = viewModel.doseUnit == unit,
                                        onClick = { viewModel.doseUnit = unit },
                                        label = { Text(unit) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Ingredients Section
        if (mode != CalculationMode.UNIT_CONVERTER) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AgriBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Ingredients (Up to 3)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Text notice regarding rounding
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AgriSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AgriPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI % will automatically be rounded to the nearest full number for calculation (e.g., 5.66% → 6%, 28.3% → 30% based on rounding rule).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 14.sp
                            )
                        }

                        // Rounding selector
                        Column {
                            Text("AI % Rounding Mode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                mapOf(
                                    "industry" to "Industry (Case 1)",
                                    "standard" to "Standard (Nearest 1%)",
                                    "multiple_10" to "Nearest 10%"
                                ).forEach { (key, label) ->
                                    Button(
                                        onClick = { viewModel.roundingMode = key },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.roundingMode == key) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (viewModel.roundingMode == key) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(label, fontSize = 10.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Dynamic Fields based on Solo/Premix Selection
                        val maxCount = if (viewModel.formulationType == "Solo" && mode == CalculationMode.DOSE_TO_AI) 1 else 3
                        for (i in 0 until maxCount) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (maxCount > 1) {
                                    Text(
                                        text = "Active Ingredient ${i + 1}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = viewModel.aiInputs[i].name,
                                        onValueChange = { newVal ->
                                            viewModel.aiInputs[i] = viewModel.aiInputs[i].copy(name = newVal)
                                        },
                                        label = { Text("AI name") },
                                        placeholder = { Text("e.g., Spinetoram") },
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = viewModel.aiInputs[i].concentration?.toString() ?: "",
                                        onValueChange = { newVal ->
                                            val numeric = newVal.toDoubleOrNull()
                                            viewModel.aiInputs[i] = viewModel.aiInputs[i].copy(concentration = numeric)
                                        },
                                        label = { Text("AI %") },
                                        modifier = Modifier.weight(0.8f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }

                                if (mode == CalculationMode.AI_TO_DOSE) {
                                    OutlinedTextField(
                                        value = viewModel.aiInputs[i].requiredDeliveryHa?.toString() ?: "",
                                        onValueChange = { newVal ->
                                            val numeric = newVal.toDoubleOrNull()
                                            viewModel.aiInputs[i] = viewModel.aiInputs[i].copy(requiredDeliveryHa = numeric)
                                        },
                                        label = { Text("Required AI delivery per Hectare (g AI/ha)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Variable Inputs Section (Formulation Dose, Farmer Practice, or Unit Converter)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AgriBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Calculation Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )

                    when (mode) {
                        CalculationMode.DOSE_TO_AI -> {
                            OutlinedTextField(
                                value = viewModel.formulationDose,
                                onValueChange = { viewModel.formulationDose = it },
                                label = { Text("Formulation Dose (scalar quantity)") },
                                placeholder = { Text("e.g. 400") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("formulation_dose_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Scale, null) },
                                singleLine = true
                            )
                        }

                        CalculationMode.FARMER_PRACTICE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.farmerDosePerL,
                                    onValueChange = { viewModel.farmerDosePerL = it },
                                    label = { Text("Farmer dose per L") },
                                    placeholder = { Text("e.g. 2.5") },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("farmer_dose_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                Column(modifier = Modifier.weight(0.8f)) {
                                    Text("Unit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = viewModel.farmerDoseUnit == "ml/L",
                                            onClick = { viewModel.farmerDoseUnit = "ml/L" },
                                            label = { Text("ml/L") }
                                        )
                                        FilterChip(
                                            selected = viewModel.farmerDoseUnit == "g/L",
                                            onClick = { viewModel.farmerDoseUnit = "g/L" },
                                            label = { Text("g/L") }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = viewModel.tankSize,
                                onValueChange = { viewModel.tankSize = it },
                                label = { Text("Tank Size capacity (L)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tank_size_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.WaterDrop, null) },
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = viewModel.tanksPerAcre,
                                onValueChange = { viewModel.tanksPerAcre = it },
                                label = { Text("Number of tanks sprayed per Acre") },
                                placeholder = { Text("e.g., 10") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tanks_per_acre_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Waves, null) },
                                singleLine = true
                            )
                        }

                        CalculationMode.UNIT_CONVERTER -> {
                            OutlinedTextField(
                                value = viewModel.converterDoseValue,
                                onValueChange = { viewModel.converterDoseValue = it },
                                label = { Text("Dose Value to convert") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("converter_dose_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Input, null) },
                                singleLine = true
                            )

                            Column {
                                Text("Input Dose Density Unit Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { viewModel.converterSourceUnit = "ha" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.converterSourceUnit == "ha") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (viewModel.converterSourceUnit == "ha") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Per Hectare (ha)")
                                    }
                                    Button(
                                        onClick = { viewModel.converterSourceUnit = "acre" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (viewModel.converterSourceUnit == "acre") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (viewModel.converterSourceUnit == "acre") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Per Acre")
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Validation Error Segment with Transition Animation
        item {
            AnimatedVisibility(
                visible = viewModel.validationError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Logo",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.validationError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Compute Actions Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetForm() },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(52.dp)
                        .testTag("reset_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }

                Button(
                    onClick = { viewModel.runCalculation(mode) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .testTag("calculate_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculate", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    title: String,
    mode: CalculationMode,
    input: SavedCalculationInput,
    output: SavedCalculationOutput,
    historyId: Int?
) {
    val context = LocalContext.current
    var isRulesExpanded by remember { mutableStateOf(false) }
    var isHistorySaved by remember { mutableStateOf(historyId != null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header and Save Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calculated on: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isHistorySaved) {
                    Button(
                        onClick = {
                            viewModel.saveActiveCalculation(title, mode, input, output)
                            isHistorySaved = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriSecondary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("save_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Result", fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(AgriSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AgriPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saved", fontSize = 12.sp, color = AgriPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Dynamic Primary Deliveries Summary
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FORMULATION SCALE OUTCOME",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = CalculatorUtils.formatDoubleValue(output.formDoseHa),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = output.formDoseHaUnit,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "per Hectare (ha)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(60.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = CalculatorUtils.formatDoubleValue(output.formDoseAcre),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = output.formDoseAcreUnit,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "per Acre",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (mode == CalculationMode.FARMER_PRACTICE) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Formulation dose per tank: ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${CalculatorUtils.formatDoubleValue(output.formDosePerTank)} ${output.formDosePerTankUnit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // If AI to Dose and formulation dose results mismatch across ingredient requirements, display informational alert
        if (mode == CalculationMode.AI_TO_DOSE && !output.dosesMatching) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Dose Imbalance Detected in Premix",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "In a multi-AI commercial premix, separate formulation dose values are required to meet each individual target AI concentration. In real practice, the chemical application formulation dose MUST be governed strictly by the approved registered product label guidelines, or configured to satisfy the primary/target active ingredient constraint safely.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2. Detailing active ingredients delivery
        if (output.results.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Active Ingredients Delivery Metrics",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    output.results.forEach { res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = res.name,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text("Rounded AI: ${res.roundedConcentration}% (from ${res.enteredConcentration}%)")
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("DELIVERY PER HA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${CalculatorUtils.formatDoubleValue(res.deliveryHa)} g AI/ha",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("DELIVERY PER ACRE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${CalculatorUtils.formatDoubleValue(res.deliveryAcre)} g AI/acre",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                if (mode == CalculationMode.AI_TO_DOSE) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column {
                                        Text("REQUIRED FORMULATION DOSE (FOR THIS SPECIFIC AI)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Ha: ${CalculatorUtils.formatDoubleValue(res.requiredFormDoseHa)} ${output.formDoseHaUnit}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Acre: ${CalculatorUtils.formatDoubleValue(res.requiredFormDoseAcre)} ${output.formDoseAcreUnit}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Rounding details note
        if (output.roundedAiUsedNote.isNotBlank()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = output.roundedAiUsedNote,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 4. Expanded core formulas & mathematical rules
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRulesExpanded = !isRulesExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Important Rules Used for Calculation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (isRulesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isRulesExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FormulaItem("Area conversion", "1 Hectare = 2.5 Acres")
                            FormulaItem("Dose conversion to Acre", "Dose per Acre = Dose per Hectare / 2.5")
                            FormulaItem("Dose conversion to Hectare", "Dose per Hectare = Dose per Acre * 2.5")
                            FormulaItem("Active Ingredient (AI) delivery", "AI Delivery/Ha = Formulation Dose/Ha * AI % / 100\nAI Delivery/Acre = AI Delivery/Ha / 2.5")
                            FormulaItem("Reverse Dose calculation", "Formulation Dose/Ha = (Required AI Delivery/Ha * 100) / AI %")
                            FormulaItem("Farmer field practice", "Formulation per Tank = Farmer Dose/L * Tank Volume\nFormulation per Acre = Formulation per Tank * Tanks per Acre")
                        }
                    }
                }
            }
        }

        // 5. Educational and Chemical Safety Disclaimer Footnote
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Disclaimer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "This calculator is for educational and field calculation support only. Always inspect registered label recommendations and verify local regulatory safety guidelines before final compound pesticide application.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Action Buttons: Share, Reset
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.resetForm()
                        viewModel.navigateToHome()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-calculate")
                }

                Button(
                    onClick = {
                        shareCalculationAsText(context, mode, input, output)
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("share_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Result")
                }
            }
        }
    }
}

@Composable
fun FormulaItem(title: String, formula: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text(text = formula, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val history by viewModel.historyState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved Calculations Histoy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (history.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAllHistory() }) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Empty History",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "No saved calculations yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Perform pesticide formulation runs on the input screens and save them to view previous computations offline.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = { viewModel.navigateToHome() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Calculate Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.loadHistoryRecord(item) }
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val tagText = item.mode.replace("_", " ")
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tagText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteHistoryRecord(item.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete item",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Model & Styling configs
data class ModeConfig(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

fun getModeConfig(mode: CalculationMode): ModeConfig {
    return when (mode) {
        CalculationMode.DOSE_TO_AI -> ModeConfig("Dose to AI Delivery", Icons.Default.Hub, AgriPrimary)
        CalculationMode.AI_TO_DOSE -> ModeConfig("AI Delivery to Dose", Icons.Default.SettingsBackupRestore, AgriSecondary)
        CalculationMode.FARMER_PRACTICE -> ModeConfig("Farmer Practice Calculator", Icons.Default.Agriculture, AgriTertiary)
        CalculationMode.UNIT_CONVERTER -> ModeConfig("Unit Dose Converter", Icons.Default.SwapCalls, Color(0xFFE65100))
    }
}

fun shareCalculationAsText(
    context: Context,
    mode: CalculationMode,
    input: SavedCalculationInput,
    output: SavedCalculationOutput
) {
    val builder = StringBuilder()
    builder.append("Report: Pesticide Formulation Dose & AI Delivery\n")
    builder.append("====================================================\n")
    if (input.productName.isNotBlank()) {
        builder.append("Product Name: ${input.productName}\n")
    }
    builder.append("Calculation Task Mode: ${getModeConfig(mode).title}\n")
    builder.append("====================================================\n\n")

    builder.append("[INPUT DATA]\n")
    if (mode == CalculationMode.DOSE_TO_AI) {
        builder.append("- Product Dose: ${input.formulationDose} ${input.doseUnit}\n")
    } else if (mode == CalculationMode.UNIT_CONVERTER) {
        val srcName = if (input.conversionSource == "ha") "Hectare" else "Acre"
        builder.append("- Source Density: ${input.doseValue} per $srcName\n")
    } else if (mode == CalculationMode.FARMER_PRACTICE) {
        builder.append("- Farmer application dose: ${input.farmerDosePerL} ${input.doseUnit} water\n")
        builder.append("- Sprayer water capacity (tank size): ${input.tankSize} Litres\n")
        builder.append("- Spray count: ${input.tanksPerAcre} tanks per Acre\n")
    }

    if (input.ais.isNotEmpty()) {
        builder.append("- Applied Active Ingredients:\n")
        input.ais.forEachIndexed { i, item ->
            val desc = if (item.requiredDeliveryHa != null) " with Target delivery: ${item.requiredDeliveryHa} g AI/ha" else ""
            builder.append("  ${i+1}. ${item.name.ifBlank { "AI ${i+1}" }} containing ${item.concentration}% formulation concentration$desc\n")
        }
    }
    builder.append("\n")

    builder.append("[COMPLETED METRIC OUTCOMES]\n")
    builder.append("- Formulation Dose per Hectare: ${CalculatorUtils.formatDoubleValue(output.formDoseHa)} ${output.formDoseHaUnit}\n")
    builder.append("- Formulation Dose per Acre: ${CalculatorUtils.formatDoubleValue(output.formDoseAcre)} ${output.formDoseAcreUnit}\n")
    if (mode == CalculationMode.FARMER_PRACTICE) {
        builder.append("- Formulation Dose per Sprayer Tank: ${CalculatorUtils.formatDoubleValue(output.formDosePerTank)} ${output.formDosePerTankUnit}\n")
    }
    builder.append("\n")

    if (output.results.isNotEmpty()) {
        builder.append("- Active Ingredients Delivery Breakdown:\n")
        output.results.forEach { res ->
            builder.append("  * ${res.name} (Rounded calculation base count: ${res.roundedConcentration}% AI):\n")
            builder.append("    - Hectare actual delivery: ${CalculatorUtils.formatDoubleValue(res.deliveryHa)} g AI/ha\n")
            builder.append("    - Acre actual delivery: ${CalculatorUtils.formatDoubleValue(res.deliveryAcre)} g AI/acre\n")
            if (res.requiredFormDoseHa > 0) {
                builder.append("    - Specific Hectare formulation required: ${CalculatorUtils.formatDoubleValue(res.requiredFormDoseHa)} ${output.formDoseHaUnit}\n")
                builder.append("    - Specific Acre formulation required: ${CalculatorUtils.formatDoubleValue(res.requiredFormDoseAcre)} ${output.formDoseAcreUnit}\n")
            }
        }
    }

    if (mode == CalculationMode.AI_TO_DOSE && !output.dosesMatching) {
        builder.append("\n* ALERT WARNING: Computed formulation doses do not match perfectly across different active ingredients. Use label recommendations.\n")
    }

    builder.append("\n----------------------------------------------------\n")
    builder.append("Disclaimer Note: This calculator is for educational and field calculation support only. Always follow registered label recommendations and local regulatory guidelines before pesticide use.\n")

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Pesticide Report: ${input.productName}")
        putExtra(android.content.Intent.EXTRA_TEXT, builder.toString())
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Report"))
}
