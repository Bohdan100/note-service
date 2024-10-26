package corp.base.note;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import corp.base.auth.User;
import corp.base.user.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public Note getById(String id) {
        return noteRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new NoSuchElementException("Note not found with id: " + id));
    }

    public List<Note> getAllNotes(String userEmail) {
        return noteRepository.getAllNotes(userEmail);
    }

    public long getCountOfAllNotes(String userEmail) {
        return noteRepository.getCountOfAllNotes(userEmail);
    }


    public List<Note> searchNotesByTitle(String query, String userEmail) {
        return noteRepository.searchNotesByTitle("%" + query + "%", userEmail);
    }

    public long getCountNotesByTitle(String query, String userEmail) {
        return noteRepository.getCountNotesByTitle("%" + query + "%", userEmail);
    }


    public List<Note> searchNotesByTitleAndContent(String query, String userEmail) {
        return noteRepository.searchNotesByTitleAndContent("%" + query + "%", userEmail);
    }

    public long getCountNotesByTitleAndContent(String query, String userEmail) {
        return noteRepository.getCountNotesByTitleAndContent("%" + query + "%", userEmail);
    }

    @Transactional
    public void save(Note note, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        note.setUser(user);
        noteRepository.save(note);
    }

    public void deleteById(String id, String userEmail) {
        Note note = noteRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new NoSuchElementException("Note not found with id: " + id));

        if (!note.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have permission to delete this note.");
        }

        noteRepository.deleteById(Long.valueOf(id));
    }

    @Transactional(rollbackOn = {NullPointerException.class}, dontRollbackOn = {IOException.class})
    public void deleteNotesByTitle(String title, String userEmail) {
        noteRepository.deleteNotesByTitle("%" + title + "%", userEmail);
    }
}
