package de.bioforscher.ligandexplorer.service;

import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Cluster;
import de.bioforscher.ligandexplorer.model.Ligand;
import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.model.StructureIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExplorerService {
    private static final Logger logger = LoggerFactory.getLogger(ExplorerService.class);

    @Autowired
    public ExplorerService() {

    }

    public Query getQuery(String query) {
        logger.info("handling query {}",
                query);
        return new Query(Query.QueryType.NAME,
                query,
                Stream.of(query).map(this::getLigand).collect(Collectors.toList()));
    }

    private static final String PDB_LIGAND_DESCRIPTION_URL = "https://files.rcsb.org/ligands/view/%s.cif";
    private static final String PDB_LIGAND_STRUCTURE_URL = "https://files.rcsb.org/ligands/view/%s_ideal.pdb";
    private static final String PDB_SERVICE_URL = "http://www.rcsb.org/pdb/rest/search";

    public Ligand getLigand(String ligandName) {
        String id = "";
        String name = "";

        // parse general ligand information
        String ligandDescriptionUrl = String.format(PDB_LIGAND_DESCRIPTION_URL, ligandName);
        try {
            InputStream inputStream = new URL(ligandDescriptionUrl).openStream();
            try {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        for(String line : bufferedReader.lines().collect(Collectors.toList())) {
                            if(line.startsWith("_chem_comp.id")) {
                                id = line.split("\\s+")[1];
                            }
                            if(line.startsWith("_chem_comp.name")) {
                                name = line.split("\\s+")[1];
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("failed to close InputStream while parsing",
                            e);
                }
            }
        } catch (IOException e) {
            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
        }

        // fetch ligand structure
        String pdbRepresentation = "";
        String ligandStructureUrl = String.format(PDB_LIGAND_STRUCTURE_URL, ligandName);
        try {
            InputStream inputStream = new URL(ligandStructureUrl).openStream();
            try {
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        pdbRepresentation = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("failed to close InputStream while parsing",
                            e);
                }
            }
        } catch (IOException e) {
            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
        }

        // fetch structures containing this ligand from PDB
        List<String> pdbIds = new ArrayList<>();
        String xmlQuery = "<orgPdbQuery>\n" +
                "  <queryType>org.pdb.query.simple.ChemCompIdQuery</queryType>\n" +
                "  <description>Chemical ID(s): ligand by name and Polymeric type is Free</description>\n" +
                "    <chemCompId>" + ligandName + "</chemCompId>\n" +
                "    <polymericType>Free</polymericType>\n" +
                "</orgPdbQuery>";
        try {
            URL url = new URL(PDB_SERVICE_URL);
            String encodedXML = URLEncoder.encode(xmlQuery,"UTF-8");

            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(encodedXML);
            wr.flush();

            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                pdbIds.add(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
        }

        logger.info("ligand {} is present in {} structures:\n{}",
                ligandName,
                pdbIds.size(),
                pdbIds);

        return new Ligand(id,
                name,
                pdbRepresentation,
                pdbIds);
    }

    public Cluster getStructure(String query,
                                String pdbId) {
        // parse PDB structure
        Structure structure = StructureParser.fromPdbId(pdbId).parse();

        // extract position of ligands
        List<StructureIdentifier> structureIdentifiers = structure.select()
                .ligands()
                .groupName(query)
                .asFilteredGroups()
                .map(group -> new StructureIdentifier(pdbId,
                        query,
                        group.getParentChain().getChainIdentifier().getChainId(),
                        group.getResidueIdentifier().getResidueNumber()))
                .collect(Collectors.toList());

        return new Cluster(structureIdentifiers,
                structure.getTitle(),
                structure.getPdbRepresentation());
    }
}