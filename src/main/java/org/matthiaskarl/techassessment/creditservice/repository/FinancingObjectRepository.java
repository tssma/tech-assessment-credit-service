package org.matthiaskarl.techassessment.creditservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FinancingObjectRepository {

    private static final String PATH = "20231210_TestData_FINANCING_OBJECT.json";

    private final List<FinancingObject> data = RepositoryUtils.read(PATH, new TypeReference<>() {
    });

    public List<FinancingObject> findAll() {
        return data;
    }

    public List<FinancingObject> findByOwnerId(long ownerId) {
        return data.stream()
                .filter(fo -> fo.owners().stream().anyMatch(owner -> owner.id() == ownerId))
                .toList();
    }
}
