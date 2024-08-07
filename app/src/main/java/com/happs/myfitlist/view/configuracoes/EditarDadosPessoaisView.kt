package com.happs.myfitlist.view.configuracoes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.happs.myfitlist.R
import com.happs.myfitlist.model.usuario.Usuario
import com.happs.myfitlist.navigation.canGoBack
import com.happs.myfitlist.room.RepositoryResponse
import com.happs.myfitlist.state.EditarDadosPessoaisState
import com.happs.myfitlist.ui.theme.TextFieldColors.colorsTextFieldsCadastro
import com.happs.myfitlist.ui.theme.myFontBody
import com.happs.myfitlist.ui.theme.myFontTitle
import com.happs.myfitlist.util.CustomAlertDialog
import com.happs.myfitlist.util.CustomTopAppBar
import com.happs.myfitlist.util.ErrorScreen
import com.happs.myfitlist.util.LoadingScreen
import com.happs.myfitlist.viewmodel.configuracoes.EditarDadosPessoaisViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditarDadosPessoaisView(
    navControllerConfiguracoes: NavController,
    editarDadosPessoaisViewModel: EditarDadosPessoaisViewModel = koinViewModel<EditarDadosPessoaisViewModel>()
) {

    val uiState by editarDadosPessoaisViewModel.editarDadosPessoaisState.collectAsState()

    val openDialog = remember { mutableStateOf(false) }

    when (val state = uiState) {
        is RepositoryResponse.Loading -> {
            LoadingScreen()
        }

        is RepositoryResponse.Success -> {
            if (openDialog.value) {
                CustomAlertDialog(
                    title = stringResource(R.string.deseja_voltar),
                    text = stringResource(R.string.as_alteracoes_serao_perdidas),
                    textButtomConfirm = stringResource(R.string.voltar),
                    onclose = { openDialog.value = false },
                    onConfirm = {
                        if (navControllerConfiguracoes.canGoBack) {
                            navControllerConfiguracoes.popBackStack("configuracoes", false)
                        }
                        openDialog.value = false
                    }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                CustomTopAppBar(
                    onBackPressed = { openDialog.value = true },
                    barTitle = stringResource(R.string.alterar_dados_pessoais)
                )

                Content(editarDadosPessoaisViewModel, state, navControllerConfiguracoes)
            }
        }

        is RepositoryResponse.Error -> {
            ErrorScreen()
        }
    }
}

@Composable
fun Content(
    viewModel: EditarDadosPessoaisViewModel,
    state: RepositoryResponse.Success<EditarDadosPessoaisState>,
    navControllerConfiguracoes: NavController
) {

    val ctx = LocalContext.current

    val usuario = state.data.usuario

    var nome by rememberSaveable { mutableStateOf("") }
    var idade by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.data) {
        nome = usuario.nome
        idade =
            if (usuario.idade.toString() == "-1") "" else usuario.idade.toString()
        peso =
            if (usuario.peso.toString() == "-1.0") "" else usuario.peso.toString()
    }

    val coroutineScope = rememberCoroutineScope()

    var isNomeEror by rememberSaveable { mutableStateOf(false) }

    var enabledButton by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(10.dp)) {
        OutlinedTextField(
            value = nome,
            onValueChange = { it ->
                if (it.length <= 100 && it.all { it.isLetter() || it.isWhitespace() }) {
                    nome = it
                    isNomeEror = false
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            isError = isNomeEror,
            supportingText = { if (isNomeEror) Text(stringResource(R.string.digite_seu_nome)) },
            placeholder = {
                Text(
                    text = stringResource(R.string.nome_obrigat_rio),
                    fontFamily = myFontBody,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = colorsTextFieldsCadastro(),
            textStyle = TextStyle(
                fontFamily = myFontBody,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        val pattern = remember { Regex("^\\d*\$") }

        OutlinedTextField(
            value = idade,
            onValueChange = {
                if (it.matches(pattern)) {
                    idade = if (it.isNotEmpty()) {
                        it.toInt().coerceAtMost(122).toString()
                    } else {
                        it
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            placeholder = {
                Text(
                    text = stringResource(R.string.idade_opcional),
                    fontFamily = myFontBody,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = colorsTextFieldsCadastro(),
            textStyle = TextStyle(
                fontFamily = myFontBody,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = {
                // Verifica se o input tem no máximo 6 caracteres e no máximo um ponto decimal
                if (it.length <= 6 && it.count { char -> char == '.' } <= 1) {
                    peso = when {
                        // Se o valor for maior que 635, seta para 635
                        it.isNotEmpty() && (it.toFloatOrNull() ?: 0f) > 635f -> {
                            "635"
                        }
                        // Se o valor é "0" ou começa com "0." permite a entrada
                        it == "0" || it.startsWith("0.") -> {
                            it
                        }
                        // Se o valor é um número decimal válido ou vazio, permite a entrada
                        it.toFloatOrNull() != null || it.isEmpty() -> {
                            it
                        }
                        // Caso contrário, mantém o valor atual de `peso`
                        else -> {
                            peso
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            placeholder = {
                Text(
                    text = stringResource(R.string.peso_kg_opcional),
                    fontFamily = myFontBody,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = colorsTextFieldsCadastro(),
            textStyle = TextStyle(
                fontFamily = myFontBody,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        ElevatedButton(
            onClick = {
                if (nome.isNotBlank()) {
                    enabledButton = false
                    val newUsuario =
                        Usuario(
                            id = usuario.id,
                            nome = nome,
                            idade = idade.toByteOrNull() ?: -1,
                            peso = peso.toFloatOrNull() ?: -1f,
                            idPlanoTreinoPrincipal = usuario.idPlanoTreinoPrincipal,
                            idPlanoDietaPrincipal = usuario.idPlanoDietaPrincipal
                        )
                    coroutineScope.launch {
                        viewModel.updateUser(newUsuario)
                    }
                    Toast.makeText(ctx, "Dados alterados com sucesso.", Toast.LENGTH_SHORT).show()
                    navControllerConfiguracoes.popBackStack("configuracoes", false)
                } else {
                    isNomeEror = true
                }
            },
            modifier = Modifier
                .width(260.dp)
                .height(60.dp)
                .align(Alignment.CenterHorizontally),
            enabled = enabledButton,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(
                    0.3f
                )
            ),
            shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.salvar),
                fontFamily = myFontTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}