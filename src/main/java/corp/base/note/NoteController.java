package corp.base.note;

import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import corp.base.auth.User;
import corp.base.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RequestMapping("/note")
@Controller
public class NoteController {
    private final NoteService noteService;
    private final UserService userService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // GET  http://localhost:8080/note/list
    @GetMapping("/list")
    public ModelAndView getAll() {
        String userEmail = getCurrentUserEmail();
        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.getAllNotes(userEmail));
        result.addObject("totalNotes", noteService.getCountOfAllNotes(userEmail));
        return result;
    }

    // GET  http://localhost:8080/note/add
    @GetMapping("/add")
    public ModelAndView showAddNotePage() {
        return new ModelAndView("add-note");
    }

    // POST http://localhost:8080/note/add?title=someTitle&content=someContent
    @PostMapping("/add")
    public String addNote(@RequestParam("title") String title, @RequestParam("content") String content) {
        String userEmail = getCurrentUserEmail();

        if (title == null || title.length() < 2) {
            return "redirect:/note/list?error=Title must contain at least 2 characters";
        }
        if (content == null || content.isEmpty()) {
            return "redirect:/note/list?error=No content";
        }

        Note newNote = new Note();
        newNote.setTitle(title);
        newNote.setContent(content);

        noteService.save(newNote, userEmail);
        return "redirect:/note/list";
    }

    // POST http://localhost:8080/note/delete/3
    @PostMapping("/delete/{id}")
    public String deleteById(@PathVariable("id") String id) {
        String userEmail = getCurrentUserEmail();
        try {
            noteService.deleteById(id, userEmail);
        } catch (NoSuchElementException e) {
            return "redirect:/note/list?error=Note not found";
        } catch (IllegalArgumentException e) {
            return "redirect:/note/list?error=You do not have permission to delete this note";
        }
        return "redirect:/note/list";
    }

    // GET  http://localhost:8080/note/edit/5
    @GetMapping("/note/edit/{id}")
    public ModelAndView editPage(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        try {
            Note note = noteService.getById(id);
            modelAndView.setViewName("edit-note");
            modelAndView.addObject("note", note);
        } catch (NoSuchElementException e) {
            modelAndView.setViewName("redirect:/note/list");
            modelAndView.addObject("error", "Note not found");
        }
        return modelAndView;
    }

    // POST  http://localhost:8080/note/edit?id=3&title=newTitle&content=newContent
    @PostMapping("/edit")
    public String editNote(@RequestParam("id") String id,
                           @RequestParam(name = "title", required = false) String title,
                           @RequestParam(name = "content", required = false) String content) {
        try {
            Note note = noteService.getById(id);

            if (title != null && !title.isEmpty()) {
                if (title.length() < 2) {
                    return "redirect:/note/list?error=Title must contain at least 2 characters";
                }
                note.setTitle(title);
            }

            if (content != null && !content.isEmpty()) {
                note.setContent(content);
            } else {
                return "redirect:/note/list?error=No content";
            }

            String userEmail = getCurrentUserEmail(); // Получаем email текущего пользователя
            noteService.save(note, userEmail);
        } catch (IllegalArgumentException e) {
            return "redirect:/note/list?error=Note not found";
        }
        return "redirect:/note/list";
    }

//  GET  http://localhost:8080/note/searchTitle?query=4
    @GetMapping("/searchTitle")
    public ModelAndView searchNotesByTitle(@RequestParam("query") String query) {
        String userEmail = getCurrentUserEmail();

        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.searchNotesByTitle(query, userEmail));
        result.addObject("totalNotes", noteService.getCountNotesByTitle(query, userEmail));
        return result;
    }

//  GET  http://localhost:8080/note/searchTitleAndContent?query=JDBC
    @GetMapping("/searchTitleAndContent")
    public ModelAndView searchNotesByTitleAndContent(@RequestParam("query") String query) {
        String userEmail = getCurrentUserEmail(); // Получаем email текущего пользователя

        ModelAndView result = new ModelAndView("list");
        result.addObject("notes", noteService.searchNotesByTitleAndContent(query, userEmail));
        result.addObject("totalNotes", noteService.getCountNotesByTitleAndContent(query, userEmail));
        return result;
    }

    // POST  http://localhost:8080/note/deleteByTitle?query=Some
    @PostMapping("/deleteByTitle")
    public String deleteNotesByTitle(@RequestParam("query") String query) {
        String userEmail = getCurrentUserEmail();
        noteService.deleteNotesByTitle(query, userEmail);
        return "redirect:/note/list";
    }

    // GET  http://localhost:8080/note/admin
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
            return new ModelAndView("redirect:/note/list");
        }
    }

    // POST  http://localhost:8080/note/admin/delete/6
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
        return "redirect:/note/admin";
    }

}
