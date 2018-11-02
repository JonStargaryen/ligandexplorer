package de.bioforscher.ligandexplorer.service.interaction;

import de.bioforscher.jstructure.feature.plip.ProteinLigandInteractionProfiler;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.model.structure.Structure;
import org.springframework.stereotype.Component;

@Component("localPlipAnnotator")
public class LocalPlipAnnotator implements InteractionAnnotator {
    @Override
    public InteractionContainer annotateInteractions(Structure structure) {
        return ProteinLigandInteractionProfiler.getInstance().annotateLigandInteractions(structure);
    }
}
