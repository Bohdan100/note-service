package corp.base.note;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import corp.base.auth.User;

@Table(name = "note")
@Entity
@Data
@ToString(exclude = "user")
public class Note {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
