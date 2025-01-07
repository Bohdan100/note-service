package corp.base.auth;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.RequiredArgsConstructor;
import java.util.List;
import corp.base.note.Note;

@Table(name = "\"user\"")
@Entity
@Data
@RequiredArgsConstructor
@ToString(exclude = "notes")
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "authority", nullable = false)
    private String authority;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Note> notes;
}
