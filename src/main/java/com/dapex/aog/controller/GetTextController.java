package com.dapex.aog.controller;

import com.dapex.aog.dto.Chapter;
import com.dapex.aog.exception.ParsingFailureException;
import com.dapex.aog.exception.TablePopulateFailureException;
import com.dapex.aog.factory.RestResponseHeaderFactory;
import com.dapex.aog.parser.ParseyMcParseFace;
import com.dapex.aog.service.DBService;
import com.dapex.aog.utils.AOGWordDocUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class GetTextController extends RestResponseHeaderFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    private DBService dbService;

    private ParseyMcParseFace pmc;

    @Autowired
    public GetTextController(DBService dbService, ParseyMcParseFace parseyMcParseFace) {
        this.pmc = parseyMcParseFace;
        this.dbService = dbService;
    }

    /**
     * <p>Endpoint to parse aog document by given label</p>
     *
     * @param aogLabel label of the aog document that need to be parsed
     * @return Returns a list of chapters with the id and chapter name with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/parse", method = RequestMethod.POST)
    public List<Chapter> parse(@Param("aogLabel") String aogLabel) {
        LOGGER.info(String.format("Parsing com.dapex.aog.dto.aog.AOG with label %s", aogLabel));
        return Optional.ofNullable(this.pmc.run(
                AOGWordDocUtility.getAOGWordDoc(aogLabel),
                AOGWordDocUtility.getOOSBucketName(aogLabel)))
                .filter(list -> !list.isEmpty())
                .orElseThrow(ParsingFailureException::new);
    }

    /**
     * <p>Endpoint to populate aog document by given label</p>
     *
     * @param aogLabel label of the aog document that need to be populated
     * @return Returns a list of chapters with the id and chapter name with {@link HttpStatus#OK}
     * or will return {@link HttpStatus#NO_CONTENT} if no aog was found
     * or will return {@link HttpStatus#INTERNAL_SERVER_ERROR} if any exception was thrown in dbPullService
     */
    @RequestMapping(value = "/api/populateTables", method = RequestMethod.POST)
    public ResponseEntity<?> populateTables(@Param("aogLabel") String aogLabel) {
        return Optional.of(this.dbService.populateTables(aogLabel))
                .filter(result -> result)
                .map(RestResponseHeaderFactory::renderResponseEntity)
                .orElseThrow(TablePopulateFailureException::new);
    }
}
