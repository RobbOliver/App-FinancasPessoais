package com.robson.financas.ui.fiscal.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.theme.Spacing

/** Marca e nome do produto — os 2 níveis mais específicos, ligados ao [com.robson.financas.data.local.entity.fiscal.ProductEntity]. */
@Composable
fun ProductIdentityDialog(
    brand: String?,
    genericName: String,
    onDismiss: () -> Unit,
    onSave: (brand: String?, genericName: String) -> Unit,
) {
    var brandInput by remember { mutableStateOf(brand.orEmpty()) }
    var nameInput by remember { mutableStateOf(genericName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marca e produto") },
        text = {
            Column {
                AppTextField(
                    value = brandInput,
                    onValueChange = { brandInput = it },
                    label = "Marca",
                    placeholder = "Ex.: Lacta",
                    modifier = Modifier.fillMaxWidth(),
                )
                AppTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = "Produto",
                    placeholder = "Ex.: Bombom Ouro Branco 20g",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                )
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Salvar",
                enabled = nameInput.isNotBlank(),
                onClick = { onSave(brandInput.trim().ifBlank { null }, nameInput.trim()) },
            )
        },
        dismissButton = {
            AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
        },
    )
}
