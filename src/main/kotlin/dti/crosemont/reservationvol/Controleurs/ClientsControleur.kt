package dti.crosemont.reservationvol.Controleurs

import dti.crosemont.reservationvol.Controleurs.Exceptions.RequêteMalFormuléeException
import dti.crosemont.reservationvol.Domaine.Modele.Client
import dti.crosemont.reservationvol.Domaine.Service.ClientsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clients")
class ClientsControleur( private val service : ClientsService) {

        @GetMapping
        fun obtenirToutLesClients(
                @RequestParam( name = "motCle", required = false ) motClé : String?
        ): ResponseEntity<List<Client>> {
                if( !motClé.isNullOrEmpty() ){
                        return ResponseEntity.ok( service.obtenirClientsParMotCle(motClé) )
                }

                return ResponseEntity.ok( service.obtenirToutLesClient() )
        }

        @GetMapping("/{id}")
        fun obtenirUnClientParId( @PathVariable id: Int ) : ResponseEntity<Client> =
                ResponseEntity.ok( service.obtenirParId( id ) )

        @GetMapping("/profile")
        fun obtenirProfile() : ResponseEntity<Client> {
                val email = "jean.dubois@email.com" // TODO : Lire l'email de l'utilisateur connecté avec token
                return ResponseEntity.ok( service.obtenirClientParEmail( email ) )
        }


        @PostMapping
        fun ajouterClient(@RequestBody client: Client): ResponseEntity<Client> =
                ResponseEntity.ok( service.ajouterClient( client ) )

        @PutMapping("/{id}")
        fun modifierClient(
                @PathVariable id: Int,
                @RequestBody client: Client
        ): ResponseEntity<Client> {
                if ( id == client.id ) {
                        return ResponseEntity.ok( service.modifierClient( client ) )
                } else {
                        throw RequêteMalFormuléeException( "Modification du client invalide" )
                }
        }

        @DeleteMapping("/{id}")
        fun supprimerUnClientParId(@PathVariable id: Int): ResponseEntity<HttpStatus> {
                service.supprimerUnClient( id )
                return ResponseEntity( HttpStatus.NO_CONTENT )
        }
}
