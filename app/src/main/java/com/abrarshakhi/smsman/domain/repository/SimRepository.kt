package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.Sim

interface SimRepository {
    fun listSims(): List<Sim>
}
