package com.gigfinance.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gigfinance.app.ui.components.AdBannerPlaceholder
import com.gigfinance.app.ui.components.GlassMetricCard
import com.gigfinance.app.ui.components.GlowingSafeCard
import com.gigfinance.app.ui.components.TransactionItem
import com.gigfinance.app.ui.theme.AmberGold
import com.gigfinance.app.ui.theme.CoralRed
import com.gigfinance.app.ui.theme.NeonGreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTransactionClick: () -> Unit,
    onEditTransactionClick: (Int) -> Unit,
    onAIClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = NeonGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeonGreen
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 32.dp,
                        bottom = 100.dp
                    )
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "This Month",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .clickable { onAIClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Smart Finance AI",
                                    tint = NeonGreen
                                )
                            }
                        }
                    }

                    item {
                        GlowingSafeCard(amount = uiState.safeToSpend)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassMetricCard(
                                title = "Net Profit",
                                amount = uiState.netProfit,
                                icon = Icons.Default.TrendingUp,
                                iconColor = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            GlassMetricCard(
                                title = "Tax Jar",
                                amount = uiState.taxJar,
                                icon = Icons.Default.AccountBalance,
                                iconColor = AmberGold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassMetricCard(
                                title = "Income",
                                amount = uiState.totalIncome,
                                icon = Icons.Default.ArrowUpward,
                                iconColor = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            GlassMetricCard(
                                title = "Expenses",
                                amount = uiState.totalExpenses,
                                icon = Icons.Default.ArrowDownward,
                                iconColor = CoralRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Category Breakdown
                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item {
                            Text(
                                text = "Expense Breakdown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        items(uiState.categoryBreakdown) { stat ->
                            val progress = if (uiState.totalExpenses > 0) (stat.amount / uiState.totalExpenses).toFloat() else 0f
                            val formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(stat.amount)
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stat.category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formattedAmount,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = CoralRed,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }

                    if (!uiState.isPremium) {
                        item {
                            AdBannerPlaceholder(modifier = Modifier.padding(bottom = 24.dp))
                        }
                    }

                    item {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions this month.\nLet's get to work!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(uiState.transactions, key = { it.id }) { transaction ->
                            var expanded by remember { mutableStateOf(false) }

                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(300)),
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TransactionItem(
                                        transaction = transaction,
                                        modifier = Modifier.clickable { expanded = true }
                                    )
                                    
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit", fontWeight = FontWeight.SemiBold) },
                                            onClick = {
                                                expanded = false
                                                onEditTransactionClick(transaction.id)
                                            },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = CoralRed, fontWeight = FontWeight.SemiBold) },
                                            onClick = {
                                                expanded = false
                                                viewModel.deleteTransaction(transaction)
                                            },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed) }
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
}
