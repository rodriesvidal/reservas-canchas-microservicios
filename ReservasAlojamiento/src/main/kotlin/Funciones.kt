package org.example


fun mostrarReservas(
    reservas: List<ReservaAlojamiento>
) {

    println()
    println("================================")
    println("      RESERVAS REGISTRADAS")
    println("================================")


    reservas.forEach { reserva

        println()

        println(
            "ID: ${reserva.id}"
        )

        println(
            "Cliente: ${reserva.nombreCliente}"
        )

        println(
            "Cantidad de noches: ${reserva.cantidadNoches}"
        )

        println(
            "Valor por noche: \$${reserva.valorPorNoche}"
        )

        println(
            "Cantidad de personas: ${reserva.cantidadPersonas}"
        )

        println(
            "Tipo alojamiento: ${reserva.tipoAlojamiento.descripcion()}"
        )

        println(
            "Valor total: \$${reserva.calcularTotal()}"
        )

        println(
            "Descripción: ${reserva.descripcion()}"
        )

        println("--------------------------------")
    }
}


fun mostrarTotalRecaudado(
    reservas: List<ReservaAlojamiento>
) {

    val totalRecaudado =
        reservas.sumOf {
            it.calcularTotal()
        }


    println()

    println(
        "TOTAL RECAUDADO: \$$totalRecaudado"
    )
}


fun mostrarReservasCaras(
    reservas: List<ReservaAlojamiento>
) {

    println()
    println("RESERVAS SUPERIORES A \$150.000")
    println("--------------------------------")


    val reservasCaras =
        reservas.filter {
            it.calcularTotal() > 150000
        }


    reservasCaras.forEach { reserva ->

        println(
            "${reserva.nombreCliente} tiene un total de \$${reserva.calcularTotal()}"
        )
    }
}


fun mostrarNombresClientes(
    reservas: List<ReservaAlojamiento>
) {

    println()
    println("NOMBRES DE CLIENTES")
    println("--------------------------------")


    val nombresClientes =
        reservas.map {
            it.nombreCliente
        }


    nombresClientes.forEach { nombre ->

        println(nombre)
    }
}


fun demostrarPolimorfismo(
    reservas: List<ReservaAlojamiento>
) {

    println()
    println("DEMOSTRACIÓN DE POLIMORFISMO")
    println("--------------------------------")


    if (reservas.isNotEmpty()) {

        val reservaGeneral: Reserva =
            reservas.first()


        println(
            reservaGeneral.descripcion()
        )
    }
}


fun demostrarManejoError(
    reservas: List<ReservaAlojamiento>
) {


    println("MANEJO DE ERROR")
    println("--------------------------------")


    try {

        val posicionInexistente = 100

        println(
            reservas[posicionInexistente]
        )

    } catch (error: IndexOutOfBoundsException) {

        println(
            "Error controlado: ${error.message}"
        )

        println(
            "Un buche queria entrar al sistema."
        )
    }


    println()

    println(
        "El programa continaa ejecutándose correctamente."
    )
}