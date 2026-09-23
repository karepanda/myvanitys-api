package com.myvanitys.api.auth.domain.port.secondary;

import com.myvanitys.api.auth.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserRepository {

  Mono<User> save(User user);

  Mono<User> findByAuthorizationId(String authorizationId);
}
