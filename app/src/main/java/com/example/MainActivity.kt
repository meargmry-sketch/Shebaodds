            onBetPlacedSuccess = {
                showMultiBetSlip = false
                selectedMainTab = 2 // Go to My Bets history to see live active slip!
                scope.launch {
                    snackbarHostState.showSnackbar("Secure Accumulator Ticket placed successfully!")
                }
            }
        )
    }

    // OVERLAY: SECURE BIOMETRIC USER PROFILE CHALLENGE
    if (showProfileAuthDialog) {
        SecureBiometricVerificationDialog(
            title = "BIOMETRIC IDENTITY UNLOCK",
            subtitle = "Verification is active for real-time secure decryption of keychains and log indices.",
            onAuthSuccess = {
                showProfileAuthDialog = false
                showProfileDialog = true
            },
            onDismissRequest = {
                showProfileAuthDialog = false
            }
        )
    }

    // OVERLAY: SECURE USER PROFILE ACCESS MODAL
    if (showProfileDialog) {
        UserProfileDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showProfileDialog = false
            },
            onLogoutClick = {
                showProfileDialog = false
                isLoggedIn = false
                sharedPrefs.edit().putBoolean("logged_in", false).apply()
            }
        )
    }

    // OVERLAY: DEPOSIT FUNDS MODAL (Telebirr payment flow Integration)
    if (showDepositFundsDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showDepositFundsDialog = false
            }
        )
    }

    // OVERLAY: ODDS ALERTS HISTORY HUB
    if (showOddsAlertsDialog) {
        val oddsAlerts by viewModel.oddsAlerts.collectAsState()
        OddsAlertsHubDialog(
            alerts = oddsAlerts,
            onDismiss = { showOddsAlertsDialog = false },
            onMarkAllRead = {
                oddsAlerts.forEach { viewModel.markAlertAsRead(it.id) }
            },
            onClearAll = {
                viewModel.clearOddsAlerts()
            },
            onMarkRead = { id ->
                viewModel.markAlertAsRead(id)
            }
        )
    }
}
}

@Composable
fun OddsAlertsHubDialog(
    alerts: List<OddsAlert>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("odds_alerts_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Odds Alerts Hub",
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ODDS ALERTS HUB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Divider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // Clear & Actions Bar
                if (alerts.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${alerts.count { !it.isRead }} unread / ${alerts.size} total",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Clear All",
                                fontSize = 11.sp,
                                color = LightRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onClearAll() }
                                    .testTag("clear_odds_alerts_button")
                            )
                        }
                    }
                }
                
                // Scrollable Alerts List
                if (alerts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No odds alerts triggered yet.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Click the bell icon on any match card to track odds fluctuations in real-time!",
                            fontSize = 10.sp,
                            color = TextMuted.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(alerts, key = { it.id }) { alert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkRead(alert.id) }
                                    .testTag("alert_item_${alert.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alert.isRead) SlateSurfaceL2 else SlateSurfaceL2.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (alert.isRead) BorderColor else AmberAccent.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (!alert.isRead) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(AmberAccent, CircleShape)
                                                )
                                            }
                                            Text(
                                                text = "${alert.teamA} vs ${alert.teamB}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${alert.fieldChanged} changed from ${String.format("%.2f", alert.oldValue)} to ${String.format("%.2f", alert.newValue)}",
                                            fontSize = 11.sp,
                                            color = TextLight
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
                                        Text(
                                            text = "Sport: ${alert.sport} • Triggered at $timeStr",
                                            fontSize = 9.sp,
                                            color = TextMuted
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
