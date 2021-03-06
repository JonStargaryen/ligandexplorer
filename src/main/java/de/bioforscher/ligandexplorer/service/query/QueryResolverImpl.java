package de.bioforscher.ligandexplorer.service.query;

import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.service.ligand.resolve.LigandResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("queryResolverImpl")
public class QueryResolverImpl implements QueryResolver {
//    private static final Logger logger = LoggerFactory.getLogger(QueryResolverImpl.class);
    private final LigandResolver ligandResolver;

    @Autowired
    public QueryResolverImpl(@Qualifier("mockLigandResolver") LigandResolver ligandResolver) {
        this.ligandResolver = ligandResolver;
    }

    @Override
    public Query getQuery(String query) {
        //TODO add further query types: e.g. SMILES, substructure etc
//        logger.info("handling query {}",
//                query);
        return new Query(Query.QueryType.NAME,
                query,
                Stream.of(query)
                        .map(ligandResolver::getLigand)
                        .collect(Collectors.toList()));
    }
}
