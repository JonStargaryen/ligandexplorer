package de.bioforscher.ligandexplorer.service.ligand.resolve;

import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Ligand;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fetch ligand information from the PDB. Provide ligand name (three-letter-code), retrieve general ligand information
 * and the structures it is present in.
 */
@Component("ligandResolverImpl")
public class LigandResolverImpl implements LigandResolver {
//    private static final Logger logger = LoggerFactory.getLogger(LigandResolverImpl.class);

    private static final String PDB_LIGAND_DESCRIPTION_URL = "https://files.rcsb.org/ligands/view/%s.cif";
    private static final String PDB_LIGAND_STRUCTURE_URL = "https://files.rcsb.org/ligands/view/%s_ideal.pdb";
    private static final String PDB_SERVICE_URL = "http://www.rcsb.org/pdb/rest/search";

    @Override
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
//                    logger.warn("failed to close InputStream while parsing",
//                            e);
                }
            }
        } catch (IOException e) {
//            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
        }

        // fetch ligand structure
        String pdbRepresentation = "";
        String ligandStructureUrl = String.format(PDB_LIGAND_STRUCTURE_URL, ligandName);
        try {
            InputStream inputStream = new URL(ligandStructureUrl).openStream();
            Structure ligand = StructureParser.fromInputStream(inputStream).skipHydrogenAtoms(true).parse();
            pdbRepresentation = ligand.getPdbRepresentation();
        } catch (IOException e) {
//            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
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
                pdbIds.add(line.toLowerCase());
            }
            bufferedReader.close();
        } catch (IOException e) {
//            logger.warn("failed to load ligand information at " + ligandDescriptionUrl);
        }

        return new Ligand(id,
                name,
                pdbRepresentation,
                pdbIds);
    }
}
