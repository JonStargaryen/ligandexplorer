package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.jstructure.feature.plip.ProteinLigandInteractionProfiler;
import de.bioforscher.jstructure.feature.plip.model.InteractionContainer;
import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.AlignedInteraction;
import de.bioforscher.ligandexplorer.model.BindingSite;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import de.bioforscher.ligandexplorer.service.ligand.LigandAligner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("allToOneClusterResolver")
public class AllToOneClusterResolver implements ClusterResolver {
    private static final Logger logger = LoggerFactory.getLogger(AllToOneClusterResolver.class);
    private final LigandAligner ligandAligner;

    @Autowired
    public AllToOneClusterResolver(LigandAligner ligandAligner) {
        this.ligandAligner = ligandAligner;
    }

    @Override
    public List<Cluster> getClusters(String ligandName, List<String> pdbIds) {
        List<BindingSite> bindingSites = pdbIds.stream()
                .limit(3)
                .flatMap(pdbId -> {
                    Structure structure = StructureParser.fromPdbId(pdbId).parse();
                    InteractionContainer interactionContainer = ProteinLigandInteractionProfiler.getInstance()
                            .annotateLigandInteractions(structure);

                    return structure.select()
                            .groupName(ligandName)
                            .asFilteredGroups()
                            .map(ligand -> {
                                StructureIdentifier structureIdentifier = new StructureIdentifier(structure.getProteinIdentifier().getPdbId(),
                                        ligandName,
                                        ligand.getParentChain().getChainIdentifier().getChainId(),
                                        ligand.getResidueIdentifier().toString(),
                                        structure.getTitle());
                                return new BindingSite(structureIdentifier,
                                        ligand,
                                        interactionContainer);
                            });
                })
                .collect(Collectors.toList());

        ligandAligner.alignBindingSites(bindingSites);

        List<Cluster> clusters = new ArrayList<>();
        //TODO shitty code: this alignment must happen first: otherwise alignment of the ligand will cause interaction centroids to be shifted as well
        List<AlignedInteraction> alignedInteractions = bindingSites.stream()
                .map(BindingSite::getAlignedInteractions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        clusters.add(new Cluster("1",
                bindingSites.stream()
                        .map(BindingSite::getStructureIdentifier)
                        .collect(Collectors.toList()),
                bindingSites.stream()
                        .map(BindingSite::getAlignedPdbRepresentation)
                        .collect(Collectors.joining(System.lineSeparator())),
                alignedInteractions));
        return clusters;
    }
}
