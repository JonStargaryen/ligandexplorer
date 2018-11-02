package de.bioforscher.ligandexplorer.service.interaction;

import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.model.structure.Structure;

public interface InteractionAnnotator {
    InteractionContainer annotateInteractions(Structure structure);
}
