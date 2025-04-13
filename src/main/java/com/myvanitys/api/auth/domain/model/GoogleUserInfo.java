package com.myvanitys.api.auth.domain.model;

public record GoogleUserInfo(
    String id,
    String email,
    String name,
    String pictureUrl
) {

}
