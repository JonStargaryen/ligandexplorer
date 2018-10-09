package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.ligandexplorer.model.Cluster;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface ClusterResolver {
    default List<Cluster> getClusters(String ligandName,
                              String pdbIdString) {
        return getClusters(ligandName, isolatePdbIds(pdbIdString));
    }

    List<Cluster> getClusters(String ligandName,
                              List<String> pdbIds);

    default List<String> isolatePdbIds(String pdbIdString) {
        return IntStream.range(0, pdbIdString.length() / 4)
                .mapToObj(i -> pdbIdString.substring(i * 4, i * 4 + 4))
                .collect(Collectors.toList());
    }

    //        List<Cluster> clusters = new ArrayList<>();
//        for(String pdbId : pdbIds) {
//            Structure structure = StructureParser.fromPdbId(pdbId).parse();
//            String pdbRepresentation = structure.select()
//                    .ligands()
//                    .groupName(ligandName)
//                    .asFilteredGroups()
//                    .findFirst()
//                    .get()
//                    .getPdbRepresentation();
//            List<StructureIdentifier> structureIdentifiers = structure.select()
//                    .ligands()
//                    .groupName(ligandName)
//                    .asFilteredGroups()
//                    .map(group -> new StructureIdentifier(pdbId,
//                            ligandName,
//                            group.getParentChain().getChainIdentifier().getChainId(),
//                            group.getResidueIdentifier().toString(),
//                            structure.getTitle()))
//                    .collect(Collectors.toList());
//            clusters.add(new Cluster(String.valueOf(clusters.size() + 1),
//                    structureIdentifiers,
//                    pdbRepresentation));
//        }
//        return clusters;
}
