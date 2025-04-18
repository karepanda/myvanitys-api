package com.myvanitys.api.auth.infrastructure.adapter.secondary.port;

import com.myvanitys.api.auth.domain.model.User;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository {

  Mono<User> save(User user);

  Mono<User> findByAuthorizationId(String authorizationId);
}
