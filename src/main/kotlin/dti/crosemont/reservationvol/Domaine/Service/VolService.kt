package dti.crosemont.reservationvol.Domaine.Service

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import dti.crosemont.reservationvol.AccesAuxDonnees.SourcesDeDonnees.VolsDAO
import dti.crosemont.reservationvol.Controleurs.Exceptions.RequêteMalFormuléeException
import org.springframework.stereotype.Service
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.resource.NoResourceFoundException
import dti.crosemont.reservationvol.Domaine.Modele.Vol
import dti.crosemont.reservationvol.Domaine.Modele.Classe
import dti.crosemont.reservationvol.Domaine.Modele.`Siège`
import org.springframework.http.HttpMethod
import java.time.LocalDateTime
import dti.crosemont.reservationvol.Controleurs.Exceptions.RessourceInexistanteException
import dti.crosemont.reservationvol.Domaine.Modele.VolStatut
import dti.crosemont.reservationvol.Domaine.OTD.VolOTD

@Service
class VolService(private val volsDAO: VolsDAO) {

    fun obtenirVolParParam(dateDebut: LocalDateTime, aeroportDebut: String, aeroportFin: String): List<Vol> {
        return volsDAO.obtenirVolParParam(dateDebut, aeroportDebut, aeroportFin)
    }

    @PreAuthorize("hasAuthority('créer:vols')")
    fun ajouterVol(vol: Vol): Vol {       
        if (!volsDAO.trajetExiste(vol.trajet.id)) {
            throw RessourceInexistanteException("Le trajet avec l'ID ${vol.trajet.id} n'existe pas.")
        }
        
        if (!volsDAO.avionExiste(vol.avion.id)) {
            throw RessourceInexistanteException("L'avion avec l'ID ${vol.avion.id} n'existe pas.")
        }

        if (vol.dateArrivee.isBefore(vol.dateDepart)) {
        throw RequêteMalFormuléeException("La date d'arrivée (${vol.dateArrivee}) ne peut pas être avant la date de départ (${vol.dateDepart}).")      
        }

        val nouveauVol = volsDAO.ajouterVol(vol)
    
        val statutsMisAJour = vol.vol_statut.map { statut ->
            statut.copy(idVol = nouveauVol.id)
        }
        
        statutsMisAJour.forEach { statut -> volsDAO.ajouterStatutVol(nouveauVol.id, statut) }
        volsDAO.ajouterPrixParClasse(nouveauVol.id, vol.prixParClasse)
    
        return nouveauVol.copy(vol_statut = statutsMisAJour)
    }
    @PreAuthorize("hasAuthority('modifier:vols')")
    fun modifierVol(id: Int, modifieVol: VolOTD): Vol {

        val volExistant = chercherParId(id) ?: throw RessourceInexistanteException("Le vol avec l'ID $id n'existe pas.")


        modifieVol.apply {
            dateDepart?.let { volExistant.dateDepart = it }
            dateArrivee?.let { volExistant.dateArrivee = it }
            avion?.let {volExistant.avion= it}
            trajet?.let {volExistant.trajet= it}
            poidsMaxBag?.let { volExistant.poidsMaxBag = it }
            prixParClasse?.let { volExistant.prixParClasse = it }
            vol_statut?.let {volExistant.vol_statut = it }
            duree?.let { volExistant.duree = it }
        }

        if (modifieVol.trajet != null) {
            if (!volsDAO.trajetExiste(modifieVol.trajet.id)) {
                throw RessourceInexistanteException("Le trajet avec l'ID ${modifieVol.trajet.id} n'existe pas.")
            }
            volExistant.trajet = modifieVol.trajet
        }

        if (modifieVol.avion != null) {
            if (!volsDAO.avionExiste(modifieVol.avion.id)) {
                throw RessourceInexistanteException("L'avion avec l'ID ${modifieVol.avion.id} n'existe pas.")
            }
            volExistant.avion = modifieVol.avion
        }

        if (modifieVol.dateArrivee != null && modifieVol.dateDepart != null) {
            if (modifieVol.dateArrivee.isBefore(modifieVol.dateDepart)) {
                throw RequêteMalFormuléeException("La date d'arrivée (${modifieVol.dateArrivee}) ne peut pas être avant la date de départ (${modifieVol.dateDepart}).")
            }
        }


        if (modifieVol.vol_statut?.any { it.idVol != id } == true) {
            throw RequêteMalFormuléeException("Le statut fait référence à un ID de vol incorrect")
        }




        return volsDAO.modifierVol(id, volExistant)
    }

    fun chercherTous(): List<Vol> = volsDAO.chercherTous()

    fun chercherParId(id: Int): Vol? {
        val vol = volsDAO.chercherParId(id)

        if(vol == null){
            throw RessourceInexistanteException("Le vol $id n'existe pas.")
        }
        return vol;
    }

    fun effacer(id: Int) {
        val vol = volsDAO.chercherParId(id)

        if(vol == null){
            throw RessourceInexistanteException("Le vol $id n'existe pas.")
        }
        volsDAO.effacer(id)
    }

    fun chercherSiegeParVolId(id: Int): List<Siège> {
        val vol = volsDAO.chercherParId(id)

        if(vol == null){
            throw RessourceInexistanteException("Le vol $id n'existe pas.")
        }
        return volsDAO.obtenirSiegeParVolId(id)
    }

}