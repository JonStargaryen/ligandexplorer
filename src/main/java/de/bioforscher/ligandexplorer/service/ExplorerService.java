package de.bioforscher.ligandexplorer.service;

import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.service.cluster.ClusterResolver;
import de.bioforscher.ligandexplorer.service.query.QueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExplorerService {
    private final QueryResolver queryResolver;
    private final ClusterResolver clusterResolver;

    @Autowired
    public ExplorerService(@Qualifier("naiveQueryResolver") QueryResolver queryResolver,
                           @Qualifier("allToOneClusterResolver")ClusterResolver clusterResolver) {
        this.queryResolver = queryResolver;
        this.clusterResolver = clusterResolver;
    }

    public Query getQuery(String query) {
        return queryResolver.getQuery(query);
    }

    public List<Cluster> getClusters(String ligandName,
                                     String pdbIdString) {
        return clusterResolver.getClusters(ligandName, pdbIdString);
    }
}