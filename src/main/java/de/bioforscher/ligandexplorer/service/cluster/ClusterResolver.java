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
}
