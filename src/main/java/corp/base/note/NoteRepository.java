package corp.base.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n FROM Note n WHERE n.user.email = :userEmail")
    List<Note> getAllNotes(@Param("userEmail") String userEmail);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.email = :userEmail")
    long getCountOfAllNotes(@Param("userEmail") String userEmail);

    @Query("FROM Note n WHERE lower(n.title) LIKE lower(:query) AND n.user.email = :userEmail")
    List<Note> searchNotesByTitle(@Param("query") String query, @Param("userEmail") String userEmail);

    @Query("SELECT COUNT(n) FROM Note n WHERE lower(n.title) LIKE lower(:query) AND n.user.email = :userEmail")
    long getCountNotesByTitle(@Param("query") String query, @Param("userEmail") String userEmail);

    @Query(value =
            "SELECT n FROM Note n " +
                    "WHERE (lower(n.title) LIKE lower(:query) OR lower(n.content) LIKE lower(:query)) " +
                    "AND n.user.id = (SELECT u.id FROM User u WHERE u.email = :userEmail)")
    List<Note> searchNotesByTitleAndContent(@Param("query") String query, @Param("userEmail") String userEmail);

    @Query("SELECT COUNT(n) FROM Note n WHERE (lower(n.title) LIKE lower(:query) OR lower(n.content) LIKE lower(:query)) AND n.user.email = :userEmail")
    long getCountNotesByTitleAndContent(@Param("query") String query, @Param("userEmail") String userEmail);

    @Modifying
    @Query("DELETE FROM Note n WHERE lower(n.title) LIKE lower(:title) AND n.user.email = :email")
    void deleteNotesByTitle(@Param("title") String title, @Param("email") String email);
}
