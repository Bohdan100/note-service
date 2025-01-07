package corp.base;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockitoAnnotations;

import static corp.base.helpers.Redirect.buildRedirectUrl;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assertions;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ConcurrentModel;

import corp.base.note.NoteController;
import corp.base.note.NoteService;
import corp.base.user.UserService;
import corp.base.note.Note;
import corp.base.note.NoteDTO;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

class NoteControllerTest {
    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteService noteService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void testGetAllNotesController() {
        when(noteService.getAllNotes(anyString())).thenReturn(Collections.emptyList());
        when(noteService.getCountOfAllNotes(anyString())).thenReturn(0L);

        ModelAndView modelAndView = noteController.getAll();
        Assertions.assertNotNull(modelAndView);
        Assertions.assertEquals("list", modelAndView.getViewName());
        verify(noteService).getAllNotes("test@example.com");
    }

    @Test
    void testAddNoteControllerValidInput() {
        NoteDTO noteDTO = new NoteDTO("Valid Title", "Valid Content");

        when(bindingResult.hasErrors()).thenReturn(false);

        String result = noteController.addNote(noteDTO, bindingResult);

        Assertions.assertEquals(buildRedirectUrl("note/list"), result);
        verify(noteService).save(any(Note.class), eq("test@example.com"));
    }

    @Test
    void testAddNoteControllerValidationError() {
        NoteDTO noteDTO = new NoteDTO("", "Content");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new org.springframework.validation.FieldError("noteDTO", "title", "Title must not be empty")));

        String result = noteController.addNote(noteDTO, bindingResult);

        Assertions.assertEquals(buildRedirectUrl("note/list?error=Title must not be empty"), result);
        verify(noteService, never()).save(any(Note.class), anyString());
    }

    @Test
    void testDeleteNoteByIdController() {
        String id = "1";
        Assertions.assertEquals(buildRedirectUrl("note/list"), noteController.deleteById(id));
        verify(noteService).deleteById(id, "test@example.com");
    }

    @Test
    void testEditPageControllerSuccess() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Test Note");

        when(noteService.getById(anyString())).thenReturn(note);

        ModelAndView modelAndView = noteController.editPage("1");
        Assertions.assertNotNull(modelAndView);
        Assertions.assertEquals("edit-note", modelAndView.getViewName());
        Assertions.assertEquals(note, modelAndView.getModel().get("note"));
    }

    @Test
    void testEditPageControllerNotFound() {
        when(noteService.getById(anyString())).thenThrow(new NoSuchElementException("Note not found"));

        ModelAndView modelAndView = noteController.editPage("1");
        Assertions.assertEquals(buildRedirectUrl("note/list"), modelAndView.getViewName());
        Assertions.assertEquals("Note not found", modelAndView.getModel().get("error"));
    }

    @Test
    void testEditNoteControllerValidInput() {
        Note note = new Note();
        note.setId(1L);
        note.setTitle("Old Title");
        note.setContent("Old Content");

        when(noteService.getById("1")).thenReturn(note);

        Model model = new ConcurrentModel();
        String result = noteController.editNote("1", "New Title", "New Content", model);

        Assertions.assertEquals(buildRedirectUrl("note/list"), result);
        verify(noteService).save(note, "test@example.com");
        Assertions.assertEquals("New Title", note.getTitle());
        Assertions.assertEquals("New Content", note.getContent());
    }

    @Test
    void testEditNoteControllerValidationError() {
        Note note = new Note();
        note.setId(1L);

        when(noteService.getById("1")).thenReturn(note);

        Model model = new ConcurrentModel();
        String result = noteController.editNote("1", "N", "New Content", model);

        Assertions.assertEquals(buildRedirectUrl("note/list?error=Title must be at least 2 characters long"), result);
        verify(noteService, never()).save(any(Note.class), anyString());
    }

    @Test
    void testEditNoteControllerNoteNotFound() {
        when(noteService.getById("1")).thenThrow(new IllegalArgumentException("Note not found"));

        Model model = new ConcurrentModel();
        String result = noteController.editNote("1", "New Title", "New Content", model);

        Assertions.assertEquals(buildRedirectUrl("note/list?error=Note not found"), result);
        verify(noteService, never()).save(any(Note.class), anyString());
    }

    @Test
    void testEditNoteControllerGenericError() {
        when(noteService.getById("1")).thenThrow(new RuntimeException("Some unexpected error"));

        Model model = new ConcurrentModel();
        String result = noteController.editNote("1", "New Title", "New Content", model);

        Assertions.assertEquals(buildRedirectUrl("note/list?error=An error occurred"), result);
        verify(noteService, never()).save(any(Note.class), anyString());
    }

    @Test
    void testSearchNotesByTitleController() {
        List<Note> notes = List.of(new Note());

        when(noteService.searchNotesByTitle(anyString(), anyString())).thenReturn(notes);
        when(noteService.getCountNotesByTitle(anyString(), anyString())).thenReturn(1L);

        ModelAndView modelAndView = noteController.searchNotesByTitle("test");
        Assertions.assertNotNull(modelAndView);
        Assertions.assertEquals("list", modelAndView.getViewName());
        Assertions.assertEquals(notes, modelAndView.getModel().get("notes"));
        Assertions.assertEquals(1L, modelAndView.getModel().get("totalNotes"));
    }

    @Test
    void testDeleteNotesByTitleController() {
        String result = noteController.deleteNotesByTitle("test");
        Assertions.assertEquals(buildRedirectUrl("note/list"), result);
        verify(noteService).deleteNotesByTitle(eq("test"), eq("test@example.com"));
    }
}
