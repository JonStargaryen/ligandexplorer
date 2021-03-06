package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.jstructure.mathematics.Pair;
import de.bioforscher.jstructure.mathematics.SetOperations;
import de.bioforscher.ligandexplorer.model.BindingSite;
import de.bioforscher.ligandexplorer.model.BitTensor;
import de.bioforscher.ligandexplorer.model.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("clusterResolverImpl")
public class ClusterResolverImpl implements ClusterResolver {
    private static final Logger logger = LoggerFactory.getLogger(ClusterResolverImpl.class);

    @Override
    public List<Cluster> getClusters(List<BindingSite> alignedBindingSites) {
        Map<String, Double> distanceMap = new HashMap<>();

        // compute binding site distances
        logger.debug("computing distances");
        alignedBindingSites.forEach(alignedBindingSite -> distanceMap.put(alignedBindingSite.getStructureIdentifier() +
                "-" + alignedBindingSite.getStructureIdentifier(), 0.0));
        List<Pair<BindingSite, BindingSite>> pairs = SetOperations.unorderedPairsOf(alignedBindingSites).collect(Collectors.toList());
        for (int i = 0; i < pairs.size(); i++) {
            Pair<BindingSite, BindingSite> pair = pairs.get(i);
            if(i % 100000 == 0) {
                logger.debug("{} / {}",
                        i,
                        pairs.size());
            }
            String key = pair.getLeft().getStructureIdentifier() + "-" + pair.getRight().getStructureIdentifier();
            String flippedKey = pair.getRight().getStructureIdentifier() + "-" + pair.getLeft().getStructureIdentifier();
            double distance = BitTensor.computeDistance(pair.getLeft().getBitTensor(), pair.getRight().getBitTensor());
            distanceMap.put(key, distance);
            distanceMap.put(flippedKey, distance);
        }

        // determine binding site similarity cutoff
//        List<Double> filteredValues = distanceMap.values()
//                .stream()
//                .filter(value -> value != 0 && value != 1)
//                .sorted()
//                .collect(Collectors.toList());
//        double cutoff = filteredValues.get((int) (filteredValues.size() * 0.5));
//        logger.info("similarity cutoff is {} for values: {}",
//                cutoff,
//                filteredValues);
        double cutoff = 0.8;

        // cluster binding sites by single-linkage clustering
        logger.debug("clustering binding sites");
        List<List<BindingSite>> rawClusters = new ArrayList<>();
        // determine similar clusters
        for (int i = 0; i < alignedBindingSites.size(); i++) {
            if(i % 100000 == 0) {
                logger.debug("{} / {}",
                        i,
                        alignedBindingSites.size());
            }
            BindingSite alignedBindingSite = alignedBindingSites.get(i);
            // stores indices of clusters which contain highly similar sequences
            List<Integer> indicesOfSimilarClusters = new ArrayList<>();

            for(int clusterIndex = 0; clusterIndex < rawClusters.size(); clusterIndex++) {
                boolean clusterContainsSimilarBindingSite = false;
                List<BindingSite> cluster = rawClusters.get(clusterIndex);
                for(BindingSite bindingSiteToCheck : cluster) {
                    if(alignedBindingSite.equals(bindingSiteToCheck)) {
                        continue;
                    }

                    double distance = distanceMap.get(alignedBindingSite.getStructureIdentifier() + "-" + bindingSiteToCheck.getStructureIdentifier());
                    // binding sites are similar
                    if(distance < cutoff) {
                        clusterContainsSimilarBindingSite = true;
                        break;
                    }
                }

                if(clusterContainsSimilarBindingSite) {
                    indicesOfSimilarClusters.add(clusterIndex);
                }
            }

            // depending on similar clusters - create new, add to existing, or merge
            if(indicesOfSimilarClusters.isEmpty()) {
                // if no similar sequence clusters were found, we create a new cluster
                rawClusters.add(Stream.of(alignedBindingSite).collect(Collectors.toList()));
            } else {
                // the hard case: add to single cluster or merge clusters
                if(indicesOfSimilarClusters.size() == 1) {
                    // the easy, yet hard case: add sequence to existing cluster
                    rawClusters.get(indicesOfSimilarClusters.get(0)).add(alignedBindingSite);
                } else {
                    // we need to join/merge existing clusters, because the processed sequence 'connects' them

                    // add first (a.k.a. new/currently processed sequence)
                    List<BindingSite> mergedCluster = Stream.of(alignedBindingSite).collect(Collectors.toList());
                    // zeck the similar clusters and add them to the joint cluster
                    for(int indexOfSimilarCluster : indicesOfSimilarClusters) {
                        mergedCluster.addAll(rawClusters.get(indexOfSimilarCluster));
                    }
                    // maybe we do not have to sort, as indices should be in ascending ordering, but just to be sure
                    Collections.sort(indicesOfSimilarClusters);
                    Collections.reverse(indicesOfSimilarClusters);
                    indicesOfSimilarClusters.stream()
                            .mapToInt(Integer::valueOf)
                            .forEach(rawClusters::remove);

                    rawClusters.add(mergedCluster);
                }
            }
        }

        // sort clusters by size
        rawClusters.sort(Comparator.comparingInt((ToIntFunction<List<BindingSite>>) List::size).reversed());

        List<Cluster> clusters = new ArrayList<>();
        for(int i = 0; i < rawClusters.size(); i++) {
            clusters.add(new Cluster(String.valueOf(i + 1), rawClusters.get(i)));
        }

        return clusters;
    }
}
