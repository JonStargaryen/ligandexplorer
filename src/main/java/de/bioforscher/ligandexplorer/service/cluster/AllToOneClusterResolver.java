package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("allToOneClusterResolver")
public class AllToOneClusterResolver implements ClusterResolver {
    @Override
    public List<Cluster> getClusters(String ligandName, List<String> pdbIds) {
        List<Cluster> clusters = new ArrayList<>();
        List<StructureIdentifier> structureIdentifiers = new ArrayList<>();
        String pdbRepresentation = null;

        for(String pdbId : pdbIds) {
            Structure structure = StructureParser.fromPdbId(pdbId).parse();
            if(pdbRepresentation == null) {
                pdbRepresentation = structure.select()
                        .ligands()
                        .groupName(ligandName)
                        .asFilteredGroups()
                        .findFirst()
                        .get()
                        .getPdbRepresentation();
            }
            structure.select()
                    .ligands()
                    .groupName(ligandName)
                    .asFilteredGroups()
                    .map(group -> new StructureIdentifier(pdbId,
                            ligandName,
                            group.getParentChain().getChainIdentifier().getChainId(),
                            group.getResidueIdentifier().toString(),
                            structure.getTitle()))
                    .forEach(structureIdentifiers::add);
        }
        clusters.add(new Cluster("1",
                structureIdentifiers,
                pdbRepresentation));
        return clusters;
    }
}
