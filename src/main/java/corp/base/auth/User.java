package corp.base.auth;

import lombok.RequiredArgsConstructor;
import lombok.Data;
import corp.base.note.Note;
import jakarta.persistence.*;


import java.util.List;

@Table(name = "\"user\"")
@Entity
@RequiredArgsConstructor
@Data
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

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
