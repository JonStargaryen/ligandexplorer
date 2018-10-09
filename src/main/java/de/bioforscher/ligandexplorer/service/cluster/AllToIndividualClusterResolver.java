package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("allToIndividualClusterResolver")
public class AllToIndividualClusterResolver implements ClusterResolver {
    @Override
    public List<Cluster> getClusters(String ligandName, List<String> pdbIds) {
        List<Cluster> clusters = new ArrayList<>();
        for(String pdbId : pdbIds) {
            Structure structure = StructureParser.fromPdbId(pdbId).parse();
            String pdbRepresentation = structure.select()
                    .ligands()
                    .groupName(ligandName)
                    .asFilteredGroups()
                    .findFirst()
                    .get()
                    .getPdbRepresentation();
            List<StructureIdentifier> structureIdentifiers = structure.select()
                    .ligands()
                    .groupName(ligandName)
                    .asFilteredGroups()
                    .map(group -> new StructureIdentifier(pdbId,
                            ligandName,
                            group.getParentChain().getChainIdentifier().getChainId(),
                            group.getResidueIdentifier().toString(),
                            structure.getTitle()))
                    .collect(Collectors.toList());
            clusters.add(new Cluster(String.valueOf(clusters.size() + 1),
                    structureIdentifiers,
                    pdbRepresentation));
        }
        return clusters;
    }
}
