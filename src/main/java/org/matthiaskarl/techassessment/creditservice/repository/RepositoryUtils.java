package org.matthiaskarl.techassessment.creditservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class RepositoryUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RepositoryUtils() {
        throw new UnsupportedOperationException("This utility class is not mean to be instantiated");
    }

    public static <T> T read(String path, TypeReference<T> type) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return MAPPER.readValue(in, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

}
