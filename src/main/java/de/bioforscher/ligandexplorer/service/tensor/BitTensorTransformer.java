package de.bioforscher.ligandexplorer.service.tensor;

import de.bioforscher.ligandexplorer.model.BindingSite;

import java.util.List;

public interface BitTensorTransformer {
    void transform(List<BindingSite> alignedBindingSites);
}
