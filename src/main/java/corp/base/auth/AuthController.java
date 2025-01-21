package corp.base.auth;

import corp.base.user.User;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static corp.base.constants.Constants.VERSION;
import static corp.base.helpers.Redirect.buildRedirectUrl;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(VERSION + "auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CustomAuthProvider authProvider;

//  GET http://localhost:8080/api/v1/auth/register
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

//  POST http://localhost:8080/api/v1/auth/register
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        String originalPassword = user.getPassword();
        String hashedPassword = passwordEncoder.encode(originalPassword);
        user.setPassword(hashedPassword);
        user.setAuthority("user");

        try {
            String sql = "INSERT INTO `user` (name, email, password, authority) VALUES (:name, :email, :password, :authority)";
            jdbcTemplate.update(sql, Map.of(
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "password", user.getPassword(),
                    "authority", user.getAuthority()
            ));

            Authentication authentication = authProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), originalPassword));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            return buildRedirectUrl("auth/login");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid name, email or password");
            return buildRedirectUrl("auth/register");
        }
    }

//  GET http://localhost:8080/api/v1/auth/login
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(name = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

//  POST http://localhost:8080/auth/login
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password,
                            RedirectAttributes redirectAttributes, Model model) {

        String sql = "SELECT password FROM `user` WHERE email = :email";
        String storedPasswordHash;

        try {
            storedPasswordHash = jdbcTemplate.queryForObject(sql, Map.of("email", email), String.class);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            return buildRedirectUrl("auth/login");
        }

        if (!passwordEncoder.matches(password, storedPasswordHash)) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            return buildRedirectUrl("auth/login");
        }

        return buildRedirectUrl("note/list");
    }

//  http://localhost:8080/api/v1/auth/logout
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
        }
        return buildRedirectUrl("auth/login");
    }
}
