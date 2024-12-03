package projekt.zespolowy.zero_waste.controller;

import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import projekt.zespolowy.zero_waste.dto.user.UserUpdateDto;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.entity.enums.AuthProvider;
import projekt.zespolowy.zero_waste.security.CustomUser;
import projekt.zespolowy.zero_waste.services.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class UserController {

    private final UserService userService;

    // Konstruktorowe wstrzykiwanie zależności
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/accountDetails")
    public String accountDetails(Model model) {
        // Pobierz aktualnie zalogowanego użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        User user = customUser.getUser();
        model.addAttribute("user", user);
        return "User/accountDetails";
    }

    @GetMapping("/editAccount")
    public String editAccountForm(Model model) {
        // Pobierz aktualnie zalogowanego użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        User user = customUser.getUser();

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setUsername(user.getUsername());
        userUpdateDto.setFirstName(user.getFirstName());
        userUpdateDto.setLastName(user.getLastName());
        userUpdateDto.setPhoneNumber(user.getPhoneNumber());

        AuthProvider authProvider = user.getProvider();

        model.addAttribute("userUpdateDto", userUpdateDto);
        model.addAttribute("authProvider", authProvider.toString());
        return "User/editAccount";
    }

    @PostMapping("/editAccount")
    public String editAccount(@Valid @ModelAttribute("userUpdateDto") UserUpdateDto userUpdateDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "User/editAccount";
        }

        // Pobierz aktualnie zalogowanego użytkownika
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        String username = customUser.getUsername();

        try {
            User updatedUser = userService.updateUser(userUpdateDto, username);
            refreshAuthentication(updatedUser, authentication);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUser customUserInner = (CustomUser) auth.getPrincipal();
            User userInner = customUserInner.getUser();
            model.addAttribute("authProvider1", userInner.getProvider());
            return "User/editAccount";
        }

        model.addAttribute("success", "Konto zaktualizowane pomyślnie");
        redirectAttributes.addFlashAttribute("success", "Konto zaktualizowane pomyślnie");
        return "redirect:/accountDetails";
    }

    private void refreshAuthentication(User updatedUser, Authentication aut) {
        CustomUser updatedCustomUser;
        if (aut instanceof OAuth2AuthenticationToken) {
            OidcUser oidcUser = (OidcUser) aut.getPrincipal();
            updatedCustomUser = new CustomUser(updatedUser, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
        } else {
            updatedCustomUser = new CustomUser(updatedUser);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                updatedCustomUser,
                updatedCustomUser.getPassword(),
                updatedCustomUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
