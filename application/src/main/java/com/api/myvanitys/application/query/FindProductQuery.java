package com.api.myvanitys.application.query;

import lombok.Getter;

@Getter
public class FindProductQuery {
    private final String searchTerms;

    public FindProductQuery(String searchTerms) {
        this.searchTerms = searchTerms;
    }

}
