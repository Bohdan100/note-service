package corp.base.auth;

import lombok.*;
import jakarta.persistence.*;

import java.util.List;
import corp.base.note.Note;

@Table(name = "\"user\"")
@Entity
@RequiredArgsConstructor
@Data
@Getter
@Setter
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
