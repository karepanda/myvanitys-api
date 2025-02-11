package com.api.myvanitys.application.query;

public class FindProductQuery {
    private final String searchTerms;

    public FindProductQuery(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public String getSearchTerms() {
        return searchTerms;
    }
}
