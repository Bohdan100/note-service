package corp.base;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assertions;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

import corp.base.note.Note;
import corp.base.note.NoteController;
import corp.base.note.NoteService;

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
    void testAddNoteController() {
        String title = "Test Title";
        String content = "Test Content";

        Assertions.assertEquals("redirect:/note/list", noteController.addNote(title, content));
        verify(noteService).save(any(Note.class), eq("test@example.com"));
    }

    @Test
    void testDeleteNoteByIdController() {
        String id = "1";
        Assertions.assertEquals("redirect:/note/list", noteController.deleteById(id));
        verify(noteService).deleteById(id, "test@example.com");
    }

    @Test
    void testEditPageControllerSuccess() {
        Note note = new Note();
        note.setId(1);
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
        Assertions.assertEquals("redirect:/note/list", modelAndView.getViewName());
        Assertions.assertEquals("Note not found", modelAndView.getModel().get("error"));
    }

    @Test
    void testEditNoteControllerSuccess() {
        Note note = new Note();
        note.setId(1);
        note.setTitle("Old Title");
        note.setContent("Old Content");

        when(noteService.getById(anyString())).thenReturn(note);

        String result = noteController.editNote("1", "New Title", "New Content");
        Assertions.assertEquals("redirect:/note/list", result);
        verify(noteService).save(any(Note.class), eq("test@example.com"));
        Assertions.assertEquals("New Title", note.getTitle());
        Assertions.assertEquals("New Content", note.getContent());
    }

    @Test
    void testEditNoteControllerValidation() {
        Note note = new Note();
        note.setId(1);

        when(noteService.getById(anyString())).thenReturn(note);

        String result = noteController.editNote("1", "N", "New Content");
        Assertions.assertEquals("redirect:/note/list?error=Title must contain at least 2 characters", result);
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
        Assertions.assertEquals("redirect:/note/list", result);
        verify(noteService).deleteNotesByTitle(eq("test"), eq("test@example.com"));
    }
}
