package de.bioforscher.ligandexplorer.service.cluster;

import de.bioforscher.ligandexplorer.model.BindingSite;
import de.bioforscher.ligandexplorer.model.Cluster;

import java.util.List;

/**
 * Compose clusters for a set of binding sites.
 */
public interface ClusterResolver {
    List<Cluster> getClusters(List<BindingSite> alignedBindingSites);
}
