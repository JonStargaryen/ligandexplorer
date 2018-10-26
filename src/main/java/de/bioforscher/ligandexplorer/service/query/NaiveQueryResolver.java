package de.bioforscher.ligandexplorer.service.query;

import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.service.ligand.LigandResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("naiveQueryResolver")
public class NaiveQueryResolver implements QueryResolver {
    private static final Logger logger = LoggerFactory.getLogger(NaiveQueryResolver.class);
    private final LigandResolver ligandResolver;

    @Autowired
    public NaiveQueryResolver(@Qualifier("pdbLigandResolver") LigandResolver ligandResolver) {
        this.ligandResolver = ligandResolver;
    }

    @Override
    public Query getQuery(String query) {
        logger.info("handling query {}",
                query);
        return new Query(Query.QueryType.NAME,
                query,
                Stream.of(query)
                        .map(ligandResolver::getLigand)
                        .collect(Collectors.toList()));
    }
}
