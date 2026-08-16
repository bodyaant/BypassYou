package com.example.bypassyou.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, number: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp)
        ) {
            Text(
                text = "Manual Contact Entry",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter contact name and phone number string to whitelist",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Contact Name") },
                placeholder = { Text("Mom, Work, Alice...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+7 999 123 45 67") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (number.isNotBlank()) {
                            onConfirm(name, number)
                        }
                    },
                    enabled = number.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Add to Whitelist",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
