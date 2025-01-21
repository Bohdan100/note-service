package corp.base.note;

import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import static corp.base.constants.Constants.VERSION;
import static corp.base.helpers.Redirect.buildRedirectUrl;

import corp.base.user.User;
import corp.base.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping(VERSION + "note")
public class NoteController {
    private final NoteService noteService;
    private final UserService userService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // GET  http://localhost:8080/api/v1/note/list
    @GetMapping("/list")
    public ModelAndView getAll() {
        String userEmail = getCurrentUserEmail();
        String userName = userService.getUserNameByEmail(userEmail);

        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.getAllNotes(userEmail));
        result.addObject("totalNotes", noteService.getCountOfAllNotes(userEmail));
        result.addObject("userName", userName);
        return result;
    }

    // GET  http://localhost:8080/api/v1/note/add
    @GetMapping("/add")
    public ModelAndView showAddNotePage() {
        return new ModelAndView("add-note");
    }

    // POST http://localhost:8080/api/v1/note/add?title=someTitle&content=someContent
    @PostMapping("/add")
    public String addNote(@ModelAttribute @Valid NoteDTO noteDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Генерируем URL с описанием ошибок
            String errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Validation error");
            return buildRedirectUrl("note/list?error=" + errorMessage);
        }
        String userEmail = getCurrentUserEmail();

        Note newNote = new Note();
        newNote.setTitle(noteDto.getTitle());
        newNote.setContent(noteDto.getContent());
        noteService.save(newNote, userEmail);

        return buildRedirectUrl("note/list");
    }

    // POST http://localhost:8080/api/v1/note/delete/3
    @PostMapping("/delete/{id}")
    public String deleteById(@PathVariable("id") String id) {
        String userEmail = getCurrentUserEmail();
        try {
            noteService.deleteById(id, userEmail);
        } catch (NoSuchElementException e) {
            return buildRedirectUrl("note/list?error=Note not found");
        } catch (IllegalArgumentException e) {
            return buildRedirectUrl("note/list?error=You do not have permission to delete this note");
        }
        return buildRedirectUrl("note/list");
    }

    // GET  http://localhost:8080/api/v1/note/edit/6
    @GetMapping("/edit/{id}")
    public ModelAndView editPage(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        try {
            Note note = noteService.getById(id);
            modelAndView.setViewName("edit-note");
            modelAndView.addObject("note", note);
        } catch (NoSuchElementException e) {
            modelAndView.setViewName(buildRedirectUrl("note/list"));
            modelAndView.addObject("error", "Note not found");
        } catch (Exception e) {
            modelAndView.setViewName(buildRedirectUrl("note/list"));
            modelAndView.addObject("error", "An error occurred");
        }
        return modelAndView;
    }

    // POST  http://localhost:8080/api/v1/note/edit?id=3&title=newTitle&content=newContent
    @PostMapping("/edit")
    public String editNote(@RequestParam("id") String id,
                           @RequestParam(name = "title", required = false) String title,
                           @RequestParam(name = "content", required = false) String content,
                           Model model) {
        if ((title != null && title.length() < 2) || (content != null && content.isEmpty())) {
            String errorMessage = (title != null && title.length() < 2)
                    ? "Title must be at least 2 characters long"
                    : "Content cannot be blank";
            return buildRedirectUrl("note/list?error=" + errorMessage);
        }
        try {
            Note note = noteService.getById(id);
            if (title != null) {
                note.setTitle(title);
            }
            if (content != null) {
                note.setContent(content);
            }

            String userEmail = getCurrentUserEmail();
            noteService.save(note, userEmail);

        } catch (IllegalArgumentException e) {
            return buildRedirectUrl("note/list?error=Note not found");
        } catch (Exception e) {
            return buildRedirectUrl("note/list?error=An error occurred");
        }
        return buildRedirectUrl("note/list");
    }

    //  GET  http://localhost:8080/api/v1/note/searchTitle?query=4
    @GetMapping("/searchTitle")
    public ModelAndView searchNotesByTitle(@RequestParam("query") String query) {
        String userEmail = getCurrentUserEmail();
        String userName = userService.getUserNameByEmail(userEmail);

        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.searchNotesByTitle(query, userEmail));
        result.addObject("totalNotes", noteService.getCountNotesByTitle(query, userEmail));
        result.addObject("userName", userName);
        return result;
    }

    //  GET  http://localhost:8080/api/v1/note/searchTitleAndContent?query=JDBC
    @GetMapping("/searchTitleAndContent")
    public ModelAndView searchNotesByTitleAndContent(@RequestParam("query") String query) {
        String userEmail = getCurrentUserEmail();
        String userName = userService.getUserNameByEmail(userEmail);

        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.searchNotesByTitleAndContent(query, userEmail));
        result.addObject("totalNotes", noteService.getCountNotesByTitleAndContent(query, userEmail));
        result.addObject("userName", userName);
        return result;
    }

    // POST  http://localhost:8080/api/v1/note/deleteByTitle?query=Some
    @PostMapping("/deleteByTitle")
    public String deleteNotesByTitle(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return buildRedirectUrl("note/list?error=Title cannot be empty");
        }

        try {
            String userEmail = getCurrentUserEmail();
            noteService.deleteNotesByTitle(query, userEmail);
        } catch (Exception e) {
            return buildRedirectUrl("note/list?error=An error occurred");
        }

        return buildRedirectUrl("note/list");
    }

    // POST  http://localhost:8080/api/v1/note/admin/delete/6
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
        return buildRedirectUrl("note/admin");
    }

    // GET  http://localhost:8080/api/v1/note/admin
    @GetMapping("/admin")
    public ModelAndView showAdminPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("admin"))) {
            List<User> users = userService.getAllUsers();
            ModelAndView result = new ModelAndView("admin");
            result.addObject("users", users);
            return result;
        } else {
            return new ModelAndView(buildRedirectUrl("note/list"));
        }
    }
}
