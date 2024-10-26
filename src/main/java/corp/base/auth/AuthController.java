package corp.base.auth;

import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
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

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CustomAuthProvider authProvider;

//  GET http://localhost:8080/auth/register
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

//  POST http://localhost:8080/auth/register
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        String originalPassword = user.getPassword();
        String hashedPassword = passwordEncoder.encode(originalPassword);
        user.setPassword(hashedPassword);
        user.setAuthority("user");

        try {
            String sql = "INSERT INTO \"user\" (name, email, password, authority) VALUES (:name, :email, :password, :authority)";
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

            return "redirect:/note/list";
        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "User with this email already exists.");
            return "redirect:/auth/register";
        }
    }

//  GET http://localhost:8080/auth/login
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

//  POST http://localhost:8080/auth/login
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password,
                            RedirectAttributes redirectAttributes, Model model) {

        String sql = "SELECT password FROM \"user\" WHERE email = :email";
        String storedPasswordHash;

        try {
            storedPasswordHash = jdbcTemplate.queryForObject(sql, Map.of("email", email), String.class);
        } catch (EmptyResultDataAccessException e) {
            model.addAttribute("status", 404);
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "error";
        }

        if (!passwordEncoder.matches(password, storedPasswordHash)) {
            model.addAttribute("status", 401);
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "error";
        }

        return "redirect:/note/list";
    }

//  http://localhost:8080/auth/logout
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
        }
        return "redirect:/auth/login";
    }
}
