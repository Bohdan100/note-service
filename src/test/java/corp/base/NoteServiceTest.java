package corp.base;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Assertions;

import corp.base.note.Note;
import corp.base.note.NoteRepository;
import corp.base.note.NoteService;
import corp.base.auth.User;
import corp.base.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

class NoteServiceTest {
    @InjectMocks
    private NoteService noteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userRepository.findByEmail(any())).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        setUpNotes();
    }

    private void setUpNotes() {
        Note note1 = createNote(1L, "Note One", "Content One");
        Note note2 = createNote(2L, "Note Two", "Content Two");
        Note note3 = createNote(3L, "Another Note", "Some more content");
        Note note4 = createNote(4L, "Final Note", "Final content");

        when(noteRepository.getAllNotes(any())).thenReturn(List.of(note1, note2, note3, note4));
    }

    private Note createNote(Long id, String title, String content) {
        Note note = new Note();
        note.setId(Math.toIntExact(id));
        note.setTitle(title);
        note.setContent(content);
        note.setUser(user);
        return note;
    }

    @Test
    void testGetAllFullNotes() {
        List<Note> notes = noteService.getAllNotes("test@example.com");
        Assertions.assertNotNull(notes);
        Assertions.assertEquals(4, notes.size());
        verify(noteRepository).getAllNotes("test@example.com");
    }

    @Test
    void testGetAllEmptyNotes() {
        String userEmail = "some@example.com";
        when(noteRepository.getAllNotes(userEmail)).thenReturn(Collections.emptyList());

        var notes = noteService.getAllNotes(userEmail);
        Assertions.assertNotNull(notes);
        Assertions.assertTrue(notes.isEmpty());
        verify(noteRepository).getAllNotes(userEmail);
    }

    @Test
    void testSaveNote() {
        when(userRepository.findByEmail(any())).thenReturn(user);
        Note note = new Note();
        note.setTitle("Test Title");
        note.setContent("Test Content");

        noteService.save(note, "test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(noteRepository).save(note);
    }

    @Test
    void testDeleteByIdSuccess() {
        String userEmail = "test@example.com";
        Note note = new Note();
        note.setUser(user);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(user.getEmail()).thenReturn(userEmail);

        Assertions.assertDoesNotThrow(() -> noteService.deleteById("1", userEmail));
        verify(noteRepository).deleteById(1L);
    }

    @Test
    void testDeleteByIdNotFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NoSuchElementException.class, () -> noteService.deleteById("1", "test@example.com"));
    }

    @Test
    void testGetCountOfAllNotes() {
        when(noteRepository.getCountOfAllNotes("test@example.com")).thenReturn(4L);

        long count = noteService.getCountOfAllNotes("test@example.com");
        Assertions.assertEquals(4L, count);
        verify(noteRepository).getCountOfAllNotes("test@example.com");
    }

    @Test
    void testSearchNotesByTitle() {
        String query = "Note";
        when(noteRepository.searchNotesByTitle("%" + query + "%", "test@example.com"))
                .thenReturn(List.of(new Note(), new Note())); // Вернем 2 совпадения

        List<Note> result = noteService.searchNotesByTitle(query, "test@example.com");
        Assertions.assertEquals(2, result.size());
        verify(noteRepository).searchNotesByTitle("%" + query + "%", "test@example.com");
    }

    @Test
    void testGetCountNotesByTitle() {
        String query = "Note";
        when(noteRepository.getCountNotesByTitle("%" + query + "%", "test@example.com")).thenReturn(2L);

        long count = noteService.getCountNotesByTitle(query, "test@example.com");
        Assertions.assertEquals(2L, count);
        verify(noteRepository).getCountNotesByTitle("%" + query + "%", "test@example.com");
    }

    @Test
    void testSearchNotesByTitleAndContent() {
        String query = "content";
        when(noteRepository.searchNotesByTitleAndContent("%" + query + "%", "test@example.com"))
                .thenReturn(List.of(new Note(), new Note(), new Note())); // Вернем 3 совпадения

        List<Note> result = noteService.searchNotesByTitleAndContent(query, "test@example.com");
        Assertions.assertEquals(3, result.size());
        verify(noteRepository).searchNotesByTitleAndContent("%" + query + "%", "test@example.com");
    }

    @Test
    void testGetCountNotesByTitleAndContent() {
        String query = "content";
        when(noteRepository.getCountNotesByTitleAndContent("%" + query + "%", "test@example.com")).thenReturn(3L);

        long count = noteService.getCountNotesByTitleAndContent(query, "test@example.com");
        Assertions.assertEquals(3L, count);
        verify(noteRepository).getCountNotesByTitleAndContent("%" + query + "%", "test@example.com");
    }

    @Test
    void testDeleteNotesByTitle() {
        doNothing().when(noteRepository).deleteNotesByTitle("%Note%", "test@example.com");

        noteService.deleteNotesByTitle("Note", "test@example.com");
        verify(noteRepository).deleteNotesByTitle("%Note%", "test@example.com");
    }
}
