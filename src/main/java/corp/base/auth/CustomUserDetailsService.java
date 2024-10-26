package corp.base.auth;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserSecurityData userSecurityData = getByIdOrNull(username);

        if (userSecurityData == null) {
            throw new UsernameNotFoundException(username);
        }

        UserDetails allUserSecurityProperties = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Arrays.stream(userSecurityData.getAuthority().split(","))
                        .map(it -> (GrantedAuthority) () -> it)
                        .collect(Collectors.toList());
            }

            @Override
            public String getPassword() {
                return userSecurityData.getPassword();
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };

        return allUserSecurityProperties;
    }

    private UserSecurityData getByIdOrNull(String email) {
        String sql = "SELECT password, authority FROM \"user\" WHERE email = :email";
        return jdbcTemplate.queryForObject(
                sql,
                Map.of("email", email),
                new UserDataMapper()
        );
    }

    private class UserDataMapper implements RowMapper<UserSecurityData> {
        @Override
        public UserSecurityData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return UserSecurityData.builder()
                    .password(rs.getString("password"))
                    .authority(rs.getString("authority"))
                    .build();
        }
    }

    @Builder
    @Data
    private static class UserSecurityData {
        private String password;
        private String authority;
    }
}
