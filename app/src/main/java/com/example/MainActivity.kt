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
