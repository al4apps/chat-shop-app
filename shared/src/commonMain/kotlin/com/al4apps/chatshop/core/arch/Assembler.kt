package com.al4apps.chatshop.core.arch

/**
 * Преобразует логическое состояние компонента (vmState) в состояние UI (uiState).
 * Передаётся в компонент параметром конструктора (с дефолтным экземпляром).
 */
interface Assembler<VM : Any, UI : Any> {

    fun assemble(vmState: VM): UI
}
