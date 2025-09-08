package co.com.crediya.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths")
public class UserPath {
    private String users;
    private String usersById;
    private String usersByEmail;
    private String login;
}
