package com.happs.myfitlist.state

import com.happs.myfitlist.model.usuario.Usuario

data class EditarDadosPessoaisState(
    val usuario: Usuario = Usuario(id = -1, nome = "", idade = 0, peso = 0f, -1, -1),
)