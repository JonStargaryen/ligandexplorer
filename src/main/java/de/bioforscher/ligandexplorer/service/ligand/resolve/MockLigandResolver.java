package de.bioforscher.ligandexplorer.service.ligand.resolve;

import de.bioforscher.jstructure.model.structure.Structure;
import de.bioforscher.jstructure.model.structure.StructureParser;
import de.bioforscher.ligandexplorer.model.Ligand;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("mockLigandResolver")
public class MockLigandResolver implements LigandResolver {
//    private static final Logger logger = LoggerFactory.getLogger(MockLigandResolver.class);

    @Override
    public Ligand getLigand(String ligandName) {
        String id = "";
        String name = "";

        // parse general ligand information
        InputStream cifInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("mockdata/" + ligandName + ".cif");
        try {
            try (InputStreamReader inputStreamReader = new InputStreamReader(cifInputStream)) {
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
                cifInputStream.close();
            } catch (IOException e) {
//                logger.warn("failed to close InputStream while parsing",
//                        e);
            }
        }

        // fetch ligand structure
        InputStream ligandStructureInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("mockdata/" + ligandName + "_ideal.pdb");
        Structure ligand = StructureParser.fromInputStream(ligandStructureInputStream).skipHydrogenAtoms(true).parse();
        String pdbRepresentation = ligand.getPdbRepresentation();

        List<String> pdbIds = Pattern.compile("\\s+").splitAsStream("1d0k 1e8k 1hsb 1kl6 1pin 1qte 1rxq 1ta9 1tkk 1xrm 1xrn 1yfs 2bd3 2bda 2bdb 2cfe 2cyh 2dfd 2eit 2ejd 2g50 2huu 2j8f 2j8g 2jb1 2qei 2qer 2wts 2z9x 3beg 3c8c 3deq 3der 3des 3dwc 3f48 3gkr 3h41 3iji 3ijq 3ip5 3ipa 3q4d 3qv4 3r1z 3tu0 3wo0 3wo1 4crf 4cvk 4cvm 4d2c 4dpg 4fkh 4g4w 4g4y 4gfi 4guk 4id9 4io5 4m6g 4nqr 4nt8 4qfl 4tph 4wcx 4whx 4xl8 4xmu 4xn8 4y2w 4ykj 5a3b 5ak7 5ak8 5b87 5b89 5cpd 5d58 5d59 5inr 5inx 5j8q 5oqt 5pgm 5ucr 5wxp 5wze 5x2n 6eom 6f0a 6fcu 6gcn 8jdw")
                .collect(Collectors.toList());
//        logger.info("ligand {} is present in {} structures: {}",
//                ligandName,
//                pdbIds.size(),
//                pdbIds);

        return new Ligand(id,
                name,
                pdbRepresentation,
                pdbIds);
    }
}
