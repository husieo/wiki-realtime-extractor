package org.dbpedia.extractor.loader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


class PageLoaderTest {

    private PageLoader pageLoader;

    @BeforeEach
    public void beforeAll(){
        pageLoader = new PageLoader();
    }

    @Test
    public void readPage() throws IOException {
        pageLoader.readPage("Diego_Maradona");
    }
}