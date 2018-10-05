package de.bioforscher.ligandexplorer.api;

import de.bioforscher.ligandexplorer.model.Query;
import de.bioforscher.ligandexplorer.service.ExplorerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/", method = RequestMethod.GET)
public class ExplorerController {
    private final ExplorerService explorerService;

    @Autowired
    public ExplorerController(ExplorerService explorerService) {
        this.explorerService = explorerService;
    }

    @RequestMapping(value = "/query/{query}", method = RequestMethod.GET)
    public Query getQuery(@PathVariable("query") String query) {
        return explorerService.getQuery(query);
    }
}

