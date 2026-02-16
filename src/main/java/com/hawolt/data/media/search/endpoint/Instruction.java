package com.hawolt.data.media.search.endpoint;


public interface Instruction {
    String name();

    String manipulate(String base, String[] args) throws Exception;

}
