package dti.crosemont.reservationvol

import dti.crosemont.reservationvol.Entites.Reservation

interface ReservationsDAO: DAO<Reservation>{
        override fun chercherTous() : List<Reservation>
        fun ajouterReservation(reservation: Reservation): Reservation
        fun chercherParId(id: Int): Reservation?
}