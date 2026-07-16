sed -i '/Button(/,/}/ {
  /Button(/c \
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {\
                    IconButton(\
                        onClick = { showAddGuestDialog = true },\
                        modifier = Modifier.testTag("add_guest_header_button")\
                    ) {\
                        Icon(\
                            imageVector = Icons.Default.PersonAddAlt1,\
                            contentDescription = stringResource(R.string.cd_add_guest),\
                            tint = MaterialTheme.colorScheme.primary\
                        )\
                    }\
                    Button(\
                        onClick = onNavigateToInvite,\
                        modifier = Modifier.testTag("invite_roommate_header_button"),\
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),\
                        shape = RoundedCornerShape(20.dp)\
                    ) {\
                        Icon(\
                            imageVector = Icons.Default.Share,\
                            contentDescription = stringResource(R.string.cd_invite_roommate),\
                            modifier = Modifier.size(16.dp)\
                        )\
                        Spacer(modifier = Modifier.width(4.dp))\
                        Text(\
                            text = stringResource(R.string.people_invite_button),\
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)\
                        )\
                    }\
                }
  d
}' app/src/main/java/com/example/ui/screens/PeopleScreen.kt
