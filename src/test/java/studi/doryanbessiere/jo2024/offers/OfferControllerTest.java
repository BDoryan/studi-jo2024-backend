package studi.doryanbessiere.jo2024.offers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import studi.doryanbessiere.jo2024.config.SecurityConfig;
import studi.doryanbessiere.jo2024.services.admins.AdminAuthService;
import studi.doryanbessiere.jo2024.services.admins.dto.AdminMeResponse;
import studi.doryanbessiere.jo2024.services.offers.Offer;
import studi.doryanbessiere.jo2024.services.offers.OfferController;
import studi.doryanbessiere.jo2024.services.offers.OfferService;
import studi.doryanbessiere.jo2024.shared.security.AdminOnlyAspect;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, AdminOnlyAspect.class})
@EnableAspectJAutoProxy
@WebMvcTest(OfferController.class)
@AutoConfigureMockMvc
class OfferControllerTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_TOKEN = "Bearer admin-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService offerService;

    @MockBean
    private AdminAuthService adminAuthService;

    @Test
    @DisplayName("GET /offers doit retourner la liste des offres")
    void getAllOffers_shouldReturnList() throws Exception {
        Offer offer1 = Offer.builder()
                .id(1L)
                .name("Pack Cérémonie d'ouverture")
                .description("Accès à la cérémonie d'ouverture des JO 2024")
                .price(150.0)
                .persons(1)
                .quantity(100)
                .build();

        Offer offer2 = Offer.builder()
                .id(2L)
                .name("Pack Natation Finale")
                .description("Billet pour la finale 100m nage libre")
                .price(120.0)
                .persons(1)
                .quantity(50)
                .build();

        Mockito.when(offerService.getAllOffers()).thenReturn(List.of(offer1, offer2));

        mockMvc.perform(get("/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Pack Cérémonie d'ouverture"))
                .andExpect(jsonPath("$[1].price").value(120.0));
    }

    @Test
    @DisplayName("GET /offers/{id} doit retourner une offre spécifique")
    void getOfferById_shouldReturnOffer() throws Exception {
        Offer offer = Offer.builder()
                .id(1L)
                .name("Pack Cérémonie d'ouverture")
                .description("Accès à la cérémonie d'ouverture des JO 2024")
                .price(150.0)
                .persons(2)
                .quantity(100)
                .build();

        Mockito.when(offerService.getOfferById(1L)).thenReturn(offer);

        mockMvc.perform(get("/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack Cérémonie d'ouverture"))
                .andExpect(jsonPath("$.price").value(150.0))
                .andExpect(jsonPath("$.persons").value(2))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @DisplayName("POST /offers doit créer une offre (admin)")
    void createOffer_shouldReturnCreatedOffer_whenAdmin() throws Exception {
        adminIsAuthorized();

        Offer offer = Offer.builder()
                .id(1L)
                .name("Pack Athlétisme")
                .description("Accès aux finales d'athlétisme")
                .price(200.0)
                .persons(2)
                .quantity(80)
                .build();

        Mockito.when(offerService.createOffer(any(Offer.class))).thenReturn(offer);

        mockMvc.perform(post("/offers")
                        .header(AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Pack Athlétisme",
                                  "description": "Accès aux finales d'athlétisme",
                                  "price": 200.0,
                                  "persons": 2,
                                  "quantity": 80
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack Athlétisme"))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.quantity").value(80));
    }

    @Test
    @DisplayName("PUT /offers/{id} doit mettre à jour une offre (admin)")
    void updateOffer_shouldReturnUpdatedOffer_whenAdmin() throws Exception {
        adminIsAuthorized();

        Offer updated = Offer.builder()
                .id(1L)
                .name("Pack modifié")
                .description("Description mise à jour")
                .price(250.0)
                .persons(3)
                .quantity(60)
                .build();

        Mockito.when(offerService.updateOffer(eq(1L), any(Offer.class))).thenReturn(updated);

        mockMvc.perform(put("/offers/1")
                        .header(AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Pack modifié",
                                  "description": "Description mise à jour",
                                  "price": 250.0,
                                  "persons": 3,
                                  "quantity": 60
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack modifié"))
                .andExpect(jsonPath("$.price").value(250.0))
                .andExpect(jsonPath("$.persons").value(3));
    }

    @Test
    @DisplayName("DELETE /offers/{id} doit supprimer une offre (admin)")
    void deleteOffer_shouldReturnNoContent_whenAdmin() throws Exception {
        adminIsAuthorized();

        mockMvc.perform(delete("/offers/1")
                        .header(AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNoContent());

        Mockito.verify(offerService).deleteOffer(1L);
    }

    @Test
    @DisplayName("POST /offers renvoie 403 quand le rôle n'est pas ADMIN")
    void createOffer_shouldReturnForbidden_whenNotAdminRole() throws Exception {
        Mockito.when(adminAuthService.getAuthenticatedAdmin("admin-token"))
                .thenReturn(new AdminMeResponse("controller@jo2024.fr", "Contrôleur", "SCANNER"));

        mockMvc.perform(post("/offers")
                        .header(AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test",
                                  "description": "Test",
                                  "price": 100.0,
                                  "persons": 1,
                                  "quantity": 10
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("access_denied"));
    }

    private void adminIsAuthorized() {
        // Simule un compte avec le rôle ADMIN pour satisfaire l'aspect @AdminOnly.
        Mockito.when(adminAuthService.getAuthenticatedAdmin("admin-token"))
                .thenReturn(new AdminMeResponse("admin@jo2024.fr", "Admin User", "ADMIN"));
    }
}
