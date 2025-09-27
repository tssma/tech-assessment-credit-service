package org.matthiaskarl.techassessment.creditservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class LimitsRepository {
    private static final String PATH = "20231214_TestData_LIMITS.json";
    private final Map<Long, Limit> byId;

    public LimitsRepository() {
        List<Limit> list = RepositoryUtils.read(PATH, new TypeReference<>() {
        });
        this.byId = list.stream().collect(Collectors.toMap(Limit::id, limit -> limit));
    }

    public Limit findById(long id) {
        return byId.get(id);
    }
}
