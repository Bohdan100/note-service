package corp.base;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.Assertions;

import corp.base.user.UserService;
import corp.base.user.UserRepository;
import corp.base.auth.User;

import java.util.Collections;
import java.util.List;

class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<User> users = userService.getAllUsers();
        Assertions.assertNotNull(users);
        Assertions.assertEquals(1, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void testDeleteUserById() {
        Long id = 1L;
        doNothing().when(userRepository).deleteById(id);

        Assertions.assertDoesNotThrow(() -> userService.deleteUserById(id));
        verify(userRepository).deleteById(id);
    }

    @Test
    void testFindByEmail() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(user);

        User foundUser = userRepository.findByEmail(email);
        Assertions.assertNotNull(foundUser);
        Assertions.assertEquals(user, foundUser);
        verify(userRepository).findByEmail(email);
    }
}
